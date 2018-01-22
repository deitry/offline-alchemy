package com.dmsvo.offlinealchemy.classes.activities;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.base.Tag;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.db.ArticleDao;
import com.dmsvo.offlinealchemy.classes.db.TagDao;
import com.dmsvo.offlinealchemy.classes.loader.ArticleParser;
import com.dmsvo.offlinealchemy.classes.runnables.ClearDb;
import com.dmsvo.offlinealchemy.classes.runnables.LoadFromDb;
import com.dmsvo.offlinealchemy.classes.loader.Loader;
import com.dmsvo.offlinealchemy.classes.runnables.DownloadArticles;
import com.dmsvo.offlinealchemy.classes.views.ArticleAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String OPEN_ARTICLE = "com.dmsvo.offlinealchemy.ARTICLE";
    public static final String OPEN_TAG = "com.dmsvo.offlinealchemy.TAG";
    public static final int I_OPEN_ARTICLE = 1;
    public static final int I_OPEN_TAGS = 2;

    public static final String PREF_NAME = "preferences";
    public static final String LAST_DATE = "edgeDate";
    public static final String NEW_COUNT = "newCount";

    Handler handler;
    AppDb db;
    Loader loader;
    String tagToOpen = "";

    public String getTagToOpen() { return tagToOpen; }

    public AppDb getDb() {
        return db;
    }

    public Handler getHandler() {
        return handler;
    }

    public Loader getLoader() {
        return loader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Dark);

        setContentView(R.layout.activity_main2);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDb.class, "articles-database")
                .addMigrations(AppDb.MIGRATION_2_3)
                .addMigrations(AppDb.MIGRATION_3_4)
