package com.example.dpmjinfo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dpmjinfo.activities.Departures;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DepartureQuery extends ScheduleQuery implements Serializable {
    private CISSqliteHelper mDb = null;
    //private List<Integer> stopIDs;
    //private List<String> stopNames;
    //private List<Integer> lineIDs;
    //private List<String> lineNames;
    private DepartureQueryModel model;
    private List<BusStop> busStops;
    private List<Line> lines;


    static final int DEFAULT_PAGE_SIZE = 10;
    static final int ALL_LINES = -1;

    DepartureQueryView view;

    public DepartureQuery(Context context/*, SQLiteDatabase db*/) {
        super(context);
        //mDb = db;
        model = new DepartureQueryModel();
        //mContext = context;

        initLocalVars();
        initModelValues();
    }

    public DepartureQuery(Context context, DepartureQueryModel model) {
        super(context);
        //mContext = context;
        this.model = model;

        initLocalVars();
    }

    private void initLocalVars() {
        //stopNames = new ArrayList<>();
        //stopIDs = new ArrayList<>();
        //lineNames = new ArrayList<>();
        //lineIDs = new ArrayList<>();
        busStops = new ArrayList<>();
        lines = new ArrayList<>();
    }

    private void initModelValues() {
        setDate(getInitialDate());
        setTime(getInitialTime());
        setPageSize(DEFAULT_PAGE_SIZE);
        setLineId(ALL_LINES);
    }

    //public List<Integer> getStopIDs() {return stopIDs;}

    public int getStopId() {
        return model.getStopId();
    }

    public void setStopId(int stopId) {
        model.setStopId(stopId);
    }

    public String getDate() {
        return model.getDate();
    }

    public void setDate(String date) {
        model.setDate(date);
    }

    public String getTime() {
        return model.getTime();
    }

    public void setTime(String time) {
        model.setTime(time);
    }

    public int getLineId() {
        return model.getLineId();
    }

    public void setLineId(int lineId) {
        model.setLineId(lineId);
    }

    public int getPageSize() {
        return model.getPageSize();
    }

    public void setPageSize(int pageSize) {
        model.setPageSize(pageSize);
    }

    public static int getAllLines() {
        return ALL_LINES;
    }

    @Override
    public boolean isPaginable() {
        return true;
    }

    @Override
    public boolean hasClickableItems() {
        return true;
    }

    private CISSqliteHelper getDb() {
        if (mDb == null) {
            OfflineFilesManager ofm = new OfflineFilesManager(mContext);
            mDb = new CISSqliteHelper(ofm.getFilePath(OfflineFilesManager.SCHEDULE));
        }

        return mDb;
    }

    protected View getQueryView() {
        //return mInflater.inflate(R.layout.departure_query, null, false);
        if (view == null) {
            view = new DepartureQueryView(this, mContext);
        }

        return view;
    }

    @Override
    protected void populateView() {
        CISSqliteHelper helper = getDb();

        busStops = helper.getBusStops();

        //add option to display all lines
        lines.add(new Line(ALL_LINES, "Všechny linky"));

        lines.addAll(helper.getLines());
        notifyLinesChanged();
        //cursor.close();
    }

    public String getName() {
        return "Odjezdy";
    }

    protected void initView(View v) {

    }

    public List<BusStopDeparture> exec(int page) {
        CISSqliteHelper helper = getDb();

        //get corresponding codes for given date
        String[] codes = getCodesForDate(getDate());

        ArrayList<BusStopDeparture> departures = new ArrayList<>(helper.getDepartures(getStopId(), codes, getDate(), getTime(), getLineId(), page, getPageSize()));

        return departures;
    }

    public void execAndDisplayResult() {

        Bundle bundle = new Bundle();
        Intent intent;

        //ArrayList<BusStopDeparture> departures = new ArrayList<>(exec(0));

        intent = new Intent(mContext.getApplicationContext(), Departures.class);
        //bundle.putSerializable("com.android.dpmjinfo.departures", departures);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    public String getInitialDate() {
        DateFormat format = new SimpleDateFormat(getDateFormat());
        Date date = new Date();
        return format.format(date);
    }

    public String getInitialTime() {
        DateFormat format = new SimpleDateFormat(getTimeFormat());
        Date date = new Date();
        return format.format(date);
    }

    private void notifyBusStopsChanged() {
        view.onBusStopsUpdated();
    }

    private void notifyLinesChanged() {
        view.onLinesUpdated();
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public LineSelectedListener getLineSelectedListener() {
        return new LineSelectedListener(this);
    }

    private void onLineSelected(int position) {
        int lineId = lines.get(position).getLineId();
        setLineId(lineId);

        busStops.clear();

        busStops.addAll(getDb().getBusStopsOfLine(lineId));

        notifyBusStopsChanged();
    }

    public StopSelectedListener getStopSelectedListener() {
        return new StopSelectedListener(this);
    }

    private void onStopSelected(int position) {
        setStopId(busStops.get(position).getCISId());
        Log.d("dbg selection", "" + position + " : " + busStops.get(position).getCISId());
    }

    private class LineSelectedListener implements AdapterView.OnItemSelectedListener {
        DepartureQuery query;

        public LineSelectedListener(DepartureQuery q) {
            query = q;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            query.onLineSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class StopSelectedListener implements AdapterView.OnItemSelectedListener {
        DepartureQuery query;

        public StopSelectedListener(DepartureQuery q) {
            query = q;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            query.onStopSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public ArrayList<String> getRequiredFileTypes() {
        ArrayList<String> required = new ArrayList<>();
        required.add(OfflineFilesManager.SCHEDULE);
        required.add(OfflineFilesManager.CALENDAR);

        return required;
    }

    @Override
    public RecycleViewClickListener getOnItemTouchListener(Context context, RecyclerView.Adapter adapter) {
        return new OnDepartureTouchListener(context, (BusStopDeparturesAdapter) adapter);
    }

    private class OnDepartureTouchListener implements RecycleViewClickListener {

        Context context;
        BusStopDeparturesAdapter adapter;

        OnDepartureTouchListener(Context context, BusStopDeparturesAdapter adapter) {
            this.context = context;
            this.adapter = adapter;
        }


        private void openDetail(int position) {
            BusStopDeparture departure = adapter.getItem(position);

            LineDetailQuery q = new LineDetailQuery(context);
            q.setConnectionId(departure.getConnectionId());
            q.setLineId(departure.getLineId());
            q.setHighlighted(departure);

            q.execAndDisplayResult();
        }

        @Override
        public void onClick(View view, final int position) {
            openDetail(position);
        }

        @Override
        public void onLongClick(View view, int position) {
            openDetail(position);
        }
    }
}
