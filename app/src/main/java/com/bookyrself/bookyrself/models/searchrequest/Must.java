
package com.bookyrself.bookyrself.models.searchrequest;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Must {

    @SerializedName("match")
    private Match mMatch;
    @SerializedName("multi_match")
    private MultiMatch mMultiMatch;
    @SerializedName("range")
    private Range mRange;

    public Match getMatch() {
        return mMatch;
    }

    public void setMatch(Match match) {
        mMatch = match;
    }

    public MultiMatch getMultiMatch() {
        return mMultiMatch;
    }

    public void setMultiMatch(MultiMatch multiMatch) {
        mMultiMatch = multiMatch;
    }

    public Range getRange() {
        return mRange;
    }

    public void setRange(Range range) {
        mRange = range;
    }

}
