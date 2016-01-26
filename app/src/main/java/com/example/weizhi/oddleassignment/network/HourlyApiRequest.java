package com.example.weizhi.oddleassignment.network;

import com.example.weizhi.oddleassignment.model.HourlyJsonPojo;
import com.example.weizhi.oddleassignment.model.Weather;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Spice request for Hourly API request
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HourlyApiRequest extends SpringAndroidSpiceRequest<Weather> {
    private final String TAG = "HourlyApiRequest";
    private final String API_KEY = "";
    private final String city;
    private final String state;

    public HourlyApiRequest(String city, String state) {
        super(Weather.class);
        this.city = city;
        this.state = state;
    }

    @Override
    public Weather loadDataFromNetwork() throws Exception {
        // We only encode white space as "%20". This is because the query parameter accept symbols
        // such as apostrophe.
        String url = String.format("http://api.wunderground.com/api/%s/hourly/q/%s/%s.json",
                API_KEY, state.replace(" ", "%20"), city.replace(" ", "%20"));

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        HourlyJsonPojo response = gson.fromJson(reader, HourlyJsonPojo.class);
        reader.close();
        urlConnection.disconnect();

        return new Weather(city, state, response.hourly_forecast.get(0).condition,
                response.hourly_forecast.get(0).temp.metric,
                (int)response.hourly_forecast.get(0).temp.english,
                response.hourly_forecast.get(0).FCTTIME.hour,
                response.hourly_forecast.get(0).icon);
    }

    public String createCacheKey() {
        return city+", "+state;
    }

}
