package com.bookyrself.bookyrself.data.profile

import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileRepo {

    private var db: DatabaseReference? = null

    init {
        if (FirebaseAuth.getInstance().uid != null) {

            val userId = FirebaseAuth.getInstance().uid

            this.db = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId!!)

            this.db!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    // Update repos observable to fetch new data
                    getProfileInfo(userId)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    getProfileInfo(userId)
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    getProfileInfo(userId)
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                    getProfileInfo(userId)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    getProfileInfo(userId)
                }
            })
        }
    }


    fun getProfileInfo(userId: String): Single<User> {
        return FirebaseService.instance
            .getUserDetails(userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .singleOrError()
    }

    suspend fun updateProfileInfo(userId: String, user: User): ProfileRepoResponse {
        val response = FirebaseServiceCoroutines.instance.patchUser(user, userId)
        return if (response.isSuccessful) {
            ProfileRepoResponse.Success(response.body())
        } else {
            ProfileRepoResponse.Failure(response.message())
        }
    }

    sealed class ProfileRepoResponse {
        class Success(val user: User?) : ProfileRepoResponse()
        class Failure(val errorMessage: String) : ProfileRepoResponse()
    }
}
