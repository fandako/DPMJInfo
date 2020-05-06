package com.example.dpmjinfo.queries;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;

import com.example.dpmjinfo.queryModels.ActualDepartureQueryModel;
import com.example.dpmjinfo.queryModels.ScheduleQueryModel;
import com.example.dpmjinfo.queryViews.ActualDeparturesView;
import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.helpers.ElpDepartureHelper;
import com.example.dpmjinfo.helpers.EsriBusStopLoader;
import com.example.dpmjinfo.helpers.EsriBusStopsDoneLoadingListener;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.activities.DeparturesActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * query object for querying actual departures
 */
public class ActualDepartureQuery extends ScheduleQuery implements EsriBusStopsDoneLoadingListener {
    private SortedSet<BusStop> busStops;
    private ActualDeparturesView view = null;
    //private ActualDepartureQueryModel model;

    public ActualDepartureQuery(Context context) {
        super(context);
        busStops = new TreeSet<BusStop>(new Comparator<BusStop>() {
            @Override
            public int compare(BusStop o1, BusStop o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        model = new ActualDepartureQueryModel();
    }

    public ActualDepartureQueryModel getModel() {
        return (ActualDepartureQueryModel) model;
    }

    public ActualDepartureQuery(Context context, ActualDepartureQueryModel model) {
        super(context);
        busStops = new TreeSet<BusStop>(new Comparator<BusStop>() {
            @Override
            public int compare(BusStop o1, BusStop o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        this.model = model;
    }

    @Override
    public ScheduleQueryModel getModelForFavourite() {
        return getModel();
    }

    @Override
    public List<Pair<String, String>> getSummary() {
        List<Pair<String, String>> summary = new ArrayList<>();

        summary.add(new Pair<>(mContext.getString(R.string.departure_query_stop_label), getModel().getBusStop().getName()));

        return summary;
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
        return mContext.getString(R.string.actual_departures_title);
    }

    @Override
    protected void initView(View v) {

    }

    /**
     * loads bus stops to fill in UI
     */
    private void loadBusStops(){
        EsriBusStopLoader.loadBusStops(this);
    }

    public List<BusStop> getBusStops(){
        return new ArrayList<BusStop>(busStops);
    }

    @Override
    public List<BusStopDeparture> exec(int page) {
        return ElpDepartureHelper.getDepartures(getModel().getBusStop());
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isInternetDependant() {
        return true;
    }

    @Override
    public void execAndDisplayResult() {
        Bundle bundle = new Bundle();
        Intent intent;

        /*intent = new Intent(mContext.getApplicationContext(), BusStopDetailActivity.class);
        bundle.putSerializable("com.android.dpmjinfo.busStop", view.getSelectedBusStop());*/
        intent = new Intent(mContext.getApplicationContext(), DeparturesActivity.class);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    /**
     * notify view that bus stop list changed
     */
    private void notifyBusStopsChanged(){
        view.onBusStopsUpdated();
    }

    @Override
    public void esriBusStopsDoneLoading(List<BusStop> busStops) {
        this.busStops.addAll(busStops);
        notifyBusStopsChanged();

        if(!busStops.isEmpty()){
            getModel().setBusStop(busStops.get(0));
            isReady = true;
        }
    }

    /**
     * called by view when bus stop is selected
     */
    private void onStopSelected() {
        getModel().setBusStop(view.getSelectedBusStop());
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
