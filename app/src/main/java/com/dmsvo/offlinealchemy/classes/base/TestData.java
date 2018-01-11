package com.dmsvo.offlinealchemy.classes.base;

import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by DmSVo on 07.01.2018.
 */

public class TestData {
    public static Comment CreateComment(int param, int articleId, int parentId)
    {
        Comment comment = new Comment();

        comment.setId(param);
        comment.setArticleId(articleId);
        comment.setAuthor("Author" + param);
        comment.setDate(new Date());
        comment.setParentId(parentId);
        comment.setBody("Body" + param);

        return comment;
    }

    public static Article CreateArticle(int param)
    {
        Article article = new Article();

        article.setId(param);
        article.setName("Title " + param);
        article.setBody("Body " + param);
        List<String> tags = new ArrayList<>();
        tags.add("tag" + param);
        article.setTags(tags);

        article.setDate(new Date());

        return article;
    }

    public static List<Comment> CreateCommentsList(int articleId)
    {
        List<Comment> comments = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            int parentId = ((i / 3) == 0)
                    ? i-1
                    : 0;
            comments.add(TestData.CreateComment(
                    articleId*100 + i,
                    articleId,
                    parentId));
        }

        return comments;
    }

    static List<CompleteArticle> GetTestData(Main2Activity activity, ArticleDao adao, CommentDao cdao)
    {
        // создаём тестовый набор данных
        for (int i = 1; i < 5; i++) {
            Article artcl = TestData.CreateArticle(i);
            List<Comment> comments = TestData.CreateCommentsList(i);
            artcl.setCommentsIds(comments);

            adao.insertAll(artcl);
            for (Comment com : comments) {
                cdao.insertAll(com);
            }
        }

        List<Article> articles = activity.getDb().getArticleDao().getAllArticles();
        List<CompleteArticle> carts = new ArrayList<>();
        for (Article artcl : articles)
        {
            List<Comment> comments =
                    activity.getDb().getCommentDao().getAllCommentsForArticle(artcl.getId());
            carts.add(new CompleteArticle(artcl, comments));
        }
        return carts;
    }
}
