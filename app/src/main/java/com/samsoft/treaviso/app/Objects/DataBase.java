package com.samsoft.treaviso.app.Objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by sam on 29/04/15.
 */
public class DataBase {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TAG = "DataBase";
    private static final String NAME_DB = "TreAviso";
    private static final String TABLE_FAVORITOS = "favoritos";
    private Context context;
    private SQLiteDatabase db;

    private SQLiteDatabase openDatabase(String dbname)
    {
        File dbfile = this.context.getDatabasePath(dbname + ".db");
        if (!dbfile.exists()) {
            boolean b = dbfile.getParentFile().mkdirs();
        }
        SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile.getAbsolutePath(), null);
        return mydb;
    }

    public DataBase(Context context) {
        this.context = context;
        db = openDatabase(NAME_DB);
        ArmarBaseDeDatos();
    }

    public void Close()
    {
        db.close();
    }

    private void ArmarBaseDeDatos() {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITOS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, lat DOUBLE , lng DOUBLE, radius INTEGER)");
    }

    public Integer addFavorito(String name,Double lat,Double lng,Integer radius)
    {
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("lat",lat);
        values.put("lng",lng);
        values.put("radius",radius);
        long id = db.insert(TABLE_FAVORITOS,null,values);
        db.setTransactionSuccessful();
        db.endTransaction();
        return (int) id;
    }

    public void removeFavorito(Integer fav)
    {
        db.beginTransaction();
        db.delete(TABLE_FAVORITOS,"id = ?",new String[]{fav.toString()});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public JSONArray getFavoritos()
    {
        Cursor c = null;
        JSONArray arr = new JSONArray();
        try {
            c = db.rawQuery("SELECT * FROM " + TABLE_FAVORITOS, new String[]{});
            while (c.moveToNext()) {
                JSONObject o = new JSONObject();
                o.put("id", c.getInt(0));
                o.put("name", c.getString(1));
                o.put("lat", c.getDouble(2));
                o.put("lng", c.getDouble(3));
                o.put("radius", c.getInt(4));
                arr.put(o);
            }
        } catch (Exception ee) {ee.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
        return arr;
    }

    public JSONObject getFavoritos(Integer id)
    {
        Cursor c = null;
        JSONObject o = null;
        try {
            c = db.rawQuery("SELECT * FROM " + TABLE_FAVORITOS + "WHERE id = ?", new String[]{id.toString()});
            if (c.moveToNext()) {
                o = new JSONObject();
                o.put("id", c.getInt(0));
                o.put("name", c.getString(1));
                o.put("lat",c.getDouble(2));
                o.put("lng",c.getDouble(3));
                o.put("radius",c.getDouble(4));
            }
        } catch (Exception ee) {ee.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
        return o;
    }


}
