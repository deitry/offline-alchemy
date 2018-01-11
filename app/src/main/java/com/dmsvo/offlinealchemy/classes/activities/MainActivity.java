package com.dmsvo.offlinealchemy.classes.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    Handler handler;
    List<CompleteArticle> articles;
    AppDb db;

    public AppDb getDb() { return db; }
    public Handler getHandler() { return handler; }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*
        mTextMessage.setText("Началась загрузка...");

        handler= new Handler(Looper.getMainLooper());

        //handler.post(
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    // отображаем скачанные записи.
                    // в первом приближении - отображаем названия в одной строке
                    Loader loader = new Loader(db);
                    articles = loader.LoadAll();

                    // TODO: добавление скачанных статей в базу данных / файл

                    handler.post(new Runnable() {
                                     @Override
                                     public void run() {

                                         // TODO: представление списка статей в виде СПИСКА объектов

                                         String textToShow = "";
                                         for (CompleteArticle artcl : articles)
                                         {
                                             textToShow += artcl.article.getName() + "; ";
                                         }
                                         mTextMessage.setText(textToShow);
                                     }
                                 });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        */
    }

}
