package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.events.EventsRepository
import com.bookyrself.bookyrself.data.events.EventsRepositoryResponse.Failure
import com.bookyrself.bookyrself.data.events.EventsRepositoryResponse.Success
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EventInvitesFragmentViewModel(application: Application) : BaseViewModel(application) {
    val eventsWithPendingInvites = MutableLiveData<HashMap<EventDetail, String>>()

    override fun load() {
        val userId = FirebaseAuth.getInstance().uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                when (val response =
                        EventsRepository
                                .getInstance(getApplication())
                                .getEventsWithPendingInvites(userId)) {
                    is Success -> {
                        eventsWithPendingInvites.postValue(response.events)
                    }
                    is Failure -> {
                        errorMessage.postValue(response.errorMessage)
                    }
                }
            }
        }
    }

    fun respondToInvite(accepted: Boolean, eventId: String, eventDetail: EventDetail) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response =
                    EventsRepository.getInstance(getApplication())
                            .respondToInvite(accepted, FirebaseAuth.getInstance().uid!!, eventId, eventDetail)) {
                is Success -> {
                    eventsWithPendingInvites.postValue(response.events)
                }
                is Failure -> {
                    errorMessage.postValue(response.errorMessage)
                }
            }
        }
    }

    class EventInvitesFragmentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventInvitesFragmentViewModel(application) as T
        }
    }
}