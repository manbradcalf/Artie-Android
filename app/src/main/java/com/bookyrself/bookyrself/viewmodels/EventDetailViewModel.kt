package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.EventDetail.MinifiedUser
import com.bookyrself.bookyrself.data.serverModels.user.EventInviteInfo
import com.bookyrself.bookyrself.data.serverModels.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDetailViewModel(application: Application, private val eventId: String) : BaseViewModel(application) {

    var event = MutableLiveData<EventDetail?>()
    var invitees = MutableLiveData<MutableList<User>>()

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
        val listOfInvitees = mutableListOf<User>()

        CoroutineScope(Dispatchers.Main).launch {
            userIds?.forEach { userId ->
                //TODO handle network errors here via sealed Result class:
                val userDetailResponse = service.getUserDetails(userId)

                if (userDetailResponse.isSuccessful && userDetailResponse.body() != null) {
                    // TODO: ugly not null assertion for unwrapping userDetailResponse.body
                    val user = userDetailResponse.body()
                    user?.userId = userId
                    user?.let { listOfInvitees.add(it) }
                }

            }
            invitees.value = listOfInvitees
        }
    }

    private fun minifyUserDetailsForEventDetailDisplay(userId: String, user: User): MinifiedUser {
        val miniUser = MinifiedUser()
        miniUser.citystate = user.citystate
        miniUser.url = user.url
        miniUser.username = user.username
        miniUser.userId = userId
        miniUser.attendingStatus = getAttendingStatus(user.events!![eventId])
        return miniUser
    }

    fun getAttendingStatus(eventInviteInfo: EventInviteInfo?): String {
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