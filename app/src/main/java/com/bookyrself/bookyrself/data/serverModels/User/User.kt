package com.bookyrself.bookyrself.data.serverModels.User

import android.graphics.drawable.Drawable
import android.net.Uri

import com.google.gson.annotations.SerializedName
import com.pchmn.materialchips.model.ChipInterface

import java.util.HashMap

import javax.annotation.Generated

@Generated("net.hexar.json2pojo")
class User : ChipInterface {

    @SerializedName("bio")
    var bio: String? = null
    @SerializedName("citystate")
    var citystate: String? = null
    @SerializedName("email")
    var email: String? = null
    @SerializedName("url")
    var url: String? = null
    @SerializedName("events")
    var events: HashMap<String, EventInviteInfo>? = null
    @SerializedName("tags")
    var tags: List<String>? = null
    @SerializedName("username")
    var username: String? = null

    @SerializedName("unavailable_dates")
    var unavailableDates: HashMap<String, Boolean>? = null

    override fun getId(): Any? {
        return null
    }

    override fun getAvatarUri(): Uri? {
        return null
    }

    override fun getAvatarDrawable(): Drawable? {
        return null
    }

    override fun getLabel(): String? {
        return this.username
    }

    override fun getInfo(): String? {
        return this.citystate
    }
}
