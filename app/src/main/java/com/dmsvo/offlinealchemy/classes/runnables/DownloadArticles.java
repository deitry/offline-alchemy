package com.dmsvo.offlinealchemy.classes.runnables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.CommentDao;
import com.dmsvo.offlinealchemy.classes.loader.Loader;

import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

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

            List<CompleteArticle> carts;
//            = activity.getLoader().LoadFromDb();

            if (fast) {
                SharedPreferences preferences = activity.getSharedPreferences(Main2Activity.PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();

                // запоминаем дату последней статьи, чтобы при следующей загрузке начинать с неё
                long lastTime = preferences.getLong(Main2Activity.LAST_DATE, 0);
                Date lastDate;
                if (lastTime > 0)
                    lastDate = new Date(lastTime);
                else
                    lastDate = new Date();

                int newCount = preferences.getInt(Main2Activity.NEW_COUNT, 0);

                if (newCount > 0) {
                    carts = activity.getLoader().LoadNumber(newCount, true, 0);
                    carts.addAll(activity.getLoader().LoadNumber(Loader.BASE_CNT - newCount, true, lastTime));
                } else {
                    carts = activity.getLoader().LoadNumber(Loader.BASE_CNT, true, lastTime);
                }

                for (CompleteArticle cart : carts)
                {
                    Date artDate = cart.article.getDate();
                    if (artDate.before(lastDate)) {
                        lastDate = artDate;
                    }
                }

                edit.putLong(Main2Activity.LAST_DATE, lastDate.getTime());
                edit.commit();
//                carts.addAll(activity.getLoader().LoadNumber(20, true));
            } else {
                carts = activity.getLoader().LoadNumber(1, false, 0);
//                carts.addAll(activity.getLoader().LoadNumber(1, false));
//                    TestData.GetTestData(activity, adao, cdao);
            }

            activity.getHandler().post(new UpdateListView(activity,
                    carts));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
