package com.bookyrself.bookyrself.data.profile

import android.content.Context
import com.bookyrself.bookyrself.data.SingletonHolder
import com.bookyrself.bookyrself.data.profile.ProfileRepoResponse.Success
import com.bookyrself.bookyrself.data.profile.ProfileRepoResponse.Failure
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileRepo private constructor(context: Context) {

    private var db: DatabaseReference? = null
    private var cacheIsDirty: Boolean = true
    private var user: User? = null

    companion object : SingletonHolder<ProfileRepo, Context>(::ProfileRepo)

    init {
        FirebaseAuth.getInstance().uid?.let { userId ->
            this.db = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)

            this.db?.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    // Update repos observable to fetch new data
                    cacheIsDirty = true
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    cacheIsDirty = true
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    cacheIsDirty = true
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                    cacheIsDirty = true
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    cacheIsDirty = true
                }
            })
        }
    }


    suspend fun getProfileInfo(userId: String): ProfileRepoResponse {
        return if (cacheIsDirty) {
            val profileResponse = FirebaseServiceCoroutines.instance.getUserDetails(userId)
            if (profileResponse.isSuccessful) {
                user = profileResponse.body()
                Success(user = profileResponse.body())
            } else {
                Failure(errorMessage = profileResponse.message())
            }
        } else {
            Success(user)
        }
    }

    suspend fun updateProfileInfo(userId: String, user: User): ProfileRepoResponse {
        val response = FirebaseServiceCoroutines.instance.patchUser(user, userId)
        return if (response.isSuccessful) {
            ProfileRepoResponse.Success(response.body())
        } else {
            ProfileRepoResponse.Failure(response.message())
        }
    }
}


sealed class ProfileRepoResponse {
    class Success(val user: User?) : ProfileRepoResponse()
    class Failure(val errorMessage: String) : ProfileRepoResponse()
}
