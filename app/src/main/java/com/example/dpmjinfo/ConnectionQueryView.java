package com.example.dpmjinfo;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;

public class ConnectionQueryView extends FrameLayout {
    ConnectionQuery query;

    EditText dateField;
    EditText timeField;
    SearchableSpinner startStopsSpinner;
    SearchableSpinner targetStopSpinner;
    BusStopSpinnerAdapter startStopAdapter;
    BusStopSpinnerAdapter targetStopAdapter;

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

        dateField.setText(query.getInitialDate());
        timeField.setText(query.getInitialTime());

        DatePickerUniversal datePicker = new DatePickerUniversal(dateField, ScheduleQuery.getDateFormat());
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
                query.setDate(s.toString());
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

        startStopsSpinner.setSelection(0);
    }

    public void onTargetStopsUpdated(){
        targetStopAdapter.clear();
        targetStopAdapter.addAll(query.getTargetStops());

        targetStopSpinner.setSelection(0);
    }
}
