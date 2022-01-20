package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class BaseStatusViewModel(application: Application) : AndroidViewModel(application) {

    val notifyExpiredUserId : MutableLiveData<Int> = MutableLiveData(-1)
}