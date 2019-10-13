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

class UserDetailViewModel(userId: String) : BaseViewModel() {

    var user = MutableLiveData<User?>()
    var events = MutableLiveData<HashMap<EventDetail, String>>()
    var contactWasAdded = MutableLiveData<Boolean>()

    init {
        loadUserData(userId)
    }

    private fun loadUserData(userId: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val userResponse = service.getUserDetails(userId)
            withContext(Dispatchers.Main) {
                if (userResponse.isSuccessful) {
                    user.value = userResponse.body()
                    val eventIds = userResponse.body()?.events?.keys
                    loadUsersEvents(eventIds)
                } else {
                    errorMessage.value = userResponse.message()
                }
            }
        }
    }

    private suspend fun loadUsersEvents(eventIds: Set<String>?) {
        val eventsHashMap = HashMap<EventDetail, String>()

        CoroutineScope(Dispatchers.IO).launch {
            eventIds?.forEach { eventId ->
                val eventDetailResponse = service.getEventData(eventId)
                withContext(Dispatchers.Main) {
                    if (eventDetailResponse.isSuccessful) {
                        val eventDetail = eventDetailResponse.body()
                        if (eventDetail != null) {
                            eventsHashMap[eventDetail] = eventId
                            events.value = eventsHashMap
                        }
                    } else {
                        errorMessage.value = eventDetailResponse.message()
                    }
                }
            }
        }
    }

    fun addContactToUser(contactId: String, userId: String) {

        CoroutineScope(Dispatchers.Main).launch {
            val addContactResponse = service.addContactToUserAsync(true, userId, contactId)
            if (addContactResponse.isSuccessful) {
                if (addContactResponse.body() != null) {
                    contactWasAdded.value = addContactResponse.body()
                }
            } else {
                errorMessage.value = addContactResponse.message()
            }
        }
    }

    //TODO: Genericize this?
    class UserDetailViewModelFactory(private val userId: String) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserDetailViewModel(userId) as T
        }
    }
}