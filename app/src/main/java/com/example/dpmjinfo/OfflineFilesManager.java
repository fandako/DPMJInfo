package com.example.dpmjinfo;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class OfflineFilesManager implements Response.Listener<JSONObject>, Response.ErrorListener {
    private Stack<JsonObjectRequest> requests;
    private Hashtable<String, String> results;
    private RequestQueue queue;
    private OfflineFileManagerRequestsDoneListener doneListener;
    private Context mContext;
    private OfflineFileDb db = null;

    public final static String CALENDAR = "calendar";
    public final static String SCHEDULE = "schedule";
    public final static String MAP = "map";

    public OfflineFilesManager(Context context) {
        requests = new Stack<>();
        results = new Hashtable<>();
        queue = Volley.newRequestQueue(context);
        mContext = context;
    }

    private OfflineFileDb getDb(){
        if(db == null){
            db = new OfflineFileDb(mContext);
            //db.onUpgrade(db.getWritableDatabase(), 1,1);
        }

        return db;
    }

    public String getDownloadDir(){
        return mContext.getExternalFilesDir(null).getPath();
    }

    public String getFilenameFromUrl(String url){
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public String getFilePathFromUrl(String url){
        return getDownloadDir() + "/" + getFilenameFromUrl(url);
    }

    private String getCurrentDate() {
        /*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        return dateFormat.format(date);*/
        DateTime d = DateTime.now();

        return d.toString(ScheduleQuery.getDateFormat());
    }

    private void checkForUpdate(String fileType) {
        String url = "http://testalbum.8u.cz/dpmjinfoserver/API/mobileUploadsAPI.php?fileType=" + fileType + "&toDate=" + getCurrentDate() + "&limit=1";

        addRequest(url);
    }

    private void addRequest(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, this, this);

        requests.push(jsonObjectRequest);
    }

    public void getFilesToDownload(OfflineFileManagerRequestsDoneListener listener, List<String> files) {
        if (files.isEmpty()) {
            //return empty hashtable
            listener.onOfflineFileManagerRequestsDone(new Hashtable<>());
        }

        //register requests to be done
        for (String file : files
        ) {
            //Log.d("dbg offline", file);
            checkForUpdate(file);
        }

        doneListener = listener;
        //add first request to queue -> requests processing starts
        queue.add(requests.pop());
    }

    private synchronized void continueIfPossible() {
        if (requests.isEmpty()) {
            //all requests are processed
            onRequestsDone();
            return;
        }

        //move to the next request
        queue.add(requests.pop());
    }

    private void onRequestsDone(){
        Hashtable<String, String> filesToDownload = new Hashtable<>();

        //loop through requests results
        for (String key: results.keySet()
        ) {
           String url = results.get(key);
           String filePath = getFilePathFromUrl(url);
           File file = new File(filePath);

           //find files which need to be downloaded
           if(!file.exists()){
               filesToDownload.put(key, url);
           }
        }

        //send list of files to be downloaded back to invoker
        doneListener.onOfflineFileManagerRequestsDone(filesToDownload);
    }

    public synchronized void onErrorResponse(VolleyError error) {
        continueIfPossible();
    }

    @Override
    public synchronized void onResponse(JSONObject response) {
        String url = "no url";
        String fileType;
        try {
            JSONArray content = (JSONArray) response.get("content");

            if (content.length() == 0) {
                //there is no file of given type on server
            } else {
                url = ((JSONObject) content.get(0)).get("mobileFile").toString();
                fileType = ((JSONObject) content.get(0)).get("fileType").toString();
                results.put(fileType, url);
            }
        } catch (Exception e) {

        } finally {
            //move to next request if there is any left
            continueIfPossible();
        }
    }

    public String getFilePath(String fileType) {
        return getDb().getFilePath(fileType);
    }

    public boolean fileDownloaded(String fileType, String url) {
        String filePath = getFilePathFromUrl(url);
        String oldFilePath = getDb().getFilePath(fileType);

        long rows = getDb().insertFile(fileType, filePath);

        if(rows != -1){
            File file = new File(oldFilePath);
            if(file.exists()){
                file.delete();
            }

            return true;
        } else {
            return false;
        }
    }
}
