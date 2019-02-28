package com.bookyrself.bookyrself.data.ResponseModels.SearchResponseUsers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Hits {

    @SerializedName("hits")
    private List<Hit> mHits;
    @SerializedName("max_score")
    private Double mMaxScore;
    @SerializedName("total")
    private Long mTotal;

    public List<Hit> getHits() {
        return mHits;
    }

    public void setHits(List<Hit> hits) {
        mHits = hits;
    }

    public Double getMaxScore() {
        return mMaxScore;
    }

    public void setMaxScore(Double maxScore) {
        mMaxScore = maxScore;
    }

    public Long getTotal() {
        return mTotal;
    }

    public void setTotal(Long total) {
        mTotal = total;
    }

}
