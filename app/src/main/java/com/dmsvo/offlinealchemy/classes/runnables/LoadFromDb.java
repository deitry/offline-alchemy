package com.dmsvo.offlinealchemy.classes.runnables;

import android.support.annotation.NonNull;

import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;

import java.lang.Runnable;
import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class LoadFromDb implements Runnable {
    private Main2Activity activity;

    public LoadFromDb(@NonNull Main2Activity activity) { this.activity = activity; }

    @Override
    public void run() {
        try {
            AppDb db = activity.getDb();
            ArticleDao adao = db.getArticleDao();
            CommentDao cdao = db.getCommentDao();

            List<CompleteArticle> carts = activity.getLoader().LoadFromDb();

            activity.getHandler().post(new UpdateListView(activity,
                    carts));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}