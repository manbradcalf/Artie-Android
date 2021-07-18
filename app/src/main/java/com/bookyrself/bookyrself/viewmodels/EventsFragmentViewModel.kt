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

class EventsFragmentViewModel(application: Application) : BaseViewModel(application) {
    var eventDetails = MutableLiveData<HashMap<EventDetail, String>>()

    override fun load() {
        FirebaseAuth.getInstance().uid?.let {
            CoroutineScope(Dispatchers.IO).launch {
                when (val response =
                        EventsRepository
                                .getInstance(getApplication())
                                .getAllEventsForUser(it)) {
                    is Success -> {
                        eventDetails.postValue(response.events)
                    }
                    is Failure -> {
                        errorMessage.postValue(response.errorMessage)
                    }
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