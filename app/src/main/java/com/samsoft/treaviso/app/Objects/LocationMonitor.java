package com.samsoft.treaviso.app.Objects;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.samsoft.treaviso.app.MapActivity;
import com.samsoft.treaviso.app.R;

import java.io.File;

/**
 * Created by sam on 24/04/15.
 */
public class LocationMonitor extends BroadcastReceiver {
    public static final String LOCATION_MONITOR_ACTION = "com.samsoft.treaviso.ENTER_AREA";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        settingRep setting = new settingRep(context.getApplicationContext());
        /*setting.putBoolean(MapActivity.RUNNING_ID,false);
        makeNotification(context);
        context.unregisterReceiver(this);*/

        Bundle datos = intent.getExtras();
        if (datos != null) {
            if (datos.containsKey(LocationManager.KEY_PROXIMITY_ENTERING)) {
                boolean status = datos.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING);
                if (status) {
                    context.unregisterReceiver(this);
                    //settingRep setting = new settingRep(context.getApplicationContext());
                    setting.putBoolean(MapActivity.RUNNING_ID,false);
                    makeNotification(context);
                }

            }
        }
    }

    public void makeNotification(Context c)
    {
        Intent main = new Intent(c, MapActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri;
        NotificationManager notificationManager = (NotificationManager) c.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //if (path.trim().isEmpty())
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //else {
        //    File ringFile = new File(path);
        //    soundUri = Uri.fromFile(ringFile);
        //}

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c.getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.appicon);
        mBuilder.setContentTitle("Llegando a destino ");
        mBuilder.setContentText("");
        mBuilder.setSound(soundUri);
        long[] vibrate = {0,500,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500,2000,500,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500};
        mBuilder.setVibrate(vibrate);
        mBuilder.setContentIntent(pendingIntent);
        Notification n = mBuilder.getNotification();
        notificationManager.notify(10, n);
    }
}
