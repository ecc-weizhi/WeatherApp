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
import com.example.weizhi.oddleassignment.network.WundergroundAPICallManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CheckWeatherIntentService extends IntentService {
    private static final String TAG = "'IntentService";

    // Result code for request
    public static final int REQUEST_SUCCESS = 1;
    public static final int REQUEST_FAIL_NO_NETWORK = 2;
    public static final int REQUEST_FAIL_UNKNOWN = 3;

    public final ArrayList<String> goodWeather;
    public final ArrayList<String> badWeather;

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

    public static void startAutoComplete(Context context, String queryText) {
        Intent intent = new Intent(context, CheckWeatherIntentService.class);
        intent.setAction(context.getString(R.string.intent_action_autocomplete_string));
        intent.putExtra(context.getString(R.string.intent_autocomplete_param_string), queryText);
        context.startService(intent);
    }

    public static void startCheckWeather(Context context, String state, String city) {
        Intent intent = new Intent(context, CheckWeatherIntentService.class);
        intent.setAction(context.getString(R.string.intent_action_hourly_string));
        intent.putExtra(context.getString(R.string.intent_state_query_param_string), state);
        intent.putExtra(context.getString(R.string.intent_city_query_param_string), city);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (action.equals(getString(R.string.intent_action_autocomplete_string))) {
                final String queryText = intent.getStringExtra(getString(R.string.intent_autocomplete_param_string));

                Intent autoCompleteResultIntent;
                if(!isOnline()){
                    // No connection
                    autoCompleteResultIntent =
                            new Intent(getString(R.string.intent_action_broadcast_autocomplete_string))
                                    .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_FAIL_NO_NETWORK)
                                    .putExtra(getString(R.string.intent_autocomplete_param_string), queryText);
                }
                else{
                    // query server and retrieve a list of cities.
                    ArrayList<String> mCityList = WundergroundAPICallManager.requestAutoComplete(queryText);
                    if(mCityList == null)
                        autoCompleteResultIntent =
                                new Intent(getString(R.string.intent_action_broadcast_autocomplete_string))
                                    .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_FAIL_UNKNOWN)
                                    .putExtra(getString(R.string.intent_autocomplete_param_string), queryText);
                    else
                        autoCompleteResultIntent =
                                new Intent(getString(R.string.intent_action_broadcast_autocomplete_string))
                                    .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_SUCCESS)
                                    .putStringArrayListExtra(getString(R.string.intent_result_autocomplete_stringarray), mCityList)
                                    .putExtra(getString(R.string.intent_autocomplete_param_string), queryText);
                }

                LocalBroadcastManager.getInstance(this).sendBroadcast(autoCompleteResultIntent);
            } else if (action.equals(getString(R.string.intent_action_hourly_string))) {
                final String state = intent.getStringExtra(getString(R.string.intent_state_query_param_string));
                final String city = intent.getStringExtra(getString(R.string.intent_city_query_param_string));

                Intent weatherResultIntent;
                if(!isOnline()){
                    // No connection
                    weatherResultIntent =
                            new Intent(getString(R.string.intent_action_broadcast_weather_string))
                                    .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_FAIL_NO_NETWORK)
                                    .putExtra(getString(R.string.intent_state_query_param_string), state)
                                    .putExtra(getString(R.string.intent_city_query_param_string), city);
                }
                else{
                    // Query server for weather information of a single city
                    Weather weather = WundergroundAPICallManager.requestWeather(state, city);
                    if(weather == null)
                        weatherResultIntent =
                                new Intent(getString(R.string.intent_action_broadcast_weather_string))
                                        .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_FAIL_UNKNOWN)
                                        .putExtra(getString(R.string.intent_state_query_param_string), state)
                                        .putExtra(getString(R.string.intent_city_query_param_string), city);
                    else
                        weatherResultIntent =
                                new Intent(getString(R.string.intent_action_broadcast_weather_string))
                                        .putExtra(getString(R.string.intent_intentservice_request_result_int), REQUEST_SUCCESS)
                                        .putExtra(getString(R.string.intent_result_hour_int), weather.hour)
                                        .putExtra(getString(R.string.intent_result_celsius_int), weather.tempCelsius)
                                        .putExtra(getString(R.string.intent_result_fahrenheit_int), weather.tempFahrenheit)
                                        .putExtra(getString(R.string.intent_result_condition_string), weather.condition)
                                        .putExtra(getString(R.string.intent_result_icon_string), weather.icon)
                                        .putExtra(getString(R.string.intent_state_query_param_string), state)
                                        .putExtra(getString(R.string.intent_city_query_param_string), city);
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(weatherResultIntent);
            } else if (action.equals(getString(R.string.intent_action_batch_string))) {
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
                        Weather w = WundergroundAPICallManager.requestWeather(s[1], s[0]);
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

                        NotificationCompat.Builder mBuilder = getNotificationBuilder("Weather report", "Bad weather");
                        mBuilder.setStyle(inboxStyle);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, mBuilder.build());
                    }

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

        if(goodWeather.contains(initial))
            return true;
        else
            return false;
    }

    private NotificationCompat.Builder getNotificationBuilder(String title, String content){
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

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(notifyIntent);

        return mBuilder;
    }
}
