package com.bookyrself.bookyrself.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsFragmentViewModel : BaseViewModel() {
    var contactsHashMap = MutableLiveData<HashMap<User, String>>()
    var isSignedIn = MutableLiveData<Boolean>()

    init {
        isSignedIn.value = FirebaseAuth.getInstance().uid != null

        if (isSignedIn.value == true) {
            //TODO: Why do I have to !! here if i'm null checking above

            loadContacts(FirebaseAuth.getInstance().uid!!)
        }
    }

    private fun loadContacts(userId: String) {

        //TODO: Logic copied from eventfragmentviewmodel, I should generesize?
        CoroutineScope(Dispatchers.IO).launch {
            val contactsResponse =
                    service.getUserContacts(userId)
            if (contactsResponse.isSuccessful && contactsResponse.body()?.keys != null) {
                contactsResponse.body()?.keys?.forEach { contactId ->
                    val contactsUserInfoResponse =
                            service.getUserDetails(contactId)
                    withContext(Dispatchers.Main) {
                        if (contactsUserInfoResponse.isSuccessful) {
                            when {
                                // User has contacts
                                contactsUserInfoResponse.body() != null -> {
                                    contactsHashMap.value?.set(contactsUserInfoResponse.body()!!, contactId)
                                }
                                // User does not have contacts
                                contactsUserInfoResponse.body() == null -> {
                                    Log.e("ContactsViewModel", "No data for userId $contactId")
                                }
                                else -> errorMessage.value = contactsUserInfoResponse.message()
                            }
                        }
                    }
                }
            }
        }
    }

    //TODO: Genericize this?
    class ContactsFragmentViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsFragmentViewModel() as T
        }
    }
}