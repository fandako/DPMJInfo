package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpmjinfo.helpers.OfflineFileDb;
import com.example.dpmjinfo.recyclerViewHandling.BaseAdapter;
import com.example.dpmjinfo.recyclerViewHandling.BusStopDeparturesAdapter;
import com.example.dpmjinfo.helpers.CISSqliteHelper;
import com.example.dpmjinfo.helpers.OfflineFilesManager;
import com.example.dpmjinfo.recyclerViewHandling.FavouriteAdapter;
import com.example.dpmjinfo.recyclerViewHandling.PaginationListener;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.recyclerViewHandling.RecyclerViewTouchListener;
import com.example.dpmjinfo.queries.ScheduleQuery;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.example.dpmjinfo.recyclerViewHandling.PaginationListener.PAGE_START;

/**
 * Displays query result in recyclerView widget
 */
public class DeparturesActivity extends AppCompatActivity {
    //recyclerView that holds items supplied by given query + adapter
    private RecyclerView recyclerView;
    private BaseAdapter adapter;

    //loading and pagination flags and params
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean iSFirstLoad = true;
    private int page_size = 10;
    private int currentPage = PAGE_START;


    private LinearLayoutManager layoutManager;

    //query for supplying items
    private ScheduleQuery query;

    //textView to display when no items are supplied by query
    private TextView noItemsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the required data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //get query object from intent
        Serializable queryModel = bundle.getSerializable("com.android.dpmjinfo.queryModel");
        String queryClass = (String) bundle.getSerializable("com.android.dpmjinfo.queryClass");

        query = ScheduleQuery.getQueryFromSerializedModel(this, queryClass, queryModel);

        getSupportActionBar().setTitle(query.getName());

        //set default page size
        page_size = query.getPageSize();

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

        //if query has highlighted items add them to adapter
        if(query.hasHighlighted()){
            adapter.highlightItems(query.getHighlighted());
        }

        //add progressbar to UI
        adapter.addLoading(query.getObjectClass());

        recyclerView.setAdapter(adapter);

        //load data
        loadNextPage();

        //for paginable queries set scrollListener to handle further loading of items
        if (query.isPaginable()) {
            recyclerView.addOnScrollListener(new PaginationListener(layoutManager, page_size) {
                @Override
                protected void loadMoreItems() {
                    if(!isLoading) {
                        isLoading = true;
                        currentPage++;
                        loadNextPage();
                    }
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

    /**
     * loads next batch of items from query
     * @return List of items
     */
    private List<?> loadNextPageItems() {
        OfflineFilesManager ofm = new OfflineFilesManager(this);
        CISSqliteHelper helper = new CISSqliteHelper(ofm.getFilePath(OfflineFilesManager.SCHEDULE));
        Log.d("dbg", "load next page");
        return new ArrayList<>(query.exec(currentPage));
    }

    /**
     * processes loaded items and sets UI items visibility based on loading/pagination flags
     * @param departures List with loaded items to process
     */
    private void processNextPageItems(List<?> departures) {
        page_size = departures.size();
        Log.d("dbg", "page size: " + page_size);

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
        isLoading = false;
    }

    /**
     * executes load of next batch of items based on query a/synchronicity
     */
    private void loadNextPage() {
        //if query is async, use async Task to load items
        if(query.isAsync()){
            new Task(this).execute(0);
        } else {
            processNextPageItems(loadNextPageItems());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class Task extends AsyncTask<Integer, Integer, List<?>>{

        private WeakReference<DeparturesActivity> activityReference;

        public Task(DeparturesActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<?> doInBackground(Integer... ints) {

            // get a reference to the activity if it is still there
            DeparturesActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return new ArrayList<>();

            return activity.loadNextPageItems();
        }

        @Override
        protected void onPostExecute(List<?> departures) {
            // get a reference to the activity if it is still there
            DeparturesActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.processNextPageItems(departures);
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.query_result_menu, menu);


        //only show add to favourite icon when query says so
        if(!query.showAddToFavourite()){
            menu.removeItem(R.id.action_add_to_favourite);
        }

        //query.getModel().setShowAddToFavourite(false);

        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_to_favourite) {
            OfflineFileDb db = new OfflineFileDb(this);

            long res = db.saveFavourite(ScheduleQuery.getQueryFromSerializedModel(this, query.getClass().getSimpleName(), query.getModelForFavourite()));

            if(res > 0){
                Toast.makeText(this, getString(R.string.added_to_favourite_toast), Toast.LENGTH_SHORT).show();
                findViewById(R.id.action_add_to_favourite).setVisibility(View.GONE);
            } else {
                if(res == -2) {
                    Toast.makeText(this, getString(R.string.favourite_already_saved_toast), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.action_add_to_favourite).setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.favourite_saving_error_toast), Toast.LENGTH_SHORT).show();
                }
            }

        }

        return super.onOptionsItemSelected(item);
    }
}
