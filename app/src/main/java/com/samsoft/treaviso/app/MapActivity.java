package com.samsoft.treaviso.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.samsoft.treaviso.app.Fragments.mapViewer;
import com.samsoft.treaviso.app.Objects.LocationMonitor;


public class MapActivity extends ActionBarActivity {
    protected static final String RUNNING_ID = "RUNNING";
    private mapViewer mapF;
    private boolean mIsRunning = false;
    private static LocationMonitor lm = new LocationMonitor();
    private TextView mtxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mtxt = (TextView) findViewById(R.id.textView);

        boolean hayDetalle =  (getSupportFragmentManager().findFragmentById(R.id.fragment) != null);
        if (hayDetalle) mapF = ((mapViewer)getSupportFragmentManager().findFragmentById(R.id.fragment));

        if (savedInstanceState != null) {
            mIsRunning = savedInstanceState.getBoolean(RUNNING_ID);
        }
        if (mIsRunning) mtxt.setText(R.string.stop_text);
        else mtxt.setText(R.string.start_text);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RUNNING_ID,mIsRunning);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void monitorClick(View v)
    {
        Log.d("MapActivity","monitorClick");
        if (!mIsRunning) {
            if (mapF != null) {
                Bundle b = mapF.getInfo();
                if (b.containsKey(mapViewer.CLICKPOSITION_LAT_ID)) {
                    double lat = b.getDouble(mapViewer.CLICKPOSITION_LAT_ID);
                    double lng = b.getDouble(mapViewer.CLICKPOSITION_LNG_ID);
                    Integer radius = b.getInt(mapViewer.RADIUS_ID);
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(LocationMonitor.LOCATION_MONITOR_ACTION), 0);
                    LocationManager LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    //LM.addProximityAlert(lat,lng,radius.floatValue(),-1L,pi);

                    AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(LocationMonitor.LOCATION_MONITOR_ACTION);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 1000, alarmIntent);

                    registerReceiver(lm,new IntentFilter(LocationMonitor.LOCATION_MONITOR_ACTION));
                    mIsRunning = true;
                    mtxt.setText(R.string.stop_text);
                }
            }
        } else {
            mIsRunning = false;
            unregisterReceiver(lm);
            mtxt.setText(R.string.start_text);
        }
    }
}
