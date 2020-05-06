package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.helpers.ElpDepartureHelper;
import com.example.dpmjinfo.helpers.NetworkHelper;
import com.example.dpmjinfo.recyclerViewHandling.BusStopDeparturesAdapter;
import com.example.dpmjinfo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying detailed info about given bus
 * stop while showing also actual departures from this bus stop
 */
public class BusStopDetailActivity extends AppCompatActivity {
    //recyclerView to hold actual departures + its adapter for BusStopDepartures
    private RecyclerView recyclerView;
    private BusStopDeparturesAdapter busStopDepartureAdapter;

    //
    private RecyclerView.LayoutManager layoutManager;

    //UI elements
    private TextView name;
    private TextView lines;
    private TextView wheelchairAccessible;
    private List departureList = new ArrayList<>();
    private TextView noInternetText;
    private Button refreshButton;

    //object holding info about given bus stop
    BusStop busStop;
    AlertDialog.Builder alertBuilder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_detail);

        getSupportActionBar().setTitle(getString(R.string.bus_stop_detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        busStop = (BusStop) bundle.getSerializable("com.android.dpmjinfo.busStop");

        name = findViewById(R.id.name);
        lines = findViewById(R.id.lines);
        wheelchairAccessible = findViewById(R.id.wheelchairAccessible);
        noInternetText = findViewById(R.id.noInternetText);
        refreshButton = findViewById(R.id.refreshButton);

        name.setText(busStop.getName());
        lines.setText(busStop.getLines());
        wheelchairAccessible.setText(busStop.getWheelchairAccessible());

        recyclerView = findViewById(R.id.recyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        busStopDepartureAdapter = new BusStopDeparturesAdapter(departureList, R.layout.busstop_departure_list_item);
        recyclerView.setAdapter(busStopDepartureAdapter);

        //on click refresh actual departures if network connection is present
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!NetworkHelper.isNetworkAvailable(BusStopDetailActivity.this)){
                    alertBuilder = new AlertDialog.Builder(BusStopDetailActivity.this);
                    alertBuilder
                            .setMessage(getString(R.string.no_internet_connection_alert_message))
                            .setTitle(getString(R.string.no_internet_connection_alert_title))
                            .setPositiveButton(getString(R.string.no_internet_connection_ok_button_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                    return;
                }

                getDeparturesFromWeb(busStop);
            }
        });

        getDeparturesFromWeb(/*message.getElp_id()*/busStop);
    }

    /**
     * Hides progress bar and displays departures
     * @param departures list of actual departures
     */
    void updateDepartures(List<BusStopDeparture> departures){
        findViewById(R.id.elpProgressBar).setVisibility(View.GONE);
        findViewById(R.id.elpProgressBarLabel).setVisibility(View.GONE);

        if(departures.size() == 0){
            findViewById(R.id.noItemsText).setVisibility(View.VISIBLE);
            return;
        }

        //add departures from web to UI data set
        busStopDepartureAdapter.addItems(departures);

        //notify about data set change
        busStopDepartureAdapter.notifyDataSetChanged();
    }

    /**
     * Starts async task for loading actual departures if network connection is present
     * @param busStop bus stop for which to get actual departures
     */
    void getDeparturesFromWeb(/*Short stopID*/BusStop busStop){
        if(NetworkHelper.isNetworkAvailable(this)) {
            noInternetText.setVisibility(View.GONE);
            refreshButton.setVisibility(View.GONE);
            new Task(this, busStop).execute(0);
        } else {
            noInternetText.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * AsyncTask class for loading actual departures
     */
    private static class Task extends AsyncTask<Integer, Integer, List<BusStopDeparture>> {

        //reference to current activity for calling callback which processes obtained departures
        private WeakReference<BusStopDetailActivity> activityReference;
        private BusStop busStop;

        public Task(BusStopDetailActivity context, BusStop b) {
            activityReference = new WeakReference<>(context);
            busStop = b;
        }

        @Override
        protected List<BusStopDeparture> doInBackground(Integer... ints) {

            // get a reference to the activity if it is still there
            BusStopDetailActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return new ArrayList<>();

            return ElpDepartureHelper.getDepartures(busStop);
        }

        @Override
        protected void onPostExecute(List<BusStopDeparture> departures) {
            // get a reference to the activity if it is still there
            BusStopDetailActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.updateDepartures(departures);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
