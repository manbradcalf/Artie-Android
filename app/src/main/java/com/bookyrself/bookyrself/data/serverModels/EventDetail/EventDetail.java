package com.bookyrself.bookyrself.data.serverModels.EventDetail;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class EventDetail implements Parcelable {

    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("date")
    private String mDate;
    @SerializedName("eventname")
    private String mEventname;
    @SerializedName("host")
    private Host mHost;
    @SerializedName("picture")
    private String mPicture;
    @SerializedName("tags")
    private List<String> mTags;
    @SerializedName("users")
    private HashMap<String, Boolean> mUserIds;

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

    public Host getHost() {
        return mHost;
    }

    public void setHost(Host host) {
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

    public HashMap<String, Boolean> getUsers() {
        return mUserIds;
    }

    public void setUsers(HashMap<String, Boolean> userIds) {
        mUserIds = userIds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCitystate);
        dest.writeString(this.mDate);
        dest.writeString(this.mEventname);
        dest.writeParcelable(this.mHost, flags);
        dest.writeString(this.mPicture);
        dest.writeStringList(this.mTags);
        dest.writeSerializable(this.mUserIds);
    }

    public EventDetail() {
    }

    protected EventDetail(Parcel in) {
        this.mCitystate = in.readString();
        this.mDate = in.readString();
        this.mEventname = in.readString();
        this.mHost = in.readParcelable(Host.class.getClassLoader());
        this.mPicture = in.readString();
        this.mTags = in.createStringArrayList();
        this.mUserIds = (HashMap<String, Boolean>) in.readSerializable();
    }

    public static final Parcelable.Creator<EventDetail> CREATOR = new Parcelable.Creator<EventDetail>() {
        @Override
        public EventDetail createFromParcel(Parcel source) {
            return new EventDetail(source);
        }

        @Override
        public EventDetail[] newArray(int size) {
            return new EventDetail[size];
        }
    };
}
