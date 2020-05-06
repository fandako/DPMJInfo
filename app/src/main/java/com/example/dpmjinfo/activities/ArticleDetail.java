package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.example.dpmjinfo.R;

import java.io.Serializable;

/**
 * Activity for displaying single article (title + text)
 */
public class ArticleDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the required data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String title = (String) bundle.getSerializable("com.android.dpmjinfo.articleTitle");
        String text = (String) bundle.getSerializable("com.android.dpmjinfo.articleText");

        TextView titleView = findViewById(R.id.title);
        TextView textView = findViewById(R.id.text);

        titleView.setText(title);
        //textView.setText(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(text));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
