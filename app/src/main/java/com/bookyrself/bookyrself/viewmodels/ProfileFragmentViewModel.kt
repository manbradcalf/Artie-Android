package com.bookyrself.bookyrself.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.User.*

class ProfileFragmentViewModel(application: Application) : BaseViewModel(application, true) {

    var user = MutableLiveData<User>()
    var events = MutableLiveData<HashMap<EventDetail,String>>()
}