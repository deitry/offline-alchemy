package com.dmsvo.offlinealchemy.classes.runnables;

import com.dmsvo.offlinealchemy.classes.db.AppDb;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class ClearDb implements Runnable {

    private AppDb db;

    public ClearDb(AppDb db) { this.db = db; }

    @Override
    public void run() {
        try {
            db.getCommentDao().clearComments();
            db.getArticleDao().clearArticles();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
