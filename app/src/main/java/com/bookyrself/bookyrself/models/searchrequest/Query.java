
package com.bookyrself.bookyrself.models.searchrequest;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Query {

    @SerializedName("bool")
    private Bool mBool;
    @SerializedName("query")
    private Query mQuery;

    public Bool getBool() {
        return mBool;
    }

    public void setBool(Bool bool) {
        mBool = bool;
    }

    public Query getQuery() {
        return mQuery;
    }

    public void setQuery(Query query) {
        mQuery = query;
    }

}
