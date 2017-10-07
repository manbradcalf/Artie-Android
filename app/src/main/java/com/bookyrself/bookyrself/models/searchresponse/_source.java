
package com.bookyrself.bookyrself.models.searchresponse;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class _source {

    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("date")
    private String mDate;
    @SerializedName("eventname")
    private String mEventname;
    @SerializedName("guid")
    private String mGuid;
    @SerializedName("host")
    private String mHost;
    @SerializedName("picture")
    private String mPicture;
    @SerializedName("tags")
    private List<String> mTags;
    @SerializedName("users")
    private List<String> mUsers;

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

    public String getGuid() {
        return mGuid;
    }

    public void setGuid(String guid) {
        mGuid = guid;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
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

    public List<String> getUsers() {
        return mUsers;
    }

    public void setUsers(List<String> users) {
        mUsers = users;
    }

}
