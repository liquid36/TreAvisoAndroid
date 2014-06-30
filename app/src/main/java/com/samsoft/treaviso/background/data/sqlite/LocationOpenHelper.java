package com.samsoft.treaviso.background.data.sqlite;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationOpenHelper extends SQLiteOpenHelper {
    public static final String SQLITE_DATABASE_NAME = "cordova_bg_locations";
    private static final int DATABASE_VERSION = 1;
    public static final String LOCATION_TABLE_NAME = "location";
    public static final String ALARM_TABLE_NAME = "alarm";
    public static final String LOCATION_TABLE_COLUMNS = 
        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
        " recordedAt TEXT," +
        " accuracy TEXT," +
        " speed TEXT," +
        " latitude TEXT," +
        " longitude TEXT";
    public static final String ALARM_TABLE_COLUMNS = 
        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
        " name TEXT," +
        " active INTEGER," +
        " metros INTEGER," + 
        " latitude TEXT," +
        " longitude TEXT," +
        " path TEXT" ;    
    public static final String LOCATION_TABLE_CREATE =
        "CREATE TABLE IF NOT EXISTS " + LOCATION_TABLE_NAME + " (" +
        LOCATION_TABLE_COLUMNS +
        ");";
	public static final String ALARM_TABLE_CREATE =
        "CREATE TABLE IF NOT EXISTS " + ALARM_TABLE_NAME + " (" +
        ALARM_TABLE_COLUMNS +
        ");";


    LocationOpenHelper(Context context) {
        super(context, SQLITE_DATABASE_NAME, null, DATABASE_VERSION);
    }    

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);
        Log.d(this.getClass().getName(), LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        
    }
}
