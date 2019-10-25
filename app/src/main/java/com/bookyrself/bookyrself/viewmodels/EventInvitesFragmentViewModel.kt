package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.events.EventsRepository
import com.bookyrself.bookyrself.data.events.EventsRepositoryResponse.*
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EventInvitesFragmentViewModel(application: Application) : BaseViewModel(application, true) {
    val eventsWithPendingInvites = MutableLiveData<HashMap<EventDetail, String>>()

    private val eventsRepo = EventsRepository.getInstance(application)

    override fun load() {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = eventsRepo.getEventsWithPendingInvites(userId!!)) {
                is Success -> {
                    eventsWithPendingInvites.value = response.events
                }
                is Failure -> {
                    errorMessage.value = response.errorMessage
                }
            }
        }
    }

    fun respondToInvite(accepted: Boolean, eventId: String, eventDetail: EventDetail) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = eventsRepo.respondToInvite(accepted, userId!!, eventId, eventDetail)) {
                is Success -> {
                    eventsWithPendingInvites.value = response.events
                }
                is Failure -> {
                    errorMessage.value = response.errorMessage
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