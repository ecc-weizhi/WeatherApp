package com.example.weizhi.oddleassignment.weatherdisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.background.CheckWeatherIntentService;
import com.example.weizhi.oddleassignment.background.MySpiceService;
import com.example.weizhi.oddleassignment.model.Weather;
import com.example.weizhi.oddleassignment.network.HourlyApiRequest;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A fragment for displaying weather info.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class WeatherDisplayFragment extends Fragment implements WeatherRecyclerViewAdapter.RecyclerFragmentInterface,
        View.OnLongClickListener {
    private final String TAG = "WeatherDisplayFragment";
    private static final boolean DEBUG = true;
    private final int THIRTY_MINUTES = 1000*60*30;

    private Weather mainWeather;
    private Hashtable<String, Weather> weatherTable;    // The source of info
    private ArrayList<Weather> weatherList;             // An ArrayList backed by weatherTable.
    private WeatherRecyclerViewAdapter mWeatherRecyclerViewAdapter;

    private FragmentActivityInterface mActivity;

    private RelativeLayout mMainWeatherLayout;
    private ImageView mMainWeatherImage;
    private TextView mMainWeatherCityText;
    private TextView mMainWeatherConditionText;
    private TextView mMainWeatherTempText;
    private ResponseReceiver mResponseReceiver;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private final SpiceManager spiceManager = new SpiceManager(MySpiceService.class);
    private final HourlyRequestListener mHourlyRequestListener = new HourlyRequestListener();

    private RelativeLayout getmMainWeatherLayout(){
        if(mMainWeatherLayout == null)
            mMainWeatherLayout = (RelativeLayout)getView().findViewById(R.id.weather_main_layout);
        return mMainWeatherLayout;
    }

    private ImageView getmMainWeatherImage(){
        if(mMainWeatherImage == null)
            mMainWeatherImage = (ImageView)getView().findViewById(R.id.weather_icon_image);
        return mMainWeatherImage;
    }

    private TextView getmMainWeatherCityText(){
        if(mMainWeatherCityText == null)
            mMainWeatherCityText = (TextView)getView().findViewById(R.id.weather_city_text);
        return mMainWeatherCityText;
    }

    private TextView getmMainWeatherConditionText(){
        if(mMainWeatherConditionText == null)
            mMainWeatherConditionText = (TextView)getView().findViewById(R.id.weather_condition_text);
        return mMainWeatherConditionText;
    }

    private TextView getmMainWeatherTempText(){
        if(mMainWeatherTempText == null)
            mMainWeatherTempText = (TextView)getView().findViewById(R.id.weather_temperature_text);
        return mMainWeatherTempText;
    }

    private ResponseReceiver getmResponseReceiver(){
        if(mResponseReceiver == null)
            mResponseReceiver = new ResponseReceiver();
        return mResponseReceiver;
    }

    private RecyclerView getmRecyclerView(){
        if(mRecyclerView == null)
            mRecyclerView = (RecyclerView)getView().findViewById(R.id.weather_small_recycler);
        return mRecyclerView;
    }

    private ProgressBar getmProgressBar(){
        if(mProgressBar == null)
            mProgressBar = (ProgressBar)getView().findViewById(R.id.loading_wheel);
        return mProgressBar;
    }

    public WeatherDisplayFragment() {
        // Required empty public constructor
    }

    public static WeatherDisplayFragment newInstance(Context context, String city, String state) {
        WeatherDisplayFragment fragment = new WeatherDisplayFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(context.getString(R.string.arg_add_new_city_string), city);
        args.putString(context.getString(R.string.arg_add_new_state_string), state);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve a list of cities key from local storage
        SharedPreferences saveFile = mActivity.getSharedPreferences(getString(R.string.preference_save_file), Context.MODE_PRIVATE);
        int count = saveFile.getInt(getString(R.string.preference_city_count_int), 0);
        String[] keyList = new String[count];
        for(int i=0; i<count; i++){
            keyList[i] = saveFile.getString(String.valueOf(i), null);
        }

        // Construct a hashtable to store cities weather information in memory.
        // This will be our source of info.
        weatherTable = new Hashtable<>();
        if(count > 0){
            for(String str: keyList){
                String[] s = str.split(", ");
                Weather w = new Weather(s[0], s[1],
                        saveFile.getString(str+getString(R.string.preference_condition_string), ""),
                        saveFile.getInt(str+getString(R.string.preference_celsius_int), 0),
                        saveFile.getInt(str+getString(R.string.preference_fahrenheit_int), 0),
                        saveFile.getInt(str+getString(R.string.preference_hour_int), 0),
                        saveFile.getString(str+getString(R.string.preference_icon_string), ""));
                weatherTable.put(w.toKey(), w);
            }
        }

        // Construct an ArrayList from our weatherTable. This will be the list of
        // weather info that our recycler view follows.
        weatherList = new ArrayList<>();
        for (Enumeration<Weather> e = weatherTable.elements(); e.hasMoreElements();){
            Weather w = e.nextElement();
            weatherList.add(w);
        }

        // Initialize our mainWeather. mainWeather is the weather info that is displayed
        // in large view in our fragment. We choose our mainWeather from weatherTable
        // and remove mainWeather from weatherList.
        mainWeather = null;
        if(savedInstanceState == null){
            // We don't have savedInstanceState. This means that we are freshly created. Check
            // arguments and show mainWeather base on arguments.
            String city = null;
            String state = null;
            if(getArguments()!= null) {
                city = getArguments().getString(getString(R.string.arg_add_new_city_string));
                state = getArguments().getString(getString(R.string.arg_add_new_state_string));
            }

            if(city != null){
                if(weatherTable.containsKey(city+", "+state)){
                    mainWeather = weatherList.get(0);
                    if(DEBUG) {
                        Log.d(TAG, "onCreate. We don't have savedInstanceState, have argument to " +
                                "add city but city is already added. Most likely android kill this" +
                                "fragment. City size: "
                                +weatherTable.size() +". Main weather: "+mainWeather.toKey());
                    }
                }
                else {
                    // We found city in arguments. Set argument city as mainWeather
                    mainWeather = new Weather(city, state);
                    weatherTable.put(mainWeather.toKey(), mainWeather);
                    weatherList.add(0, mainWeather);

                    if(DEBUG) {
                        Log.d(TAG, "onCreate. Launched from CitySearchActivity with city size: "
                                +weatherTable.size() +". Add Main weather: "+mainWeather.toKey());
                    }
                }
            }
            else{
                mainWeather = weatherList.get(0);
                if(DEBUG) {
                    Log.d(TAG, "onCreate. Launched from home with city size: "
                            +weatherTable.size() +". Main weather: "+mainWeather.toKey());
                }
            }
        }
        else{
            // We have savedInstanceState. This is a re-creation of fragment. Initialize mainWeather
            // using savedInstanceState.
            mainWeather = weatherList.get(0);
            if(DEBUG) Log.d(TAG, "onCreate. Recreate fragment. City size: "+weatherTable.size()+
                    ". Main weather: "+mainWeather.toKey());
        }

        // remove mainWeather from weatherList
        weatherList.remove(0);
        mWeatherRecyclerViewAdapter = new WeatherRecyclerViewAdapter(this, weatherList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather_display, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivityInterface) {
            mActivity = (FragmentActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentActivityInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
        updateMainWeather();
    }

    @Override
    public void onResume(){
        super.onResume();

        IntentFilter mIntentFilter = new IntentFilter();
        //mIntentFilter.addAction(getString(R.string.intent_action_broadcast_weather_string));
        mIntentFilter.addAction(getString(R.string.intent_action_broadcast_batch_string));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(getmResponseReceiver(), mIntentFilter);
        getmRecyclerView().setAdapter(mWeatherRecyclerViewAdapter);

        HourlyApiRequest request = new HourlyApiRequest(mainWeather.cityName, mainWeather.stateName);
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, request.createCacheKey(),
                THIRTY_MINUTES, mHourlyRequestListener);

        getmMainWeatherLayout().setVisibility(View.INVISIBLE);
        getmProgressBar().setVisibility(View.VISIBLE);
        getmMainWeatherLayout().setOnLongClickListener(this);

        for(Weather w: weatherList){
            request = new HourlyApiRequest(w.cityName, w.stateName);
            spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, request.createCacheKey(),
                    THIRTY_MINUTES, mHourlyRequestListener);
        }
    }

    @Override
    public void onPause(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(getmResponseReceiver());
        getmMainWeatherLayout().setOnLongClickListener(null);

        // write to storage.
        SharedPreferences savedFile = mActivity.getSharedPreferences(getString(R.string.preference_save_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedFile.edit();
        editor.putInt(getString(R.string.preference_city_count_int), weatherTable.size());
        editor.putBoolean(getString(R.string.preference_has_city_bool), weatherTable.size() > 0);
        Enumeration<String> e = weatherTable.keys();
        for(int i=0; i<weatherTable.size(); i++){
            String key = e.nextElement();
            editor.putString(String.valueOf(i), key);
            editor.putString(key+getString(R.string.preference_condition_string), weatherTable.get(key).condition);
            editor.putInt(key + getString(R.string.preference_celsius_int), weatherTable.get(key).tempCelsius);
            editor.putInt(key+getString(R.string.preference_fahrenheit_int), weatherTable.get(key).tempFahrenheit);
            editor.putInt(key+getString(R.string.preference_hour_int), weatherTable.get(key).hour);
            editor.putString(key+getString(R.string.preference_icon_string), weatherTable.get(key).icon);
        }
        editor.commit();

        mMainWeatherLayout = null;
        mMainWeatherImage = null;
        mMainWeatherCityText = null;
        mMainWeatherConditionText = null;
        mMainWeatherTempText = null;
        mResponseReceiver = null;
        mRecyclerView = null;

        super.onPause();
    }

    @Override
    public void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onItemSelected(Weather item, int position) {
        if(DEBUG) Log.d(TAG, "Selected: "+item.toKey()+" at position: "+position);
        weatherList.remove(position);
        mWeatherRecyclerViewAdapter.notifyItemRemoved(position);
        weatherList.add(position, mainWeather);
        mWeatherRecyclerViewAdapter.notifyItemInserted(position);
        mainWeather = item;

        HourlyApiRequest request = new HourlyApiRequest(mainWeather.cityName, mainWeather.stateName);
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, request.createCacheKey(),
                THIRTY_MINUTES, mHourlyRequestListener);

        getmMainWeatherLayout().setVisibility(View.INVISIBLE);
        getmProgressBar().setVisibility(View.VISIBLE);
    }

    public void addCity(String city, String state){
        if(weatherTable.containsKey(city+", "+state)){
            Toast toast = Toast.makeText(getActivity(), String.format(getString(R.string.already_added),
                    city+", "+state), Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Weather newWeather = new Weather(city, state);
            int addIndex = weatherList.size();
            weatherList.add(addIndex, mainWeather);
            mWeatherRecyclerViewAdapter.notifyItemInserted(addIndex);
            weatherTable.put(newWeather.toKey(), newWeather);
            mainWeather = newWeather;
        }
    }

    private void updateMainWeather(){
        getmMainWeatherCityText().setText(mainWeather.toKey());
        // exit early if we do not have weather info yet.
        if(mainWeather.icon != null){
            // Decide which icon to use
            switch(mainWeather.icon){
                case "clear":
                case "mostlysunny":
                case "partlysunny":
                case "sunny":
                    getmMainWeatherImage().setImageResource(R.drawable.sun_1x);
                    break;
                case "cloudy":
                case "partlycloudy":
                    getmMainWeatherImage().setImageResource(R.drawable.cloudy_1x);
                    break;
                case "flurries":
                case "snow":
                case "chancesnow":
                case "chanceflurries":
                    getmMainWeatherImage().setImageResource(R.drawable.snow_1x);
                    break;
                case "fog":
                    getmMainWeatherImage().setImageResource(R.drawable.fog_1x);
                    break;
                case "hazy":
                    getmMainWeatherImage().setImageResource(R.drawable.fog_cloudy_1x);
                    break;
                case "mostlycloudy":
                    getmMainWeatherImage().setImageResource(R.drawable.very_cloudy_1x);
                    break;
                case "sleet":
                case "chancesleet":
                    getmMainWeatherImage().setImageResource(R.drawable.hail_1x);
                    break;
                case "rain":
                case "chancerain":
                    getmMainWeatherImage().setImageResource(R.drawable.rain_1x);
                    break;
                case "tstorms":
                case "chancetstorms":
                    getmMainWeatherImage().setImageResource(R.drawable.thunderstorm_1x);
                    break;
                default:
                    Log.e(TAG, "found unknown icon[" +mainWeather.icon+"]");
                    break;
            }
            getmMainWeatherConditionText().setText(mainWeather.condition);
            String temperature = String.format(getString(R.string.temperature_format),
                    mainWeather.tempCelsius, mainWeather.tempFahrenheit);
            getmMainWeatherTempText().setText(temperature);

            // Check to use night sky
            if(mainWeather.hour>7 && mainWeather.hour <=19){
                getActivity().getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.blue_400));
            }
            else{
                getActivity().getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.blue_900));
            }
        }
        getmProgressBar().setVisibility(View.GONE);
        getmMainWeatherLayout().setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onLongClick(View v) {
        if(weatherTable.size()>1){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Remove country")
                    .setMessage(mainWeather.toKey())
                    .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(DEBUG) Log.d(TAG, "Remove "+mainWeather.toKey());
                            // Do stuff
                            weatherTable.remove(mainWeather.toKey());
                            int removeIndex = weatherList.size()-1;
                            mainWeather = weatherList.get(removeIndex);
                            if(DEBUG) Log.d(TAG, "Next main weather: "+ mainWeather.toKey());
                            weatherList.remove(removeIndex);
                            mWeatherRecyclerViewAdapter.notifyItemRemoved(removeIndex);
                            HourlyApiRequest request = new HourlyApiRequest(mainWeather.cityName, mainWeather.stateName);
                            spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, request.createCacheKey(),
                                    THIRTY_MINUTES, mHourlyRequestListener);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
        return true;
    }

    public interface FragmentActivityInterface {
        SharedPreferences getSharedPreferences(String name, int mode);
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class ResponseReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(getString(R.string.intent_action_broadcast_batch_string))){
                int resultCode = intent.getIntExtra(getString(R.string.intent_intentservice_request_result_int), 0);
                switch(resultCode){
                    case CheckWeatherIntentService.REQUEST_SUCCESS:
                        if(DEBUG) Log.d(TAG, "Received batch update success message");

                        SharedPreferences saveFile = mActivity.getSharedPreferences(getString(R.string.preference_save_file),
                                Context.MODE_PRIVATE);
                        int count = saveFile.getInt(getString(R.string.preference_city_count_int), 0);
                        String[] keyList = new String[count];
                        for(int i=0; i<count; i++){
                            keyList[i] = saveFile.getString(String.valueOf(i), null);
                        }

                        // Batch result are written to local storage. Therefore we read from local storage
                        // to get the updated info.
                        for(String key : keyList){
                            Weather w = weatherTable.get(key);
                            w.condition = saveFile.getString(key + getString(R.string.preference_condition_string), "");
                            w.tempCelsius = saveFile.getInt(key+getString(R.string.preference_celsius_int), 0);
                            w.tempFahrenheit = saveFile.getInt(key + getString(R.string.preference_fahrenheit_int), 0);
                            w.hour = saveFile.getInt(key + getString(R.string.preference_hour_int), 0);
                            w.icon = saveFile.getString(key+getString(R.string.preference_icon_string), "");
                        }

                        updateMainWeather();
                        mWeatherRecyclerViewAdapter.notifyDataSetChanged();

                        break;
                    case CheckWeatherIntentService.REQUEST_FAIL_NO_NETWORK:
                        Toast toast = Toast.makeText(getActivity(),"No Connection", Toast.LENGTH_LONG);
                        toast.show();
                        break;
                    default:
                        Log.e(TAG, "Received unknwon result code for batch result broadcast.");
                }
            }
        }
    }

    private class HourlyRequestListener implements RequestListener<Weather> {

        @Override
        public void onRequestFailure(SpiceException e) {

        }

        @Override
        public void onRequestSuccess(Weather result) {
            Log.d(TAG, "Request for " + result.toKey() + " succeeded");
            Weather oldWeather = weatherTable.get(result.toKey());
            oldWeather.update(result.condition, result.tempCelsius, result.tempFahrenheit,
                    result.hour, result.icon);
            if(result.toKey().equals(mainWeather.toKey())){
                updateMainWeather();
            }
            else{
                int indexToUpdate = weatherList.indexOf(oldWeather);
                if(indexToUpdate != -1) {
                    mWeatherRecyclerViewAdapter.notifyItemChanged(indexToUpdate);
                }
            }
        }
    }
}
