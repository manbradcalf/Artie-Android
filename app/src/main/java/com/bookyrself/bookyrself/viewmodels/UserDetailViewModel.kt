package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.User.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDetailViewModel(application: Application, private val userDetailId: String) : BaseViewModel(application) {

    var user = MutableLiveData<User?>()
    var events = MutableLiveData<HashMap<EventDetail, String>>()
    var contactWasAdded = MutableLiveData<Boolean>()

    init {
        load()
    }

    override fun load() {
        CoroutineScope(Dispatchers.IO).launch {
            val userResponse = service.getUserDetails(userDetailId)
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

    class UserDetailViewModelFactory(private val application: Application,
                                     private val userId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserDetailViewModel(application, userId) as T
        }
    }
}