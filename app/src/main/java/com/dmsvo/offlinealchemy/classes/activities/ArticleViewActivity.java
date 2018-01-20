package com.dmsvo.offlinealchemy.classes.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
//import android.support.design.widget.CollapsingToolbarLayout;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.loader.ArticleParser;
import com.dmsvo.offlinealchemy.classes.loader.Loader;
import com.dmsvo.offlinealchemy.classes.views.CommentAdapter;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;

import java.util.Random;

// https://habrahabr.ru/post/166351/

public class ArticleViewActivity extends AppCompatActivity {

    private CompleteArticle cart;
    Handler handler;
    Intent intent;

    public Handler getHandler() { return handler; }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Dark);

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

            intent = getIntent();
            cart = (CompleteArticle) intent.getSerializableExtra(Main2Activity.OPEN_ARTICLE);

            handler = new Handler(Looper.getMainLooper());

            // если статья не загружена, попытаться загрузить
            if (cart == null) {
                final Uri link = intent.getData();
                ProgressBar pbar = findViewById(R.id.progressBar2);
                pbar.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String path = link.toString();
                        int id = ArticleParser.GetIdFromPath(path);

                        cart = Loader.GetInstance().GetArticle(id);

                            runOnUiThread(new Runnable() {
                                @Override
                            public void run() {
                                updateArticle();
                                ProgressBar pbar = findViewById(R.id.progressBar2);
                                pbar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }).start();
            } else {
                updateArticle();
                setResult(Activity.RESULT_OK, intent);
            }

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
            case R.id.action_open_browser:
            {
                try {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(Loader.root + cart.article.getId() + ".html"));
                    startActivity(browserIntent);
                } catch (Throwable t) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не удалось открыть страницу!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            }
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

//                            loader.SaveInDb(cart);

                            intent.putExtra(Main2Activity.OPEN_ARTICLE, cart);

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
            case R.id.action_settings:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CollapsingToolbarLayout appBar = ArticleViewActivity.this.findViewById(R.id.toolbar_layout);
                                appBar.setTitle("Рандом " + new Random().nextInt(10));
                            }
                        });
                    }
                }).start();
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
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (cart != null) {
                CollapsingToolbarLayout appBar = ArticleViewActivity.this.findViewById(R.id.toolbar_layout);
                appBar.setTitle(cart.article.getName());
            }
        }

        TextView commentsHeader = findViewById(R.id.comments_header);
        commentsHeader.setText("Комментарии: " + cart.comments.size());

        TextView bodyView = findViewById(R.id.article_body);
        bodyView.setText(Html.fromHtml(cart.article.getBody()));
        bodyView.setMovementMethod(LinkMovementMethod.getInstance());

        if (cart != null) {
            CommentAdapter adapter = new CommentAdapter(
                    ArticleViewActivity.this,
                    cart.comments);

            ListView commentsView = findViewById(R.id.commentsView);
            commentsView.setAdapter(adapter);
        }
    }
}
