package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventInvitesFragmentViewModel : BaseViewModel() {
    var eventsWithPendingInvites = MutableLiveData<HashMap<EventDetail, String>>()
    var noEventsWithPendingInvitesReturned = MutableLiveData<Boolean>()

    // TODO: I really hate having to manage all these different hashmaps and lists
    private val eventIdsOfPendingEvents = mutableListOf<String>()
    private val eventsOfPendingInvitesHashMap = HashMap<EventDetail, String>()

    override fun load() {
        CoroutineScope(Dispatchers.IO).launch {
            val eventInvitesResponse =
                    service.getUsersEventInvites(userId!!)

            //TODO: Look at the logic here. I want to notify view if we make a call but there are no pending invites
            if (eventInvitesResponse.isSuccessful) {
                // Get all the event invites and grab only pending invites
                withContext(Dispatchers.Main) {
                    eventInvitesResponse.body()?.forEach { entry ->
                        if (isInvitePendingResponse(entry)) {
                            eventIdsOfPendingEvents.add(entry.key)
                        }
                    }
                    // Now get all the pending invite details
                    for (eventId in eventIdsOfPendingEvents) {
                        val eventWithPendingInviteResponse =
                                service.getEventData(eventId)
                        if (eventWithPendingInviteResponse.body() != null) {
                            // update function local EventDetail, EventId hashmap
                            eventsOfPendingInvitesHashMap[eventWithPendingInviteResponse.body()!!] = eventId
                            // set LiveData value to local hashmap
                            eventsWithPendingInvites.value = eventsOfPendingInvitesHashMap
                        }
                    }
                }
            }
        }
    }

    fun respondToInvite(accepted: Boolean, eventId: String, eventDetail: EventDetail) {
        when (accepted) {
            true -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val acceptInviteResponse = service.acceptInvite(true, userId!!, eventId)
                    if (acceptInviteResponse.isSuccessful) {
                        val updateEventDetailWithUserResponse =
                                service.setEventUserAsAttending(true, userId, eventId)
                        if (updateEventDetailWithUserResponse.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                // TODO: I really hate having to manage all these different hashmaps and lists
                                eventsOfPendingInvitesHashMap.remove(eventDetail)
                                eventsWithPendingInvites.value = eventsOfPendingInvitesHashMap
                            }
                        } else {
                            // TODO: Failed to update userId node on event object
                        }
                    } else {
                        // TODO: Failed to update invite node on user object
                    }
                }
            }
            false -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val rejectInviteResponse = service.rejectInvite(true, userId!!, eventId)
                    if (rejectInviteResponse.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            // TODO: I really hate having to manage all these different hashmaps and lists
                            eventsOfPendingInvitesHashMap.remove(eventDetail)
                            eventsWithPendingInvites.value = eventsOfPendingInvitesHashMap
                        }
                    }
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
}