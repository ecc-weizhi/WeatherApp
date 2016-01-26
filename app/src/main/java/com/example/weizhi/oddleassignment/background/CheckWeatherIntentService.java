package com.example.weizhi.oddleassignment.background;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.citysearch.CitySearchActivity;
import com.example.weizhi.oddleassignment.model.Weather;
import com.example.weizhi.oddleassignment.network.HourlyApiRequest;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An IntentService to pull weather information in background.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CheckWeatherIntentService extends IntentService {
    private static final String TAG = "IntentService";
    private static final boolean DEBUG = false;

    // Result code for request
    public static final int REQUEST_SUCCESS = 1;
    public static final int REQUEST_FAIL_NO_NETWORK = 2;
    public static final int REQUEST_FAIL_UNKNOWN = 3;

    private final ArrayList<String> goodWeather;
    private final ArrayList<String> badWeather;

    public CheckWeatherIntentService() {
        super("CheckWeatherIntentService");

        // Initialize a set of good and bad weathers
        String[] goodWeatherArray = {"clear","mostlysunny","partlysunny","sunny",
                "cloudy","partlycloudy","fog","hazy","mostlycloudy"};
        String[] badWeatherArray = {"flurries","snow","chancesnow","chanceflurries",
                "sleet","chancesleet","rain","chancerain","tstorms","chancetstorms"};
        goodWeather =
                new ArrayList<String>(Arrays.asList(goodWeatherArray));
        badWeather =
                new ArrayList<String>(Arrays.asList(badWeatherArray));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (action.equals(getString(R.string.intent_action_batch_string))) {
                if(DEBUG) Log.d(TAG, "received batch intent.");

                Intent weatherResultIntent;
                if(!isOnline()){
                    // No connection
                    weatherResultIntent =
                            new Intent(getString(R.string.intent_action_broadcast_batch_string))
                                    .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_FAIL_NO_NETWORK);
                }
                else{
                    // Obtain a list of cities that we are monitoring.
                    SharedPreferences saveFile = getSharedPreferences(getString(R.string.preference_save_file), Context.MODE_PRIVATE);
                    int count = saveFile.getInt(getString(R.string.preference_city_count_int), 0);
                    String[] keyList = new String[count];
                    for(int i=0; i<count; i++){
                        keyList[i] = saveFile.getString(String.valueOf(i), null);
                    }

                    // Setup for notification. We construct an ArrayList to store cities which has weather that deteriorates.
                    NotificationCompat.InboxStyle inboxStyle =
                            new NotificationCompat.InboxStyle();
                    ArrayList<String> events = new ArrayList<>();
                    inboxStyle.setBigContentTitle("Bad weather report");

                    SharedPreferences.Editor editor = saveFile.edit();
                    for(String key: keyList){
                        // Loop through every city that we are monitoring and query their weather info.
                        String[] s = key.split(", ");
                        HourlyApiRequest request = new HourlyApiRequest(s[1], s[0]);
                        Weather w = null;

                        try {
                            w = request.loadDataFromNetwork();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(w!=null){
                            editor.putString(key+getString(R.string.preference_condition_string), w.condition);
                            editor.putInt(key + getString(R.string.preference_celsius_int), w.tempCelsius);
                            editor.putInt(key+getString(R.string.preference_fahrenheit_int), w.tempFahrenheit);
                            editor.putInt(key+getString(R.string.preference_hour_int), w.hour);

                            // We have obtained an updated weather info. Check to see if weather has deteriorate.
                            // Add it to our ArrayList if weather deteriorates.
                            String initial = saveFile.getString(key+getString(R.string.preference_icon_string), "sunny");
                            String future = w.icon;
                            if(isDeteriorate(initial, future)) {
                                if(events.size()<5)
                                    events.add(key+": "+w.condition);
                            }

                            editor.putString(key+getString(R.string.preference_icon_string), w.icon);

                            if(DEBUG) Log.d(TAG, w.toKey()+" updated");
                        }
                    }
                    editor.commit();

                    // Limits the number events shown to 5 and fire off the notification.
                    if(events.size()>0){
                        for(String s: events){
                            inboxStyle.addLine(s);
                        }
                        if(events.size() == 5)
                            inboxStyle.addLine("...");

                        NotificationCompat.Builder mBuilder = getNotificationBuilder();
                        mBuilder.setStyle(inboxStyle);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, mBuilder.build());
                    }

                    if(DEBUG) Log.d(TAG, "batch update completed. sending broadcast...");
                    weatherResultIntent =
                            new Intent(getString(R.string.intent_action_broadcast_batch_string))
                                    .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_SUCCESS);
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(weatherResultIntent);
            }
        }
    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isDeteriorate(String initial, String future){
        if(!badWeather.contains(future))
            return false;

        return goodWeather.contains(initial);
    }

    private NotificationCompat.Builder getNotificationBuilder(){
        // Creates an Intent for the Activity
        Intent intent = new Intent(this, CitySearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Creates the PendingIntent
        PendingIntent notifyIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Weather report")
                .setContentText("Bad weather")
                .setContentIntent(notifyIntent);
    }
}
