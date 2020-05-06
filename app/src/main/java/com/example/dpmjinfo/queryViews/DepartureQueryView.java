package com.example.dpmjinfo.queryViews;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.spinnerHandling.BusStopSpinnerAdapter;
import com.example.dpmjinfo.DatePickerUniversal;
import com.example.dpmjinfo.Line;
import com.example.dpmjinfo.spinnerHandling.LineSpinnerAdapter;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.TimePickerUniversal;
import com.example.dpmjinfo.queries.DepartureQuery;
import com.example.dpmjinfo.queries.ScheduleQuery;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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

    private final String DATE_FORMAT = "d. MMMM yyyy";

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

        stopsSpinner.setSelection(0, true);
    }

    public void onBusStopsUpdated(int positionToSelect) {
        int position = stopsSpinner.getSelectedItemPosition();

        busStopAdapter.clear();
        busStopAdapter.addAll(query.getBusStops());
        busStopAdapter.notifyDataSetChanged();

        //position juggling - spinner does not trigger onItemSelected when last selected index == new index even
        //if data set was changed
        if(position == positionToSelect){
            if(position == 0) {
                stopsSpinner.setSelection(positionToSelect + 1, false);
            } else {
                stopsSpinner.setSelection(positionToSelect - 1, false);
            }
        }

        stopsSpinner.setSelection(positionToSelect, true);
    }

    public void onLinesUpdated() {
        lineAdapter.clear();
        lineAdapter.addAll(query.getLines());
        lineAdapter.notifyDataSetChanged();

        lineSpinner.setSelection(0, true);
    }
}