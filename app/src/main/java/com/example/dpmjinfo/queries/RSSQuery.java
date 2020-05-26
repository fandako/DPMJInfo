package com.example.dpmjinfo.queries;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpmjinfo.R;
import com.example.dpmjinfo.activities.ArticleDetail;
import com.example.dpmjinfo.activities.DeparturesActivity;
import com.example.dpmjinfo.queryModels.ScheduleQueryModel;
import com.example.dpmjinfo.recyclerViewHandling.ArticleAdapter;
import com.example.dpmjinfo.recyclerViewHandling.BaseAdapter;
import com.example.dpmjinfo.recyclerViewHandling.RecycleViewClickListener;
import com.prof.rssparser.Article;
import com.prof.rssparser.Channel;
import com.prof.rssparser.OnTaskCompleted;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * query object for querying articles from web using RSS feed
 */
public class RSSQuery extends ScheduleQuery {
    private final Boolean lock = false;
    private final String url = "https://www.androidauthority.com/feed/";
    private boolean loaded;
    private boolean cacheResults = false;
    List<Article> cachedArticles = null;

    public RSSQuery(Context context) {
        super(context);

        model = new ScheduleQueryModel();
        model.setShowAddToFavourite(false);
        loaded = false;
    }

    public RSSQuery(Context context, ScheduleQueryModel model) {
        super(context);

        this.model = model;
        loaded = false;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isInternetDependant() {
        return true;
    }

    public void setCacheResults(boolean cacheResults) {
        this.cacheResults = cacheResults;
    }

    @Override
    public List<Article> exec(int page) {
        Parser parser = new Parser();
        List<Article> articles = new ArrayList<>();

        if(cachedArticles != null){
            articles.addAll(cachedArticles);
            cachedArticles = null;

            return articles;
        }

        parser.onFinish(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(@NonNull Channel channel) {

                for (Article a:channel.getArticles()) {
                    Log.d("dbg", "title: " + a.getTitle());
                }
                articles.addAll(channel.getArticles());

                synchronized (lock){
                    loaded = true;
                    lock.notify();
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                synchronized (lock){
                    loaded = true;
                    lock.notify();
                }
            }
        });

        parser.execute(url);

        synchronized (lock){
            while(!loaded) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        for (Article a:articles) {
            Log.d("dbg", a.getTitle());
        }

        if (cacheResults) {
            cachedArticles = new ArrayList<>();
            cachedArticles.addAll(articles);
        }

        return articles;
    }

    @Override
    public Class getObjectClass() {
        return Article.class;
    }

    @Override
    public String getName() {
        return mContext.getString(R.string.articles_title);
    }

    @Override
    public BaseAdapter getAdapter() {
        return new ArticleAdapter();
    }

    @Override
    public void execAndDisplayResult() {
        Bundle bundle = new Bundle();
        Intent intent;

        intent = new Intent(mContext.getApplicationContext(), DeparturesActivity.class);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    @Override
    public boolean hasClickableItems() {
        return true;
    }

    @Override
    public RecycleViewClickListener getOnItemTouchListener(Context context, RecyclerView.Adapter adapter) {
        return new OnDepartureTouchListener(context, (ArticleAdapter) adapter);
    }

    private class OnDepartureTouchListener implements RecycleViewClickListener {

        Context context;
        ArticleAdapter adapter;

        OnDepartureTouchListener(Context context, ArticleAdapter adapter) {
            this.context = context;
            this.adapter = adapter;
        }


        private void openDetail(int position) {
            Article article = adapter.getItem(position);

            Bundle bundle = new Bundle();
            Intent intent;

            intent = new Intent(mContext.getApplicationContext(), ArticleDetail.class);
            bundle.putSerializable("com.android.dpmjinfo.articleTitle", article.getTitle());
            bundle.putSerializable("com.android.dpmjinfo.articleText", article.getContent());
            intent.putExtras(bundle);

            //start given activity
            mContext.startActivity(intent);
        }

        @Override
        public void onClick(View view, final int position) {
            openDetail(position);
        }

        @Override
        public void onLongClick(View view, int position) {
            openDetail(position);
        }
    }
}
