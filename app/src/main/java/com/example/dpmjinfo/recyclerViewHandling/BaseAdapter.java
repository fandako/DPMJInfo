package com.example.dpmjinfo.recyclerViewHandling;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * base adapter for recyclerView
 * @param <T> viewHolder class i.e. BaseAdapter.BaseViewHolder or extending
 * @param <U> adapter items class
 */
public abstract class BaseAdapter<T extends BaseAdapter.BaseViewHolder, U> extends RecyclerView.Adapter<T> {
    protected List<U> items = new ArrayList<>();
    protected List<U> highlightedItems = new ArrayList<>();
    protected boolean isLoaderVisible;
    protected int layout;
    protected int highlightedLayout;

    protected static final int VIEW_TYPE_LOADING = 0;
    protected static final int VIEW_TYPE_NORMAL = 1;
    protected static final int VIEW_TYPE_HIGHLIGHTED = 2;

    /**
     * adds given items to highlighted
     * @param items items to highlight
     */
    public void highlightItems(List<U> items) {
        highlightedItems.addAll(items);
    }

    /**
     * retrieve positions of highlighted items in items array
     * @return list of positions
     */
    public abstract List<Integer> getHighlightedPositions();

    /**
     * adds items to adapter
     * @param postItems items to add
     */
    public void addItems(List<U> postItems) {
        items.addAll(postItems);
        notifyDataSetChanged();
    }

    /**
     * add loading dummy item to display progressbar
     * when loading next batch of items from query
     * @param impl class of adapter items
     */
    public void addLoading(Class<? extends U> impl) {
        isLoaderVisible = true;
        try {
            items.add(impl.getDeclaredConstructor().newInstance());
        } catch (Exception e){
            Log.d("dbg", e.getMessage());
            throw new Error();
        }

        notifyItemInserted(items.size() - 1);
    }

    /**
     * remove loading dummy item to hide progressbar
     */
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

    /**
     * retrieve item on given position
     * @param position position
     * @return item
     */
    public U getItem(int position) {
        return items.get(position);
    }

    /**
     * clears adapter items list
     */
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
