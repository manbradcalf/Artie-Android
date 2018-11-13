package com.bookyrself.bookyrself.models.SerializedModels.SearchResponseEvents;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class _shards {

    @SerializedName("failed")
    private Long mFailed;
    @SerializedName("successful")
    private Long mSuccessful;
    @SerializedName("total")
    private Long mTotal;

    public Long getFailed() {
        return mFailed;
    }

    public void setFailed(Long failed) {
        mFailed = failed;
    }

    public Long getSuccessful() {
        return mSuccessful;
    }

    public void setSuccessful(Long successful) {
        mSuccessful = successful;
    }

    public Long getTotal() {
        return mTotal;
    }

    public void setTotal(Long total) {
        mTotal = total;
    }

}
