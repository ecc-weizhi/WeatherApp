package com.example.weizhi.oddleassignment.network;

import com.example.weizhi.oddleassignment.model.AutoCompleteJsonPojo;
import com.example.weizhi.oddleassignment.model.SuggestionList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Spice Request for auto complete request.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class AutoCompleteApiRequest extends SpringAndroidSpiceRequest<SuggestionList> {
    private final String TAG = "AutoCompleteApiRequest";
    private final String queryText;

    public AutoCompleteApiRequest(String queryText) {
        super(SuggestionList.class);
        this.queryText = queryText;
    }

    @Override
    public SuggestionList loadDataFromNetwork() throws Exception {
        String url = "http://autocomplete.wunderground.com/aq?h=0&format=Json&query="
                + URLEncoder.encode(queryText, "UTF-8");

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        AutoCompleteJsonPojo response = gson.fromJson(reader, AutoCompleteJsonPojo.class);
        reader.close();
        urlConnection.disconnect();

        ArrayList<String> mList = new ArrayList<>();
        for(AutoCompleteJsonPojo.City c: response.RESULTS){
            if(c.type.equals("city")){
                mList.add(c.name);
            }
        }

        return new SuggestionList(queryText, mList);
    }

    public String createCacheKey() {
        return queryText;
    }

}
