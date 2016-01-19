package com.example.weizhi.oddleassignment.citysearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.weatherdisplay.WeatherDisplayActivity;

/**
 * Activity to search for cities.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CitySearchActivity extends AppCompatActivity implements CitySearchFragment.FragmentActivityInterface {
    private final String TAG = "CitySearchActivity";

    private boolean hasCity;
    private boolean isLaunchedByActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);

        SharedPreferences saveFile = getSharedPreferences(getString(R.string.preference_save_file), MODE_PRIVATE);
        hasCity = saveFile.getBoolean(getString(R.string.preference_has_city_bool), false);

        Intent mIntent = getIntent();
        isLaunchedByActivity = mIntent.getBooleanExtra(getString(R.string.intent_launched_by_activity_bool), false);

        if(!isLaunchedByActivity){
            // This activity is NOT launched by WeatherDisplayActivity
            if(hasCity){
                // We are launched from home screen and we have saved city. Finish this activity
                // and go to WeatherDisplayActivity.
                finish();
                Intent startWeatherDisplayIntent = new Intent(this, WeatherDisplayActivity.class);
                startActivity(startWeatherDisplayIntent);
            }
            else{
                // We are launched by home screen and we does not have saved city.
                // Remove close button and continue.
                ImageButton closeButton = (ImageButton)findViewById(R.id.close_button);
                if(closeButton != null)
                    closeButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!isOnline()){
            Toast toast = Toast.makeText(this,"No Connection", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void onClosePressed(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCitySelected(String city, String state) {
        if(isLaunchedByActivity){
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.intent_add_new_city_string), city);
            intent.putExtra(getString(R.string.intent_add_new_state_string), state);

            setResult(RESULT_OK, intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, WeatherDisplayActivity.class);
            intent.putExtra(getString(R.string.intent_add_new_city_string), city);
            intent.putExtra(getString(R.string.intent_add_new_state_string), state);

            finish();
            startActivity(intent);
        }
    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
