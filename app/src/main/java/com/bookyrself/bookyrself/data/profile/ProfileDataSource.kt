package com.bookyrself.bookyrself.data.profile

import com.bookyrself.bookyrself.data.serverModels.User.User

import io.reactivex.Flowable

interface ProfileDataSource {

    fun getProfileInfo(userId: String): Flowable<User>

    fun updateProfileInfo(userId: String, user: User): Flowable<User>

}
