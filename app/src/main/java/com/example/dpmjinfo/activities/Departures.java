package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dpmjinfo.BaseAdapter;
import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.BusStopDeparturesAdapter;
import com.example.dpmjinfo.CISSqliteHelper;
import com.example.dpmjinfo.DepartureQuery;
import com.example.dpmjinfo.DepartureQueryModel;
import com.example.dpmjinfo.OfflineFilesManager;
import com.example.dpmjinfo.PaginationListener;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.RecyclerViewTouchListener;
import com.example.dpmjinfo.ScheduleQuery;
import com.example.dpmjinfo.ScheduleQueryModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.dpmjinfo.PaginationListener.PAGE_START;

public class Departures extends AppCompatActivity {
    private RecyclerView recyclerView;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean iSFirstLoad = true;
    private int page_size = 10;
    private int currentPage = PAGE_START;
    private BusStopDeparturesAdapter adapter2;
    private BaseAdapter adapter;
    private LinearLayoutManager layoutManager;
    //DepartureQuery query;
    private ScheduleQuery query;
    private TextView noItemsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the required data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //get list of bus departures
        //ArrayList<BusStopDeparture> departures = (ArrayList<BusStopDeparture>) bundle.getSerializable("com.android.dpmjinfo.departures");
        //DepartureQueryModel queryModel = (DepartureQueryModel) bundle.getSerializable("com.android.dpmjinfo.queryModel");
        Serializable queryModel = bundle.getSerializable("com.android.dpmjinfo.queryModel");
        String queryClass = (String) bundle.getSerializable("com.android.dpmjinfo.queryClass");
        //query = new DepartureQuery(this, queryModel);
        query = ScheduleQuery.getQueryFromSerializedModel(this, queryClass, queryModel);

        getSupportActionBar().setTitle(query.getName());

        //get list of bus departures
        //ArrayList<BusStopDeparture> departures = new ArrayList<>(query.exec(currentPage));
        //page_size = departures.size();

        //page_size = departures.size();

        noItemsText = findViewById(R.id.noItemsText);

        recyclerView = findViewById(R.id.departures);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter with data to display
        if (query.hasClickableItems()) {
            adapter = query.getAdapter(); //new BusStopDeparturesAdapter(new ArrayList<>(), R.layout.busstop_departure_list_item_caret);
            recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerView, query.getOnItemTouchListener(this, adapter)));
        } else {
            adapter = query.getAdapter(); //new BusStopDeparturesAdapter(new ArrayList<>());
        }

        if(query.hasHighlighted()){
            Log.d("dbg", "has highlighted");
            adapter.highlightItem(query.getHighlighted());
        }

        adapter.addLoading(query.getObjectClass());

        recyclerView.setAdapter(adapter);

        loadNextPage();

        if (query.isPaginable()) {
            recyclerView.addOnScrollListener(new PaginationListener(layoutManager, page_size) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    currentPage++;
                    loadNextPage();
                    //new Task().execute(0);
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });
        } else {
            isLastPage = true;
        }
    }

    private List<?> loadNextPageItems() {
        OfflineFilesManager ofm = new OfflineFilesManager(this);
        CISSqliteHelper helper = new CISSqliteHelper(ofm.getFilePath(OfflineFilesManager.SCHEDULE));

        return new ArrayList<>(query.exec(currentPage));
    }

    private void processNextPageItems(List<?> departures) {
        page_size = departures.size();

        if (currentPage != PAGE_START || departures.isEmpty() || iSFirstLoad) adapter.removeLoading();
        adapter.addItems(departures);

        if(iSFirstLoad && departures.isEmpty()){
            noItemsText.setVisibility(View.VISIBLE);
        }

        if (!departures.isEmpty() && query.isPaginable()) {
            adapter.addLoading(query.getObjectClass());
        } else {
            isLastPage = true;
        }
        isLoading = false;

        //if processing data from first load operation on  query with highlighted item/s
        if(iSFirstLoad && query.hasHighlighted()){
            iSFirstLoad = false;
            List<Integer> highlighted = adapter.getHighlightedPositions();

            //scroll to first highlighted item if there is any
            if(!highlighted.isEmpty()) {
                layoutManager.scrollToPositionWithOffset(highlighted.get(0), 20);
            }
        }

        iSFirstLoad = false;
    }

    private void loadNextPage() {
        //if query is async, use async Task to load items
        if(query.isAsync()){
            new Task().execute(0);
        } else {
            processNextPageItems(loadNextPageItems());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class Task extends AsyncTask<Integer, Integer, List<?>>{

        @Override
        protected List<?> doInBackground(Integer... ints) {
            return loadNextPageItems();
        }

        @Override
        protected void onPostExecute(List<?> departures) {
            processNextPageItems(departures);
        }
    }
}
