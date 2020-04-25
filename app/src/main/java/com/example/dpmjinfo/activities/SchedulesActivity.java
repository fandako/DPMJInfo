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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dpmjinfo.ActualDepartureQuery;
import com.example.dpmjinfo.DepartureQuery;
import com.example.dpmjinfo.MainActivity;
import com.example.dpmjinfo.OfflineFileManagerRequestsDoneListener;
import com.example.dpmjinfo.OfflineFilesManager;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.ScheduleQuery;
import com.example.dpmjinfo.ScheduleQuerySpinnerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
    ArrayList<String> missingFiles;

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

        ofm = new OfflineFilesManager(this);

        scheduleQueries = new ArrayList<ScheduleQuery>();
        missingFiles = new ArrayList<>();

        ScheduleQuerySpinnerAdapter scheduleQuerySpinnerAdapter = new ScheduleQuerySpinnerAdapter(this, scheduleQueries);

        querySpinner.setAdapter(scheduleQuerySpinnerAdapter);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    ofm.getFilesToDownload(SchedulesActivity.this, missingFiles);
                } else {
                    alertBuilder = new AlertDialog.Builder(getApplicationContext());
                    alertBuilder
                            .setMessage("Není připojení k internetu")
                            .setTitle("Chyba stahování")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                } else {
                    queryLayout.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
                    downloadText.setVisibility(View.GONE);
                    downloadButton.setVisibility(View.GONE);
                    scheduleQuery.populateView();
                    scheduleQuery.show();
                }
                Log.d("dbg", "onItemSelected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dbg", "search button clicked");
                ((ScheduleQuery) querySpinner.getSelectedItem()).execAndDisplayResult();
            }
        });

        OfflineFilesManager ofm = new OfflineFilesManager(this);

        if (isNetworkAvailable()) {
            registerScheduleQuery(new ActualDepartureQuery(this));

            ArrayList<String> requiredFiles = new ArrayList<>();
            requiredFiles.add(OfflineFilesManager.SCHEDULE);
            requiredFiles.add(OfflineFilesManager.CALENDAR);
            ofm.getFilesToDownload(this, requiredFiles);
        }

        registerScheduleQuery(new DepartureQuery(this/*, db*/));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void registerScheduleQuery(ScheduleQuery scheduleQuery) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        queryLayout.addView(scheduleQuery.getView());

        scheduleQueries.add(scheduleQuery);
        ((ScheduleQuerySpinnerAdapter) querySpinner.getAdapter()).notifyDataSetChanged();

        querySpinner.setSelection(0);
    }

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

    private boolean initSqliteDb() {
        try {
            db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            return true;
        } catch (SQLiteException e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == DOWNLOAD_REQUEST) {
            if (resultCode == RESULT_OK) {
                queryLayout.setVisibility(View.VISIBLE);

                downloadButton.setVisibility(View.GONE);
                downloadText.setVisibility(View.GONE);

                ScheduleQuery scheduleQuery = (ScheduleQuery) querySpinner.getSelectedItem();
                scheduleQuery.populateView();
                scheduleQuery.show();
            } else {
                //download canceled or failed
                alertBuilder = new AlertDialog.Builder(this);
                alertBuilder
                        .setMessage("Při stahování došlo k chybě")
                        .setTitle("Chyba stahování")
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
                    .setMessage("Je k dispozici nová verze jizdních řádů. Stáhnout Nyní?")
                    .setTitle("Aktualizace")
                    .setPositiveButton("Stáhnout", new DialogInterface.OnClickListener() {
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
                    .setNeutralButton("Nyní ne", new DialogInterface.OnClickListener() {
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
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_map) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
