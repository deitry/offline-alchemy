package com.dmsvo.offlinealchemy.classes.base;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DmSVo on 06.01.2018.
 */

public class CompleteArticle implements Serializable {
    public Article article;
    public List<Comment> comments;
        // поскольку мы не можем хранить комментарии в теле Article
        // (Room не умеет подгружать их в БД)
        // в Article храним id-шники комментариев,
        // а в FullArticle соединяем Article с собсно комментариями
        // выводить "в свет" будем именно FullArticle

    public CompleteArticle(Article article, List<Comment> comments)
    {
        this.article = article;
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = new ArrayList<>();
        }
    }
}
