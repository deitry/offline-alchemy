package com.dmsvo.offlinealchemy.classes.runnables;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.activities.ArticleViewActivity;
import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.views.ArticleAdapter;

import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class UpdateListView implements Runnable {

    private Main2Activity activity;
    private List<CompleteArticle> articles;

    public UpdateListView(@NonNull Main2Activity activity, @NonNull List<CompleteArticle> articles) {
        this.activity = activity;
        this.articles = articles;
    }

    @Override
    public void run() {
        try {
            // определяем массив типа String
//            List<String> articleNames = new ArrayList<>();
//            for (Article article : articles) {
//                articleNames.add(article.getName());
//            }
            // используем адаптер данных
            ArticleAdapter adapter = new ArticleAdapter(
                    activity,
                    articles);             //android.R.layout.simple_list_item_1,

            ListView articlesView = activity.findViewById(R.id.articleslist);
            articlesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(activity, ArticleViewActivity.class);

                    CompleteArticle cart = (CompleteArticle) adapterView.getItemAtPosition(i);
                    if (cart != null) {
                        intent.putExtra(activity.OPEN_ARTICLE, cart);
                        activity.startActivity(intent);

                        cart.article.setWasRead(1);

                        final Article updatedArticle = cart.article;

                        Thread thr = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateListView.this.activity.getDb().getArticleDao().update(updatedArticle);
                            }
                        });
                        thr.start();
                    }
                }
            });

            articlesView.setAdapter(adapter);

            ProgressBar pbar = activity.findViewById(R.id.progressBar);
            pbar.setVisibility(View.INVISIBLE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
