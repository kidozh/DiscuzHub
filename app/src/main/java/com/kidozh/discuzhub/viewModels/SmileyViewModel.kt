package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.PagedList
import com.kidozh.discuzhub.daos.SmileyDao
import com.kidozh.discuzhub.database.SmileyDatabase

class SmileyViewModel(application: Application) : AndroidViewModel(application) {

    var myPagingConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build()
    lateinit var dao : SmileyDao

    init {
        dao = SmileyDatabase.getInstance(application).getDao()
    }
}