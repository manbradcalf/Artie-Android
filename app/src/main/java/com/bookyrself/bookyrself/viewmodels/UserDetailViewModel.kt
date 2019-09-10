package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDetailViewModel(userId: String) : ViewModel() {

    var user = MutableLiveData<User?>()
    var events = MutableLiveData<HashMap<EventDetail, String>>()
    var contactWasAdded = MutableLiveData<Boolean>()
    var responseErrorMessage = MutableLiveData<String>()

    init {
        loadUserData(userId)
    }

    private fun loadUserData(userId: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val userResponse = FirebaseServiceCoroutines.instance.getUserDetails(userId)
            withContext(Dispatchers.Main) {
                if (userResponse.isSuccessful) {
                    user.value = userResponse.body()
                    val eventIds = userResponse.body()?.events?.keys
                    loadUsersEvents(eventIds)
                } else {
                    responseErrorMessage.value = userResponse.message()
                }
            }
        }
    }

    private suspend fun loadUsersEvents(eventIds: Set<String>?) {
        val eventsHashMap = HashMap<EventDetail, String>()

        CoroutineScope(Dispatchers.IO).launch {
            eventIds?.forEach { eventId ->
                val eventDetailResponse = FirebaseServiceCoroutines.instance.getEventData(eventId)
                withContext(Dispatchers.Main) {
                    if (eventDetailResponse.isSuccessful) {
                        val eventDetail = eventDetailResponse.body()
                        if (eventDetail != null) {
                            eventsHashMap[eventDetail] = eventId
                            events.value = eventsHashMap
                        }
                    }
                }
            }
        }
    }

    fun addContactToUser(contactId: String, userId: String) {

        val addContactJob = FirebaseServiceCoroutines.instance.addContactToUserAsync(true, userId, contactId)

        CoroutineScope(Dispatchers.Main).launch {
            val response = addContactJob
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