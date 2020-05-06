package com.example.dpmjinfo.spinnerHandling;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dpmjinfo.Line;
import com.example.dpmjinfo.R;

import java.util.List;

public class LineSpinnerAdapter extends BaseSpinnerAdapter<Line> {


    public LineSpinnerAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
    }

    protected View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView spinnerItem = (TextView) view.findViewById(R.id.text);

        Line line = items.get(position);

        spinnerItem.setText(line.getLineName());

        return view;
    }
}

