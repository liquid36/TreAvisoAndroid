package com.samsoft.treaviso.background.data.sqlite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import com.samsoft.treaviso.background.data.Alarm;
import com.samsoft.treaviso.background.data.Location;
import com.samsoft.treaviso.background.data.LocationDAO;

public class SQLiteLocationDAO implements LocationDAO {
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String TAG = "SQLiteLocationDAO";
	private Context context;
	
	public SQLiteLocationDAO(Context context) {
		this.context = context;
	}
	
	private SQLiteDatabase openDatabase(String dbname, String password)
	{
		File dbfile = this.context.getDatabasePath(dbname + ".db");

		if (!dbfile.exists()) {
		    dbfile.getParentFile().mkdirs();
		}
		SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		mydb.execSQL(LocationOpenHelper.LOCATION_TABLE_CREATE);
		mydb.execSQL(LocationOpenHelper.ALARM_TABLE_CREATE);
		return mydb;
	}
	
	public Alarm[] getActiveAlarm() {
		SQLiteDatabase db = null;
		Cursor c = null;
		List<Alarm> all = new ArrayList<Alarm>();
		try {
			db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
			c = db.rawQuery("SELECT * FROM "+ LocationOpenHelper.ALARM_TABLE_NAME + " WHERE active > 0",new String[] {});
			while (c.moveToNext()) {
				all.add(hydrateA(c));
			}
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return all.toArray(new Alarm[all.size()]);
	}

    public Alarm getAlarm(long id)
    {
        SQLiteDatabase db = null;
        Cursor c = null;
        Alarm a = null;
        try {
            db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
            c = db.rawQuery("SELECT * FROM "+ LocationOpenHelper.ALARM_TABLE_NAME + " WHERE id = ?" ,new String[] {Long.toString(id)});
            if (c.moveToFirst())
                a = hydrateA(c);
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return a;
    }

    public Alarm[] getAllAlarm() {
        SQLiteDatabase db = null;
        Cursor c = null;
        List<Alarm> all = new ArrayList<Alarm>();
        try {
            db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
            c = db.rawQuery("SELECT * FROM "+ LocationOpenHelper.ALARM_TABLE_NAME ,new String[] {});
            while (c.moveToNext()) {
                all.add(hydrateA(c));
            }
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return all.toArray(new Alarm[all.size()]);
    }


    public boolean persistAlarm(Alarm alarm) {
        SQLiteDatabase db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
        db.beginTransaction();
        ContentValues values = getContentValues(alarm);
        long rowId = db.insert(LocationOpenHelper.ALARM_TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        if (rowId > -1) {
            alarm.setId(rowId);
            return true;
        } else {
            return false;
        }

    }

    public void updateAlarm(Alarm alarm) {
        SQLiteDatabase db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
        db.beginTransaction();
        ContentValues values = getContentValues(alarm);
        long rowId = db.update(LocationOpenHelper.ALARM_TABLE_NAME, values, "id = ?" , new String[]{alarm.getId().toString()} );
        db.setTransactionSuccessful(); db.endTransaction();
        db.close();
    }

    public Location[] getAllLocations() {
        SQLiteDatabase db = null;
        Cursor c = null;
        List<Location> all = new ArrayList<Location>();
        try {
            db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
            c = db.query(LocationOpenHelper.LOCATION_TABLE_NAME, null, null, null, null, null, null);
            while (c.moveToNext()) {
                all.add(hydrate(c));
            }
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return all.toArray(new Location[all.size()]);
    }


    public boolean persistLocation(Location location) {
		SQLiteDatabase db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
		db.beginTransaction();
		ContentValues values = getContentValues(location);
		long rowId = db.insert(LocationOpenHelper.LOCATION_TABLE_NAME, null, values);
		Log.d("LocationUpdateService", "After insert, rowId = " + rowId);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		if (rowId > -1) {
			location.setId(rowId);
			return true;
		} else {
			return false;
		}
		
	}

    public void deleteAlarm(Alarm alarm) {
        SQLiteDatabase db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
        db.beginTransaction();
        db.delete(LocationOpenHelper.ALARM_TABLE_NAME, "id = ?", new String[]{alarm.getId().toString()});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

	public void deleteLocation(Location location) {
		SQLiteDatabase db = this.openDatabase(LocationOpenHelper.SQLITE_DATABASE_NAME, "");
		db.beginTransaction();
		db.delete(LocationOpenHelper.LOCATION_TABLE_NAME, "id = ?", new String[]{location.getId().toString()});
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	
	
	private Alarm hydrateA(Cursor c) {
		Alarm l = new Alarm();
		l.setId(c.getLong(c.getColumnIndex("id")));
		l.setName(c.getString(c.getColumnIndex("name")));
		l.setPath(c.getString(c.getColumnIndex("path")));
		l.setLatitude(c.getString(c.getColumnIndex("latitude")));
		l.setLongitude(c.getString(c.getColumnIndex("longitude")));
		l.setMetros(c.getInt(c.getColumnIndex("metros")));
		l.setActive(c.getInt(c.getColumnIndex("active")));
		return l;
	}
	
	private Location hydrate(Cursor c) {
		Location l = new Location();
		l.setId(c.getLong(c.getColumnIndex("id")));
		l.setRecordedAt(stringToDate(c.getString(c.getColumnIndex("recordedAt"))));
		l.setLatitude(c.getString(c.getColumnIndex("latitude")));
		l.setLongitude(c.getString(c.getColumnIndex("longitude")));
		l.setAccuracy(c.getString(c.getColumnIndex("accuracy")));
		l.setSpeed(c.getString(c.getColumnIndex("speed")));
		
		return l;
	}
	
	private ContentValues getContentValues(Location location) {
		ContentValues values = new ContentValues();
		values.put("latitude", location.getLatitude());
		values.put("longitude", location.getLongitude());
		values.put("recordedAt", dateToString(location.getRecordedAt()));	
		values.put("accuracy",  location.getAccuracy());
		values.put("speed", location.getSpeed());
		return values;
	}

    private ContentValues getContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put("latitude", alarm.getLatitude());
        values.put("longitude", alarm.getLongitude());
        values.put("active", alarm.getActive());
        values.put("name",  alarm.getName());
        values.put("metros", alarm.getMetros());
        values.put("path", alarm.getPath());
        return values;
    }

	public Date stringToDate(String dateTime) {
		SimpleDateFormat iso8601Format = new SimpleDateFormat(DATE_FORMAT);
		Date date = null;
		try {
			date = iso8601Format.parse(dateTime);
			
		} catch (ParseException e) {
			Log.e("DBUtil", "Parsing ISO8601 datetime ("+ dateTime +") failed", e);
		}
		
		return date;
	}
	
	public String dateToString(Date date) {
		SimpleDateFormat iso8601Format = new SimpleDateFormat(DATE_FORMAT);
		return iso8601Format.format(date);
	}

}
