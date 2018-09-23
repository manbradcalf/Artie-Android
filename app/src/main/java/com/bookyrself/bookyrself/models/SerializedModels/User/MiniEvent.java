package com.bookyrself.bookyrself.models.SerializedModels.User;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class MiniEvent {

    @SerializedName("date")
    private String mDate;
    @SerializedName("eventname")
    private String mEventname;
    @SerializedName("id")
    private String mId;
    @SerializedName("isInviteAccepted")
    private Boolean mIsInviteAccepted;

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

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public Boolean getmIsInviteAccepted() {
        return mIsInviteAccepted;
    }

    public void setmIsInviteAccepted(Boolean isInviteAccepted) {
        mIsInviteAccepted = isInviteAccepted;
    }

}
