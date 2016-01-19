package com.example.weizhi.oddleassignment.weatherdisplay;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.background.CheckWeatherIntentService;
import com.example.weizhi.oddleassignment.background.OnBootReceiver;
import com.example.weizhi.oddleassignment.citysearch.CitySearchActivity;

/**
 * An Activity for displaying weather information.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class WeatherDisplayActivity extends AppCompatActivity implements WeatherDisplayFragment.FragmentActivityInterface {
    private static final String TAG = "'WeatherDisplayActivity";
    public static final int ADD_CITY_REQUEST_CODE = 1;

    private WeatherDisplayFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather_display);

        Intent intent = getIntent();
        String city = intent.getStringExtra(getString(R.string.intent_add_new_city_string));
        String state = intent.getStringExtra(getString(R.string.intent_add_new_state_string));

        if(city == null){
            // Add fragment with no arguments
            mFragment = new WeatherDisplayFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_display_fragment_container, mFragment).commit();
        }
        else{
            // Add fragment with arguments
            mFragment = WeatherDisplayFragment.newInstance(this, city, state);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_display_fragment_container, mFragment).commit();
        }
    }

    public void onAddPress(View view){
        Intent addCityIntent = new Intent(this, CitySearchActivity.class);
        addCityIntent.putExtra(getString(R.string.intent_launched_by_activity_bool), true);
        startActivityForResult(addCityIntent, ADD_CITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADD_CITY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                String city = data.getStringExtra(getString(R.string.intent_add_new_city_string));
                String state = data.getStringExtra(getString(R.string.intent_add_new_state_string));
                mFragment.addCity(city, state);
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();

        ComponentName receiver = new ComponentName(this, OnBootReceiver.class);
        PackageManager pm = getPackageManager();

        if(pm.getComponentEnabledSetting(receiver) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED){
            // receiver is initially disabled. When user launch this app for the first time, we
            // will enable receiver forever.
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Intent intent;
            intent = new Intent(this, CheckWeatherIntentService.class);
            intent.setAction(getString(R.string.intent_action_batch_string));
            PendingIntent pi = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                    5000, AlarmManager.INTERVAL_HALF_HOUR, pi);
        }
    }
}
