package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpmjinfo.helpers.NetworkHelper;
import com.example.dpmjinfo.helpers.OfflineFileDb;
import com.example.dpmjinfo.queries.ActualDepartureQuery;
import com.example.dpmjinfo.queries.ConnectionQuery;
import com.example.dpmjinfo.queries.DepartureQuery;
import com.example.dpmjinfo.helpers.OfflineFileManagerRequestsDoneListener;
import com.example.dpmjinfo.helpers.OfflineFilesManager;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.queries.FavouriteQuery;
import com.example.dpmjinfo.queries.RSSQuery;
import com.example.dpmjinfo.queries.ScheduleQuery;
import com.example.dpmjinfo.spinnerHandling.ScheduleQuerySpinnerAdapter;
import com.prof.rssparser.Article;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TooManyListenersException;

/**
 * Main screen activity. Displays queries selection and UI elements to set individual parameters of these queries.
 */
public class SchedulesActivity extends AppCompatActivity implements OfflineFileManagerRequestsDoneListener {
    TextView downloadText;
    Button downloadButton;
    Button searchButton;
    LinearLayout queryLayout;
    Spinner querySpinner;
    AlertDialog.Builder alertBuilder = null;
    SQLiteDatabase db;
    List<ScheduleQuery> scheduleQueries;
    private File file;
    OfflineFilesManager ofm;
    //files that need to be downloaded for queries to work
    ArrayList<String> missingFiles;
    TextView noInternetText;
    Button refreshButton;
    RSSQuery rssQuery;
    TextView newsCount;
    ImageView newsCountBackground;

    static final int DOWNLOAD_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        downloadText = findViewById(R.id.downloadText);
        downloadButton = findViewById(R.id.download);
        queryLayout = findViewById(R.id.query);
        searchButton = findViewById(R.id.button);
        querySpinner = findViewById(R.id.queryType);
        noInternetText = findViewById(R.id.noInternetText);
        refreshButton = findViewById(R.id.refreshButton);

        ofm = new OfflineFilesManager(this);

        scheduleQueries = new ArrayList<ScheduleQuery>();
        missingFiles = new ArrayList<>();

        rssQuery = new RSSQuery(this);

        ScheduleQuerySpinnerAdapter scheduleQuerySpinnerAdapter = new ScheduleQuerySpinnerAdapter(this, scheduleQueries, R.layout.spinner_item_no_divider);

