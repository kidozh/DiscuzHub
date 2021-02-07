package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.PrivateMessage
import com.kidozh.discuzhub.entities.Thread
import java.util.ArrayList


class PrivateMessageResult  : BaseResult() {
    @JsonProperty("Variables")
    lateinit var variables: PrivateMessageVariables

    @JsonIgnoreProperties(ignoreUnknown = true)
    class PrivateMessageVariables : VariableResults() {
        @JsonProperty("list")
        var pmList : List<PrivateMessage> = ArrayList()
        var count : Int = 0
        @JsonProperty("perpage")
        var perPage: Int = 0
        var page: Int = 0
        var pmid : Int = 0
    }

    override fun isError(): Boolean {
        return message == null
    }
}