package com.bookyrself.bookyrself.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth

open class BaseViewModel : ViewModel() {
    val userId = FirebaseAuth.getInstance().uid
    val errorMessage = MutableLiveData<String>()
    val isSignedIn = MutableLiveData<Boolean>(userId != null)
    val service = FirebaseServiceCoroutines.instance

    init {
        if (isSignedIn.value == true) {
            this.load()
        }
    }

    open fun load() {}

    //TODO: Genericize this?
    class BaseViewModelFactory: ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.newInstance()
        }
    }
}