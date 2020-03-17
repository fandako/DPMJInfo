package com.example.dpmjinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MapObjectSelectionItemsAdapter extends RecyclerView.Adapter<MapObjectSelectionItemsAdapter.MyViewHolder> {
    List<MapObjectSelectionItem> mObjects;
    public MapObjectSelectionItemsAdapter(List<MapObjectSelectionItem> objects) {
        this.mObjects = objects;
    }

    public List<MapObjectSelectionItem> getObjectList(){
        return mObjects;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.map_object_selection_list_item, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        MapObjectSelectionItem data=mObjects.get(i);
        viewHolder.icon.setImageResource(data.getIconDrawableID());
        viewHolder.title.setText(data.getTitle());
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        LinearLayout parent;
        public MyViewHolder(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
        }
    }
}
