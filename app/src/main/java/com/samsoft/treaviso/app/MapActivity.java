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
import android.widget.Toast;

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
    private Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
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
        mMenu = menu;
        if (mIsRunning) {
            mMenu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_stop);
            mMenu.findItem(R.id.act_service).setTitle(R.string.stop_text);
        } else {
            mMenu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_play);
            mMenu.findItem(R.id.act_service).setTitle(R.string.start_text);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.act_service) monitorClick();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("MapActivity", "onStart() event");
        if (settings.contains(RUNNING_ID) != null) {
            mIsRunning = settings.getBoolean(RUNNING_ID);
        } else mIsRunning = false;

        if (mIsRunning) {
            registerReceiver(alert, new IntentFilter(LocationMonitor.LOCATION_MONITOR_ACTION));
        }
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
        Log.d("MapActivity", "onResumen() event ");
    }

    @Override
    public void onDestroy()
    {
        Log.d("MapActivity", "onDestroy() event");
        super.onDestroy();
        if (mIsRunning) {
            try {
                unregisterReceiver(alert);
                unregisterReceiver(lm);
            } catch (Exception e) { e.printStackTrace();}
        }
        settings.putBoolean(RUNNING_ID,false);
    }

    public void monitorClick()
    {
        Log.d("MapActivity","monitorClick");
        if (!mIsRunning) {
            if (mapF != null) {
                Bundle b = mapF.getInfo();
                if (b.containsKey(mapViewer.CLICKPOSITION_LAT_ID)) {
                    double lat = b.getDouble(mapViewer.CLICKPOSITION_LAT_ID);
                    double lng = b.getDouble(mapViewer.CLICKPOSITION_LNG_ID);
                    Integer radius = b.getInt(mapViewer.RADIUS_ID);

                    Intent i = new Intent(LocationMonitor.LOCATION_MONITOR_ACTION);
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
                    LocationManager LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    LM.addProximityAlert(lat,lng,radius.floatValue(),-1L,pi);

                    /*AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(LocationMonitor.LOCATION_MONITOR_ACTION);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 1000, alarmIntent);*/

                    registerReceiver(lm,new IntentFilter(LocationMonitor.LOCATION_MONITOR_ACTION));
                    registerReceiver(alert,new IntentFilter(LocationMonitor.LOCATION_MONITOR_ACTION));
                    mIsRunning = true;
                    mMenu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_stop);
                    mMenu.findItem(R.id.act_service).setTitle(R.string.stop_text);
                } else {
                    Toast.makeText(getApplicationContext(),R.string.noPoint,Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            mIsRunning = false;
            mMenu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_play);
            mMenu.findItem(R.id.act_service).setTitle(R.string.start_text);
            try {
                unregisterReceiver(lm);
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    private  BroadcastReceiver alert = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsRunning = false;
            mMenu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_play);
            mMenu.findItem(R.id.act_service).setTitle(R.string.start_text);
            unregisterReceiver(alert);
        }
    };
}
