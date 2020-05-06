package com.example.dpmjinfo.queries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.recyclerViewHandling.BusStopDeparturesAdapter;
import com.example.dpmjinfo.helpers.CISSqliteHelper;
import com.example.dpmjinfo.queryModels.DepartureQueryModel;
import com.example.dpmjinfo.queryViews.DepartureQueryView;
import com.example.dpmjinfo.Line;
import com.example.dpmjinfo.helpers.OfflineFilesManager;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.recyclerViewHandling.RecycleViewClickListener;
import com.example.dpmjinfo.activities.DeparturesActivity;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * query object for querying departures from given stop
 */
public class DepartureQuery extends ScheduleQuery implements Serializable {
    private CISSqliteHelper mDb = null;
    //private DepartureQueryModel model;
    private List<BusStop> busStops;
    private List<Line> lines;


    private static final int DEFAULT_PAGE_SIZE = 10;
    public static final int ALL_LINES = -1;

    private DepartureQueryView view;

    public DepartureQuery(){}

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

    public DepartureQueryModel getModel() {
        return (DepartureQueryModel) model;
    }

    @Override
    public void intiForFavourite() {
        setDate(DateTime.now().toString(getDateFormat()));
        getModel().setShowAddToFavourite(false);
    }

    @Override
    public List<Pair<String, String>> getSummary() {
        List<Pair<String, String>> summary = new ArrayList<>();

        summary.add(new Pair<>(mContext.getString(R.string.departure_query_time_label), getTime()));
        summary.add(new Pair<>(mContext.getString(R.string.departure_query_stop_label), getModel().getStop().getName()));

        return summary;
    }

    private void initLocalVars() {
        busStops = new ArrayList<>();
        lines = new ArrayList<>();
    }

    private void initModelValues() {
        setDate(getInitialDate());
        setTime(getInitialTime());
        setPageSize(DEFAULT_PAGE_SIZE);
        setLine(new Line(ALL_LINES, mContext.getString(R.string.departure_query_all_lines)));
    }

    //public List<Integer> getStopIDs() {return stopIDs;}

    public int getStopId() {
        return getModel().getStopId();
    }

    public void setStop(BusStop stop) {
        getModel().setStop(stop);
    }

    public String getDate() {
        return getModel().getDate();
    }

    public void setDate(String date) {
        getModel().setDate(date);
    }

    public String getTime() {
        return getModel().getTime();
    }

    public void setTime(String time) {
        getModel().setTime(time);
    }

    public int getLineId() {
        return getModel().getLineId();
    }

    public void setLine(Line line) {
        getModel().setLine(line);
    }

    public int getPageSize() {
        return getModel().getPageSize();
    }

    public void setPageSize(int pageSize) {
        getModel().setPageSize(pageSize);
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
        notifyBusStopsChanged();

        //add option to display all lines
        lines.add(new Line(ALL_LINES, mContext.getString(R.string.departure_query_all_lines)));

        lines.addAll(helper.getLines());
        notifyLinesChanged();
        //cursor.close();
    }

    public String getName() {
        return mContext.getString(R.string.departure_query_title);
    }

    protected void initView(View v) {

    }

    public List<BusStopDeparture> exec(int page) {
        CISSqliteHelper helper = getDb();

        //get corresponding codes for given date
        String[] codes = getCodesForDate(getDate());

        return new ArrayList<>(helper.getDepartures(getStopId(), codes, getDate(), getTime(), getLineId(), page, getPageSize()));
    }

    public void execAndDisplayResult() {

        Bundle bundle = new Bundle();
        Intent intent;

        //ArrayList<BusStopDeparture> departures = new ArrayList<>(exec(0));

        intent = new Intent(mContext.getApplicationContext(), DeparturesActivity.class);
        //bundle.putSerializable("com.android.dpmjinfo.departures", departures);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    public String getInitialDate() {
        /*DateFormat format = new SimpleDateFormat(getDateFormat());
        Date date = new Date();
        return format.format(date);*/
        DateTime d = DateTime.now();

        return d.toString(getDateFormat());
    }

    public String getInitialTime() {
        /*DateFormat format = new SimpleDateFormat(getTimeFormat());
        Date date = new Date();
        return format.format(date);*/
        DateTime d = DateTime.now();

        return d.toString(getTimeFormat());
    }

    private void notifyBusStopsChanged() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.onBusStopsUpdated();
            }
        });
    }

    private void notifyBusStopsChanged(int positionToSelect) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.onBusStopsUpdated(positionToSelect);
            }
        });
    }

    private void notifyLinesChanged() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.onLinesUpdated();
            }
        });
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
        setLine(lines.get(position));

        int selectedBustop = getStopId();

        busStops.clear();

        busStops.addAll(getDb().getBusStopsOfLine(lineId));

        int positionToSelect = 0;

        for(int i = 0; i < busStops.size(); i++) {
            if(busStops.get(i).getCISId() == selectedBustop) {
                positionToSelect = i;
            }
        }

        notifyBusStopsChanged(positionToSelect);
    }

    public StopSelectedListener getStopSelectedListener() {
        return new StopSelectedListener(this);
    }

    private void onStopSelected(int position) {
        setStop(busStops.get(position));
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
            q.addHighlighted(departure);

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
