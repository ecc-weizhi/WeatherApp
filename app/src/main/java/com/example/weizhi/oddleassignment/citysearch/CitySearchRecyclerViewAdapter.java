package com.example.weizhi.oddleassignment.citysearch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weizhi.oddleassignment.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerViewAdapter for showing list of suggested cities. This adapter implements both
 * Filterable and TextWatcher to enable it to detect changes in city search box, call our
 * autocomplete API and provide a list of suggestion.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CitySearchRecyclerViewAdapter extends RecyclerView.Adapter<CitySearchRecyclerViewAdapter.ViewHolder> {
    private final String TAG = "CityRecyclerViewAdapter";

    private List<String> citySuggestionList;
    private final RecyclerFragmentInterface mFragment;

    public CitySearchRecyclerViewAdapter(RecyclerFragmentInterface mFragment) {
        citySuggestionList = new ArrayList<>();
        this.mFragment = mFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_city_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.name = citySuggestionList.get(position);
        holder.nameTextView.setText(citySuggestionList.get(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView mTextView = (TextView)v.findViewById(R.id.city_text);
                String[] s = mTextView.getText().toString().split(", ");

                if (mFragment != null) {
                    mFragment.onCitySelected(s[0], s[1]);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return citySuggestionList.size();
    }

    public void setList(List<String> newList){
        this.citySuggestionList = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameTextView;
        public String name;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameTextView = (TextView) view.findViewById(R.id.city_text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameTextView.getText() + "'";
        }
    }

    public interface RecyclerFragmentInterface{
        void onCitySelected(String city, String state);
    }
}
