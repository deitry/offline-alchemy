package com.dmsvo.offlinealchemy.classes.loader;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DmSVo on 31.12.2017.
 */

public class ArticleParser {

    // не хотим создавать экземпляры
    private ArticleParser() {}

    static int GetIdFromPath(String path)
    {
        return Integer.parseInt(path.substring(34, path.length() - 5));
    }

    /**
     * Строит статью на основе переданного документа html
     *
     * @param loader      передаём объект загрузчика, чтобы удобно подгрузить комментарии
     * @param articleBody
     * @return
     */
    static CompleteArticle BuildArticle(Loader loader, Document articleBody) {

        // проходимся по тексту
        // находим
        // - название
        // - теги
        // - дату
        // - текст
        // - блок комментариев

        // <div class="b-singlepost-wrapper">
        //      <h1> <span></span> TITLE </h1
        //      <div class="b-singlepost-bodywrapper">
        //          <article class=" b-singlepost-body entry-content e-content  " .. >
        //              BODY
        Elements elms = articleBody.getElementsByClass("b-singlepost-wrapper");
        if (elms == null || elms.size() == 0) return null;
        Element wrapper = elms.first();

        elms = wrapper.getElementsByTag("h1");
        if (elms == null || elms.size() == 0) return null;
        String title = elms.first().text();

        elms = wrapper.getElementsByTag("article");
        if (elms == null || elms.size() == 0) return null;
        String body = elms.first().html();
//        String bodyHtml = elms.first().html();
//        String bodyData = elms.first().data();

        // поиск тегов
        //class: b-singlepost-tags-items
        //  <a>tag1</a>
        List<String> tags = new ArrayList<>();
        elms = articleBody.getElementsByClass("b-singlepost-tags-items");
        if (elms != null && elms.size() > 0) {
            tags = BuildTags(elms.first());
        }

        // выковыривать id
        // <link rel="canonical" href="https://evo-lutio.livejournal.com/601702.html">
        elms = articleBody.getElementsByAttributeValue("rel", "canonical");
        if (elms == null || elms.size() == 0) return null;
        Element link = elms.first();
        String path = link.attr("href");
        int id = GetIdFromPath(path);

        // дата и время
        // <time class=" b-singlepost-author-date published dt-published ">
        //      <a href="https://evo-lutio.livejournal.com/2018/">2018</a>-
        //      <a href="https://evo-lutio.livejournal.com/2018/01/">01</a>-
        //      <a href="https://evo-lutio.livejournal.com/2018/01/02/">02</a>
        //      14:12:00
        // </time>
        elms = articleBody.getElementsByClass(" b-singlepost-author-date published dt-published ");
        if (elms == null || elms.size() == 0) return null;
        Element date = elms.first();
        elms = date.getElementsByTag("a");
        Date dateTime = new Date();
        Calendar calendar; // = new GregorianCalendar();
        if (elms != null && elms.size() >= 3) {
            int year = Integer.parseInt(elms.get(0).text());
            int month = Integer.parseInt(elms.get(1).text());
            int day = Integer.parseInt(elms.get(2).text());
            String time = date.text();
            String[] timeArr = time.substring(11).split(":");
            calendar = new GregorianCalendar(year, month-1, day,
                    Integer.parseInt(timeArr[0]),
                    Integer.parseInt(timeArr[1]),
                    Integer.parseInt(timeArr[2]));
            dateTime = calendar.getTime();
        }

        List<Comment> comments = GetComments(   // loader,
                GetAllComments(loader, id),     // articleBody),
                id);

        Article artcl = new Article();
        artcl.setName(title);
        artcl.setBody(body);
        artcl.setTags(tags);
        artcl.setId(id);
        artcl.setDate(dateTime);
        artcl.setCommentsIds(comments);
        artcl.setLoaded(1);

        CompleteArticle complete = new CompleteArticle(artcl, comments);

        return complete;
    }

    static public CompleteArticle BuildArticleFast(Document articleBody)
    {
        Elements elms = articleBody.getElementsByClass("entry-title");
        if (elms == null || elms.size() == 0) return null;
        Element titleEl = elms.first();

        elms = titleEl.getElementsByTag("a");
        if (elms == null || elms.size() == 0) return null;
        Element titleLink = elms.first();

        String title = titleLink.text();
        int id = GetIdFromPath(titleLink.attr("href"));


        //<abbr class="updated" title="2018-01-11T11:18:00+03:00">January 11th, 11:18</abbr>
        elms = articleBody.getElementsByClass("entry-date");
        if (elms == null || elms.size() == 0) return null;
        Element dateEl = elms.first();

        elms = dateEl.getElementsByClass("updated");
        if (elms == null || elms.size() == 0) return null;
        Element updatedDate = elms.first();
        String dateString = updatedDate.attr("title");

        Calendar cal = Calendar.getInstance();
        // 2018-01-11T11:18:00+03:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(dateString));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Date dateTime = cal.getTime();

        elms = articleBody.getElementsByClass("entry-content");
        if (elms == null || elms.size() == 0) return null;
        Element bodyEl = elms.first();
        String body = bodyEl.html();

        //ljtags
        elms = articleBody.getElementsByClass("ljtags");
        List<String> tags = null;
        if (elms != null && elms.size() > 0) {
            tags = BuildTags(elms.first());
        } else {
            tags = new ArrayList<>();
        }

        Article artcl = new Article();
        artcl.setName(title);
        artcl.setBody(body);
        artcl.setTags(tags);
        artcl.setId(id);
        artcl.setDate(dateTime);
        artcl.setLoaded(0);
        artcl.setComments(new ArrayList<Integer>());

        CompleteArticle complete = new CompleteArticle(artcl, null);
        return complete;
    }

