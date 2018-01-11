package com.dmsvo.offlinealchemy.classes.loader;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by DmSVo on 31.12.2017.
 */

// https://stackoverflow.com/questions/31145811/android-how-to-get-response-string-from-callback-using-okhttp

public class Loader { // implements Callback { // extends ILoader
    private final static String root = "https://evo-lutio.livejournal.com/";
    private OkHttpClient client;
    static AppDb db;

    static Loader instance = null;

    static public Loader GetInstance() { return instance; }
    static public AppDb GetAppDb() { return db; }
    public String getRoot() { return root; }

    public Loader(AppDb db) {

        this.db = db;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);

        client = builder.build();

        instance = this;
    }

    public List<CompleteArticle> LoadNumber(int number, boolean fast) {
        //page = LoadPage(root);
        String pagePath = root;
        String page;
        int total = number;

        List<CompleteArticle> fullList = new ArrayList<>();

        while (true) {
            page = LoadPage(pagePath); //entry-wrap js-emojis
            Document doc = Jsoup.parse(page);

            //if (ArticleGroupParser.HasArticles(doc))
            //    break;

            List<CompleteArticle> list = GetArticles(doc, total, fast);
            fullList.addAll(list);
            total = total - list.size();

//             // FIXME: пока на стадии тестирования, даже не пытаемся лезть назад
//            if (true) break;

            if (total <= 0)
                break;

            //if (ArticleGroupParser.CanPrev(doc))
            pagePath = ArticleGroupParser.PrevPath(doc);
            if (pagePath == "")
                break;
        }

        return fullList;
    }

    public List<CompleteArticle> LoadFromDb() {
        List<CompleteArticle> carts = new ArrayList<CompleteArticle>();

        List<Article> articles = db.getArticleDao().getAllArticles();
        for (Article artcl : articles) {
            List<Comment> comts = GetComments(artcl.getId());

            carts.add(new CompleteArticle(
                    artcl,
                    comts
            ));
        }
        return carts;
    }

    public List<CompleteArticle> LoadAll() {
        return null;
    }

    String LoadPage(String path) {
        String body = "";

        Request request = new Request.Builder()
                .url(path)
                .build();
        try {
            Response response = client.newCall(request).execute();
            body = response.body().string();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        return body;
    }

    /**
     * Принимает на вход html-страницу, содержащую сколько-то статей,
     * возвращает список
     * @param document
     * @param number количество загружаемых статей
     * @param fast загружаем только описания, не загружаем статью целиком
     * @return
     */
    List<CompleteArticle> GetArticles(Document document, int number, boolean fast) {
        List<CompleteArticle> list = new ArrayList<> ();

        // проходимся по странице и находим id всех статей
//        Elements articles = page.getElementsByTag("article");
//        for (Element article : articles)
//        {
//            Article obj = LoadArticle(article);
//            list.add(obj);
//        }

        // находим контейнеры с описаниями статей
        Elements elms = document.getElementsByClass("entry-wrap js-emojis");
        int i = 0;
        for (Element el : elms)
        {
            if (i == number) break; // FIXME: на стадии отладки, чтобы не очень тормозило

            // проверяем, есть ли статья с таким id в бд
            // если нету - скачиваем
            String path = ArticleGroupParser.GetArticlePath(el);
            int id = Integer.parseInt(path.substring(34, path.length() - 5));
            Article found = db.getArticleDao().getArticle(id);
                // если статья с таким id есть в бд, добавляем её в список вместо закачки
                // вообще-т нам может потребоваться догрузить новые комментарии
            if (found != null)
            {
                if (!fast) {
                    List<Comment> comments = GetComments(id);
                    list.add(new CompleteArticle(found, comments));
                }
                continue;
            }

            CompleteArticle artcl = null;

            if (fast) {
                artcl = ArticleParser.BuildArticleFast(Jsoup.parse(el.html()));
            } else {
                artcl = LoadArticle(el);
            }

            list.add(artcl);

            SaveInDb(artcl);

            i++;
        }

        return list;
    }

    /**
     * Загружаем статью, исходя из её объекта на странице, содержащей множество статей
     * @param el
     * @return
     */
    CompleteArticle LoadArticle(Element el) {

        return LoadArticle(ArticleGroupParser.GetArticlePath(el));
    }

    public CompleteArticle LoadArticle(String path) {

        try {
            String body = LoadPage(path);
            CompleteArticle result = ArticleParser.BuildArticle(this, Jsoup.parse(body));

            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            //throw t;
        }
        return null;
    }

    public boolean isOnline()
    {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public List<Comment> GetComments(int articleId)
    {
//        return db.getCommentDao().getAllCommentsForArticle(articleId);
        return GetComments(articleId, 0, 0);
    }

    private List<Comment> GetComments(int articleId, int parentId, int level) // recursive
    {
        CommentDao cdao = db.getCommentDao();
        List<Comment> base = cdao.getAllChildComments(articleId, parentId);
        List<Comment> result = new ArrayList<>();
        for (Comment com : base)
        {
            com.setLevel(level);
            result.add(com);

            try {
                List<Comment> child = GetComments(articleId, com.getId(), level + 1);
                result.addAll(child);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return result;
    }

//    private List<Comment> GetCommentsHierarhic(int articleId, int parentId)
//    {
//        CommentDao cdao = db.getCommentDao();
//        List<Comment> base = cdao.getAllChildComments(articleId, parentId);
//
//        if (base != null && base.size() > 0) {
//            for (Comment com : base) {
//                try {
//                    com.responses = GetCommentsHierarhic(articleId, com.getId());
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            }
//        }
//        return base;
//    }

    public void SaveInDb(CompleteArticle cart)
    {
        ArticleDao adao = db.getArticleDao();

        Article found = adao.getArticle(cart.article.getId()); //ArticleParser.GetIdFromPath(path)
        try {
            if (found == null) {
                adao.insertAll(cart.article);
            } else {
                adao.update(cart.article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SaveInDb(cart.comments);
    }

    void SaveInDb(List<Comment> comments)
    {
        if (comments == null) return;

        for (Comment cmnt : comments) {
            try {
                Comment foundCmt = db.getCommentDao().getComment(cmnt.getId());
                if (foundCmt == null)
                {
                        db.getCommentDao().insertAll(cmnt);

                } else {
                    db.getCommentDao().update(cmnt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    JSONArray LoadComments(int articleId)
    {
        //https://evo-lutio.livejournal.com/evo-lutio/__rpc_get_thread?journal=evo_lutio&itemid=601527&flat=&skip=&media=&expand_all=1&_=1515533187677
        // https://evo-lutio.livejournal.com/evo-lutio/__rpc_get_thread?
        //      journal=evo_lutio&
        //      itemid=605247&
        //      flat=&
        //      skip=&
        //      media=&
        //      thread=52696895&
        //      expand_all=1&
        //      _=1515533187677
        String basePath = "https://evo-lutio.livejournal.com/evo-lutio/__rpc_get_thread";

        Date now = new Date();
        JSONArray totalComments = new JSONArray();
        int pageNum = 1;
        int total = 0;
        int loaded = 0;
        do
        {
            HttpUrl url = HttpUrl.parse(basePath).newBuilder()
                    .addQueryParameter("journal","evo_lutio")
                    .addQueryParameter("itemid",articleId + "")
                    .addQueryParameter("flat","")
                    .addQueryParameter("skip","")
                    .addQueryParameter("media","")
                    .addQueryParameter("page","" + pageNum)
                    .addQueryParameter("expand_all","1")
                    .addQueryParameter("_", now.getTime()/1000 + "")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            String body = "";

            try {
                Response response = client.newCall(request).execute();

                body = response.body().string();
                JSONObject json = new JSONObject(body);

                if (json != null)
                {
                    if (total == 0) {
                        // устанавливаем значение только один раз - чтобы не возникло глюков,
                        // если вдруг число комментариев изменилось между запросами
                        total = json.optInt("replycount"); // репликант, ха-ха, зовите Декарда
                    }

                    JSONArray comments = json.getJSONArray("comments");
                    loaded += comments.length();

                    CommentParser.MergeArrays(totalComments, comments);
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                break; // мало ли чего
            }
            pageNum++;
        } while (loaded < total);

        return totalComments;
    }

    public int GetCommentLevel(Comment comment)
    {
        int level = comment.getLevel();
        if (level > 0) return level;

        if (comment.getParentId() == 0) {
            comment.setLevel(1);
            db.getCommentDao().update(comment);
            return 1;
        }

        level = 1 + GetCommentLevel(db.getCommentDao().getComment(comment.getParentId()));
        comment.setLevel(level);
        db.getCommentDao().update(comment);

        return level;
    }
}
