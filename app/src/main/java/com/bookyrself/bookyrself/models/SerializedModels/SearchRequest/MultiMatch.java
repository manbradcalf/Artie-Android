package com.bookyrself.bookyrself.models.SerializedModels.SearchRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MultiMatch {

    @SerializedName("query")
    @Expose
    private String query;
    @SerializedName("fields")
    @Expose
    private List<String> fields = null;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

}
