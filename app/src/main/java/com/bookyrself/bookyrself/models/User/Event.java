package com.bookyrself.bookyrself.models.User;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Event {

    @SerializedName("date")
    private String mDate;
    @SerializedName("eventname")
    private String mEventname;
    @SerializedName("id")
    private Long mId;

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

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

}
