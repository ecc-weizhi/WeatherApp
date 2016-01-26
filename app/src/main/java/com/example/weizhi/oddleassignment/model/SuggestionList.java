package com.example.weizhi.oddleassignment.model;

import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class SuggestionList {
    public final String queryText;
    public final List<String> suggestions;

    public SuggestionList(String queryText, List<String> suggestions){
        this.queryText = queryText;
        this.suggestions = suggestions;
    }
}
