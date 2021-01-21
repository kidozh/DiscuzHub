package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.ThreadInfo
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class NewThreadsResult : BaseResult() {
    @JsonProperty("Variables")
    lateinit var forumVariables: ForumVariables

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ForumVariables : VariableResults() {
        @JsonProperty("data")
        var forumThreadList: List<ThreadInfo> = ArrayList()
    }

    override fun isError(): Boolean {
        return message == null
    }
}