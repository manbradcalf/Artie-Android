package com.bookyrself.bookyrself.models.SerializedModels.EventDetail;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class EventDetail {

    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("date")
    private String mDate;
    @SerializedName("eventname")
    private String mEventname;
    @SerializedName("host")
    private List<Host> mHost;
    @SerializedName("picture")
    private String mPicture;
    @SerializedName("tags")
    private List<String> mTags;
    @SerializedName("users")
    private List<MiniUser> mMiniUsers;

    public String getCitystate() {
        return mCitystate;
    }

    public void setCitystate(String citystate) {
        mCitystate = citystate;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getEventname() {
        return mEventname;
    }

    public void setEventname(String eventname) {
        mEventname = eventname;
    }

    public List<Host> getHost() {
        return mHost;
    }

    public void setHost(List<Host> host) {
        mHost = host;
    }

    public String getPicture() {
        return mPicture;
    }

    public void setPicture(String picture) {
        mPicture = picture;
    }

    public List<String> getTags() {
        return mTags;
    }

    public void setTags(List<String> tags) {
        mTags = tags;
    }

    public List<MiniUser> getUsers() {
        return mMiniUsers;
    }

    public void setUsers(List<MiniUser> miniUsers) {
        mMiniUsers = miniUsers;
    }

}
