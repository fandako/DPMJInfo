package com.example.dpmjinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.example.dpmjinfo.activities.BusStopDetailActivity;
import com.example.dpmjinfo.activities.Departures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;

public class ActualDepartureQuery extends ScheduleQuery implements EsriBusStopsDoneLoadingListener{
    Set<BusStop> busStops;
    ActualDeparturesView view = null;
    ActualDepartureQueryModel model;


    public ActualDepartureQuery(Context context) {
        super(context);
        busStops = new HashSet<BusStop>();
        model = new ActualDepartureQueryModel();
    }

    public ActualDepartureQuery(Context context, ActualDepartureQueryModel model) {
        super(context);
        busStops = new HashSet<BusStop>();
        this.model = model;
    }

    protected View getQueryView(){
        //return mInflater.inflate(R.layout.actual_departures_query, null, false);
        if(view == null){
            view = new ActualDeparturesView(this, mContext);
        }

        return view;
    }

    @Override
    protected void populateView() {
        loadBusStops();
    }

    public String getName(){
        return "Aktuální odjezdy";
    }

    @Override
    protected void initView(View v) {

    }

    private void loadBusStops(){
        EsriBusStopLoader.loadBusStops(this);
    }

    public List<BusStop> getBusStops(){
        return new ArrayList<BusStop>(busStops);
    }

    @Override
    public List<BusStopDeparture> exec(int page) {
        return ElpDepartureHelper.getDepartures(model.getBusStop());
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void execAndDisplayResult() {
        Bundle bundle = new Bundle();
        Intent intent;

        /*intent = new Intent(mContext.getApplicationContext(), BusStopDetailActivity.class);
        bundle.putSerializable("com.android.dpmjinfo.busStop", view.getSelectedBusStop());*/
        intent = new Intent(mContext.getApplicationContext(), Departures.class);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    private void notifyBusStopsChanged(){
        view.onBusStopsUpdated();
    }

    @Override
    public void esriBusStopsDoneLoading(List<BusStop> busStops) {
        this.busStops.addAll(busStops);
        notifyBusStopsChanged();
    }

    private void onStopSelected() {
        model.setBusStop(view.getSelectedBusStop());
    }

    public StopSelectedListener getStopSelectedListener() {
        return new StopSelectedListener(this);
    }

    private class StopSelectedListener implements AdapterView.OnItemSelectedListener {
        ActualDepartureQuery query;

        public StopSelectedListener(ActualDepartureQuery q) {
            query = q;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            query.onStopSelected();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
