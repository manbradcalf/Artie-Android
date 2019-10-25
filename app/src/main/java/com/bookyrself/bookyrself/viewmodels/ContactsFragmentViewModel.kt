package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.data.serverModels.User.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsFragmentViewModel : BaseViewModel() {

    var contactsHashMap = MutableLiveData<HashMap<User, String>?>()

    override fun load() {
        //TODO: Logic copied from eventfragmentviewmodel, I should generesize?
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
}