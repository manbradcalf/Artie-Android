package com.bookyrself.bookyrself.data.contacts

import android.content.Context
import com.bookyrself.bookyrself.data.SingletonHolder
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactsRepo private constructor(context: Context) {

    private var db: DatabaseReference? = null
    private var cacheIsDirty: Boolean = true
    private val contacts = HashMap<User, String>()
    private val service = FirebaseServiceCoroutines.instance

    companion object : SingletonHolder<ContactsRepo, Context>(::ContactsRepo)

    init {
        // Clear contacts on Sign Out
        FirebaseAuth.getInstance().addAuthStateListener()
        { auth ->
            if (auth.uid == null) {
                contacts.clear()
                cacheIsDirty = true
            }
        }
        // Add database listener
        if (FirebaseAuth.getInstance().uid != null) {
            this.db = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .child("contacts")

            this.db!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
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

    suspend fun getContacts(userId: String): ContactsRepoResponse {
        if (cacheIsDirty) {
            contacts.clear()
            val contactsResponse = service.getUserContacts(userId)
            if (contactsResponse.isSuccessful) {
                contactsResponse.body()?.keys?.forEach { contactUserId ->
                    val contactInfoResponse = service.getUserDetails(contactUserId)
                    if (contactInfoResponse.isSuccessful && contactInfoResponse.body() != null) {
                        contacts[contactInfoResponse.body()!!] = contactUserId
                    }
                }
            } else {
                ContactsRepoResponse.Failure("Error retrieving contacts for $userId" +
                        "\n" +
                        "${contactsResponse.errorBody()}")
            }
            cacheIsDirty = false
        }
        return ContactsRepoResponse.Success(contacts)
    }
}

sealed class ContactsRepoResponse {
    class Success(val contacts: HashMap<User, String>?) : ContactsRepoResponse()
    class Failure(val errorMessage: String) : ContactsRepoResponse()
}