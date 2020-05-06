package com.example.dpmjinfo.recyclerViewHandling;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.dpmjinfo.R;
import com.prof.rssparser.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends BaseAdapter<BaseAdapter.BaseViewHolder, Article> {

    public ArticleAdapter() {
        layout = R.layout.article_list_item;
    }

    @Override
    public List<Integer> getHighlightedPositions() {
        return new ArrayList<>();
    }

    @NonNull
    @Override
    public BaseAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading, viewGroup, false));
            default:
                throw new Error();
        }
    }

    @Override
    public void onBindViewHolder(BaseAdapter.BaseViewHolder viewHolder, int i) {
        viewHolder.onBind(i);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == items.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    public class ViewHolder extends BaseAdapter.BaseViewHolder {
        TextView title, description;
        LinearLayout parent;
        Context context;
        AppCompatImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            title = itemView.findViewById(R.id.title);
            //querySummary = itemView.findViewById(R.id.favouriteSummary);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.image);
            context = itemView.getContext();
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);

            Article a = items.get(position);

            title.setText(a.getTitle());
            description.setText(a.getDescription());
        }
    }
}
