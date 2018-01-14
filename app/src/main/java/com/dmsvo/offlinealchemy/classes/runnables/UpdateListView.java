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

    public UpdateListView(@NonNull Main2Activity activity,
                          @NonNull List<CompleteArticle> articles
    ) {
        this.activity = activity;
        this.articles = articles;
    }

    @Override
    public void run() {
        try {

            ListView articlesView = activity.findViewById(R.id.articleslist);
            ArticleAdapter adapter = (ArticleAdapter) articlesView.getAdapter();
            if (adapter == null) {
                adapter = new ArticleAdapter(
                        activity,
                        articles);             //android.R.layout.simple_list_item_1,
                articlesView.setAdapter(adapter);
                articlesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(activity, ArticleViewActivity.class);

                        CompleteArticle cart = (CompleteArticle) adapterView.getItemAtPosition(i);
                        if (cart != null) {
                            cart.article.setWasRead(1);

                            intent.putExtra(activity.OPEN_ARTICLE, cart);
                            activity.startActivityForResult(intent,1);
                        }
                    }
                });
            } else {
                // не просто добавлять, а в конкретные места?
                adapter.addItems(articles);
            }

            //articlesView.setAdapter(adapter);
//            adapter.notifyDataSetChanged();

            ProgressBar pbar = activity.findViewById(R.id.progressBar);
            pbar.setVisibility(View.INVISIBLE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
