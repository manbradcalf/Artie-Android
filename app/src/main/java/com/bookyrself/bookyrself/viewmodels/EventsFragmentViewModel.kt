package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.events.EventsRepository
import com.bookyrself.bookyrself.data.events.EventsRepositoryResponse.Failure
import com.bookyrself.bookyrself.data.events.EventsRepositoryResponse.Success
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by benmedcalf on 3/11/18.
 */
class EventsFragmentViewModel(application: Application) : BaseViewModel(application, true) {
    var eventDetailsHashMap = MutableLiveData<HashMap<EventDetail, String>>()
    private val eventsRepo = EventsRepository.getInstance(application)

    override fun load() {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = eventsRepo.getAllEvents(userId!!)) {
                is Success -> {
                    eventDetailsHashMap.value = response.events
                }
                is Failure -> {
                    errorMessage.value = response.errorMessage
                }
            }
        }
    }

    class EventsFragmentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventsFragmentViewModel(application) as T
        }
    }
}