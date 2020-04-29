package com.example.dpmjinfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class ScheduleQuery {
    private LayoutInflater mInflater;
    protected Context mContext;
    private View mView = null;
    private boolean isPopulated;

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

    private boolean isPopulated() { return isPopulated; }

    public boolean isAsync() {
        return false;
    }

    public boolean hasHighlighted() {
        return false;
    }

    public List<?> getHighlighted() {
        return new ArrayList<>();
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

    //calculates date of easter sunday
    public static DateTime getEasterDate(int year){
        int a = year% 19;
        int b = (int) Math.floor(year/100);
        int c = year % 100;
        int d = (int) Math.floor(b/4);
        int e = b % 4;
        int f = (int) Math.floor((b + 8) / 25);
        int g = (int) Math.floor((b - f + 1) / 3);
        int h = (19*a + b - d - g + 15) % 30;
        int i = (int) Math.floor(c/4);
        int k = c%4;
        int L = (32 + 2*e + 2*i - h - k) % 7;
        int m = (int) Math.floor((a + 11*h + 22*L) / 451);
        int month = (int) Math.floor((h + L - 7*m + 114) / 31);
        int day = (((h + L - (7*m) + 114) % 31) + 1);

        //Log.d("dbg", day + "." + month + "." + year);
        return new DateTime(year, month, day, 0, 0, 0, 0);

        /*int a = year % 19;
        int b = year >> 2;
        int c = (b / 25) + 1;
        int d = (c * 3) >> 2;
        int e = ((a * 19) - ((c * 8 + 5) / 25) + d + 15) % 30;
        e += (29578 - a - e * 32) >> 10;
        e -= ((year % 7) + b - d + e + 2) % 7;
        d = e >> 5;
        int day = e - d * 31;
        int month = d + 3;*/
    }

    public boolean checkIfDateIsHoliday(DateTime date){

        int year = date.getYear();
        DateTime easterSunday = getEasterDate(year);


        //easter Monday
        DateTime easterMonday = easterSunday.plusDays(1);
        if(date.compareTo(easterMonday) == 0){
            return true;
        }

        //easter Friday
        DateTime easterFriday = easterSunday.plusDays(-2);
        if(date.compareTo(easterFriday) == 0){
            return true;
        }

        //check for holidays which are not moveable (defined in csv file)
        OfflineFilesManager ofm = new OfflineFilesManager(mContext);

        String csvFile = ofm.getFilePath("calendar");
        String line = "";
        String cvsSplitBy = ";";
        boolean firstLine = true;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                //skip csv header with column names
                if(firstLine){
                    firstLine = false;
                    continue;
                }

                // use comma as separator
                String[] values = line.split(cvsSplitBy);

                if(date.getDayOfMonth() == Integer.parseInt(values[0]) && date.getMonthOfYear() == Integer.parseInt(values[1])){
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getPageSize(){
        return Integer.MAX_VALUE;
    };

    public String[] getCodesForDate(String date) {
        List<String> codes = new ArrayList<>();

        DateTime dateTime = DateTime.parse(date, DateTimeFormat.forPattern(getDateFormat()));

        int dayOfWeek = dateTime.getDayOfWeek(); //calendar.get(Calendar.DAY_OF_WEEK);

        //check if date is holiday and return corresponding code
        if(checkIfDateIsHoliday(dateTime)){
            codes.add("+");
            return codes.toArray(new String[0]);
        }

        //check what day of week the date is (codes 1..7 = Monday...Sunday, X = workdays)
        switch (dayOfWeek) {
            case 1:
                codes.add("X");
                codes.add("1");
                break;
            case 2:
                codes.add("X");
                codes.add("2");
                break;
            case 3:
                codes.add("X");
                codes.add("3");
                break;
            case 4:
                codes.add("X");
                codes.add("4");
                break;
            case 5:
                codes.add("X");
                codes.add("5");
                break;
            case 6:
                codes.add("6");
                break;
            case 7:
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
        if(hasClickableItems()) {
            return new BusStopDeparturesAdapter(new ArrayList<>(), R.layout.busstop_departure_list_item_caret);
        } else {
            return new BusStopDeparturesAdapter(new ArrayList<>(), R.layout.busstop_departure_list_item);
        }
    }

    public Class getObjectClass(){
        return BusStopDeparture.class;
    }
}
