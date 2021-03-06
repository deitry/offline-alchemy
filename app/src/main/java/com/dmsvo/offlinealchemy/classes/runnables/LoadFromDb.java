package com.dmsvo.offlinealchemy.classes.runnables;

import android.support.annotation.NonNull;

import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;
import com.dmsvo.offlinealchemy.classes.loader.ArticleParser;

import java.lang.Runnable;
import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class LoadFromDb implements Runnable {
    private Main2Activity activity;
    private int count;
    private int offset;

    public LoadFromDb(@NonNull Main2Activity activity, int count, int offset) {
        this.activity = activity;
        this.count = count;
        this.offset = offset;
    }

    @Override
    public void run() {
        try {
            AppDb db = activity.getDb();
            ArticleDao adao = db.getArticleDao();
            CommentDao cdao = db.getCommentDao();

            List<CompleteArticle> carts;
            String tag = activity.getTagToOpen();
            String search = activity.searchString;

            if (tag != null && !tag.equals("")) {
                carts = activity.getLoader().LoadFromDb(tag, count, offset);
            } else if (search != null && !search.equals("")) {
                carts = activity.getLoader().SearchInDb(search, count, offset,
                        activity.searchTitle, activity.searchContent, activity.searchComments);
            } else {
                carts = activity.getLoader().LoadFromDb(count, offset);
            }

            activity.getHandler().post(new UpdateListView(activity, carts));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
