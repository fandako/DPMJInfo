package com.example.dpmjinfo.recyclerViewHandling;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
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
import com.example.dpmjinfo.helpers.OfflineFileDb;
import com.example.dpmjinfo.queries.FavouriteQuery;
import com.example.dpmjinfo.queries.ScheduleQuery;

import java.util.ArrayList;
import java.util.List;

public class FavouriteAdapter extends BaseAdapter<BaseAdapter.BaseViewHolder, ScheduleQuery> {

    public FavouriteAdapter() {
        layout = R.layout.favourite_list_item;
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

    public interface OnDeleteClickListener {
        public void onDeleteClick();
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
        TextView queryType;
        LinearLayout parent, summaryLayout;
        Context context;
        AppCompatImageView delete;

        ViewHolder(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            queryType = itemView.findViewById(R.id.favouriteType);
            //querySummary = itemView.findViewById(R.id.favouriteSummary);
            summaryLayout = itemView.findViewById(R.id.summaryItems);
            delete = itemView.findViewById(R.id.delete);
            context = itemView.getContext();
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);

            ScheduleQuery query = items.get(position);

            queryType.setText(query.getName());

            List<Pair<String, String>> summaryItems = query.getSummary();

            summaryLayout.removeAllViews();

            for (Pair<String, String> p: summaryItems) {
                LinearLayout l = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.favourite_summary_item, null, false);

                ((TextView) l.findViewById(R.id.label)).setText(p.first);
                ((TextView) l.findViewById(R.id.value)).setText(p.second);

                summaryLayout.addView(l);
            }

            delete.setOnTouchListener(new DeleteClickListener(query));
        }

        private class DeleteClickListener implements View.OnTouchListener{
            private ScheduleQuery query;

            public DeleteClickListener(ScheduleQuery query){
                this.query = query;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    OfflineFileDb db = new OfflineFileDb(context);
                    if (db.deleteFavourite(query)) {

                        int itemPosition = items.indexOf(query);
                        items.remove(query);

                        notifyItemRemoved(itemPosition);
                        notifyItemRangeChanged(itemPosition, items.size());
                    } else {
                        Toast.makeText(context, context.getString(R.string.favourite_delete_fail_toast), Toast.LENGTH_SHORT).show();
                    }
                }

                //return true -> do not propagate to recyclerView item
                return true;
            }
        }
    }
}
