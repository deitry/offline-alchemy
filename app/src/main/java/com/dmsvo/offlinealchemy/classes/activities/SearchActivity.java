package com.dmsvo.offlinealchemy.classes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dmsvo.offlinealchemy.R;

public class SearchActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Dark);
        setContentView(R.layout.activity_search2);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void onSearchClickBtn(View v)
    {
        // TODO: нужна отдельная активити с отображнием результатов поиска
        Intent newIntent = new Intent(
                SearchActivity.this,
                Main2Activity.class
        );

        EditText searchString = this.findViewById(R.id.search_string);
        CheckBox checkTitle = this.findViewById(R.id.check_title);
        CheckBox checkContent = this.findViewById(R.id.check_content);
        CheckBox checkComments = this.findViewById(R.id.check_comments);

        newIntent.putExtra(Main2Activity.SEARCH_STRING,
                searchString.getText().toString());

        newIntent.putExtra(Main2Activity.SEARCH_TITLE,
                checkTitle.isChecked());
        newIntent.putExtra(Main2Activity.SEARCH_CONTENT,
                checkContent.isChecked());
        newIntent.putExtra(Main2Activity.SEARCH_COMMENTS,
                checkComments.isChecked());

        SearchActivity.this.startActivity(newIntent);
    }
}
