
package com.bookyrself.bookyrself.models.SearchRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Body {

    @SerializedName("query")
    @Expose
    private Query query;

    @SerializedName("size")
    @Expose
    private Integer size;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
