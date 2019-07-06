package com.bookyrself.bookyrself.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.MiniUser
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventDetailViewModel(val eventId: String) : ViewModel() {

    var event = MutableLiveData<EventDetail?>()
    var invitees = MutableLiveData<MutableList<Pair<String, MiniUser>>>()

    init {
        loadEvent(eventId)
    }

    private fun loadEvent(eventId: String) {
        val eventDetailJob = FirebaseServiceCoroutines.instance.getEventData(eventId)

        CoroutineScope(Dispatchers.Main).launch {
            val eventDetailResponse = eventDetailJob.await()
            event.value = eventDetailResponse

            val userIds = eventDetailResponse.users?.keys
            loadEventsUsers(userIds)
        }
    }

    private fun loadEventsUsers(userIds: MutableSet<String>?) {
        val listOfInvitees = mutableListOf<Pair<String, MiniUser>>()

        CoroutineScope(Dispatchers.Main).launch {
            userIds?.forEach { userId ->
                //TODO handle network errors here via sealed Result class:
                // https://stackoverflow.com/questions/54077592/kotlin-coroutines-handle-error-and-implementation
                val userDetailResponse = FirebaseServiceCoroutines.instance.getUserDetails(userId).await()

                val miniUser = minifyUserDetailsForEventDetailDisplay(userId, userDetailResponse)
                val inviteeWithUserId = Pair(userId, miniUser)
                listOfInvitees.add(inviteeWithUserId)
            }

            invitees.value = listOfInvitees
        }
    }

    private fun minifyUserDetailsForEventDetailDisplay(userId: String, user: com.bookyrself.bookyrself.data.ServerModels.User.User): MiniUser {
        val miniUser = MiniUser()
        miniUser.citystate = user.citystate
        miniUser.url = user.url
        miniUser.username = user.username
        miniUser.userId = userId
        miniUser.attendingStatus = getAttendingStatus(user.events!![eventId])
        return miniUser
    }

    private fun getAttendingStatus(eventInviteInfo: EventInviteInfo?): String {
        var status = "Invited"
        if (eventInviteInfo != null) {
            if (eventInviteInfo.isInviteRejected != null) {
                if (eventInviteInfo.isInviteRejected!!) {
                    status = "Not attending"
                }
            }

            if (eventInviteInfo.isInviteAccepted != null) {
                if (eventInviteInfo.isInviteAccepted!!) {
                    status = "Attending"
                }
            }
        }
        return status
    }

    //TODO: Genericize this?
    class EventDetailViewModelFactory(private val eventId: String) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventDetailViewModel(eventId) as T
        }
    }
}