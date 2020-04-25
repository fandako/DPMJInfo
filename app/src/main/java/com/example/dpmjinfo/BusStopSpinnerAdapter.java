package com.example.dpmjinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BusStopSpinnerAdapter extends ArrayAdapter<BusStop> {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<BusStop> items;
    private final int mResource;

    public BusStopSpinnerAdapter(@NonNull Context context, @LayoutRes int resource,
                                 @NonNull List objects) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView spinnerItem = (TextView) view.findViewById(R.id.text);

        BusStop busStop = items.get(position);

        spinnerItem.setText(busStop.getName());

        return view;
    }
}
