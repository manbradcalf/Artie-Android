package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationActivityViewModel(application: Application) : BaseViewModel(application) {
    val userLiveData = MutableLiveData<User>()

    fun createUser(user: User, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = FirebaseServiceCoroutines.instance.updateUser(user, userId)
            if (response.isSuccessful) {
                userLiveData.postValue(response.body())
            } else {
                errorMessage.postValue(response.message())
            }
        }
    }
}

class AuthenticationActivityViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthenticationActivityViewModel(application) as T
    }
}