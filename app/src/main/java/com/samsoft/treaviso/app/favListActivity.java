package com.samsoft.treaviso.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.samsoft.treaviso.app.Fragments.favoriteList;

import org.json.JSONObject;

/**
 * Created by sam on 29/04/15.
 */
public class favListActivity extends ActionBarActivity implements favoriteList.favoriteListListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favlist);
    }

    public void onFavoriteClick(JSONObject id)
    {

    }
}
