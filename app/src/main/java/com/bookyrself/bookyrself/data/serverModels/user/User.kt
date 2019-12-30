package com.bookyrself.bookyrself.data.serverModels.user

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.pchmn.materialchips.model.ChipInterface
import java.util.*
import javax.annotation.Generated

@Generated("net.hexar.json2pojo")
class User() : ChipInterface, Parcelable {
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

    var userId: String? = null

    constructor(parcel: Parcel) : this() {
        bio = parcel.readString()
        citystate = parcel.readString()
        email = parcel.readString()
        url = parcel.readString()
        tags = parcel.createStringArrayList()
        username = parcel.readString()
        userId = parcel.readString()
    }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bio)
        parcel.writeString(citystate)
        parcel.writeString(email)
        parcel.writeString(url)
        parcel.writeStringList(tags)
        parcel.writeString(username)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
