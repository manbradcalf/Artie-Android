package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.serverModels.User.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsFragmentViewModel(application: Application): BaseViewModel(application, true) {

    var contactsHashMap = MutableLiveData<HashMap<User, String>?>()

    override fun load() {
        //TODO: Move to UserRepo
        CoroutineScope(Dispatchers.IO).launch {
            val contactsResponse = service.getUserContacts(userId!!)
            if (contactsResponse.isSuccessful) {
                contactsResponse.body()?.keys?.forEach { contactId ->
                    val contactsUserInfoResponse = service.getUserDetails(contactId)
                    withContext(Dispatchers.Main) {
                        if (contactsUserInfoResponse.isSuccessful) {
                            contactsHashMap.value?.set(contactsUserInfoResponse.body()!!, contactId)
                        } else {
                            errorMessage.value = contactsUserInfoResponse.message()
                        }
                    }
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