        querySpinner.setAdapter(scheduleQuerySpinnerAdapter);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    ofm.getFilesToDownload(SchedulesActivity.this, missingFiles);
                } else {
                    alertBuilder = new AlertDialog.Builder(SchedulesActivity.this);
                    alertBuilder
                            .setCancelable(false)
                            .setMessage(getString(R.string.no_internet_connection_alert_message))
                            .setTitle(getString(R.string.download_error_alert_title))
                            .setPositiveButton(getString(R.string.no_internet_connection_ok_button_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
            }
        });

        querySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onQuerySelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    showConnectionErrorAlert();

                    return;
                }

                onQuerySelected(querySpinner.getSelectedItemPosition());
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleQuery q = (ScheduleQuery) querySpinner.getSelectedItem();
                if(q.isInternetDependant()){
                    if(!isNetworkAvailable()){
                        showConnectionErrorAlert();

                        return;
                    }
                }

                if(q.isReady()) {
                    q.execAndDisplayResult();
                } else {
                    Toast.makeText(SchedulesActivity.this, getString(R.string.query_not_populated_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });

        OfflineFilesManager ofm = new OfflineFilesManager(this);

        //if network available -> check for files updates
        if (isNetworkAvailable()) {


            Log.d("dbg", "check for updates");

            ArrayList<String> requiredFiles = new ArrayList<>();
            requiredFiles.add(OfflineFilesManager.SCHEDULE);
            requiredFiles.add(OfflineFilesManager.CALENDAR);
            ofm.getFilesToDownload(this, requiredFiles);
        }

        //register queries
        registerScheduleQuery(new ActualDepartureQuery(this));
        registerScheduleQuery(new DepartureQuery(this/*, db*/));
        registerScheduleQuery(new ConnectionQuery(this/*, db*/));
    }

    /**
     * shows alert which informs user that there is no internet connection
     */
    private void showConnectionErrorAlert() {
        alertBuilder = new AlertDialog.Builder(SchedulesActivity.this);
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
    }

    /**
     * handles query selection, hides and displays elements based on query readiness (no missing files etc)
     * @param position position of selected query in array
     */
    private void onQuerySelected(int position){
        List<ScheduleQuery> qs = ((ScheduleQuerySpinnerAdapter) querySpinner.getAdapter()).getItems();

        for (ScheduleQuery q : qs) {
            q.hide();
        }
        ScheduleQuery scheduleQuery = (ScheduleQuery) querySpinner.getAdapter().getItem(position);

        missingFiles.clear();
        for (String fileType : scheduleQuery.getRequiredFileTypes()) {
            String filePath = ofm.getFilePath(fileType);

            if(filePath.equals("")){
                missingFiles.add(fileType);
            }

            Log.d("dbg", filePath);
        }

        if(!missingFiles.isEmpty()){
            downloadButton.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            queryLayout.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            noInternetText.setVisibility(View.GONE);
            refreshButton.setVisibility(View.GONE);
        } else {
            if(scheduleQuery.isInternetDependant() && !isNetworkAvailable()){
                noInternetText.setVisibility(View.VISIBLE);
                refreshButton.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.GONE);
                downloadText.setVisibility(View.GONE);
                downloadButton.setVisibility(View.GONE);
                return;
            }

            queryLayout.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);
            noInternetText.setVisibility(View.GONE);
            refreshButton.setVisibility(View.GONE);
            scheduleQuery.populate();
            scheduleQuery.show();
        }
        Log.d("dbg", "onItemSelected");
    }

    /**
     * wrapper for NetworkHelper.isNetworkAvailable
     * @return true if network is available else false
     */
    private boolean isNetworkAvailable() {
        /*ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();*/
        return NetworkHelper.isNetworkAvailable(this);
    }

    /**
     * adds given query to spinner items and adds its view to activity
     * @param scheduleQuery query to register
     */
    private void registerScheduleQuery(ScheduleQuery scheduleQuery) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        queryLayout.addView(scheduleQuery.getView());

        scheduleQueries.add(scheduleQuery);
        ((ScheduleQuerySpinnerAdapter) querySpinner.getAdapter()).notifyDataSetChanged();

        querySpinner.setSelection(0);
    }

    /**
     * initialize - display container containing query views and search button
     */
    private void init() {
        OfflineFilesManager ofm = new OfflineFilesManager(this);
        String schedulePath = ofm.getFilePath(OfflineFilesManager.SCHEDULE);

        queryLayout.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);

        file = new File(schedulePath);

        //check if map is present in device
        /*if (file.exists()) {
            Log.d("dbg", "schedule already downloaded");
            if (initSqliteDb()) {
                registerScheduleQuery(new DepartureQuery(this, db));
                queryLayout.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
            } else {
                //unable to connect to schedule database -> alert error
            }
            return;
        }*/

        //downloadText.setVisibility(View.VISIBLE);
        //downloadButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request it is that we're responding to
        if (requestCode == DOWNLOAD_REQUEST) {
            if (resultCode == RESULT_OK) {
                queryLayout.setVisibility(View.VISIBLE);

                downloadButton.setVisibility(View.GONE);
                downloadText.setVisibility(View.GONE);

                ScheduleQuery scheduleQuery = (ScheduleQuery) querySpinner.getSelectedItem();
                scheduleQuery.populate();
                scheduleQuery.show();
            } else {
                //download canceled or failed
                alertBuilder = new AlertDialog.Builder(this);
                alertBuilder
                        .setMessage(getString(R.string.download_error_alert_message))
                        .setTitle(getString(R.string.download_error_alert_title))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                init();
                            }
                        });

                AlertDialog alert = alertBuilder.create();
                alert.show();
            }

            init();
        }
    }


    @Override
    public void onOfflineFileManagerRequestsDone(Hashtable<String, String> results) {
        ArrayList<String> urls = new ArrayList<>();
        for (String key : results.keySet()
        ) {
            Log.d("dbg offline", key + " - " + results.get(key));
            urls.add(results.get(key));
        }

        if (!urls.isEmpty()) {
            //download canceled or failed
            alertBuilder = new AlertDialog.Builder(this);
            alertBuilder
                    .setCancelable(false)
                    .setMessage(getString(R.string.update_available_alert_message))
                    .setTitle(getString(R.string.update_available_alert_title))
                    .setPositiveButton(getString(R.string.update_available_alert_ok_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OfflineFilesManager ofm = new OfflineFilesManager(getApplicationContext());
                            Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("com.example.dpmjinfo.url", results);
                            bundle.putSerializable("com.example.dpmjinfo.downloadDir", ofm.getDownloadDir());
                            intent.putExtras(bundle);
                            startActivityForResult(intent, DOWNLOAD_REQUEST);
                        }
                    })
                    .setNeutralButton(getString(R.string.update_available_alert_cancel_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            init();
                        }
                    });

            AlertDialog alert = alertBuilder.create();
            alert.show();
        }

        init();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.schedule_activity_menu, menu);

        MenuItem articlesItem = menu.findItem(R.id.action_articles);
        View articlesItemView = articlesItem.getActionView();
        articlesItemView.findViewById(R.id.newsIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(articlesItem);
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                newsCount = findViewById(R.id.newsCount);
                newsCountBackground = findViewById(R.id.newsCountBackground);

                if(isNetworkAvailable()) {
                    //load articles to display new articles count in menu
                    new Task(SchedulesActivity.this).execute(0);
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_map) {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_favourite) {
            FavouriteQuery q = new FavouriteQuery(this);

            q.execAndDisplayResult();
        }

        if (id == R.id.action_articles) {
            if(isNetworkAvailable()) {
                OfflineFileDb db = new OfflineFileDb(this);

                DateTime d = DateTime.now();
                db.insertArticlesDate(d.toString("yyyy-MM-dd HH:mm:ss"));

                newsCount.setVisibility(View.INVISIBLE);
                newsCountBackground.setVisibility(View.INVISIBLE);

                rssQuery.execAndDisplayResult();
            } else {
                showConnectionErrorAlert();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private List<?> loadArticles(){
        //load and cache articles
        rssQuery.setCacheResults(true);
        List<Article> articles = rssQuery.exec(0);
        //disable caching of further article queries
        rssQuery.setCacheResults(false);

        return articles;
    }

    private void onArticlesLoaded(List<?> articles){
        int count = articles.size();

        OfflineFileDb db = new OfflineFileDb(this);
        String date = db.getArticlesDate();

        if(!date.equals("")) {
            count = 0;
            DateTime currentArticlesDate = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));

            for (Object a : articles) {
                Article art = (Article) a;

                //Log.d("DBG", art.getPubDate());
                //Log.d("DBG", currentArticlesDate.toString("dd MMM yyyy HH:mm:ss"));
                DateTime d = DateTime.parse(art.getPubDate().subSequence(5, 25).toString(), DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss").withLocale(Locale.ENGLISH));

                if(d.compareTo(currentArticlesDate) > 0){
                    count++;
                }
            }
        }

        if(count != 0) {
            newsCount.setText(String.format("%d", count));

            newsCountBackground.setVisibility(View.VISIBLE);
            newsCount.setVisibility(View.VISIBLE);
        }
    }

    private static class Task extends AsyncTask<Integer, Integer, List<?>> {

        private WeakReference<SchedulesActivity> activityReference;

        public Task(SchedulesActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<?> doInBackground(Integer... ints) {

            // get a reference to the activity if it is still there
            SchedulesActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return new ArrayList<>();

            return activity.loadArticles();
        }

        @Override
        protected void onPostExecute(List<?> articles) {
            // get a reference to the activity if it is still there
            SchedulesActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.onArticlesLoaded(articles);
        }
    }
}
