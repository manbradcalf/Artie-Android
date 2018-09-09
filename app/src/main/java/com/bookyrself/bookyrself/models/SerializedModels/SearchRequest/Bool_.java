package com.bookyrself.bookyrself.models.SerializedModels.SearchRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bool_ {

    @SerializedName("must")
    @Expose
    private Must_ must;

    public Must_ getMust() {
        return must;
    }

    public void setMust(Must_ must) {
        this.must = must;
    }

}
