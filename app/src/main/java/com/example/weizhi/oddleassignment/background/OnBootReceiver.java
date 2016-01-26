package com.example.weizhi.oddleassignment.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.example.weizhi.oddleassignment.R;

/**
 * A BroadcastReceiver to start an alarm to query weather information upon booting.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class OnBootReceiver extends BroadcastReceiver {
    private final String TAG = "OnBootReceiver";
    private static final boolean DEBUG = false;

    public OnBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set an alarm to update weather information once every 30 minute (roughly) upon booting.
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent myIntent = new Intent(context, CheckWeatherIntentService.class);
            myIntent.setAction(context.getString(R.string.intent_action_batch_string));
            PendingIntent pi = PendingIntent.getService(context, 1, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                    5000, AlarmManager.INTERVAL_HALF_HOUR , pi);
            if(DEBUG) Log.d(TAG, "Received boot complete. Started alarmmanager.");
        }
    }
}