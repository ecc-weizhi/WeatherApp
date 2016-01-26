package com.example.weizhi.oddleassignment.citysearch;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * A simple TextWatcher.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
class SearchBoxTextWatcher implements TextWatcher {
    private final TextWatcherListener mListener;

    public SearchBoxTextWatcher(TextWatcherListener listener){
        mListener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mListener.onAfterTextChanged(s.toString());
    }

    public interface TextWatcherListener{
        void onAfterTextChanged(String s);
    }
}
