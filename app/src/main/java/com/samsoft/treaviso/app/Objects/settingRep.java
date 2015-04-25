package com.samsoft.treaviso.app.Objects;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sam on 25/04/15.
 */
public class settingRep {
    public Context contex;
    public SharedPreferences repo;
    protected static final String REFERENCE_KEY = "TREAVISO";
    public settingRep(Context c)
    {
        contex = c;
        repo = contex.getSharedPreferences(REFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public Boolean contains(String key)
    {
        return repo.contains(key);
    }

    public String getString(String s)
    {
        try {
            return repo.getString(s, "");
        } catch (Exception e) {e.printStackTrace();return "";}
    }

    public Integer getInteger(String s)
    {
        try {
            return repo.getInt(s, 0);
        } catch (Exception e) {e.printStackTrace();return 0;}
    }

    public Boolean getBoolean(String s)
    {
        try {
            return repo.getBoolean(s, false);
        } catch (Exception e) {e.printStackTrace();return false;}
    }

    public void putInteger(String k,Integer v) {
        SharedPreferences.Editor editor = repo.edit();
        editor.putInt(k, v);
        editor.commit();
    }

    public void putString(String k,String v) {
        SharedPreferences.Editor editor = repo.edit();
        editor.putString(k, v);
        editor.commit();
    }

    public void putBoolean(String k,Boolean v) {
        SharedPreferences.Editor editor = repo.edit();
        editor.putBoolean(k, v);
        editor.commit();
    }
}
