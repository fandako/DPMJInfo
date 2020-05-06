package com.example.dpmjinfo.spinnerHandling;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dpmjinfo.R;

import java.util.List;

public abstract class CustomArraySpinnerAdapter<T> extends ArrayAdapter<T> {
    private final LayoutInflater mInflater;
    private final Context mContext;
    protected final List<T> items;
    private final int mResource;
    private final int mDropdownResource;

    public CustomArraySpinnerAdapter(@NonNull Context context, @LayoutRes int resource, @LayoutRes int dropdownResource,
                                       @NonNull List objects) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        mDropdownResource = dropdownResource;
        items = objects;
    }

    public CustomArraySpinnerAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, getDefaultSpinnerResource(), 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = getDefaultSpinnerResource();
        mDropdownResource = getDefaultSpinnerDropdownResource();
        items = objects;
    }

    protected static int getDefaultSpinnerResource(){
        return R.layout.spinner_item;
    }

    protected static int getDefaultSpinnerDropdownResource(){
        return R.layout.spinner_dropdown_item;
    }

    public List<T> getItems(){
        return items;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, mDropdownResource);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, mResource);
    }

    protected abstract View createItemView(int position, View convertView, ViewGroup parent, int layout);

    protected LayoutInflater getLayoutInflater(){
        return mInflater;
    }
}
