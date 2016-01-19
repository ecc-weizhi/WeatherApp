package com.example.weizhi.oddleassignment.citysearch;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.network.WundergroundAPICallManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerViewAdapter for showing list of suggested cities. This adapter implements both
 * Filterable and TextWatcher to enable it to detect changes in city search box, call our
 * autocomplete API and provide a list of suggestion.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CitySearchRecyclerViewAdapter extends RecyclerView.Adapter<CitySearchRecyclerViewAdapter.ViewHolder>
        implements Filterable, TextWatcher {
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

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null && constraint.length()>=2) {
                    citySuggestionList = WundergroundAPICallManager.requestAutoComplete(constraint.toString());

                    // Now assign the values and count to the FilterResults object
                    filterResults.values = citySuggestionList;
                    filterResults.count = citySuggestionList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
        return myFilter;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        getFilter().filter(s.toString());
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
