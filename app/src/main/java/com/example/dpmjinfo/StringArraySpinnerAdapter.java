package com.example.dpmjinfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class StringArraySpinnerAdapter extends CustomArraySpinnerAdapter<String> {
    public StringArraySpinnerAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, objects);
    }

    @Override
    protected View createItemView(int position, View convertView, ViewGroup parent, int layout) {
        final View view = getLayoutInflater().inflate(layout, parent, false);

        TextView spinnerItem = (TextView) view.findViewById(R.id.text);

        String item = items.get(position);

        spinnerItem.setText(item);

        return view;
    }
}
