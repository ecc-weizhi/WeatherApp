package com.example.weizhi.oddleassignment.weatherdisplay;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.model.Weather;

import java.util.ArrayList;

/**
 * A RecyclerViewAdapter for our horizontal scrolling list of weather info. Weather info
 * to displayed are stored in weatherList. Modification to weatherList should call the
 * respective notify methods so we can update the views accordingly.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class WeatherRecyclerViewAdapter extends RecyclerView.Adapter<WeatherRecyclerViewAdapter.ViewHolder> {
    private final String TAG = "'WeatherRecyclerAdapter";
    private ArrayList<Weather> weatherList;
    private final RecyclerFragmentInterface mFragment;

    public WeatherRecyclerViewAdapter(Fragment fragment, @NonNull ArrayList<Weather> weatherList) {
        if (fragment instanceof RecyclerFragmentInterface) {
            mFragment = (RecyclerFragmentInterface) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement RecyclerFragmentInterface");
        }

        this.weatherList = weatherList;
    }

    public void insertWeather(Weather weather, int index){
        weatherList.add(index, weather);
        notifyItemInserted(index);
    }

    public void removeWeather(int index){
        weatherList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public WeatherRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_weather_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeatherRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.mWeather = weatherList.get(position);
        holder.cityTextView.setText(holder.mWeather.toKey());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFragment) {
                    mFragment.onItemSelected(weatherList.get(position), position);
                }
            }
        });

        // exit early if we do not have weather info yet.
        if(holder.mWeather.icon == null)
            return;

        // Decide which icon to use
        switch(holder.mWeather.icon){
            case "clear":
            case "mostlysunny":
            case "partlysunny":
            case "sunny":
                holder.iconImageView.setImageResource(R.drawable.sun_1x);
                break;
            case "cloudy":
            case "partlycloudy":
                holder.iconImageView.setImageResource(R.drawable.cloudy_1x);
                break;
            case "flurries":
            case "snow":
            case "chancesnow":
            case "chanceflurries":
                holder.iconImageView.setImageResource(R.drawable.snow_1x);
                break;
            case "fog":
                holder.iconImageView.setImageResource(R.drawable.fog_1x);
                break;
            case "hazy":
                holder.iconImageView.setImageResource(R.drawable.fog_cloudy_1x);
                break;
            case "mostlycloudy":
                holder.iconImageView.setImageResource(R.drawable.very_cloudy_1x);
                break;
            case "sleet":
            case "chancesleet":
                holder.iconImageView.setImageResource(R.drawable.hail_1x);
                break;
            case "rain":
            case "chancerain":
                holder.iconImageView.setImageResource(R.drawable.rain_1x);
                break;
            case "tstorms":
            case "chancetstorms":
                holder.iconImageView.setImageResource(R.drawable.thunderstorm_1x);
                break;
            default:
                Log.d(TAG, "found unknown icon[" +holder.mWeather.icon+"] for "+holder.mWeather.toKey());
                break;
        }
        holder.conditionTextView.setText(holder.mWeather.condition);
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView iconImageView;
        public final TextView cityTextView;
        public final TextView conditionTextView;
        public Weather mWeather;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            iconImageView = (ImageView) view.findViewById(R.id.weather_small_icon_image);
            cityTextView = (TextView) view.findViewById(R.id.weather_small_city_text);
            conditionTextView = (TextView) view.findViewById(R.id.weather_small_condition_text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + cityTextView.getText() + "'";
        }
    }

    public interface RecyclerFragmentInterface{
        void onItemSelected(Weather item, int position);
    }
}
