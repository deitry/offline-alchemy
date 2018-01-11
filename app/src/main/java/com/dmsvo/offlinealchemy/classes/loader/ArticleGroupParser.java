package com.dmsvo.offlinealchemy.classes.loader;

/**
 * Created by DmSVo on 31.12.2017.
 */

import com.dmsvo.offlinealchemy.classes.base.Article;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Функции парсинга для страницы, содержащей сколько-то статей (от нуля и больше)
 */
public class ArticleGroupParser {
    /**
     * Есть ли в данном документе хотя бы одна статья
     * @param document
     * @return
     */
    static List<Article> GetArticles(Document document) {
        // находим контейнеры с описаниями статей
        Elements elms = document.getElementsByClass("entry-wrap js-emojis");
        for (Element el : elms)
        {
            // берём путь к конечной статье
            // запрашиваем статью
            //
        }
        return null;
    }

    static boolean CanPrev(Document document) {
        return false;
    }

    /**
     * Получение пути со списком предыдущих статей
     * @param document
     * @return
     */
    static String PrevPath(Document document) {
        // имеется всего один элемент с классом prev
        // у него имеется единственная вложенная нода типа "a href="prev_path"
        Elements elms = document.getElementsByClass("prev");
        if (elms != null && elms.size() > 0)
        {
            Element el = elms.first();
            elms = el.getElementsByTag("a");
            if (elms== null || elms.size() == 0) return "";

            Element first =elms.first();
            String path = first.attr("href");

            return path;
        }
        return "";
    }

    static String GetArticlePath(Element article) {
        Elements titles = article.getElementsByClass("entry-title");
        if (titles == null || titles.size() == 0) return "";

        Element title = titles.first();
        Element link = title.getElementsByTag("a").first();

        String path = link.attr("href");
        return path;
    }
}
