package com.dmsvo.offlinealchemy.classes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
//import android.support.design.widget.CollapsingToolbarLayout;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.loader.Loader;
import com.dmsvo.offlinealchemy.classes.views.CommentAdapter;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;

// https://habrahabr.ru/post/166351/

public class ArticleViewActivity extends AppCompatActivity {

    private CompleteArticle cart;
    Handler handler;

    public Handler getHandler() { return handler; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_article_view);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            Intent intent = getIntent();
            cart = (CompleteArticle) intent.getSerializableExtra(Main2Activity.OPEN_ARTICLE);

            handler = new Handler(Looper.getMainLooper());

            updateArticle();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String entryName = getResources().getResourceEntryName(id);
        String resourceName = getResources().getResourceName(id);

        // стандартный подход почему-то не работает
        if (entryName.contentEquals("home"))
        {
            finish();
            return true;
        }

        switch (id) {
            case R.id.reload_article:
            {
                Thread networking = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final Loader loader = Loader.GetInstance();

                        if (loader.isOnline() == false)
                        {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Отсутствует подключение к интернету!",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        CompleteArticle newVersion = loader.LoadArticle(loader.getRoot()
                                    + cart.article.getId()
                                    + ".html");

                        if (newVersion != null) {
                            cart = newVersion;

                            loader.SaveInDb(cart);

                            ArticleViewActivity.this
                                    .getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    updateArticle();

                                    ProgressBar pbar = findViewById(R.id.progressBar2);
                                    pbar.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
                            ArticleViewActivity.this
                                    .getHandler().post(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                               Toast toast = Toast.makeText(getApplicationContext(),
                                                                       "Ошибка загрузки!",
                                                                       Toast.LENGTH_SHORT);
                                                               toast.show();

                                                               ProgressBar pbar = findViewById(R.id.progressBar2);
                                                               pbar.setVisibility(View.INVISIBLE);
                                                           }
                                                       });
                        }
                    }
                });

                ProgressBar pbar = findViewById(R.id.progressBar2);
                pbar.setVisibility(View.VISIBLE);

                networking.start();
            }

                break;
            case R.id.homeAsUp:
                super.onBackPressed();
                break;
            case R.id.home:
                super.onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    void updateArticle()
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            //final Drawable upArrow = getResources().getDrawable(R.drawable.ic_home_black_24dp);
            //upArrow.setColorFilter(getResources().getColor(R.color.background_material_light), PorterDuff.Mode.SRC_ATOP);
            //actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true); //Set this to true if selecting "home" returns up by a single level in your UI rather than back to the top level or front page.
            //actionBar.setHomeAsUpIndicator(upArrow); // set a custom icon for the default home button
            actionBar.setTitle(cart.article.getName());
        }

        TextView commentsHeader = findViewById(R.id.comments_header);
        commentsHeader.setText("Комментарии: " + cart.comments.size());

//            TextView headerView = findViewById(R.id.article_header);
//            headerView.setText();

        TextView bodyView = findViewById(R.id.article_body);
        bodyView.setText(Html.fromHtml(cart.article.getBody()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                final CommentAdapter adapter = new CommentAdapter(
                        ArticleViewActivity.this,
                        Loader.GetInstance().GetComments(cart.article.getId()));
//                          Loader.GetAppDb().getCommentDao().getAllChildComments(
//                                cart.article.getId(),
//                                0));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ListView commentsView = findViewById(R.id.commentsView);
                        commentsView.setAdapter(adapter);
                        commentsView.setOnTouchListener(new View.OnTouchListener() {

                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                    return true; // Indicates that this has been handled by you and will not be forwarded further.
                                }
                                return false;
                            }});

                    }
                });
            }
        }).start();
    }
}
