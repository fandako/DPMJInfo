package com.example.dpmjinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OfflineFileDb extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "filesDb";
    private static final String TABLE_FILES = "files";
    private static final String KEY_FILETYPE = "id";
    private static final String KEY_PATH = "name";

    public OfflineFileDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FILES + "("
                + KEY_FILETYPE + " TEXT PRIMARY KEY," + KEY_PATH + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
        // Create tables again
        onCreate(db);
    }

    public String getFilePath(String fileType){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_FILES, new String[]{KEY_PATH},  KEY_FILETYPE + "=?",  new String[]{fileType}, null, null, null, null);

        if(!cursor.moveToNext()){
            return "";
        }

        return cursor.getString(cursor.getColumnIndex(KEY_PATH));
    }

    public long insertFile(String fileType, String path){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_FILETYPE, fileType);
        cValues.put(KEY_PATH, path);

        return db.insertWithOnConflict(TABLE_FILES, null, cValues, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
