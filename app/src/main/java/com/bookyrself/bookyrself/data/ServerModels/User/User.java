
package com.bookyrself.bookyrself.data.ServerModels.User;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class User implements ChipInterface {

    @SerializedName("bio")
    private String mBio;
    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("url")
    private String mUrl;
    @SerializedName("events")
    private HashMap<String, EventInviteInfo> mEvents;
    @SerializedName("tags")
    private List<String> mTags;
    @SerializedName("username")
    private String mUsername;

    public HashMap<String, Boolean> getUnavailableDates() {
        return mUnavailableDates;
    }

    public void setUnavailableDates(HashMap<String, Boolean> mUnavailableDates) {
        this.mUnavailableDates = mUnavailableDates;
    }

    @SerializedName("unavailable_dates")
    private HashMap<String, Boolean> mUnavailableDates;

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

    public HashMap<String, EventInviteInfo> getEvents() {
        return mEvents;
    }

    public void setEvents(HashMap<String, EventInviteInfo> events) {
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

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return this.mUsername;
    }

    @Override
    public String getInfo() {
        return this.mCitystate;
    }
}
