package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.EventDetail.MiniUser
import com.bookyrself.bookyrself.data.serverModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.serverModels.User.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDetailViewModel(application: Application, private val eventId: String) : BaseViewModel(application) {

    var event = MutableLiveData<EventDetail?>()
    var invitees = MutableLiveData<MutableList<Pair<String, MiniUser>>>()

    override fun load() {
        CoroutineScope(Dispatchers.IO).launch {
            val eventDetailCall = service.getEventData(eventId)
            if (eventDetailCall.isSuccessful) {
                val eventDetailResponse = eventDetailCall.body()
                val userIds = eventDetailResponse?.users?.keys
                loadEventsUsers(userIds)
                withContext(Dispatchers.Main) {
                    event.value = eventDetailResponse
                }
            }
        }
    }

    private fun loadEventsUsers(userIds: MutableSet<String>?) {
        val listOfInvitees = mutableListOf<Pair<String, MiniUser>>()

        CoroutineScope(Dispatchers.Main).launch {
            userIds?.forEach { userId ->
                //TODO handle network errors here via sealed Result class:
                val userDetailResponse = service.getUserDetails(userId)

                if (userDetailResponse.isSuccessful) {
                    // TODO: ugly not null assertion for unwrapping userDetailResponse.body
                    val miniUser = minifyUserDetailsForEventDetailDisplay(userId, userDetailResponse.body()!!)
                    val inviteeWithUserId = Pair(userId, miniUser)
                    listOfInvitees.add(inviteeWithUserId)
                }
            }
            invitees.value = listOfInvitees
        }
    }

    private fun minifyUserDetailsForEventDetailDisplay(userId: String, user: User): MiniUser {
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

    class EventDetailViewModelFactory(private val application: Application,
                                      private val eventId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventDetailViewModel(application, eventId) as T
        }
    }
}