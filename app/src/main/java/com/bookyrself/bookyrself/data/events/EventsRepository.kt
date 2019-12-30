package com.bookyrself.bookyrself.data.events

import android.content.Context
import android.util.Log
import com.bookyrself.bookyrself.data.SingletonHolder
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.user.EventInviteInfo
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EventsRepository private constructor(context: Context) {

    private val allUsersEvents = HashMap<EventDetail, String>()
    private val eventsOfPendingInvites = HashMap<EventDetail, String>()
    private var cacheIsDirty: Boolean = true
    private var db: DatabaseReference? = null
    private val service = FirebaseServiceCoroutines.instance

    companion object : SingletonHolder<EventsRepository, Context>(::EventsRepository)

    init {
        // Clear events on Sign Out
        FirebaseAuth.getInstance().addAuthStateListener()
        { auth ->
            if (auth.uid == null) {
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

    suspend fun getAllEventsForUser(userId: String): EventsRepositoryResponse {
        return if (cacheIsDirty) {
            allUsersEvents.clear()
            // go to server
            val userResponse = service.getUserDetails(userId)
            if (userResponse.isSuccessful) {
                // clear and update allUserEvents
                allUsersEvents.clear()

                userResponse.body()?.events?.keys?.forEach { eventId ->
                    val eventResponse = service.getEventData(eventId)
                    if (eventResponse.isSuccessful) {
                        eventResponse.body()?.let { allUsersEvents.put(it, eventId) }
                    } else {
                        Log.e("EventsRepo", "Unable to fetch event detail $eventId for user $userId" +
                                "\nError:" +
                                "\n${eventResponse.errorBody()}")
                    }
                }
                EventsRepositoryResponse.Success(allUsersEvents)
            } else {
                EventsRepositoryResponse.Failure("Unable to find user with userId $userId")
            }
        } else {
            EventsRepositoryResponse.Success(allUsersEvents)
        }
    }

    suspend fun getEventsWithPendingInvites(userId: String): EventsRepositoryResponse {
        if (cacheIsDirty) {
            eventsOfPendingInvites.clear()
            val userResponse = service.getUserDetails(userId)
            if (userResponse.isSuccessful) {
                userResponse.body()?.events?.filter { isInvitePendingResponse(it) }?.keys?.forEach { eventId ->
                    val eventWithPendingInviteResponse = service.getEventData(eventId)
                    if (eventWithPendingInviteResponse.isSuccessful) {
                        eventWithPendingInviteResponse.body()?.let { eventsOfPendingInvites[it] = eventId }
                    }
                }
            } else {
                return EventsRepositoryResponse.Failure("Unable to find user with userId $userId")
            }
        }
        return EventsRepositoryResponse.Success(eventsOfPendingInvites)
    }

    suspend fun respondToInvite(accepted: Boolean, userId: String, eventId: String, eventDetail: EventDetail): EventsRepositoryResponse {
        return if (accepted) {
            if (service.acceptInvite(true, userId, eventId).isSuccessful &&
                    service.setEventUserAsAttending(true, userId, eventId).isSuccessful) {
                eventsOfPendingInvites.remove(eventDetail)
                EventsRepositoryResponse.Success(eventsOfPendingInvites)
            } else {
                EventsRepositoryResponse.Failure("Unable to accept Invite")
            }
        } else {
            return if (service.rejectInvite(true, userId, eventId).isSuccessful) {
                eventsOfPendingInvites.remove(eventDetail)
                EventsRepositoryResponse.Success(eventsOfPendingInvites)
            } else {
                EventsRepositoryResponse.Failure("Unable to accept Invite")
            }
        }
    }
}

private fun isInvitePendingResponse(eventInvite: Map.Entry<String, EventInviteInfo>?): Boolean {
    // All fields are false by default, so if all fields remain false,
    // because every interaction flips a flag to true,
    // it means the user hasn't interacted at all with the invite so the invite is pending
    return ((!eventInvite?.value?.isInviteAccepted!!)
            && (!eventInvite.value.isInviteRejected!!)
            && (!eventInvite.value.isHost!!))
}

sealed class EventsRepositoryResponse {
    class Success(val events: HashMap<EventDetail, String>?) : EventsRepositoryResponse()
    class Failure(val errorMessage: String) : EventsRepositoryResponse()
}
