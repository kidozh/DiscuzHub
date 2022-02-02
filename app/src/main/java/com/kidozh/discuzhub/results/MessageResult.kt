package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.ErrorMessage
import java.io.Serializable

class MessageResult : Serializable {
    @JsonProperty("messagestr")
    var content = ""

    @JsonProperty("messageval")
    var key = ""
    fun toErrorMessage(): ErrorMessage {
        return ErrorMessage(key, content)
    }
}