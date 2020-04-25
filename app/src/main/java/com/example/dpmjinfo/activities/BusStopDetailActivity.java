package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.BusStopDeparturesAdapter;
import com.example.dpmjinfo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BusStopDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter busStopDepartureAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView name;
    private TextView lines;
    private TextView wheelchairAccessible;
    private List departureList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_detail);

        getSupportActionBar().setTitle("Detail zastávky");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        BusStop busStop = (BusStop) bundle.getSerializable("com.android.dpmjinfo.busStop");

        name = findViewById(R.id.name);
        lines = findViewById(R.id.lines);
        wheelchairAccessible = findViewById(R.id.wheelchairAccessible);

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
        busStopDepartureAdapter = new BusStopDeparturesAdapter(departureList);
        recyclerView.setAdapter(busStopDepartureAdapter);

        getDeparturesFromWeb(/*message.getElp_id()*/busStop);
    }

    void updateDepartures(List<BusStopDeparture> departures){
        //add departures from web to UI dataset
        departureList.addAll(departures);
        //notify about dataset change
        busStopDepartureAdapter.notifyDataSetChanged();
    }

    void getDeparturesFromWeb(/*Short stopID*/BusStop busStop){
        new Thread(new Runnable() {
            private List<BusStopDeparture> departures = new ArrayList<>();

            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                String url = /*"http://elp.dpmj.cz/ElpDepartures/Home/Departures?stop=" + stopID*/busStop.getHref();

                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements divs = doc.select("div.stationNameBox");

                    for (Element div : divs) {
                        builder.append("\n").append(div.previousElementSibling().text()).append(" ").append(div.text()).append(" ").append(div.nextElementSibling().nextElementSibling().text());
                        BusStopDeparture departure = new BusStopDeparture(div.previousElementSibling().text(), div.text(), div.nextElementSibling().nextElementSibling().text());
                        departures.add(departure);
                    }
                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDepartures(departures);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
