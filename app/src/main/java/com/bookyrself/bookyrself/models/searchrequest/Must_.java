
package com.bookyrself.bookyrself.models.searchrequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Must_ {

    @SerializedName("range")
    @Expose
    private Range range;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

}
