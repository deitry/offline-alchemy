package com.dmsvo.offlinealchemy.classes.runnables;

import android.support.annotation.NonNull;

import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;

import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class DownloadArticles implements Runnable {

    private Main2Activity activity;
    boolean fast;

    public DownloadArticles(@NonNull Main2Activity activity, boolean fast) {
        this.activity = activity;
        this.fast = fast;
    }

    @Override
    public void run() {
        try  {
            ArticleDao adao = activity.getDb().getArticleDao();
            CommentDao cdao = activity.getDb().getCommentDao();

            List<CompleteArticle> carts =
                    activity.getLoader().LoadFromDb();

            if (fast) {
                carts.addAll(activity.getLoader().LoadNumber(20, true));
            } else {
                carts.addAll(activity.getLoader().LoadNumber(1, false));
//                    TestData.GetTestData(activity, adao, cdao);
            }

            activity.getHandler().post(new UpdateListView(activity,
                    carts));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
