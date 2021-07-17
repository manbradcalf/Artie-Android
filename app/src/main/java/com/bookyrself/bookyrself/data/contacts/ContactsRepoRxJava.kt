package com.bookyrself.bookyrself.data.contacts

import android.util.Log
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*

class ContactsRepoRxJava : ContactsDataSource {

    private var cacheIsDirty: Boolean? = null
    private val contactsMap: HashMap<String, User> = HashMap()
    private var db: DatabaseReference? = null

    init {
        this.cacheIsDirty = true

        FirebaseAuth.getInstance().addAuthStateListener {
            contactsMap.clear()
            cacheIsDirty = true
        }

        if (FirebaseAuth.getInstance().uid != null) {
            this.db = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .child("contacts")

            this.db!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    cacheIsDirty = true
                    Log.e("Contacts Repo: ", "Child added")
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    cacheIsDirty = true
                    Log.e("Contacts Repo: ", "Child changed")
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    cacheIsDirty = true
                    Log.e("Contacts Repo: ", "Child removed")
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                    cacheIsDirty = true
                    Log.e("Contacts Repo: ", "Child moved")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    cacheIsDirty = true
                    Log.e("Contacts Repo: ", databaseError.message)
                }
            })
        }
    }

    /**
     * Methods
     */
    override fun getContactsForUser(userId: String): Observable<Map.Entry<String, User>> {

        if (cacheIsDirty!!) {
            // Cache is dirty, get from network
            return FirebaseService.instance
                    .getUserContacts(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { it.entries }
                    .firstOrError()
                    .flattenAsObservable { it }
                    .flatMap { entry ->
                        FirebaseService.instance
                        // Get each contacts user info
                                .getUserDetails(entry.key)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map {
                                    // add the contact to our user's contacts repository
                                    contactsMap[entry.key] = it
                                    cacheIsDirty = false
                                    AbstractMap.SimpleEntry<String, User>(entry.key, it)
                                }
                    }
        } else {
            // Cache is clean, get local copy
            return Observable.fromIterable(contactsMap.entries)
                    .map { AbstractMap.SimpleEntry<String, User>(it.key, it.value) }
        }
    }
}