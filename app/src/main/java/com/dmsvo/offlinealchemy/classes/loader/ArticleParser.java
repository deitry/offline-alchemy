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

        List<Comment> comments = GetComments(loader.LoadComments(id), id);

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

    static List<Comment> GetComments(JSONArray commentsJson, int articleId)
    {
        List<Comment> list = new ArrayList<>();

        int len = commentsJson.length();
        try {
            for (int i = 0; i < len; i++) {
                JSONObject commentJson = commentsJson.getJSONObject(i);
                Comment comment = CommentParser.BuildComment(commentJson, articleId);
                if (comment != null) {
                    list.add(comment);
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
