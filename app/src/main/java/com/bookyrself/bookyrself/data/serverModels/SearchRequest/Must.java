package com.bookyrself.bookyrself.data.ServerModels.SearchRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Must {

    @SerializedName("match")
    @Expose
    private Match match;
    @SerializedName("multi_match")
    @Expose
    private MultiMatch multiMatch;

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public MultiMatch getMultiMatch() {
        return multiMatch;
    }

    public void setMultiMatch(MultiMatch multiMatch) {
        this.multiMatch = multiMatch;
    }

}
