
package com.bookyrself.bookyrself.models.SerializedModels.User;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class EventInfo {

    @SerializedName("isInviteAccepted")
    private Boolean mIsInviteAccepted;

    public Boolean getIsInviteAccepted() {
        return mIsInviteAccepted;
    }

    public void setIsInviteAccepted(Boolean isInviteAccepted) {
        mIsInviteAccepted = isInviteAccepted;
    }

}
