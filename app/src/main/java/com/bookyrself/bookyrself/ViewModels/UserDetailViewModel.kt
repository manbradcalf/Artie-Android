package com.bookyrself.bookyrself.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.set

class UserDetailViewModel(userId: String) : ViewModel() {

    var user = MutableLiveData<User?>()
    var events = MutableLiveData<HashMap<EventDetail, String>>()
    var contactWasAdded = MutableLiveData<Boolean>()

    init {
        loadUserData(userId)
    }

    private fun loadUserData(userId: String) {
        val userJob = FirebaseServiceCoroutines.instance.getUserDetails(userId)

        CoroutineScope(Dispatchers.Main).launch {
            val userDetailResponse = userJob.await()

            //TODO Handle network errors here via sealed Result class
            // https://stackoverflow.com/questions/54077592/kotlin-coroutines-handle-error-and-implementation
            user.value = userDetailResponse

            val eventIds = userDetailResponse.events?.keys
            loadUsersEvents(eventIds)
        }
    }

    private fun loadUsersEvents(eventIds: Set<String>?) {
        val eventsHashMap = HashMap<EventDetail, String>()

        CoroutineScope(Dispatchers.Main).launch {
            eventIds?.forEach { eventId ->
                val eventJob = FirebaseServiceCoroutines.instance.getEventData(eventId)
                val eventDetailResponse = eventJob.await()
                eventsHashMap[eventDetailResponse] = eventId
            }
            events.value = eventsHashMap
        }
    }

    fun addContactToUser(contactId: String, userId: String) {

        val addContactJob = FirebaseServiceCoroutines.instance.addContactToUserAsync(true, userId, contactId)

        CoroutineScope(Dispatchers.Main).launch {
            val response = addContactJob.await()
            contactWasAdded.value = response
        }
    }

    //TODO: Genericize this?
    class UserDetailViewModelFactory(private val userId: String) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserDetailViewModel(userId) as T
        }
    }
}


