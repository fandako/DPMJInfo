package com.example.dpmjinfo.queries;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dpmjinfo.queryModels.ActualDepartureQueryModel;
import com.example.dpmjinfo.queryModels.ScheduleQueryModel;
import com.example.dpmjinfo.recyclerViewHandling.BaseAdapter;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.recyclerViewHandling.BusStopDeparturesAdapter;
import com.example.dpmjinfo.queryModels.ConnectionQueryModel;
import com.example.dpmjinfo.queryModels.DepartureQueryModel;
import com.example.dpmjinfo.queryModels.LineDetailQueryModel;
import com.example.dpmjinfo.helpers.OfflineFilesManager;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.recyclerViewHandling.RecycleViewClickListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * query base class
 */
public class ScheduleQuery {
    private LayoutInflater mInflater;
    //context - activity reference
    protected Context mContext;
    private View mView = null;
    private boolean isPopulated;
    protected ScheduleQueryModel model;
    protected boolean isReady = false;

    public ScheduleQuery(){}

    public ScheduleQuery(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        isPopulated = false;
    }

    public boolean isReady() {
        return isReady;
    }

    public ScheduleQueryModel getModel() {
        return model;
    }

    /**
     * get view for given query
     * used to display query in schedule activity
     * @return view
     */
    protected /*abstract*/ View getQueryView(){return null;}

    /**
     * populates query view with values
     */
    protected /*abstract*/ void populateView(){};

    /**
     * init query model for displaying as favourite
     * for example hide add to favourite button, set date to current date, etc
     */
    public void intiForFavourite() {
        getModel().setShowAddToFavourite(false);
    }

    /**
     * populates query view if it was not already populated
     */
    public void populate() {
        if(!isPopulated()) {
            populateView();
            isPopulated = true;
        }
    }

    /**
     * get initialized query view
     * @return view
     */
    public View getView() {
        if (mView == null) {
            mView = getQueryView();
            hide();
            initView(mView);
        }

        return mView;
    }

    /**
     * hide query view
     */
    public void hide() {
        getView().setVisibility(View.GONE);
    }

    /**
     * show query view
     */
    public void show() {
        getView().setVisibility(View.VISIBLE);
    }

    /**
     * gets query name
     * @return query name
     */
    public /*abstract*/ String getName(){return "";}

    /**
     * initialized view
     * @param v query view
     */
    protected /*abstract */void initView(View v){};

    /**
     * retrieve results of query for its current parameters held in its model
     * @param page
     * @return list of resulting items
     */
    public /*abstract*/ List exec(int page){return null;}

    /**
     * displays query result in new activity
     */
    public /*abstract*/ void execAndDisplayResult(){}

    /**
     * gets query parameters summary to display in favourite queries list
     * @return list of parameters and their corresponding labels
     */
    public List<Pair<String, String>> getSummary() {
        return new ArrayList<>();
    }

    /**
     * gets list of file types which are required for the query to function properly
     * @return list of file types
     */
    public ArrayList<String> getRequiredFileTypes(){
        return new ArrayList<>();
    }

    /**
     * indicates whether or not is query paginable
     * @return true if paginable, false otherwise
     */
    public boolean isPaginable() {
        return false;
    }

    /**
     * indicate whether result items are supposed to be clickable
     * @return
     */
    public boolean hasClickableItems() {
        return false;
    }

    /**
     * indicates whether query view has been already populated
     * @return
     */
    public boolean isPopulated() { return isPopulated; }

    /**
     * indicates whether query should be loaded asynchronously
     * @return
     */
    public boolean isAsync() {
        return false;
    }

    /**
     * indicates whether query requires internet connection for its function
     * @return
     */
    public boolean isInternetDependant() {
        return false;
    }

    /**
     * indicates whether there are items to highlight in result
     * @return
     */
    public boolean hasHighlighted() {
        return false;
    }

    /**
     * gets list of items to be highlighted
     * @return list of items
     */
    public List<?> getHighlighted() {
        return new ArrayList<>();
    }

    /**
     * indicates whether save to favourite button should be displayed
     * @return
     */
    public boolean showAddToFavourite() {
        return getModel().isShowAddToFavourite();
    }

    /**
     * get model to be saved when query saving query as favourite
     * @return model
     */
    public ScheduleQueryModel getModelForFavourite(){
        return null;
    }

    /**
     * get listener that defines actions to perform on result item click
     * @param context context - activity reference
     * @param adapter adapter from recyclerView which contains displayed items
     * @return listener
     */
    public RecycleViewClickListener getOnItemTouchListener(Context context, RecyclerView.Adapter adapter) {
        return new DummyOnItemTouchListener();
    }

    /**
     * dummy on click listener - does not perform anything on click
     */
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

    /**
     * created query of given class from serialized model
     * used to pass queries between different activities
     * @param context context - activity reference
     * @param className class name of given query
     * @param model model of given query as Serializable (for example from intent bundle)
     * @return query object of given class
     */
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
            case "FavouriteQuery":
                return new FavouriteQuery(context, (ScheduleQueryModel) model);
            case "RSSQuery":
                return new RSSQuery(context, (ScheduleQueryModel) model);
            default: return null;
        }
    }

    /**
     * calculates date of easter sunday
     * @param year year
     * @return date of easter sunday for given year
     */
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

    /**
     * checks if given date is holiday base on holidays defined in downloaded holiday definition for
     * non moveable holidays and check for easter holidays using computus algorithm
     * @param date date to check
     * @return true if date is holiday, false otherwise
     */
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

    /**
     * get query page size
     * @return page size
     */
    public int getPageSize(){
        return Integer.MAX_VALUE;
    };

    /**
     * retrieves connection code for given date i.e. goes on Mondays, goes on workdays, goes on holidays
     * @param date date
     * @return array of connection codes
     */
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

    /**
     * gets date format used by backend fot date representation
     * @return date format string
     */
    public static String getDateFormat() {
        return "yyyy-MM-dd";
    }

    /**
     * gets time format used by backend for time representation
     * @return time format string
     */
    public static String getTimeFormat() {
        return "HH:mm";
    }

    /**
     * get adapter to be used with recyclerView to display query result
     * @return adapter
     */
    public BaseAdapter getAdapter(){
        if(hasClickableItems()) {
            return new BusStopDeparturesAdapter(new ArrayList<>(), R.layout.busstop_departure_list_item_caret);
        } else {
            return new BusStopDeparturesAdapter(new ArrayList<>(), R.layout.busstop_departure_list_item);
        }
    }

    /**
     * gets class of result objects
     * @return class of result objects
     */
    public Class getObjectClass(){
        return BusStopDeparture.class;
    }
}
