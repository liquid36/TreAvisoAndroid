package com.samsoft.treaviso.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
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

import com.samsoft.treaviso.app.Dialogs.InputDialog;
import com.samsoft.treaviso.app.Fragments.mapViewer;
import com.samsoft.treaviso.app.Objects.DataBase;
import com.samsoft.treaviso.app.Objects.LocationMonitor;
import com.samsoft.treaviso.app.Objects.settingRep;

import org.json.JSONObject;


public class MapActivity extends ActionBarActivity {
    public static final String RUNNING_ID = "RUNNING";
    public static final String FROM_ID = "FROM_FAV";
    private  mapViewer mapF;
    private boolean mIsRunning = false;
    private static LocationMonitor lm = new LocationMonitor();
    private TextView mtxt;
    private settingRep settings;
    private Menu mMenu;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        settings = new settingRep(getApplicationContext());
        boolean hayDetalle =  (getSupportFragmentManager().findFragmentById(R.id.fragment) != null);

        mapF = ((mapViewer)getSupportFragmentManager().findFragmentById(R.id.fragment));

        if (settings.contains(RUNNING_ID) != null) {
            mIsRunning = settings.getBoolean(RUNNING_ID);
        } else mIsRunning = false;
        db = new DataBase(getApplicationContext());

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey(FROM_ID)) {
            try {
                JSONObject o = new JSONObject(b.getString(FROM_ID));
                mapF.setPoint(o.getDouble("lat"),o.getDouble("lng"),o.getInt("radius"));
            } catch (Exception e) { e.printStackTrace();}

        }
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

    public  Boolean isMarker()
    {
        Bundle info = mapF.getInfo();
        if (info.containsKey(mapViewer.CLICKPOSITION_LAT_ID)) return true;
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.act_service) monitorClick();
        if (id == R.id.act_add) {
            if (isMarker()) {
                final mapViewer frag = mapF;
                InputDialog dialog = new InputDialog(this, getString(R.string.txt_add_favorite), getString(R.string.txt_descripcion_input),
                        new InputDialog.inputDialogListener() {
                            @Override
                            public void onAcceptClick(String txt) {
                                if (txt.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.txt_empty_txt), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Bundle info = frag.getInfo();
                                Double lat = info.getDouble(mapViewer.CLICKPOSITION_LAT_ID);
                                Double lng = info.getDouble(mapViewer.CLICKPOSITION_LNG_ID);
                                Integer radius = info.getInt(mapViewer.RADIUS_ID);
                                db.addFavorito(txt,lat,lng,radius);
                            }

                            @Override
                            public void onCancelClick() {

                            }
                        });
                dialog.show();
            } else {
                Toast.makeText(getApplicationContext(),getString(R.string.txt_no_click_yet),Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.action_favoritos)
        {
            startActivity(new Intent(this,favListActivity.class));
        }
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
        NotificationManager nM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nM.cancelAll();
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
