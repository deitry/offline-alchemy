package com.dmsvo.offlinealchemy.classes.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.base.Tag;
import com.dmsvo.offlinealchemy.classes.db.TagDao;
import com.dmsvo.offlinealchemy.classes.loader.Loader;
import com.dmsvo.offlinealchemy.classes.views.TagAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagsActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();

        updateList();
    }

    void updateList() {
        ProgressBar pbar = findViewById(R.id.progressBar3);
        pbar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // если тегов нет, наполняем
                TagDao tdao = Loader.GetAppDb().getTagDao();
                Loader loader = Loader.GetInstance();
                int total = tdao.getCount();
//                        if (total == 0) {
                loader.updateTags();
//                        }

                final List<Tag> tags = tdao.getAllTags();
                Collections.sort(tags);

//                final Set<Tag> tags = new HashSet<>(tagsList);

                // заполняем листвью тегами
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView tagsView = findViewById(R.id.tags_list);
                        BaseAdapter tagAdapter = new TagAdapter(
                                TagsActivity.this,
                                tags);
                        tagsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Tag tag = (Tag) adapterView.getItemAtPosition(i);
                                intent.putExtra(Main2Activity.OPEN_TAG,
                                        tag.getData());
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        });
                        tagsView.setAdapter(tagAdapter);
                        ProgressBar pbar = findViewById(R.id.progressBar3);
                        pbar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tags_menu, menu);
        return true;
    }
}
