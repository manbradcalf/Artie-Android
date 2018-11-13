
package com.bookyrself.bookyrself.models.SerializedModels.User;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class User {

    @SerializedName("bio")
    private String mBio;
    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("url")
    private String mUrl;
    @SerializedName("events")
    private HashMap<String, EventInfo> mEvents;
    @SerializedName("tags")
    private List<String> mTags;
    @SerializedName("username")
    private String mUsername;

    public String getBio() {
        return mBio;
    }

    public void setBio(String bio) {
        mBio = bio;
    }

    public String getCitystate() {
        return mCitystate;
    }

    public void setCitystate(String citystate) {
        mCitystate = citystate;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public HashMap<String, EventInfo> getEvents() {
        return mEvents;
    }

    public void setEvents(HashMap<String, EventInfo> events) {
        mEvents = events;
    }

    public List<String> getTags() {
        return mTags;
    }

    public void setTags(List<String> tags) {
        mTags = tags;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

}
