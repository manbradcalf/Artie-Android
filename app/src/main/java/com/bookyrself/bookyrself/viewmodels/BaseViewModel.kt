package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth

open class BaseViewModel(application: Application, requiresAuth: Boolean) : AndroidViewModel(application) {
    val userId = FirebaseAuth.getInstance().uid
    val errorMessage = MutableLiveData<String>()
    val isSignedIn = MutableLiveData<Boolean>(userId != null)
    val service = FirebaseServiceCoroutines.instance

    init {
        //TODO: I can probably clean this logic up
        if (requiresAuth) {
            if (isSignedIn.value == true) {
                this.load()
            }
        } else {
            this.load()
        }
    }

    open fun load() {}
}
