package com.samsoft.treaviso.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.mburman.fileexplore.FileExplore;
import com.samsoft.treaviso.background.data.Alarm;
import com.samsoft.treaviso.background.data.DAOFactory;
import com.samsoft.treaviso.background.data.LocationDAO;
import android.util.Log;
import android.widget.Toast;

public class AlarmFrame extends ActionBarActivity {

    private long editID;
    private Alarm editA;
    private LocationDAO db;
    EditText name;
    EditText metros;
    EditText path;
    EditText lat;
    EditText lon;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_frame);
        name = (EditText) findViewById(R.id.txtName);
        metros = (EditText) findViewById(R.id.txtMetros);
        path = (EditText) findViewById(R.id.txtPath);
        lat = (EditText) findViewById(R.id.txtLat);
        lon = (EditText) findViewById(R.id.txtLong);

        Bundle i = getIntent().getExtras();
        editID = i.getLong("accion", 0);
        db = DAOFactory.createLocationDAO(getApplicationContext());

        editA = db.getAlarm(editID);
        if (editA != null) {
            name.setText(editA.getName());
            metros.setText(Integer.toString(editA.getMetros()));
            path.setText(editA.getPath());
            lat.setText(editA.getLatitude());
            lon.setText(editA.getLongitude());
            setTitle("Editar Alarma");
        } else {
            setTitle("Nueva Alarma");
            editA = new Alarm();
            editA.setActive(1);
        }

        path.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(AlarmFrame.this, FileExplore.class);
                startActivityForResult(i, 2);
                return true;
            }
        });

        path.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus) makeToast("Long press for file explorer");
            }

        });
        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToast("Long press for file explorer");
            }
        });

    }

    public void setTitle(String t)
    {
        if (android.os.Build.VERSION.SDK_INT >= 11)
            getActionBar().setTitle(t);
        else
            getSupportActionBar().setTitle(t);
    }



    public void makeToast(String s) {
        Context context = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return;
    }

    public boolean checkData()
    {
        if (lon.getText().toString().trim().isEmpty() || lat.getText().toString().trim().isEmpty() ) {
            makeToast("Falta elegir coordenadas");
            return false;
        }
        return true;
    }

    public void saveClick()
    {
        if (name.getText().toString().trim().isEmpty()) editA.setName("[unNamed]");
        else editA.setName(name.getText().toString());

        editA.setPath(path.getText().toString());
        editA.setLongitude(lon.getText().toString());
        editA.setLatitude(lat.getText().toString());

        try {
            editA.setMetros(Integer.parseInt(metros.getText().toString()));
        } catch (Exception e){ editA.setMetros(400); }

        if (editA.getId() > 0)
            db.updateAlarm(editA);
        else
            db.persistAlarm(editA);
        Intent resultIntent = new Intent( AlarmFrame.this , AlarmFrame.class);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1): {
                if (resultCode == Activity.RESULT_OK) {
                    lat = (EditText) findViewById(R.id.txtLat);
                    lon = (EditText) findViewById(R.id.txtLong);
                    double dlat = data.getDoubleExtra("lat", 0);
                    double dlon = data.getDoubleExtra("lon", 0);
                    lat.setText(Double.toString(dlat));
                    lon.setText(Double.toString(dlon));
                }
                break;
            }
            case 2: {
                if (resultCode == Activity.RESULT_OK) {
                    path.setText(data.getStringExtra("path"));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alarm_frame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.act_back:
                Intent resultIntent = new Intent( AlarmFrame.this , AlarmFrame.class);
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            case R.id.act_do:
                if (checkData())
                    saveClick();
                return true;
            case R.id.act_search:
                Intent i = new Intent(AlarmFrame.this, MapViewer.class);
                try {
                    i.putExtra("Metros", Integer.parseInt(metros.getText().toString()));
                } catch (Exception e) { i.putExtra("Metros",400); }
                startActivityForResult(i, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    public void startMap(View m)
    {
        Intent i = new Intent(AlarmFrame.this, MapViewer.class);
        try {
            i.putExtra("Metros", Integer.parseInt(metros.getText().toString()));
        } catch (Exception e) { i.putExtra("Metros",400); }
        startActivityForResult(i, 1);
    }

}
