package com.samsoft.treaviso.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import com.samsoft.treaviso.app.Objects.settingRep;


public class MapActivity extends ActionBarActivity {
    public static final String RUNNING_ID = "RUNNING";
    private mapViewer mapF;
    private boolean mIsRunning = false;
    private static LocationMonitor lm = new LocationMonitor();
    private TextView mtxt;
    private settingRep settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mtxt = (TextView) findViewById(R.id.textView);
        settings = new settingRep(getApplicationContext());
        boolean hayDetalle =  (getSupportFragmentManager().findFragmentById(R.id.fragment) != null);
        if (hayDetalle) mapF = ((mapViewer)getSupportFragmentManager().findFragmentById(R.id.fragment));

        if (settings.contains(RUNNING_ID) != null) {
            mIsRunning = settings.getBoolean(RUNNING_ID);
        } else mIsRunning = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RUNNING_ID,mIsRunning);
        settings.putBoolean(RUNNING_ID,mIsRunning);
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

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("MapActivity", "onStart() event");
        if (mIsRunning) mtxt.setText(R.string.stop_text);
        else mtxt.setText(R.string.start_text);
    }

    @Override
    public void onPause()
    {
        Log.d("MapActivity", "onPause() event");
        super.onPause();
        if (mIsRunning) {
            unregisterReceiver(alert);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        settings = new settingRep(getApplicationContext());
        if (settings.contains(RUNNING_ID) != null) {
            mIsRunning = settings.getBoolean(RUNNING_ID);
        } else mIsRunning = false;
        Log.d("MapActivity", "onResumen() event " + mIsRunning);

        if (mIsRunning) mtxt.setText(R.string.stop_text);
        else mtxt.setText(R.string.start_text);
        if (mIsRunning) registerReceiver(alert,new IntentFilter(LocationMonitor.LOCATION_MONITOR_ACTION));
    }

    @Override
    public void onDestroy()
    {
        Log.d("MapActivity", "onDestroy() event");
        super.onDestroy();
        if (mIsRunning) {
            unregisterReceiver(alert);
            //unregisterReceiver(lm);
        }
        settings.putBoolean(RUNNING_ID,false);
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
                    registerReceiver(alert,new IntentFilter(LocationMonitor.LOCATION_MONITOR_ACTION));
                    mIsRunning = true;
                    mtxt.setText(R.string.stop_text);
                }
            }
        } else {
            mIsRunning = false;
            mtxt.setText(R.string.start_text);
            try {
                unregisterReceiver(lm);
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    private  BroadcastReceiver alert = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsRunning = false;
            mtxt.setText(R.string.start_text);
            unregisterReceiver(alert);
        }
    };
}
