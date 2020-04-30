package com.example.dpmjinfo.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dpmjinfo.OfflineFilesManager;
import com.example.dpmjinfo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class DownloadActivity extends AppCompatActivity {

    private TextView fileNameField;
    private ProgressBar progressBar;
    //private Button downloadButton;
    private Button cancelButton;
    private TextView progressText;

    private DownloadManager downloadManager;
    private long downloadID;
    private String filePath = "";
    private File file;
    String downloadURL;
    String downloadDir;

    HashMap<String, String> downloadUrls;
    Integer downloadCnt;
    Integer downloadedCnt;
    Object[] downloadFileTypes;
    OfflineFilesManager ofm;

    //listen to download complete broadcast to know when download finishes
    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadID);
                //query.setFilterByStatus(~(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL));
                Cursor cursor = downloadManager.query(query);
                if (!cursor.moveToFirst()) {
                    Log.d("dbg", "no query result");
                    cursor.close();
                    return;
                }
                do {
                    long status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    if(status == DownloadManager.STATUS_SUCCESSFUL){
                        Log.d("dbg", "download complete");
                        //complete();
                        moveToNextDownload();
                    }

                    if(status == DownloadManager.STATUS_FAILED){
                        Log.d("dbg", "download failed");
                        cancel();
                    }
                    //stop progress checking
                    stopProgressChecker();
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    };


    private static final int PROGRESS_DELAY = 16;
    Handler handler = new Handler();
    private boolean isProgressCheckerRunning = false;

    /**
     * Checks download progress.
     */
    private void checkProgress() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);
        //query.setFilterByStatus(~(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL));
        Cursor cursor = downloadManager.query(query);
        if (!cursor.moveToFirst()) {
            Log.d("dbg", "no query result");
            cursor.close();
            return;
        }
        do {
            long reference = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            long progress = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long size = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

            if (size == -1) {
                size = 1;
                progress = 0;
            }
            int percentage = (int) ((progress * 100 / size) );

            //progressBar.setProgress(percentage);
            progressText.setText(getResources().getString(R.string.percent, percentage));
            // do whatever you need with the progress
        } while (cursor.moveToNext());
        cursor.close();
    }

    /**
     * Starts watching download progress.
     * <p>
     * This method is safe to call multiple times. Starting an already running progress checker is a no-op.
     */
    private void startProgressChecker() {
        if (!isProgressCheckerRunning) {
            progressChecker.run();
            isProgressCheckerRunning = true;
        }
    }

    /**
     * Stops watching download progress.
     */
    private void stopProgressChecker() {
        handler.removeCallbacks(progressChecker);
        isProgressCheckerRunning = false;
    }

    /**
     * Checks download progress and updates status, then re-schedules itself.
     */
    private Runnable progressChecker = new Runnable() {
        @Override
        public void run() {
            try {
                checkProgress();
                // manager reference not found. Commenting the code for compilation
                //manager.refresh();
            } finally {
                handler.postDelayed(progressChecker, PROGRESS_DELAY);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        ActionBar bar = getSupportActionBar();

        bar.setTitle(getString(R.string.download_activity_title));
        bar.setDisplayHomeAsUpEnabled(true);

        fileNameField = findViewById(R.id.fileName);
        progressBar = findViewById(R.id.progressBar);
        //downloadButton = findViewById(R.id.download);
        cancelButton = findViewById(R.id.cancel);
        progressText = findViewById(R.id.progress);

        progressText.setText("0%");

        /*downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setProgress(1);
                downloadButton.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.VISIBLE);

                downloadFile(downloadURL);
            }
        });*/

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.remove(downloadID);
                progressText.setText("0%");
                progressBar.setProgress(0);
                //downloadButton.setVisibility(View.VISIBLE);
                //cancelButton.setVisibility(View.INVISIBLE);

                cancel();
            }
        });


        // Get the Intent that started this activity and extract the required data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //downloadURL = (String) bundle.getSerializable("com.example.dpmjinfo.url");
        downloadDir = (String) bundle.getSerializable("com.example.dpmjinfo.downloadDir");


        downloadUrls = (HashMap<String, String>) bundle.getSerializable("com.example.dpmjinfo.url");
        downloadCnt = downloadUrls.size();
        downloadedCnt = 0;
        downloadFileTypes = downloadUrls.keySet().toArray();
        ofm = new OfflineFilesManager(this);

        fileNameField.setText(getString(R.string.download_progress, downloadedCnt, downloadCnt));

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        progressBar.setProgress(1);
        //downloadFile(downloadURL);
        downloadFile(downloadUrls.get(downloadFileTypes[downloadedCnt]));
    }

    private void moveToNextDownload(){
        ofm.fileDownloaded((String) downloadFileTypes[downloadedCnt], downloadUrls.get(downloadFileTypes[downloadedCnt]));
        downloadedCnt++;

        if(downloadedCnt.equals(downloadCnt)){
            complete();
        } else {
            fileNameField.setText(getString(R.string.download_progress, downloadedCnt, downloadCnt));
            progressBar.setProgress(0);
            downloadFile(downloadUrls.get(downloadFileTypes[downloadedCnt]));
        }
    }

    private void downloadFile(String url) {
        String fileName = ofm.getFilenameFromUrl(url);
        filePath = ofm.getFilePathFromUrl(url);

        file = new File(filePath);

        //check if file is present
        if (file.exists()) {
            //finish activity with RESULT_OK
            //complete();
            moveToNextDownload();
        }

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        try {
       /*
       Create a DownloadManager.Request with all the information necessary to start the download
        */
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            //Restrict the types of networks over which this download may proceed.
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            //Set whether this download may proceed over a roaming connection.
            request.setAllowedOverRoaming(false);
            //Set the title of this download, to be displayed in notifications (if enabled).
            request.setTitle(fileName);
            //Set a description of this download, to be displayed in notifications (if enabled)
            request.setDescription("Downloading...");
            request.setMimeType("application/octet-stream");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDestinationUri(Uri.fromFile(file));// Uri of the destination file
            request.allowScanningByMediaScanner();

            downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

            //start checking for download progress
            startProgressChecker();
        } catch (IllegalArgumentException e) {
            //BaseUtils.showToast(mContext, "Download link is broken or not available for download");
            Log.e("error", "Method: downloadFile: Download link is broken");

        }
    }

    public void complete() {
        unregisterReceiver(onDownloadComplete);

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void cancel() {
        unregisterReceiver(onDownloadComplete);

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        cancel();
        unregisterReceiver(onDownloadComplete);
        return true;
    }
}
