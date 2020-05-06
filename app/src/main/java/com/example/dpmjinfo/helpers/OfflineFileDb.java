package com.example.dpmjinfo.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.JsonReader;
import android.util.Log;
import android.util.Pair;

import com.example.dpmjinfo.queries.ScheduleQuery;
import com.example.dpmjinfo.queryModels.ScheduleQueryModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * class for managing data in local sqlite database as current locally saved files paths and favourite queries
 */
public class OfflineFileDb extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "filesDb";
    private static final String TABLE_FILES = "files";
    private static final String KEY_FILETYPE = "id";
    private static final String KEY_PATH = "name";

    private static final String TABLE_FAVOURITE = "favourite";
    private static final String KEY_QUERY_TYPE = "queryClass";
    private static final String KEY_QUERY_MODEL = "queryModel";

    public OfflineFileDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FILES + "("
                + KEY_FILETYPE + " TEXT PRIMARY KEY," + KEY_PATH + " TEXT" + ")";

        String FAVOURITE_TABLE = "CREATE TABLE " + TABLE_FAVOURITE + "("
                + KEY_QUERY_TYPE + " TEXT," + KEY_QUERY_MODEL + " BLOB" + ", PRIMARY KEY (" + KEY_QUERY_TYPE + ", " + KEY_QUERY_MODEL + "))";

        db.execSQL(CREATE_TABLE);
        db.execSQL(FAVOURITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITE);
        // Create tables again
        onCreate(db);
    }

    /**
     * return file path for given filetype
     * @param fileType fileType (OfflineFilesManager.MAP, ...)
     * @return filepath on success, if file is not present returns empty string
     */
    public String getFilePath(String fileType){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_FILES, new String[]{KEY_PATH},  KEY_FILETYPE + "=?",  new String[]{fileType}, null, null, null, null);

        if(!cursor.moveToNext()){
            return "";
        }

        String path = cursor.getString(cursor.getColumnIndex(KEY_PATH));

        cursor.close();

        return path;
    }

    /**
     * insert information about file to db, replaces filepath on file type conflict
     * @param fileType type of file (OfflineFilesManager.MAP, ...)
     * @param path filepath
     * @return number of affected lines by insert
     */
    public long insertFile(String fileType, String path){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_FILETYPE, fileType);
        cValues.put(KEY_PATH, path);

        return db.insertWithOnConflict(TABLE_FILES, null, cValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * delete record about given file type
     * @param fileType type of file
     * @return number of affected lines by delete
     */
    public long deleteFile(String fileType){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_FILETYPE, fileType);

        return db.delete(TABLE_FILES, KEY_FILETYPE + "=?", new String[] {fileType});
    }

    /**
     * begins db transaction
     */
    public void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }

    /**
     * commits db transaction
     */
    public void commit() {
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
    }

    /**
     * performs rollback on active transaction
     */
    public void rollback() {
        getWritableDatabase().endTransaction();
    }

    /**
     * checks of favourite is already present in db
     * @param queryType type of query
     * @param model serialized model
     * @return true if favourite found in db, false otherwise
     */
    public boolean isFavouriteSaved(String queryType, byte[] model){
        SQLiteDatabase db = this.getReadableDatabase();

        final String sql = "SELECT count(*) FROM " + TABLE_FAVOURITE + " WHERE " + KEY_QUERY_TYPE + "=? AND " + KEY_QUERY_MODEL + "=?";

        SQLiteStatement st = db.compileStatement(sql);
        st.bindString(1, queryType);
        st.bindBlob(2, model);
        long res = st.simpleQueryForLong();

        return res > 0;
    }

    /**
     * get list of favourites as list of pairs
     * @return list of favourites
     */
    public List<Pair<String, ScheduleQueryModel>> getFavourites() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVOURITE, null,  null,  null, null, null, null, null);

        List<Pair<String, ScheduleQueryModel>> favourites = new ArrayList<>();

        while (cursor.moveToNext()){
            String queryType = cursor.getString(cursor.getColumnIndex(KEY_QUERY_TYPE));

            byte[] data = cursor.getBlob(cursor.getColumnIndex(KEY_QUERY_MODEL));

            try {
                ByteArrayInputStream baip = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(baip);
                ScheduleQueryModel queryModel = (ScheduleQueryModel) ois.readObject();
                favourites.add(new Pair<>(queryType, queryModel));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cursor.close();

        return favourites;
    }

    /**
     * convert qeury model to byte array to save as blob down the line
     * @param model query model
     * @return byte array representation of query model
     * @throws IOException
     */
    public byte[] queryModelAsBytes(ScheduleQueryModel model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(model);
        byte[] queryModelAsBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(queryModelAsBytes);

        return queryModelAsBytes;
    }

    /**
     * saves query to db as favourite if it was not already saved earlier
     * @param query query to be saved as favourite
     * @return -2 if query already saved, number of rows affected by insert if no exception occurred, -10 otherwise
     */
    public long saveFavourite(ScheduleQuery query) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryType = query.getClass().getSimpleName();

        try {
            byte[] queryModelAsBytes = queryModelAsBytes(query.getModel());

            ContentValues cValues = new ContentValues();
            cValues.put(KEY_QUERY_TYPE, queryType);
            cValues.put(KEY_QUERY_MODEL, queryModelAsBytes);

            //if favourite already saved
            if(isFavouriteSaved(queryType, queryModelAsBytes)){
                return -2;
            }

            return db.insert(TABLE_FAVOURITE, null, cValues);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -10;
    }

    /**
     * deletes query from favourites
     * @param query query to delete
     * @return true on success, false otherwise
     */
    public boolean deleteFavourite(ScheduleQuery query){
        SQLiteDatabase db = this.getReadableDatabase();

        byte[] queryModelAsBytes;
        try {
            queryModelAsBytes = queryModelAsBytes(query.getModel());
        } catch (Exception e) {
            return false;
        }

        final String sql = "DELETE FROM " + TABLE_FAVOURITE + " WHERE " + KEY_QUERY_TYPE + "=? AND " + KEY_QUERY_MODEL + "=?";

        SQLiteStatement st = db.compileStatement(sql);
        st.bindString(1, query.getClass().getSimpleName());
        st.bindBlob(2, queryModelAsBytes);
        int res = st.executeUpdateDelete();

        Log.d("dbg", "sql delete row cnt:" + res );

        return res > 0;
    }
}
