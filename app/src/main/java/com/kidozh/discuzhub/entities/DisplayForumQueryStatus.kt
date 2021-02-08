package com.kidozh.discuzhub.entities

import android.util.Log
import java.util.*

class DisplayForumQueryStatus(var fid: Int, page: Int) {
    @JvmField
    var page = 1
    var perPage = 10
    @JvmField
    var hasLoadAll = false

    // orderby:[dateline,replies,views]
    var orderBy = ""
    var specialType = ""

    // filter:
    var filter = ""
    var filterTypeId = -1
    var dateline = 0
    fun clear() {
        page = 1
        perPage = 15
        orderBy = ""
        filterTypeId = 0
        filter = ""
    }

    fun setInitAuthorId(authorId: Int) {
        page = 1
        hasLoadAll = false
    }

    fun setInitPage(page: Int) {
        this.page = page
        hasLoadAll = false
    }

    fun generateQueryHashMap(): HashMap<String, String> {
        val options = HashMap<String, String>()
        options["fid"] = fid.toString()
        options["page"] = page.toString()
        options["ppp"] = perPage.toString()
        if (orderBy != "") {
            options["orderby"] = orderBy
        }
        Log.d(TAG, "Type id " + filterTypeId)
        if (filterTypeId != -1) {
            options["typeid"] = filterTypeId.toString()
            options["filter"] = "typeid"
        }
        if (orderBy != "") {
            options["orderby"] = orderBy
            options["filter"] = "reply"
        }
        if (specialType != "") {
            options["specialtype"] = specialType
            options["filter"] = "specialtype"
        }
        if (dateline != 0) {
            options["dateline"] = dateline.toString()
            options["filter"] = "dateline"
        }
        if (filter != "") {
            options["filter"] = filter
        }
        return options
    }

    companion object {
        private val TAG = DisplayForumQueryStatus::class.java.simpleName
    }

    init {
        this.page = page
    }
}