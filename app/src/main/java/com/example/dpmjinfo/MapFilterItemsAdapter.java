package com.example.dpmjinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MapFilterItemsAdapter extends RecyclerView.Adapter<MapFilterItemsAdapter.MyViewHolder> {
    private List<String> lines;
    private List<String> checkedLines;

    public MapFilterItemsAdapter(List<String> lines, List<String> checkedLines) {
        this.lines = lines;
        this.checkedLines = checkedLines;
    }

    public List<String> getLinesList(){
        return lines;
    }

    public List<String> getCheckedLines() { return checkedLines; }

    public void removeCheckedLine(String line) {
        checkedLines.remove(line);
    }

    public void addCheckedLine(String line) {
        checkedLines.add(line);
    }

    public String getItem(int position) {
        return getLinesList().get(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.map_filter_list_item, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        String data = lines.get(i);
        viewHolder.checkedTextView.setText(data);

        if(checkedLines.contains(data)){
            viewHolder.checkedTextView.setChecked(true);
        } else {
            viewHolder.checkedTextView.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckedTextView checkedTextView;
        LinearLayout parent;
        public MyViewHolder(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            checkedTextView = itemView.findViewById(R.id.checkedTextView);
        }
    }
}