    static private List<String> BuildTags(Element tagsEl)
    {
        if (tagsEl == null) return null;

        List<String> tags = new ArrayList<>();

        Elements tagsHtml = tagsEl.getElementsByTag("a");
        if (tagsHtml != null && tagsHtml.size() > 0) {
            for (Element el : tagsHtml) {
                tags.add(el.text());
            }
        }

        return tags;
    }

    static JSONArray GetAllComments(Loader loader, int articleId)
    {
        return loader.LoadComments(articleId);
    }


    static JSONArray GetAllComments(Loader loader, Document articleBody)
    {
        // собрать все комментарии с этой страницы
        JSONArray all
                = GetJsonComments(articleBody);

        // перейти если есть ссылка на следующую страницу, собрать всё с неё
        Elements links = articleBody.getElementsByClass("b-pager-link--next");
        if (links != null && links.size() > 0)
        {
            Element el = links.first();
            String href = el.attr("href");
            String path2next = loader.getRoot() + href;

            Document nextPage = Jsoup.parse(loader.LoadPage(path2next));
            if (nextPage != null) {
                JSONArray additional = GetAllComments(loader, nextPage);

                if (additional != null && additional.length() > 0) {
                    for (int i = 0; i < additional.length(); i++) {
                        try {
                            JSONObject obj = additional.getJSONObject(i);
                            all.put(obj);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }

        return all;
    }

    static JSONArray GetJsonComments(Document articleBody)
    {
        // FIXME: загружать комментарии
        // TODO: а если часть комментариев на новой странице??!!
        // пример ссылки для перехода на следующую страницу
        // как узнать, сколько всего комментов и нужно ли переходить?
//        <a class="
//        b-pager-link
//        b-pager-link--next
//        js-elem-bgcolor--before
//        " href="/521344.html?page=2" target="_self">
        // <script>
        //      Site.page = {JSON}

        Elements elms = articleBody.select("script");
        Pattern p = Pattern.compile(".*Site\\.page = (.+)"); // (?is) //Regex for the value of the key
        String commentsBody = "";
        for (Element el : elms)
        {
            String scriptBody = el.html();
            String[] scriptStrs = scriptBody.split("\n");
            for (String s : scriptStrs) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    commentsBody = m.group(1);
                }
            }
        }

        if (commentsBody != "") {
            JSONObject commentsJson;

            try {
                commentsJson = new JSONObject(commentsBody);

                // проходимся по всем комментариям
                // если не имеет родителя - парсим
                // если имеет - пропускаем

                JSONArray commentsBlock = commentsJson.getJSONArray("comments");

                // если есть переход на следующую страницу,
                // то переходим на неё и считываем комментарии оттуда

                return commentsBlock;

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static List<Comment> GetComments(JSONArray commentsJson, int articleId)
    {
        List<Comment> list = new ArrayList<>();

        int len = commentsJson.length();
        try {
            for (int i = 0; i < len; i++) {
                JSONObject commentJson = commentsJson.getJSONObject(i);
                Comment comment = CommentParser.BuildComment(commentJson, articleId);
                list.add(comment);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return list;
    }

    static List<Comment> GetComments(Loader loader, JSONArray commentsJson, int articleId)
    {
        List<Comment> list = new ArrayList<>();

        int len = commentsJson.length();
        try {

            for (int i = 0; i < len; i++) {
                JSONObject commentJson = commentsJson.getJSONObject(i);

                // поскольку вложенные комментарии могут не подгружаться,
                // для универсальности мы считываем только "корневые" комментарии,
                // и для каждого запрашиваем список вложенных

                if (commentJson.getInt("parent") != 0) continue;

                Comment comment = CommentParser.BuildComment(commentJson, articleId);
                list.add(comment);
                //loader.db.getCommentDao().insertAll(comment);

                // получаем ответы на этот комментарий
                String commentPath = "https://evo-lutio.livejournal.com/" + articleId + ".html"
                        + "?thread=" + comment.getId() + "#t" + comment.getId();
                String response = loader.LoadPage(commentPath);

                // иногда приходит html ответ, иногда json ??

                //JSONArray jsonComments = GetJsonComments(new Document(response));
                //JSONObject childComments = new JSONObject(response);



                // можно было бы решить эту задачу, рекурсивно вызывая GetComments,
                // "рекурсия" всегда будет одного уровня, да и сама функция усложнится
                // разнообразными проверками. Надо будет соптимизировать
                JSONArray childCommensArray
//                        = childComments.getJSONArray("comments");
                        = GetJsonComments(Jsoup.parse(response));
                int len2 = childCommensArray.length();

                for (int j = 0; j < len2; j++) {
                    JSONObject comJson2 = childCommensArray.getJSONObject(j);
                    Comment comment2 = CommentParser.BuildComment(comJson2, articleId); // TODO: некоторые комментарии так и остаются неподгруженными

                    list.add(comment2);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String Tag(String tag) {
        return "%" + tag + "%";
    }
}
