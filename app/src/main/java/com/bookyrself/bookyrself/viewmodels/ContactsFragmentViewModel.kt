package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.contacts.ContactsRepo
import com.bookyrself.bookyrself.data.contacts.ContactsRepoResponse.*
import com.bookyrself.bookyrself.data.serverModels.User.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsFragmentViewModel(application: Application) : BaseViewModel(application, true) {
    val contactsHashMap = MutableLiveData<HashMap<User, String>?>()

    override fun load() {
        //TODO: Move to UserRepo
        CoroutineScope(Dispatchers.IO).launch {
            when (val response =
                    ContactsRepo
                            .getInstance(getApplication())
                            .getContacts(userId!!)) {
                is Success -> {
                    contactsHashMap.postValue(response.contacts)
                }
                is Failure -> {
                    errorMessage.postValue(response.errorMessage)
                }
            }
        }
    }

    class ContactsFragmentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsFragmentViewModel(application) as T
        }
    }
}