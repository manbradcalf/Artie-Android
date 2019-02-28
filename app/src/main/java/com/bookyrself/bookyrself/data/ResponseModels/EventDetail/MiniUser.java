package com.bookyrself.bookyrself.data.ResponseModels.EventDetail;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class MiniUser {

    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("url")
    private String mUrl;
    @SerializedName("userId")
    private String mUserId;
    @SerializedName("username")
    private String mUsername;
    @SerializedName("attendingStatus")
    private String mAttendingStatus;

    public String getCitystate() {
        return mCitystate;
    }

    public void setCitystate(String citystate) {
        mCitystate = citystate;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getAttendingStatus() {
        return mAttendingStatus;
    }

    public void setAttendingStatus(String mAttendingStatus) {
        this.mAttendingStatus = mAttendingStatus;
    }
}
