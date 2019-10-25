package com.bookyrself.bookyrself.data.Profile

import com.bookyrself.bookyrself.data.ServerModels.User.User

import io.reactivex.Flowable

interface ProfileDataSource {

    fun getProfileInfo(userId: String): Flowable<User>

    fun updateProfileInfo(userId: String, user: User): Flowable<User>

}
