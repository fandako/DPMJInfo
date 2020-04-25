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
    private SQLiteDatabase mDb = null;
    private List<Integer> stopIDs;
    private List<String> stopNames;
    private List<Integer> lineIDs;
    private List<String> lineNames;
    private DepartureQueryModel model;


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

    public DepartureQuery(Context context, DepartureQueryModel model){
        super(context);
        //mContext = context;
        this.model = model;

        initLocalVars();
    }

    private void initLocalVars(){
        stopNames = new ArrayList<String>();
        stopIDs = new ArrayList<Integer>();
        lineNames = new ArrayList<String>();
        lineIDs = new ArrayList<Integer>();
    }

    private void initModelValues(){
        setDate(getInitialDate());
        setTime(getInitialTime());
        setPageSize(DEFAULT_PAGE_SIZE);
        setLineId(ALL_LINES);
    }

    public List<Integer> getStopIDs() {
        return stopIDs;
    }

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

    private SQLiteDatabase getDb() {
        if (mDb == null) {
            OfflineFilesManager ofm = new OfflineFilesManager(mContext);
            mDb = SQLiteDatabase.openDatabase(ofm.getFilePath(OfflineFilesManager.SCHEDULE), null, SQLiteDatabase.OPEN_READONLY);
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
    public void populateView() {
        Cursor cursor = getDb().query("STOPS", new String[]{"stopID", "stopName"}, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            stopNames.add(cursor.getString(cursor.getColumnIndex("stopName")));
            stopIDs.add(cursor.getInt(cursor.getColumnIndex("stopID")));
        }
        notifyBusStopsChanged();
        cursor.close();

        //add option to display all lines
        lineIDs.add(ALL_LINES);
        lineNames.add("Všechny linky");

        cursor = getDb().query("LINES", new String[]{"lineID", "lineName"}, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            lineNames.add(cursor.getString(cursor.getColumnIndex("lineName")));
            lineIDs.add(cursor.getInt(cursor.getColumnIndex("lineID")));
        }
        notifyLinesChanged();
        cursor.close();
    }

    public String getName() {
        return "Odjezdy";
    }

    protected void initView(View v) {

    }

    public List<BusStopDeparture> exec(int page) {
        OfflineFilesManager ofm = new OfflineFilesManager(mContext);
        CISSqliteHelper helper = new CISSqliteHelper(ofm.getFilePath(OfflineFilesManager.SCHEDULE));

        //ArrayList<BusStopDeparture> departures = new ArrayList<>(helper.getDepartures(50428, new String[]{"00002", "00009"}, "2020-03-23", "00:00", -1, 0, 10));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getDateFormat());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(getDate()));
        } catch (Exception e){

        }

        /*for(int i = 20; i <= 26; i++) {
            //calendar.set(2020, 4, i);
            calendar.set(2020, 3, i);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            Log.d("dbg calendar", "day: " + dayOfWeek);
        }*/

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] codes = new String[2];

        switch (dayOfWeek) {
            case 2:
                codes[0] = "00002";
                codes[1] = "00005";
                break;
            case 3:
                codes[0] = "00002";
                codes[1] = "00006";
                break;
            case 4:
                codes[0] = "00002";
                codes[1] = "00007";
                break;
            case 5:
                codes[0] = "00002";
                codes[1] = "00008";
                break;
            case 6:
                codes[0] = "00002";
                codes[1] = "00009";
                break;
            case 7:
                codes[1] = "00003";
                break;
            case 1:
                codes[1] = "00004";
                break;
        }

        //Log.d("dbg", getStopId() + ", " + codes[1] + ", " + getDate() + ", " + getTime() + ", " + getLineId() + ", " + getPageSize());
        ArrayList<BusStopDeparture> departures = new ArrayList<>(helper.getDepartures(getStopId(), codes, getDate(), getTime(), getLineId(), page, getPageSize()));

        //helper.close();

        return departures;
    }

    public void execAndDisplayResult() {

        Bundle bundle = new Bundle();
        Intent intent;

        ArrayList<BusStopDeparture> departures = new ArrayList<>(exec(0));

        intent = new Intent(mContext.getApplicationContext(), Departures.class);
        bundle.putSerializable("com.android.dpmjinfo.departures", departures);
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

    public String getDateFormat() {
        return "yyyy-MM-dd";
    }

    public String getTimeFormat() {
        return "HH:mm";
    }

    private void notifyBusStopsChanged() {
        view.onBusStopsUpdated();
    }

    private void notifyLinesChanged() {
        view.onLinesUpdated();
    }

    public List<String> getBusStops() {
        return stopNames;
    }

    public List<String> getLines() {
        return lineNames;
    }

    public LineSelectedListener getLineSelectedListener() {
        return new LineSelectedListener(this);
    }

    private void onLineSelected(int position) {
        setLineId(lineIDs.get(position));

        final String sql = "SELECT S.stopID, S.stopName FROM LINESTOPS L INNER JOIN STOPS S ON L.stopID=S.stopID WHERE L.lineID=?";

        Cursor cursor;
        if (lineIDs.get(position) != ALL_LINES) {
            cursor = getDb().rawQuery(sql, new String[]{lineIDs.get(position).toString()});
        } else {
            cursor = getDb().query("STOPS", new String[]{"stopID", "stopName"}, null, null, null, null, null, null);
        }

        if (!cursor.moveToNext()) {
            Log.d("dbg", "No result");
            return;
        }

        stopIDs.clear();
        stopNames.clear();

        do {
            stopIDs.add(cursor.getInt(cursor.getColumnIndex("stopID")));
            stopNames.add(cursor.getString(cursor.getColumnIndex("stopName")));
        }
        while (cursor.moveToNext());

        notifyBusStopsChanged();
    }

    public StopSelectedListener getStopSelectedListener() {
        return new StopSelectedListener(this);
    }

    private void onStopSelected(int position) {
        setStopId(stopIDs.get(position));
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
