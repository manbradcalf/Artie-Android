package com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")

@SuppressWarnings("unused")
public class _source {

    @SerializedName("bio")
    private String mBio;
    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("events")
    private List<Event> mEvents;
    @SerializedName("phone")
    private String mPhone;
    @SerializedName("picture")
    private String mPicture;
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

    public List<Event> getEvents() {
        return mEvents;
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
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

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

}
