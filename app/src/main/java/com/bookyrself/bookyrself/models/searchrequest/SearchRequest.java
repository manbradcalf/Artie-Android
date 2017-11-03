package com.bookyrself.bookyrself.models.searchrequest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by benmedcalf on 11/1/17.
 */

public class SearchRequest {
    @SerializedName("query")
    private Query mQuery;

    public Query getmQuery() {
        return mQuery;
    }

    public void setmQuery(Query mQuery) {
        this.mQuery = mQuery;
    }
}
