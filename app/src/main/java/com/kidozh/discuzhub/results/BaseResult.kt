package com.kidozh.discuzhub.results

import android.text.TextUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.ErrorMessage

open class BaseResult {
    @JsonProperty("Version")
    var apiVersion: Int = 4

    @JsonProperty("Charset")
    lateinit var Charset: String

    @JvmField
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("Message")
    var message: MessageResult? = null

    @JvmField
    @JsonIgnoreProperties(ignoreUnknown = true)
    var error = ""
    open fun isError(): Boolean {
        return message != null || !TextUtils.isEmpty(error)
    }

    val errorMessage: ErrorMessage?
        get() = if (message != null) {
            ErrorMessage(message!!.key, message!!.content)
        } else if (!TextUtils.isEmpty(error)) {
            ErrorMessage(error, error)
        } else {
            null
        }
}