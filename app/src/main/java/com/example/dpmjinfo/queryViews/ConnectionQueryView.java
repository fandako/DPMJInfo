package com.example.dpmjinfo.queryViews;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.spinnerHandling.BusStopSpinnerAdapter;
import com.example.dpmjinfo.DatePickerUniversal;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.TimePickerUniversal;
import com.example.dpmjinfo.queries.ConnectionQuery;
import com.example.dpmjinfo.queries.ScheduleQuery;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

public class ConnectionQueryView extends FrameLayout {
    ConnectionQuery query;

    EditText dateField;
    EditText timeField;
    SearchableSpinner startStopsSpinner;
    SearchableSpinner targetStopSpinner;
    BusStopSpinnerAdapter startStopAdapter;
    BusStopSpinnerAdapter targetStopAdapter;

    private final String DATE_FORMAT = "d. MMMM yyyy";

    public ConnectionQueryView(ConnectionQuery q, Context context, AttributeSet attrs) {
        super(context, attrs);
        query = q;
        initView();
    }

    public ConnectionQueryView(ConnectionQuery q, Context context) {
        super(context);
        query = q;
        initView();
    }

    public ConnectionQueryView(ConnectionQuery q, Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        query = q;
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.connection_query, this);

        dateField = findViewById(R.id.date);
        timeField = findViewById(R.id.time);

        dateField.setInputType(InputType.TYPE_NULL);
        timeField.setInputType(InputType.TYPE_NULL);

        DateTime d = DateTime.parse(query.getInitialDate(), DateTimeFormat.forPattern(ScheduleQuery.getDateFormat()));

        dateField.setText(d.toString(DATE_FORMAT));
        timeField.setText(query.getInitialTime());

        DatePickerUniversal datePicker = new DatePickerUniversal(dateField, DATE_FORMAT/*ScheduleQuery.getDateFormat()*/);
        TimePickerUniversal timePicker = new TimePickerUniversal(timeField, ScheduleQuery.getTimeFormat());

        timeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                query.setTime(s.toString());
            }
        });

        dateField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                DateTime d = DateTime.parse(s.toString(), DateTimeFormat.forPattern(DATE_FORMAT));

                query.setDate(d.toString(ScheduleQuery.getDateFormat()));
            }
        });

        startStopsSpinner = findViewById(R.id.startStop);
        targetStopSpinner = findViewById(R.id.targetStop);

        startStopAdapter = new BusStopSpinnerAdapter(getContext(), R.layout.spinner_item, new ArrayList<BusStop>());
        startStopsSpinner.setAdapter(startStopAdapter);

        targetStopAdapter = new BusStopSpinnerAdapter(getContext(), R.layout.spinner_item, new ArrayList<BusStop>());
        targetStopSpinner.setAdapter(targetStopAdapter);

        targetStopSpinner.setOnItemSelectedListener(query.getTargetStopSelectedListener());

        startStopsSpinner.setOnItemSelectedListener(query.getStartStopSelectedListener());
    }

    public void onStartStopsUpdated(){
        startStopAdapter.clear();
        startStopAdapter.addAll(query.getStartStops());
        startStopAdapter.notifyDataSetChanged();

        startStopsSpinner.setSelection(0, true);
    }

    public void onTargetStopsUpdated(){
        targetStopAdapter.clear();
        targetStopAdapter.addAll(query.getTargetStops());
        targetStopAdapter.notifyDataSetChanged();

        targetStopSpinner.setSelection(0, true);
    }

    public void onTargetStopsUpdated(int positionToSelect){
        int position = targetStopSpinner.getSelectedItemPosition();

        targetStopAdapter.clear();
        targetStopAdapter.addAll(query.getTargetStops());
        targetStopAdapter.notifyDataSetChanged();

        //position juggling - spinner does not trigger onItemSelected when last selected index == new index even
        //if data set was changed
        if(position == positionToSelect){
            if(position == 0) {
                targetStopSpinner.setSelection(positionToSelect + 1, false);
            } else {
                targetStopSpinner.setSelection(positionToSelect - 1, false);
            }
        }

        targetStopSpinner.setSelection(positionToSelect, true);
    }
}
