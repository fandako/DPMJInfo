package com.example.dpmjinfo.spinnerHandling;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dpmjinfo.R;
import com.example.dpmjinfo.queries.ScheduleQuery;
import com.example.dpmjinfo.spinnerHandling.CustomArraySpinnerAdapter;

import java.util.List;

public class ScheduleQuerySpinnerAdapter extends CustomArraySpinnerAdapter<ScheduleQuery> {
    public ScheduleQuerySpinnerAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, objects);
    }

    public ScheduleQuerySpinnerAdapter(@NonNull Context context, @NonNull List objects, int spinnerLayout) {
        super(context, spinnerLayout, R.layout.spinner_dropdown_item, objects);
    }

    protected View createItemView(int position, View convertView, ViewGroup parent, int layout){
        final View view = getLayoutInflater().inflate(layout, parent, false);

        TextView spinnerItem = (TextView) view.findViewById(R.id.text);

        ScheduleQuery scheduleQuery = items.get(position);

        spinnerItem.setText(scheduleQuery.getName());

        return view;
    }
}
