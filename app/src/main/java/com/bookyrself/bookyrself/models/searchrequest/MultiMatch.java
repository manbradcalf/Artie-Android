
package com.bookyrself.bookyrself.models.searchrequest;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class MultiMatch {

    @SerializedName("fields")
    private List<String> mFields;
    @SerializedName("query")
    private String mQuery;

    public List<String> getFields() {
        return mFields;
    }

    public void setFields(List<String> fields) {
        mFields = fields;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        mQuery = query;
    }

}
