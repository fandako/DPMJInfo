package com.example.dpmjinfo;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;

public class DepartureQueryView extends FrameLayout {
    DepartureQuery query;

    EditText dateField;
    EditText timeField;
    SearchableSpinner stopsSpinner;
    SearchableSpinner lineSpinner;
    //StringArraySpinnerAdapter stopAdapter;
    LineSpinnerAdapter lineAdapter;
    BusStopSpinnerAdapter busStopAdapter;

    public DepartureQueryView(DepartureQuery q, Context context, AttributeSet attrs) {
        super(context, attrs);
        query = q;
        initView();
    }

    public DepartureQueryView(DepartureQuery q, Context context) {
        super(context);
        query = q;
        initView();
    }

    public DepartureQueryView(DepartureQuery q, Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        query = q;
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.departure_query, this);

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

        stopsSpinner = findViewById(R.id.stop);
        lineSpinner = findViewById(R.id.line);

        busStopAdapter = new BusStopSpinnerAdapter(getContext(), R.layout.spinner_item, new ArrayList<BusStop>());
        //stopAdapter = new StringArraySpinnerAdapter(getContext(), new ArrayList<String>());
        stopsSpinner.setAdapter(busStopAdapter);

        lineAdapter = new LineSpinnerAdapter(getContext(), R.layout.spinner_item, new ArrayList<Line>());
        lineSpinner.setAdapter(lineAdapter);

        lineSpinner.setOnItemSelectedListener(query.getLineSelectedListener());

        stopsSpinner.setOnItemSelectedListener(query.getStopSelectedListener());
    }

    public void onBusStopsUpdated() {
        busStopAdapter.clear();
        busStopAdapter.addAll(query.getBusStops());

        stopsSpinner.setSelection(0);
    }

    public void onLinesUpdated() {
        lineAdapter.clear();
        lineAdapter.addAll(query.getLines());

        lineSpinner.setSelection(0);
    }
}