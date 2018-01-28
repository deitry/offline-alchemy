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
    boolean loadOld;

    public DownloadArticles(@NonNull Main2Activity activity, boolean loadOld) {
        this.activity = activity;
        this.loadOld = loadOld;
    }

    @Override
    public void run() {
        try  {
            ArticleDao adao = activity.getDb().getArticleDao();
            CommentDao cdao = activity.getDb().getCommentDao();

            List<CompleteArticle> carts;
//            = activity.getLoader().LoadFromDb();
            SharedPreferences preferences = activity.getSharedPreferences(Main2Activity.PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            
            // новый подход - загружаем не "по 1 и по 5" и не "быстро или медленно", а "новые и старые"

            if (loadOld) {
                // по старому, но без проверок на новые статьи
                long lastTime = preferences.getLong(Main2Activity.LAST_DATE, 0);
                Date lastDate;
                if (lastTime > 0)
                    lastDate = new Date(lastTime);
                else
                    lastDate = new Date();

                carts = activity.getLoader().LoadNumber(Loader.BASE_CNT, true, lastTime);

                // запоминаем дату последней статьи, чтобы при следующей загрузке начинать с неё
                for (CompleteArticle cart : carts)
                {
                    Date artDate = cart.article.getDate();
                    if (artDate.before(lastDate)) {
                        lastDate = artDate;
                    }
                }

                edit.putLong(Main2Activity.LAST_DATE, lastDate.getTime());
                edit.commit();

            } else {
                // получаем все новые статьи с глагне - нам всё равно надо будет её загрузить,
                // можно не сохранять значение "а сколько там сейчас новых"
                carts = activity.getLoader().LoadNew();
            }

            activity.getHandler().post(new UpdateListView(activity,
                    carts));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
