
package com.bookyrself.bookyrself.models.searchrequest;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bool {

    @SerializedName("must")
    @Expose
    private List<Must> must = null;
    @SerializedName("filter")
    @Expose
    private Filter filter;

    public List<Must> getMust() {
        return must;
    }

    public void setMust(List<Must> must) {
        this.must = must;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
