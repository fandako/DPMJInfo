package com.example.dpmjinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class ActualDeparturesView extends FrameLayout {
    ActualDepartureQuery query;

    Spinner stopsSpinner;
    LinearLayout fields;
    ProgressBar progressBar;
    TextView progressBarLabel;

    public ActualDeparturesView(ActualDepartureQuery q, Context context, AttributeSet attrs) {
        super(context, attrs);
        query = q;
        initView();
    }

    public ActualDeparturesView(ActualDepartureQuery q, Context context) {
        super(context);
        query = q;
        initView();
    }

    public ActualDeparturesView(ActualDepartureQuery q, Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        query = q;
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.actual_departures_query, this);

        stopsSpinner = findViewById(R.id.elpStop);
        fields = findViewById(R.id.elpFields);
        progressBar = findViewById(R.id.elpProgressBar);
        progressBarLabel = findViewById(R.id.elpProgressBarLabel);

        BusStopSpinnerAdapter stopAdapter = new BusStopSpinnerAdapter(getContext(), R.layout.spinner_item, new ArrayList<BusStop>());

        // Drop down layout style - list view with radio button
        stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        stopsSpinner.setAdapter(stopAdapter);

        stopsSpinner.setOnItemSelectedListener(query.getStopSelectedListener());
    }

    public void onBusStopsUpdated() {
        BusStopSpinnerAdapter adapter = ((BusStopSpinnerAdapter) stopsSpinner.getAdapter());

        adapter.clear();
        adapter.addAll(query.getBusStops());

        onLoadingDone();
    }

    private void onLoadingDone() {
        progressBarLabel.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        fields.setVisibility(View.VISIBLE);
    }

    public BusStop getSelectedBusStop() {
        return (BusStop) stopsSpinner.getSelectedItem();
    }
}
