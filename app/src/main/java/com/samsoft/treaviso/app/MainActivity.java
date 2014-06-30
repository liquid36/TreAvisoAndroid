package com.samsoft.treaviso.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.samsoft.treaviso.background.LocationUpdateService;
import com.samsoft.treaviso.background.data.Alarm;
import com.samsoft.treaviso.background.data.DAOFactory;
import com.samsoft.treaviso.background.data.LocationDAO;

public class MainActivity extends ActionBarActivity {
    private LayoutInflater inflater;
    private LocationDAO dao;
    private boolean menuMode;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menuMode = false;

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dao = DAOFactory.createLocationDAO(getApplicationContext());
        RefreshScreen(false);

    }

    public void RefreshScreen(boolean delMode)
    {
        LinearLayout la = (LinearLayout) findViewById(R.id.ListAlarm);
        //la.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        la.removeAllViews();
        Alarm[] als = dao.getAllAlarm();
        if (als.length == 0) {
            TextView t = new TextView(getApplicationContext());
            Button b = new Button(getApplicationContext());
            t.setText("Ningun aviso creado\nPresione en Añadir para comenzar");
            t.setTextSize(22);
            t.setTextColor(Color.BLUE);
            b.setText("Añadir Aviso");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, AlarmFrame.class);
                    i.putExtra("accion",0);
                    startActivityForResult(i, 1);
                }
            });
            la.addView(t);
            la.addView(b);
        }
        for (int i = 0; i < als.length; ++i) {
            la.addView(CreateView(als[i],delMode));
        }
    }

    public void startClick()
    {
        if (getStat()) stopGeo();
        else startGeo();
    }

    public void startGeo()
    {
        makeToast("Starting system");
        menu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_stop);
        Intent updateServiceIntent = new Intent(this, LocationUpdateService.class);
        updateServiceIntent.putExtra("stationaryRadius", "50");
        updateServiceIntent.putExtra("desiredAccuracy", "10");
        updateServiceIntent.putExtra("distanceFilter", "50");
        updateServiceIntent.putExtra("locationTimeout", "60");
        updateServiceIntent.putExtra("isDebugging", "true");
        this.startService(updateServiceIntent);
        saveStat(true);
    }

    public void stopGeo()
    {
        makeToast("Stopping system");
        menu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_play);
        Intent updateServiceIntent = new Intent(this, LocationUpdateService.class);
        this.stopService(updateServiceIntent);
        saveStat(false);
    }

    public View CreateView(Alarm a,boolean delMode)
    {
        View v = inflater.inflate(R.layout.arow, null);
        TextView t = (TextView) v.findViewById(R.id.txtName);
        CheckBox c = (CheckBox) v.findViewById(R.id.cbActive);
        ToggleButton tb = (ToggleButton) v.findViewById(R.id.tbactive);
        c.setTag(a);
        t.setTag(a);
        tb.setTag(a);
        t.setText(a.getName());

        v.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Color.CYAN);
                        break;
                    case MotionEvent.ACTION_UP:
                        //set color back to default
                        v.setBackgroundColor(Color.WHITE);
                        break;
                }
                return false;
            }
        });

        if (!delMode){
            tb.setChecked(a.getActive() != 0 ? true : false);
            c.setVisibility(View.GONE);
        } else {
            c.setChecked(false);
            tb.setVisibility(View.GONE);
        }



        if (!delMode) {
            tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    Alarm a = (Alarm) buttonView.getTag();
                    a.setActive(isChecked? 1 : 0);
                    dao.updateAlarm(a);
                }
            });


            v.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View d) {
                    Alarm a = (Alarm) d.findViewById(R.id.cbActive).getTag();
                    Intent i = new Intent(MainActivity.this, AlarmFrame.class);
                    i.putExtra("accion",a.getId());
                    startActivityForResult(i, 1);
                    return true;
                    }
            });
        }
        return v;
    }

    public void makeToast(String s) {
        Context context = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return;
    }


    public boolean getStat()
    {
        SharedPreferences settings = getSharedPreferences("TreAviso", 0);
        return settings.getBoolean("serviceStat", false);
    }

    public void saveStat(boolean b)
    {
        SharedPreferences settings = getSharedPreferences("TreAviso", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("serviceStat", b);
        editor.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    RefreshScreen(false);
                }
                break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        if (getStat()) menu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_stop);
        else menu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_play);
        return true;
    }

    public Menu mm;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        mm = menu;
        menu.clear();
        if (menuMode)
            getMenuInflater().inflate(R.menu.menu_delete,menu);
        else
        {
            getMenuInflater().inflate(R.menu.main, menu);
            if (getStat()) menu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_stop);
            else menu.findItem(R.id.act_service).setIcon(R.drawable.ic_action_play);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.act_service:
                startClick();
                return true;
            case R.id.action_settings :
                return true;
            case R.id.act_del:
                menuMode = true;
                RefreshScreen(true);
                onPrepareOptionsMenu(mm);
                break;
            case R.id.act_back:
                menuMode = false;
                RefreshScreen(false);
                onPrepareOptionsMenu(mm);
                break;
            case R.id.act_do:
                LinearLayout la = (LinearLayout) findViewById(R.id.ListAlarm);
                for(int i = 0; i < la.getChildCount() ;i++) {
                    RelativeLayout row = (RelativeLayout) la.getChildAt(i);
                    CheckBox c = (CheckBox) row.findViewById(R.id.cbActive);
                    if (c.isChecked()) {
                        Alarm a = (Alarm) c.getTag();
                        dao.deleteAlarm(a);
                    }
                }
                menuMode = false;
                RefreshScreen(false);
                onPrepareOptionsMenu(mm);
                break;
            case R.id.act_add:
                Intent i = new Intent(MainActivity.this, AlarmFrame.class);
                i.putExtra("accion",0);
                startActivityForResult(i, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
