package com.bookyrself.bookyrself.data.Events

import android.content.Context
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.services.FirebaseService
import com.bookyrself.bookyrself.utils.TinyDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Flowable
import io.reactivex.Flowable.fromIterable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.*
import kotlin.NoSuchElementException

class EventsRepository(context: Context) : EventDataSource {


    private val attendingEventsStrings: ArrayList<String>

    private val eventsWithPendingInvites: HashMap<String, EventDetail>
    private val allUsersEvents: HashMap<String, EventDetail>
    private var cacheIsDirty: Boolean = false
    private var db: DatabaseReference? = null
    private val tinyDB: TinyDB


    init {
        this.cacheIsDirty = true
        this.eventsWithPendingInvites = HashMap()
        this.allUsersEvents = HashMap()
        this.attendingEventsStrings = ArrayList()
        this.tinyDB = TinyDB(context)

        // Clear events on Sign Out
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.uid == null) {
                tinyDB.clear()
                eventsWithPendingInvites.clear()
                allUsersEvents.clear()
                cacheIsDirty = true
            }
        }

        // Add database listener
        if (FirebaseAuth.getInstance().uid != null) {
            this.db = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .child("events")


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

    override fun getAllEvents(userId: String): Flowable<Map.Entry<String, EventDetail>> {

        if (cacheIsDirty) {
            return FirebaseService.instance
                    .getUsersEventInvites(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .firstOrError()
                    .toFlowable()
                    // send down an entry<string, eventinviteInfo>
                    .flatMapIterable<Map.Entry<String, EventInviteInfo>> { it.entries }
                    .flatMap<Map.Entry<String, EventDetail>> { eventInvite ->
                        FirebaseService.instance
                                .getEventData(eventInvite.key)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map<AbstractMap.SimpleEntry<String, EventDetail>> { eventDetail ->

                                    // Populate list of strings describing attending events for the widget
                                    if (eventInvite.value.isInviteAccepted!! || eventInvite.value.getIsInviteAccepted()!!) {

                                        if (!attendingEventsStrings.contains(String.format("%s in %s on %s", eventDetail.eventname, eventDetail.citystate, eventDetail.date))) {
                                            attendingEventsStrings.add(String.format("%s in %s on %s", eventDetail.eventname, eventDetail.citystate, eventDetail.date))
                                            tinyDB.putListString("attendingEventsString", attendingEventsStrings)
                                        }
                                    }
                                    // Regardless, add all events to allUserEvents hashmap
                                    allUsersEvents[eventInvite.key] = eventDetail
                                    AbstractMap.SimpleEntry(eventInvite.key, eventDetail)
                                }
                    }

        } else {
            // Cache is clean, get local copy
            return allUsersEvents.entries
                    .map { AbstractMap.SimpleEntry<String, EventDetail>(it.key, it.value) }
                    .toFlowable()
        }
    }

    override fun getEventsWithPendingInvites(userId: String): Flowable<AbstractMap.SimpleEntry<String, EventDetail>>? {

        if (cacheIsDirty) {
            // Cache is dirty so fetch from the service
            return FirebaseService.instance
                    .getUsersEventInvites(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map<Set<Map.Entry<String, EventInviteInfo>>> { it.entries }
                    .flatMapIterable { entries -> entries }
                    .filter { entry -> !isInvitePendingResponse(entry) }

                    // Now check if the list of pending invites is empty
                    .firstOrError()
                    .toFlowable()

                    // Fetch event info for each pending invite
                    .flatMap { eventInvite ->
                        FirebaseService.instance
                                .getEventData(eventInvite.key)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map<AbstractMap.SimpleEntry<String, EventDetail>> { eventDetail ->
                                    eventsWithPendingInvites[eventInvite.key] = eventDetail
                                    cacheIsDirty = false
                                    AbstractMap.SimpleEntry(eventInvite.key, eventDetail)
                                }
                    }
        } else {
            // Cache is clean, so we fetch from cache
            return if (eventsWithPendingInvites.isNotEmpty()) {
                fromIterable(eventsWithPendingInvites.entries)
                        .map { entry -> AbstractMap.SimpleEntry<String, EventDetail>(entry.key, entry.value) }
            } else {
                // No events in cache have pending invites
                fromIterable(eventsWithPendingInvites.entries)
                        //TODO: this translation from mutable to abstract map seems pointless
                        .map { entry -> AbstractMap.SimpleEntry<String, EventDetail>(entry.key, entry.value) }
                        .doOnNext { throw NoSuchElementException() }
            }
        }
    }

    override fun acceptEventInvite(userId: String, eventId: String): Flowable<Boolean> {
        return FirebaseService.instance
                .acceptInvite(true, userId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    FirebaseService.instance
                            .setEventUserAsAttending(true, userId, eventId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
                .doOnNext {
                    // Remove from pending invitations cache
                    eventsWithPendingInvites.remove(eventId)
                }
    }

    override fun rejectEventInvite(userId: String, eventId: String): Flowable<Response<Void>> {
        return FirebaseService.instance.rejectInvite(true, userId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    FirebaseService.instance
                            .setEventUserAsAttending(false, userId, eventId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
                .flatMap {
                    FirebaseService.instance
                            .removeUserFromEvent(eventId, userId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext {
                                allUsersEvents.remove(eventId)
                                eventsWithPendingInvites.remove(eventId)
                            }
                }
    }
}
