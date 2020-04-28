package com.example.dpmjinfo;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T extends BusStopDeparturesAdapter.BaseViewHolder, U> extends RecyclerView.Adapter<T> {
    protected List<U> items = new ArrayList<>();
    protected List<U> highlightedItems = new ArrayList<>();
    protected boolean isLoaderVisible;

    public void highlightItem(U item) {
        highlightedItems.add(item);
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
            Log.d("dbg", e.getMessage());
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
}
