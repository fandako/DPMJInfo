package com.example.dpmjinfo;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScheduleQuerySpinnerAdapter extends CustomArraySpinnerAdapter<ScheduleQuery> {
    public ScheduleQuerySpinnerAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, objects);
    }

    protected View createItemView(int position, View convertView, ViewGroup parent, int layout){
        final View view = getLayoutInflater().inflate(layout, parent, false);

        TextView spinnerItem = (TextView) view.findViewById(R.id.text);

        ScheduleQuery scheduleQuery = items.get(position);

        spinnerItem.setText(scheduleQuery.getName());

        return view;
    }
}
