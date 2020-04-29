package com.example.dpmjinfo;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T extends BaseAdapter.BaseViewHolder, U> extends RecyclerView.Adapter<T> {
    protected List<U> items = new ArrayList<>();
    protected List<U> highlightedItems = new ArrayList<>();
    protected boolean isLoaderVisible;
    protected int layout;
    protected int highlightedLayout;

    protected static final int VIEW_TYPE_LOADING = 0;
    protected static final int VIEW_TYPE_NORMAL = 1;
    protected static final int VIEW_TYPE_HIGHLIGHTED = 2;

    public void highlightItems(List<U> items) {
        highlightedItems.addAll(items);
    }

    public abstract List<Integer> getHighlightedPositions();

    public void addItems(List<U> postItems) {
        items.addAll(postItems);
        notifyDataSetChanged();
    }

    public void addLoading(Class<? extends U> impl) {
        isLoaderVisible = true;
        try {
            items.add(impl.getDeclaredConstructor().newInstance());
        } catch (Exception e){
            throw new Error();
        }

        notifyItemInserted(items.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        if(!items.isEmpty()) {
            int position = items.size() - 1;
            U item = getItem(position);
            if (item != null) {
                items.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    U getItem(int position) {
        return items.get(position);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        private int mCurrentPosition;

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void clear();

        public void onBind(int position) {
            mCurrentPosition = position;
            clear();
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }
    }

    public class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void clear() {
        }
    }
}
