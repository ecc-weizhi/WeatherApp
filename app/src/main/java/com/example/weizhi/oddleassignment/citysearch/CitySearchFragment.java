package com.example.weizhi.oddleassignment.citysearch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.weizhi.oddleassignment.R;

/**
 * Fragment to search for cities
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CitySearchFragment extends Fragment implements CitySearchRecyclerViewAdapter.RecyclerFragmentInterface {
    private final String TAG = "'CitySearchFragment";

    private FragmentActivityInterface mActivity;
    private CitySearchRecyclerViewAdapter mCitySearchRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private EditText mCityEdit;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onResume(){
        super.onResume();
        getmRecyclerView().setAdapter(getAdapter());
        getmCityEdit().addTextChangedListener(getAdapter());
    }

    @Override
    public void onPause(){
        getmCityEdit().removeTextChangedListener(getAdapter());
        mRecyclerView = null;
        mCityEdit = null;
        super.onPause();
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

    public interface FragmentActivityInterface {
        void onCitySelected(String city, String state);
    }
}
