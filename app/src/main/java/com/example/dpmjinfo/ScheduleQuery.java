package com.example.dpmjinfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class ScheduleQuery {
    LayoutInflater mInflater;
    protected Context mContext;
    View mView = null;
    protected boolean isPopulated;

    public ScheduleQuery(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        isPopulated = false;
    }

    protected abstract View getQueryView();

    protected abstract void populateView();

    public void populate() {
        if(!isPopulated()) {
            populateView();
            isPopulated = true;
        }
    }

    public View getView() {
        if (mView == null) {
            mView = getQueryView();
            hide();
            initView(mView);
        }

        return mView;
    }

    public void hide() {
        getView().setVisibility(View.GONE);
    }

    public void show() {
        getView().setVisibility(View.VISIBLE);
    }

    public abstract String getName();

    protected abstract void initView(View v);

    public abstract List exec(int page);

    public abstract void execAndDisplayResult();

    public ArrayList<String> getRequiredFileTypes(){
        return new ArrayList<>();
    }

    public boolean isPaginable() {
        return false;
    }

    public boolean hasClickableItems() {
        return false;
    }

    protected boolean isPopulated() { return isPopulated; }

    public boolean isAsync() {
        return false;
    }

    public boolean hasHighlighted() {
        return false;
    }

    public BusStopDeparture getHighlighted() {
        return null;
    }

    public RecycleViewClickListener getOnItemTouchListener(Context context, RecyclerView.Adapter adapter) {
        return new DummyOnItemTouchListener();
    }

    private class DummyOnItemTouchListener implements RecycleViewClickListener {
        @Override
        public void onClick(View view, final int position) {
            //do nothing
        }

        @Override
        public void onLongClick(View view, int position) {
            //do nothing
        }
    }

    public static ScheduleQuery getQueryFromSerializedModel(Context context, String className, Serializable model){
        switch (className){
            case "DepartureQuery":
                return new DepartureQuery(context, (DepartureQueryModel) model);
            case "ActualDepartureQuery":
                return new ActualDepartureQuery(context, (ActualDepartureQueryModel) model);
            case "LineDetailQuery":
                return new LineDetailQuery(context, (LineDetailQueryModel) model);
            case "ConnectionQuery":
                return new ConnectionQuery(context, (ConnectionQueryModel) model);
            default: return null;
        }
    }

    public static String[] getCodesForDate(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getDateFormat());
        Calendar calendar = Calendar.getInstance();

        //exception raised while parsing date -> cant recover without possible looping
        try {
            calendar.setTime(simpleDateFormat.parse(date));
        } catch (Exception e) {
            throw new Error();
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        List<String> codes = new ArrayList<>();

        switch (dayOfWeek) {
            case 2:
                codes.add("X");
                codes.add("1");
                break;
            case 3:
                codes.add("X");
                codes.add("2");
                break;
            case 4:
                codes.add("X");
                codes.add("3");
                break;
            case 5:
                codes.add("X");
                codes.add("4");
                break;
            case 6:
                codes.add("X");
                codes.add("5");
                break;
            case 7:
                codes.add("6");
                break;
            case 1:
                codes.add("7");
                break;
        }

        return codes.toArray(new String[0]);
    }

    public static String getDateFormat() {
        return "yyyy-MM-dd";
    }

    public static String getTimeFormat() {
        return "HH:mm";
    }

    public BaseAdapter getAdapter(){
        return new BusStopDeparturesAdapter(new ArrayList<>(), R.layout.busstop_departure_list_item_caret);
    }

    public Class getObjectClass(){
        return BusStopDeparture.class;
    }
}
