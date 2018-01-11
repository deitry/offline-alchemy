package com.dmsvo.offlinealchemy.classes.activities;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.runnables.ClearDb;
import com.dmsvo.offlinealchemy.classes.runnables.LoadFromDb;
import com.dmsvo.offlinealchemy.classes.loader.Loader;
import com.dmsvo.offlinealchemy.classes.runnables.DownloadArticles;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String OPEN_ARTICLE = "com.dmsvo.offlinealchemy.ARTICLE";

    Handler handler;
    AppDb db;
    Loader loader;

    public AppDb getDb() { return db; }
    public Handler getHandler() { return handler; }
    public Loader getLoader() { return loader; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
//                .fallbackToDestructiveMigration()
                .build();

        handler = new Handler(Looper.getMainLooper());
        loader = new Loader(db);

        Thread thread = new Thread(new LoadFromDb(this));
        thread.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                        .setNegativeButton("Отмена", null)                        //Do nothing on no
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}