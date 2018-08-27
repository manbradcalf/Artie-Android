package com.bookyrself.bookyrself.models.SearchResponseUsers;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class SearchResponseUsers {

    @SerializedName("hits")
    private Hits mHits;
    @SerializedName("timed_out")
    private Boolean mTimedOut;
    @SerializedName("took")
    private Long mTook;
    @SerializedName("_shards")
    private com.bookyrself.bookyrself.models.SearchResponseUsers._shards m_shards;

    public Hits getHits() {
        return mHits;
    }

    public void setHits(Hits hits) {
        mHits = hits;
    }

    public Boolean getTimedOut() {
        return mTimedOut;
    }

    public void setTimedOut(Boolean timedOut) {
        mTimedOut = timedOut;
    }

    public Long getTook() {
        return mTook;
    }

    public void setTook(Long took) {
        mTook = took;
    }

    public com.bookyrself.bookyrself.models.SearchResponseUsers._shards get_shards() {
        return m_shards;
    }

    public void set_shards(com.bookyrself.bookyrself.models.SearchResponseUsers._shards _shards) {
        m_shards = _shards;
    }

}
