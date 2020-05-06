package com.example.dpmjinfo.helpers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dpmjinfo.queries.ScheduleQuery;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

/**
 * class for managing offline files, checking for updates and getting filepaths
 */
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

    /**
     * get download dir path
     * @return
     */
    public String getDownloadDir(){
        return mContext.getExternalFilesDir(null).getPath();
    }

    /**
     * gets file name from supplied url
     * @param url url to parse
     * @return file name
     */
    public String getFilenameFromUrl(String url){
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * gets filepath which corresponds with given url
     * @param url url to parse
     * @return filepath
     */
    public String getFilePathFromUrl(String url){
        return getDownloadDir() + "/" + getFilenameFromUrl(url);
    }

    /**
     * gets current date
     * @return string formatted current date
     */
    private String getCurrentDate() {
        /*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        return dateFormat.format(date);*/
        DateTime d = DateTime.now();

        return d.toString(ScheduleQuery.getDateFormat());
    }

    /**
     * add request to check for given file type update
     * @param fileType type of file
     */
    private void checkForUpdate(String fileType) {
        String url = "http://testalbum.8u.cz/dpmjinfoserver/API/mobileUploadsAPI.php?fileType=" + fileType + "&toDate=" + getCurrentDate() + "&limit=1";

        Log.d("dbg", url);
        addRequest(url);
    }

    /**
     * creates new jsonObjectRequest based on supplied url
     * @param url url
     */
    private void addRequest(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, this, this);

        requests.push(jsonObjectRequest);
    }

    /**
     * check for updates for given files, return list of available updated using callback on supplied listener
     * @param listener listener for result callback
     * @param files list of file types for which to check for updates
     */
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

    /**
     * continue if any request if remaining else finish
     */
    private synchronized void continueIfPossible() {
        if (requests.isEmpty()) {
            //all requests are processed
            onRequestsDone();
            return;
        }

        //move to the next request
        queue.add(requests.pop());
    }

    /**
     * called when all request are done, return result to caller with callback on listener
     */
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

    /**
     * on request error continue to next request
     * @param error volley error object
     */
    public synchronized void onErrorResponse(VolleyError error) {
        Log.d("dbgOfflineFile", "request error");
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
                Log.d("dbg offline", "no file on server");
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

    /**
     * retrieve filepath of file of given type
     * @param fileType type of file
     * @return filepath
     */
    public String getFilePath(String fileType) {

        String filePath = getDb().getFilePath(fileType);

        if(filePath.equals("")){
            return filePath;
        }

        File file = new File(filePath);

        //check if file really exists in filesystem
        if(file.exists()) {
            return filePath;
        } else {
            return "";
        }
    }

    /**
     * try to save record about file downloaded from given url to db, on db insert failure deletes file
     * to avoid conflicts
     * @param fileType type of file
     * @param url url
     * @return true on success, false otherwise
     */
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

    /**
     * delete file of given type
     * @param fileType type of file
     * @return true on success, false otherwise
     */
    public boolean deleteFile(String fileType) {
        String path = getFilePath(fileType);

        getDb().beginTransaction();
        long res = getDb().deleteFile(fileType);

        File file = new File(path);

        if(res > 0){
            if(file.delete()){
                //if file successfully deleted -> commit database changes
                getDb().commit();
                return true;
            } else {
                //if file delete failed -> rollback database changes
                getDb().rollback();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        getDb().close();
    }
}
