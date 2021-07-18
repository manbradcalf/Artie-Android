package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookyrself.bookyrself.data.profile.ProfileRepo
import com.bookyrself.bookyrself.data.profile.ProfileRepoResponse.Failure
import com.bookyrself.bookyrself.data.profile.ProfileRepoResponse.Success
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragmentViewModel(application: Application) : BaseViewModel(application) {

    var user = MutableLiveData<User>()
    var events = MutableLiveData<HashMap<EventDetail, String>>()

    override fun load() {
        FirebaseAuth.getInstance().uid?.let {
            CoroutineScope(Dispatchers.IO).launch {
                when (val response = ProfileRepo.getInstance(getApplication()).getProfileInfo(it)) {
                    is Success -> {
                        user.postValue(response.user)
                    }
                    is Failure -> {
                        errorMessage.postValue(response.errorMessage)
                    }
                }
            }
        }
    }

    class ProfileFragmentViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProfileFragmentViewModel(application) as T
        }
    }
}