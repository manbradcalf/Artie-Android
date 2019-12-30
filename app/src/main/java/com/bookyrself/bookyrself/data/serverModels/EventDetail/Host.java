package com.bookyrself.bookyrself.data.serverModels.EventDetail;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Host implements Parcelable {

    @SerializedName("citystate")
    private String mCitystate;
    @SerializedName("url")
    private String mUrl;
    @SerializedName("userId")
    private String mUserId;
    @SerializedName("username")
    private String mUsername;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCitystate);
        dest.writeString(this.mUrl);
        dest.writeString(this.mUserId);
        dest.writeString(this.mUsername);
    }

    public Host() {
    }

    protected Host(Parcel in) {
        this.mCitystate = in.readString();
        this.mUrl = in.readString();
        this.mUserId = in.readString();
        this.mUsername = in.readString();
    }

    public static final Parcelable.Creator<Host> CREATOR = new Parcelable.Creator<Host>() {
        @Override
        public Host createFromParcel(Parcel source) {
            return new Host(source);
        }

        @Override
        public Host[] newArray(int size) {
            return new Host[size];
        }
    };
}
