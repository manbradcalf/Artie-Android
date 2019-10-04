package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventInvitesFragmentViewModel : ViewModel() {
    var eventInvites = MutableLiveData<HashMap<EventInviteInfo, String>>()

    fun acceptInvite(eventId: String) {

    }

    fun rejectInvite(eventId: String) {

    }

    fun loadPendingInvites(userId: String) {
        val pendingEventInvites = HashMap<EventInviteInfo, String>()

        CoroutineScope(Dispatchers.IO).launch {
            val eventInvitesResponse =
                    FirebaseServiceCoroutines.instance.getUsersEventInvites(userId)
            if (eventInvitesResponse.isSuccessful) {
                withContext(Dispatchers.Main) {
                    eventInvitesResponse.body()?.forEach { entry ->
                        if (isInvitePendingResponse(entry)) {
                            // InviteInfo is the key for eventInvites live data because intent creation in recyclerview
                            // in fragment
                            pendingEventInvites[entry.value] = entry.key
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