//                .fallbackToDestructiveMigration()
                .build();

        handler = new Handler(Looper.getMainLooper());
        loader = new Loader(db);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(OPEN_TAG)) {
            try {
                tagToOpen = intent.getStringExtra(OPEN_TAG);
                if (tagToOpen != null & !tagToOpen.equals("")) {
                    this.setTitle(tagToOpen);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        ProgressBar pbar = findViewById(R.id.progressBar);
        pbar.setVisibility(View.VISIBLE);

        new Thread(new LoadFromDb(this, Loader.BASE_CNT, 0)).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == I_OPEN_ARTICLE) {
            if(resultCode == Activity.RESULT_OK){
                CompleteArticle result = (CompleteArticle) data.getSerializableExtra(this.OPEN_ARTICLE);

                ListView articlesView = findViewById(R.id.articleslist);
                final ArticleAdapter adapter = (ArticleAdapter) articlesView.getAdapter();

                for (int i = 0; i < adapter.getCount(); i++)
                {
                    final CompleteArticle item = (CompleteArticle) adapter.getItem(i);
                    if (item.article.getId() == result.article.getId())
                    {
                        item.article = result.article;
                        item.comments = result.comments;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                loader.SaveInDb(result);
                            }
                        }).start();

                        break;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_has_new:
                if (loader.isOnline()) {
                    ProgressBar pbar = findViewById(R.id.progressBar);
                    pbar.setVisibility(View.VISIBLE);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final int newArticles = Main2Activity.this.loader.hasNewArticles();
                            if (newArticles > 0) {

                                SharedPreferences preferences =
                                        Main2Activity.this.getSharedPreferences(
                                                Main2Activity.PREF_NAME,
                                                MODE_PRIVATE);
                                SharedPreferences.Editor edit = preferences.edit();

                                edit.putInt(Main2Activity.NEW_COUNT, newArticles);
                                if (newArticles >= 50) {
                                    // если скопилось слишком много новых статей, на всякий случай сбрасываем
                                    // дату последней скачанной статьи, чтобы перезакачать все
                                    edit.putLong(Main2Activity.LAST_DATE, 0);
                                }
                                edit.commit();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Новых статей: " + newArticles,
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                        ProgressBar pbar = findViewById(R.id.progressBar);
                                        pbar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Новых статей нет!",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                        ProgressBar pbar = findViewById(R.id.progressBar);
                                        pbar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }
                    });
                    thread.start();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Отсутствует подключение к интернету!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            case R.id.action_refresh_list:
                if (loader.isOnline()) {
                    ProgressBar pbar = findViewById(R.id.progressBar);
                    pbar.setVisibility(View.VISIBLE);

                    Thread thread = new Thread(new DownloadArticles(this,true));
                    thread.start();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Отсутствует подключение к интернету!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            case R.id.action_open_path: {
                if (loader.isOnline()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this);
                    alertDialog.setTitle("Открыть по ссылке");
                    alertDialog.setMessage("Введите путь к статье");

                    final EditText input = new EditText(Main2Activity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    alertDialog.setView(input);

                    alertDialog.setPositiveButton("Ок",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String path = input.getText().toString();
                                    if (path != null && !path.equals("")) {
                                        // пытаемся загрузить статью
                                        ProgressBar pbar = findViewById(R.id.progressBar);
                                        pbar.setVisibility(View.VISIBLE);

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                CompleteArticle cart = null;
                                                try {
                                                    String tPath;
                                                    int artId = 0;
                                                    try {
                                                        artId = Integer.parseInt(path);
                                                    } catch (Throwable t) {

                                                    }

                                                    if (artId != 0) {
                                                        tPath = loader.getRoot() + artId + ".html";
                                                    } else {
                                                        tPath = path;
                                                    }

                                                    cart = loader.LoadArticle(tPath);
                                                } catch (Throwable t) {
                                                    t.printStackTrace();
                                                }

                                                if (cart != null) {
                                                    loader.SaveInDb(cart);
                                                    final List<CompleteArticle> list = new ArrayList<>();
                                                    list.add(cart);

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ListView articlesView = Main2Activity.this.findViewById(R.id.articleslist);
                                                            ArticleAdapter adapter = (ArticleAdapter) articlesView.getAdapter();

                                                            adapter.addItems(list);

                                                            ProgressBar pbar = findViewById(R.id.progressBar);
                                                            pbar.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                } else {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast toast = Toast.makeText(Main2Activity.this.getApplicationContext(),
                                                                    "Не удалось загрузить статью!",
                                                                    Toast.LENGTH_SHORT);
                                                            toast.show();

                                                            ProgressBar pbar = findViewById(R.id.progressBar);
                                                            pbar.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                }
                                            }
                                        }).start();
                                    } else {
                                        Toast toast = Toast.makeText(Main2Activity.this.getApplicationContext(),
                                                "Отсутствует путь!",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }
                            });

                    alertDialog.setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    alertDialog.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Отсутствует подключение к интернету!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }

                return true;
            }
            case R.id.action_settings:
                return true;
            case R.id.action_download_article:
                if (loader.isOnline()) {
                    ProgressBar pbar = findViewById(R.id.progressBar);
                    pbar.setVisibility(View.VISIBLE);

                    Thread thread = new Thread(new DownloadArticles(this, false));
                    thread.start();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Отсутствует подключение к интернету!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            case R.id.action_clear_db: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setTitle("Очистить базу данных")
                        .setMessage("Вы уверены?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener()
                {
                    public void onClick (DialogInterface dialog,int which){
                        // очистка базы данных
                        Thread myThread = new Thread(new ClearDb(Main2Activity.this.db));
                        myThread.start();

                        ListView articlesView = findViewById(R.id.articleslist);
                        articlesView.setAdapter(null);
                    }
                })
                        .setNegativeButton("Отмена", null)   //Do nothing on no
                        .show();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        final ArticleDao adao = db.getArticleDao();

        if (id == R.id.nav_overall) {
            // отобразить статьи без тега
            tagToOpen = null;

            ProgressBar pbar = findViewById(R.id.progressBar);
            pbar.setVisibility(View.VISIBLE);

            ListView articlesView = findViewById(R.id.articleslist);
            articlesView.setAdapter(null);

            Thread thread = new Thread(new LoadFromDb(this, Loader.BASE_CNT, 0));
            thread.start();

        } else if (id == R.id.nav_random) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int cnt = adao.getCount();
                        int rint = new Random().nextInt(cnt);
                        Article article = adao.getArticleByOffset(rint);
                        List<Comment> comments = loader.GetComments(article.getId());

                        Intent intent = new Intent(Main2Activity.this, ArticleViewActivity.class);

                        CompleteArticle cart = new CompleteArticle(article, comments);
                        if (cart != null) {
                            cart.article.setWasRead(1);

                            intent.putExtra(Main2Activity.this.OPEN_ARTICLE, cart);
                            Main2Activity.this.startActivityForResult(intent, 1);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }).start();

        } else if (id == R.id.nav_future_read) {

        } else if (id == R.id.nav_favorite) {

        } else if (id == R.id.nav_by_tags) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // если тегов нет, заполняем
                    Intent intent = new Intent(
                            Main2Activity.this,
                            TagsActivity.class);
                    intent.putExtra("search_type", 1);
                    Main2Activity.this.startActivity(   // requestCode = 2
                            intent);
                }
            }).start();

        } else if (id == R.id.nav_by_date) {

        } else if (id == R.id.nav_search) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClickBtn(View v)
    {
        ListView articles = findViewById(R.id.articleslist);
        int cur = articles.getAdapter().getCount();

        ProgressBar pbar = findViewById(R.id.progressBar);
        pbar.setVisibility(View.VISIBLE);

        // FIXME: вообще говоря, оффсет для чтения из базы данных и текущее количество
        // записей - это скорее всего немного разные вещи. Попробуем так
        new Thread(new LoadFromDb(this,Loader.BASE_CNT, cur)).start();
        //Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
    }

}
