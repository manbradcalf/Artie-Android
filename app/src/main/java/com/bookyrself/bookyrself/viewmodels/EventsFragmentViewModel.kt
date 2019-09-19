package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.bookyrself.bookyrself.services.clients.UsersClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by benmedcalf on 3/11/18.
 */
class EventsFragmentViewModel : ViewModel() {
    private var userId: String? = null
    var eventDetailsHashMap = MutableLiveData<HashMap<EventDetail, String>>()
    var errorMessage = MutableLiveData<String>()
    var signedOutMessage = MutableLiveData<String>()

    init {
        if (FirebaseAuth.getInstance().uid != null) {
            userId = FirebaseAuth.getInstance().uid
        } else {
            signedOutMessage.value = "Sign in to see your events!"
        }
    }

    //TODO: Copied over from UserDetailViewModel. This will also be needed in ProfileViewModel. How to consolidte considering they rely on livedata in the activity?
    private fun loadUsersEventInfo(userId: String) {
        //TODO: Check if the cache is dirty here. If it is we need to go to network
        val events = HashMap<EventDetail, String>()

        CoroutineScope(Dispatchers.IO).launch {
            val userResponse = UsersClient.service.getUserDetails(userId)
            if (userResponse.isSuccessful && userResponse.body()?.events?.keys != null) {
                userResponse.body()?.events?.keys?.forEach { eventId ->
                    val eventDetailResponse = FirebaseServiceCoroutines.instance.getEventData(eventId)
                    withContext(Dispatchers.Main) {
                        if (eventDetailResponse.isSuccessful) {
                            val eventDetail = eventDetailResponse.body()
                            if (eventDetail != null) {
                                events[eventDetail] = eventId
                                eventDetailsHashMap.value = events
                            } else {
                                errorMessage.value = eventDetailResponse.message()
                            }
                        }
                    }
                }
            }
        }
    }

    //TODO: Genericize this?
    class EventsFragmentViewModelFactory(): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventsFragmentViewModel() as T
        }
    }
}

