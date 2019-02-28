package com.bookyrself.bookyrself.data.ResponseModels.SearchRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Filter {

    @SerializedName("bool")
    @Expose
    private Bool_ bool;

    public Bool_ getBool() {
        return bool;
    }

    public void setBool(Bool_ bool) {
        this.bool = bool;
    }

}
