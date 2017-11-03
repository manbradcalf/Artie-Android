
package com.bookyrself.bookyrself.models.searchrequest;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Bool {

    @SerializedName("filter")
    private Filter mFilter;
    @SerializedName("must")
    private List<Must> mMust;

    public Filter getFilter() {
        return mFilter;
    }

    public void setFilter(Filter filter) {
        mFilter = filter;
    }

    public List<Must> getMust() {
        return mMust;
    }

    public void setMust(List<Must> must) {
        mMust = must;
    }

}
