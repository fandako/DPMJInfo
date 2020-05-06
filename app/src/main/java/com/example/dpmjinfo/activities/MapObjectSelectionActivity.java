package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.MapObjectSelectionBusStopItem;
import com.example.dpmjinfo.MapObjectSelectionItem;
import com.example.dpmjinfo.MapObjectSelectionItemsAdapter;
import com.example.dpmjinfo.MapObjectSelectionVehicleItem;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.recyclerViewHandling.RecycleViewClickListener;
import com.example.dpmjinfo.recyclerViewHandling.RecyclerViewTouchListener;
import com.example.dpmjinfo.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays group of map items which where to close together to recognize single one on user touch, user can select item
 * to display its detail
 */
public class MapObjectSelectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter busStopDepartureAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<MapObjectSelectionItem> objects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_object_selection);

        //change activity title
        getSupportActionBar().setTitle(getString(R.string.map_object_selection_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the required data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //get list of bus stops
        List<BusStop> busStops = (ArrayList<BusStop>) bundle.getSerializable("com.android.dpmjinfo.busStops");
        for (BusStop busStop : busStops) {
            objects.add(new MapObjectSelectionBusStopItem(busStop));
        }

        //get list of vehicles
        ArrayList<Vehicle> vehicles = (ArrayList<Vehicle>) bundle.getSerializable("com.android.dpmjinfo.vehicles");
        for (Vehicle vehicle : vehicles) {
            objects.add(new MapObjectSelectionVehicleItem(vehicle));
        }

        recyclerView = findViewById(R.id.mapObjectList);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter with data to display
        busStopDepartureAdapter = new MapObjectSelectionItemsAdapter(objects);
        recyclerView.setAdapter(busStopDepartureAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this,
                recyclerView, new RecycleViewClickListener() {
            private void openDetail(int position){
                //get object from position
                MapObjectSelectionItem item = ((MapObjectSelectionItemsAdapter) recyclerView.getAdapter()).getObjectList().get(position);

                //create intent with corresponding activity class
                Intent intent = new Intent(getApplicationContext(), item.getDetailActivityClass());
                Bundle bundle = new Bundle();

                //bundle data to send to detail activity base on type
                if(item.getObjectClass() == BusStop.class){
                    bundle.putSerializable(item.getBundleID(), (BusStop) item.getObject());
                } else {
                    bundle.putSerializable(item.getBundleID(), (Vehicle) item.getObject());
                }
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onClick(View view, final int position) {
                openDetail(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                openDetail(position);
            }
        }));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
