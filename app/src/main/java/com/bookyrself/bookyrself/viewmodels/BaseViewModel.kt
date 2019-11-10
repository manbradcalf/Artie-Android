package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val errorMessage = MutableLiveData<String>()
    val service = FirebaseServiceCoroutines.instance
    open fun load() {}
}
