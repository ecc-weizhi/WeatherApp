package com.example.weizhi.oddleassignment.citysearch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.weizhi.oddleassignment.R;
import com.example.weizhi.oddleassignment.background.MySpiceService;
import com.example.weizhi.oddleassignment.model.SuggestionList;
import com.example.weizhi.oddleassignment.network.AutoCompleteApiRequest;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

/**
 * Fragment to search for cities
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CitySearchFragment extends Fragment implements CitySearchRecyclerViewAdapter.RecyclerFragmentInterface,
        SearchBoxTextWatcher.TextWatcherListener{
    private final String TAG = "CitySearchFragment";

    private FragmentActivityInterface mActivity;
    private CitySearchRecyclerViewAdapter mCitySearchRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private EditText mCityEdit;
    private SearchBoxTextWatcher mSearchBoxTextWatcher;

    private final SpiceManager spiceManager = new SpiceManager(MySpiceService.class);

    private SearchBoxTextWatcher getmSearchBoxTextWatcher(){
        if(mSearchBoxTextWatcher == null)
            mSearchBoxTextWatcher = new SearchBoxTextWatcher(this);
        return mSearchBoxTextWatcher;
    }

    private CitySearchRecyclerViewAdapter getAdapter(){
        if(mCitySearchRecyclerViewAdapter == null)
            mCitySearchRecyclerViewAdapter = new CitySearchRecyclerViewAdapter(this);
        return mCitySearchRecyclerViewAdapter;
    }

    private RecyclerView getmRecyclerView(){
        if(mRecyclerView == null)
            mRecyclerView = (RecyclerView)getView().findViewById(R.id.city_suggestion_recycler);
        return mRecyclerView;
    }

    private EditText getmCityEdit(){
        if(mCityEdit == null)
            mCityEdit = (EditText) getView().findViewById(R.id.city_search_edit);
        return mCityEdit;
    }

    public CitySearchFragment() {
        // Required empty public constructor
    }

    public static CitySearchFragment newInstance() {
        return new CitySearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_city_search, container, false);
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
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
    }


    @Override
    public void onResume(){
        super.onResume();
        getmRecyclerView().setAdapter(getAdapter());
        getmCityEdit().addTextChangedListener(getmSearchBoxTextWatcher());
    }

    @Override
    public void onPause(){
        getmCityEdit().removeTextChangedListener(getmSearchBoxTextWatcher());
        mSearchBoxTextWatcher = null;
        mRecyclerView = null;
        mCityEdit = null;
        super.onPause();
    }

    @Override
    public void onStop(){
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCitySelected(String city, String state){
        mActivity.onCitySelected(city, state);
    }

    @Override
    public void onAfterTextChanged(String s) {
        if(s.length()>1) {
            AutoCompleteApiRequest request = new AutoCompleteApiRequest(s);
            spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, request.createCacheKey(),
                    DurationInMillis.ONE_DAY, new AutoCompleteRequestListener());
        }
        else{
            getAdapter().setList(new ArrayList<String>());
        }
    }

    public interface FragmentActivityInterface {
        void onCitySelected(String city, String state);
    }

    private class AutoCompleteRequestListener implements RequestListener<SuggestionList>{

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(SuggestionList suggestionList) {
            if(getmCityEdit().getText().toString().equals(suggestionList.queryText)) {
                getAdapter().setList(suggestionList.suggestions);
            }
        }
    }
}
