package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.firebase.auth.FirebaseAuth

open class BaseViewModel(application: Application, requiresAuth: Boolean) : AndroidViewModel(application) {
    val errorMessage = MutableLiveData<String>()
    val service = FirebaseServiceCoroutines.instance

    init {
        if ((FirebaseAuth.getInstance().uid != null) || !requiresAuth) {
            this.load()
        }
    }
        open fun load() {}
    }
