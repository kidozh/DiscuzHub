package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class AdminThreadViewModel(application: Application) : AndroidViewModel(application) {

    val adminStatusMutableLiveData: MutableLiveData<AdminStatus> = MutableLiveData(AdminStatus())
    val loadingStatusMutableLiveData : MutableLiveData<Boolean> = MutableLiveData(false)

    class AdminStatus{
        var operatePin: Boolean = false
        var operateDigest: Boolean = false
        var pinnedLevel : Int = 0
        var digestLevel: Int = 0
        var promote: Boolean = false
    }
}