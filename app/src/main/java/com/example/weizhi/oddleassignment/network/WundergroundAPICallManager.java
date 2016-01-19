package com.example.weizhi.oddleassignment.network;

import android.util.Log;

import com.example.weizhi.oddleassignment.model.Weather;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This class handles all API calls to Wunderground server.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class WundergroundAPICallManager {
    private static final String TAG = "'APICallManager";
    public static final String FORMAT = "xml";
    public static final String API_KEY = "";
    public static final String AUTO_COMPLETE_URL = "http://autocomplete.wunderground.com/aq?h=0&format=XML&query=";
    public static final String HOURLY_URL = "http://api.wunderground.com/api/"+API_KEY+"/hourly/q/";


    public static ArrayList<String> requestAutoComplete(String queryText) {
        URL url = null;
        try {
            url = new URL(AUTO_COMPLETE_URL+ URLEncoder.encode(queryText, "UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "IOException for: "+url.toString());
        }

        HttpURLConnection urlConnection = null;
        InputStream in = null;
        ArrayList<String> mCityList = new ArrayList();
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(2000 /* milliseconds */);
            urlConnection.setConnectTimeout(3000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            in = new BufferedInputStream(urlConnection.getInputStream());
            mCityList = WundergroundXMLParser.parseCity(in);
        } catch (IOException e) {
            Log.e(TAG, "IOException for: "+url.toString());
        } finally {
            if(urlConnection != null)
                urlConnection.disconnect();
        }

        return mCityList;
    }

    public static Weather requestWeather(String state, String city) {
        URL url = null;
        try {
            // Seems like we only have to encode whitespace. Symbols like apostrophe and slash are accepted.
            String stateURL = state.replace(" ", "%20");
            String cityURL = city.replace(" ", "%20");
            url = new URL(HOURLY_URL+stateURL+"/"+cityURL+"."+FORMAT);
        } catch (IOException e) {
            Log.e(TAG, "IOException for: "+url.toString());
        }

        HttpURLConnection urlConnection = null;
        InputStream in = null;
        Weather weather = null;
        for(int i=0; i<3; i++){
            // Retry 3 times before giving up
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000 /* milliseconds */);
                urlConnection.setConnectTimeout(3000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);

                in = new BufferedInputStream(urlConnection.getInputStream());

                weather = WundergroundXMLParser.parseWeather(in, city, state);
            } catch (IOException e) {
                Log.e(TAG, "IOException for: "+url.toString());
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            if(weather!=null)
                break;
        }

        return weather;
    }
}
