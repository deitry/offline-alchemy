package com.dmsvo.offlinealchemy.classes.loader;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.base.Tag;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;
import com.dmsvo.offlinealchemy.classes.db.Converters;
import com.dmsvo.offlinealchemy.classes.db.TagDao;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    public final static String root = "https://evo-lutio.livejournal.com/";
    private OkHttpClient client;
    static AppDb db;

    static Loader instance = null;

    public static final int BASE_CNT = 5;

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

    public List<CompleteArticle> LoadNumber(int number, boolean fast, long last) {
        String pagePath;
        if (last == 0)
            pagePath = root;
        else {
            // TODO: можно оптимизировать - к первым 500 можно обращаться через глагне / ?skip=300
            // тогда на странице загружаются по ~50 статей

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(last));
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            pagePath = root + year + "/"
                    + ((month >= 10) ? month : "0" + month) + "/"
                    + ((day >= 10) ? day : "0" + day) + "/";
        }

        String page;
        int total = number;

        List<CompleteArticle> fullList = new ArrayList<>();

        while (true) {
            page = LoadPage(pagePath);
            Document doc = Jsoup.parse(page);

            List<CompleteArticle> list = GetArticles(doc, total, fast);
            fullList.addAll(list);
            total = total - list.size();

            if (total <= 0)
                break;

            pagePath = ArticleGroupParser.PrevPath(doc);
            if (pagePath == "")
                break;
        }

        return fullList;
    }

    public List<CompleteArticle> LoadNew() {
        // при загрузке новых, всегда начинаем сначала
        String pagePath = root;

        String page;

        List<CompleteArticle> fullList = new ArrayList<>();

        while (true) {
            page = LoadPage(pagePath);
            Document doc = Jsoup.parse(page);

            // FIXME : вставили прямо сюда, потому что условие выхода из глобального цикла
            // было спрятано во вложенной функции

            // тут такой маленький гномик хихикает и подсказывает,
            // что надо всё-всё переосмыслить

            // находим контейнеры с описаниями статей
            Elements elms = doc.getElementsByClass("entry-wrap js-emojis");
            for (Element el : elms)
            {
                // проверяем, есть ли статья с таким id в бд
                // если нету - скачиваем
                String path = ArticleGroupParser.GetArticlePath(el);
                int id = ArticleParser.GetIdFromPath(path);
                Article found = db.getArticleDao().getArticle(id);

                // если статья с таким id есть в бд, значит новые кончились
                if (found != null)
                {
                    // выходим, если только это не прикреплённая запись
                    if (el.html().contains("[sticky post]"))
                        continue;

                    return fullList;
                }

                CompleteArticle artcl
                        = ArticleParser.BuildArticleFast(Jsoup.parse(el.html()));

                fullList.add(artcl);
                SaveInDb(artcl);
            }

            pagePath = ArticleGroupParser.PrevPath(doc);
            if (pagePath == "")
                break;
        }

        return fullList;
    }

    public List<CompleteArticle> LoadFromDb(String tag, int count, int offset) {
        List<CompleteArticle> carts = new ArrayList<>();
        List<Article> articles;

        if (tag.equals("Непрочитанные")) {
            articles = db.getArticleDao().getSomeUnreadArticles(
                    ArticleParser.Tag(""),
                    count,
                    offset);
        } else if (tag.equals("Незагруженные")) {
            articles = db.getArticleDao().getSomeUnloadedArticles(
                    ArticleParser.Tag(""),
                    count,
                    offset);
        } else if (tag.equals("Избранные")) {
            articles = db.getArticleDao().getSomeFavoriteArticles(
                    ArticleParser.Tag(""),
                    count,
                    offset);
        } else {
            articles = db.getArticleDao().getSomeArticles(
                    ArticleParser.Tag(tag),
                    count,
                    offset);
        }


        for (Article artcl : articles) {
            try {
                //List<Comment> comts = GetComments(artcl.getId());
                try {
                    int cnt = db.getCommentDao().getCommentCount(artcl.getId());
                    artcl.setCommentCount(cnt);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                carts.add(new CompleteArticle(
                        artcl,
                        null
                ));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return carts;
    }

    public List<CompleteArticle> SearchInDb(String search, int count, int offset,
                                            boolean inTitle, boolean inContent, boolean inComments) {
        List<CompleteArticle> carts = new ArrayList<>();
        List<Article> articles = new ArrayList<>();

        if (inTitle) {
            articles.addAll(db.getArticleDao().searchInTitle(
                    ArticleParser.Tag(search)));
        }

        if (inContent) {
            articles.addAll(db.getArticleDao().searchInContent(
                    ArticleParser.Tag(search)));
        }

        for (Article artcl : articles) {
            try {
                //List<Comment> comts = GetComments(artcl.getId());
                try {
                    int cnt = db.getCommentDao().getCommentCount(artcl.getId());
                    artcl.setCommentCount(cnt);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                carts.add(new CompleteArticle(
                        artcl,
                        null
                ));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return carts;
    }

    public List<CompleteArticle> LoadFromDb(int count, int offset) {
        List<CompleteArticle> carts = new ArrayList<>();

        List<Article> articles = db.getArticleDao().getSomeArticles(count, offset);

        for (Article artcl : articles) {
            //List<Comment> comts = GetComments(artcl.getId());
            try {
                int cnt = db.getCommentDao().getCommentCount(artcl.getId());
                artcl.setCommentCount(cnt);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            carts.add(new CompleteArticle(
                    artcl,
                    null // comts
                    // не добавляем комментарии по умолчанию, подгружаем их позже
            ));
        }
        return carts;
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

    public CompleteArticle GetArticle(int id) {
        // если статья есть в бд и она загружена, возвращаем из бд
        // комментарии подгружаем в любом случае
        CompleteArticle cart;
        Article found = db.getArticleDao().getArticle(id);
        if (found != null) {
            List<Comment> comments;

            if (isOnline()) {
                comments = LoadComments(id);
            } else {
                comments = GetComments(id);
            }

            cart = new CompleteArticle(found, comments);
        } else {
            cart = LoadArticle(root + id + ".html");
        }

        // сохраняем в бд. Если добавились комментарии - хорошо,
        // если обновилось тело статьи - ещё лучше
        SaveInDb(cart);
        return cart;
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

        // находим контейнеры с описаниями статей //[sticky post]
        Elements elms = document.getElementsByClass("entry-wrap js-emojis");
        int i = 0;
        for (Element el : elms)
        {
            if (i == number) break;

            // проверяем, есть ли статья с таким id в бд
            // если нету - скачиваем
            String path = ArticleGroupParser.GetArticlePath(el);
            int id = ArticleParser.GetIdFromPath(path);
            Article found = db.getArticleDao().getArticle(id);

            // если такая статья уже есть, пропускаем
            if (found != null)
            {
                continue;
            }

            CompleteArticle artcl;

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

    List<CompleteArticle> LoadNewArticles(Document document) {
        List<CompleteArticle> list = new ArrayList<> ();

        // проходимся по странице и находим id всех статей

        // находим контейнеры с описаниями статей
        Elements elms = document.getElementsByClass("entry-wrap js-emojis");
        for (Element el : elms)
        {
            // проверяем, есть ли статья с таким id в бд
            // если нету - скачиваем
            String path = ArticleGroupParser.GetArticlePath(el);
            int id = ArticleParser.GetIdFromPath(path);
            Article found = db.getArticleDao().getArticle(id);

            // если статья с таким id есть в бд, значит новые кончились
            if (found != null)
            {
                // выходим, если только это не прикреплённая запись
                if (el.html().contains("[sticky post]"))
                    continue;

                break;
            }

            CompleteArticle artcl
                    = ArticleParser.BuildArticleFast(Jsoup.parse(el.html()));

            list.add(artcl);
            SaveInDb(artcl);
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


    /**
     * Возвращаем все комментарии из бд
     * @param articleId
     * @return
     */
    public List<Comment> GetComments(int articleId)
    {
//        return db.getCommentDao().getAllCommentsForArticle(articleId);
        List<Comment> comts = GetComments(articleId, 0, 0);
        return comts;
    }

    /**
     * Рекурсивно возвращаем все комментарии из бд
     * @param articleId
     * @param parentId
     * @param level
     * @return
     */
    private List<Comment> GetComments(int articleId, int parentId, int level) // recursive
    {
        if (level > 0 && parentId == 0) return new ArrayList<>();

        CommentDao cdao = db.getCommentDao();
        List<Comment> base = cdao.getAllChildComments(articleId, parentId);
        List<Comment> result = new ArrayList<>();
        for (Comment com : base)
        {
            if (com.getId() == 0) continue;

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

    public void SaveInDb(CompleteArticle cart)
    {
        if (cart == null) return;

        ArticleDao adao = db.getArticleDao();

        Article found = adao.getArticle(cart.article.getId()); //ArticleParser.GetIdFromPath(path)
        try {
            if (cart.comments != null) {
                cart.article.setCommentCount(cart.comments.size());
            }

            if (found == null) {
                adao.insertAll(cart.article);
            } else {
                adao.update(cart.article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SaveCommentsInDb(cart.comments);
        SaveTagsInDb(cart.article.getTags());
    }

    void SaveCommentsInDb(List<Comment> comments)
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

    void SaveTagsInDb(List<String> tags)
    {
        if (tags == null) return;
        TagDao tdao = db.getTagDao();

        for (String tag : tags) {
            try {
                Tag foundTag = tdao.getTag(tag);
                if (foundTag == null)
                {
                    tdao.insertAll(new Tag(tag));
                } else {
                    foundTag.inc();
                    tdao.update(foundTag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Comment> LoadComments(int articleId) {
        List<Comment> comts = ArticleParser.GetComments(
                LoadCommentsJson(articleId),
                articleId);

        SaveCommentsInDb(comts);
        comts = GetComments(articleId);
        return comts;
    }


    /**
     * Загружаем массив со всеми комментариями
     * @param articleId
     * @return
     */
    JSONArray LoadCommentsJson(int articleId)
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

    public void updateTags() {
        TagDao tdao = db.getTagDao();

        tdao.clearTags();

        int cnt = tdao.getCount();
        if (cnt == 0)
        {
            List<Tag> tags = getAllTags();
            for (Tag tag : tags)
            {
                tdao.insertAll(tag);
            }
        } else {
            // если у нас сколько-то тегов уже есть?
        }
    }

    List<Tag> getAllTags() {
        List<Article> articles = db.getArticleDao().getAllArticles();

        List<Tag> tags = new ArrayList<>();

        Map<String, Integer> tagsMap = new HashMap<>();

        for (Article article : articles) {
            List<String> artTags = article.getTags();
            for (String tag : artTags) {
                if (tagsMap.containsKey(tag)) {
                    tagsMap.put(tag, tagsMap.get(tag)+1);
                } else {
                    tagsMap.put(tag, 1);
                }
            }
        }

        for (Map.Entry entry : tagsMap.entrySet()) {
            tags.add(new Tag(
                    (String) entry.getKey(),
                    (Integer) entry.getValue()));
        }

        return tags;
    }

    public int hasNewArticles()
    {
        String data = LoadPage("http://evo-lutio.livejournal.com/data/rss");
            // результат можно кэшировать и использовать позжеы

        Document doc = Jsoup.parse(data);
        //Article latest = db.getArticleDao().getLatest();
        //long date = latest.getDate().getTime();

        Elements elms = doc.getElementsByTag("guid");
        if (elms == null || elms.size() == 0) return 0;

        int count = 0;

        // достаточно проверить крайний элемент
        for (Element elm : elms) {
            try {
                String articlePath = elm.text();
                int id = ArticleParser.GetIdFromPath(articlePath);

                Article found = db.getArticleDao().getArticle(id);

                if (found == null)
                    count++;
            } catch (Throwable t) {

            }
        }

        return count;
    }

    public int hasNewComments(int articleId) {
        int count = 0;
        String basePath = "https://evo-lutio.livejournal.com/evo-lutio/__rpc_get_thread";

        HttpUrl url = HttpUrl.parse(basePath).newBuilder()
                .addQueryParameter("journal","evo_lutio")
                .addQueryParameter("itemid",articleId + "")
                .addQueryParameter("flat","")
                .addQueryParameter("skip","")
                .addQueryParameter("media","")
                .addQueryParameter("expand_all","0")
                .addQueryParameter("_", new Date().getTime()/1000 + "")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        String body = "";

        try {
            Response response = client.newCall(request).execute();

            body = response.body().string();
            JSONObject json = new JSONObject(body);

            if (json != null) {
                count = json.optInt("replycount"); // репликант, ха-ха, зовите Декарда
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        return count;
    }
}
