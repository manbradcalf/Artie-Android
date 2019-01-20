
package com.bookyrself.bookyrself.models.SerializedModels.User;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class EventInfo {

    @SerializedName("isInviteAccepted")
    private Boolean mIsInviteAccepted;

    @SerializedName("isInviteRejected")
    private Boolean mIsInviteRejectted;

    @SerializedName("isHost")
    private Boolean mIsHost;

    public Boolean getIsInviteAccepted() {
        return mIsInviteAccepted;
    }

    public void setIsInviteAccepted(Boolean isInviteAccepted) {
        mIsInviteAccepted = isInviteAccepted;
    }

    public Boolean getIsHost() {
        return mIsHost;
    }

    public void setIsHost(Boolean mIsHost) {
        this.mIsHost = mIsHost;
    }

    public Boolean getIsInviteRejectted() {
        return mIsInviteRejectted;
    }

    public void setIsInviteRejectted(Boolean mIsInviteRejectted) {
        this.mIsInviteRejectted = mIsInviteRejectted;
    }
}
