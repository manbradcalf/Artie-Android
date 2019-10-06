package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventInvitesFragmentViewModel : ViewModel() {
    var eventsWithPendingInvites = MutableLiveData<HashMap<EventDetail, String>>()
    var isSignedIn = MutableLiveData<Boolean>()

    init {
        isSignedIn.value = FirebaseAuth.getInstance().uid != null

        if (isSignedIn.value == true) {
            //TODO: Why do I have to !! here if i'm null checking above
            loadPendingInvites(FirebaseAuth.getInstance().uid!!)
        }
    }

    fun acceptInvite(eventId: String) {

    }

    fun rejectInvite(eventId: String) {

    }

    private fun loadPendingInvites(userId: String) {
        val eventIdsOfPendingEvents = mutableListOf<String>()
        val eventsOfPendingInvitesHashMap = HashMap<EventDetail, String>()

        CoroutineScope(Dispatchers.IO).launch {
            val eventInvitesResponse =
                    FirebaseServiceCoroutines.instance.getUsersEventInvites(userId)

            if (eventInvitesResponse.isSuccessful) {
                // Get all the event invites and grab only pending invites
                withContext(Dispatchers.Main) {
                    eventInvitesResponse.body()?.forEach { entry ->
                        if (isInvitePendingResponse(entry)) {
                            // InviteInfo is the key for eventInvites live data because intent creation in recyclerview in fragment
                            eventIdsOfPendingEvents.add(entry.key)
                        }
                    }

                    // Now get all the pending invite details
                    for (eventId in eventIdsOfPendingEvents) {
                        val eventWithPendingInviteResponse =
                                FirebaseServiceCoroutines.instance.getEventData(eventId)
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

    private fun isInvitePendingResponse(eventInvite: Map.Entry<String, EventInviteInfo>?): Boolean {
        // All fields are false by default, so if all fields remain false,
        // because every interaction flips a flag to true,
        // it means the user hasn't interacted at all with the invite so the invite is pending
        return ((!eventInvite?.value?.isInviteAccepted!!)
                && (!eventInvite.value.isInviteRejected!!)
                && (!eventInvite.value.isHost!!))
    }

    fun acceptInvite(accepted: Boolean, userId: String, eventInvite: Map.Entry<String, EventDetail>) {

    }

    //TODO: Genericize this?
    class EventInvitesFragmentViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventInvitesFragmentViewModel() as T
        }
    }
}