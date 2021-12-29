package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.Thread
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class DisplayThreadsResult : BaseResult() {
    @JsonProperty("Variables")
    var forumVariables: ForumVariables = ForumVariables()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ForumVariables : VariableResults() {
        @JsonProperty(value = "data")
        var forumThreadList: List<Thread>? = ArrayList()

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("perpage")
        var perPage = 0
    }

    override fun isError(): Boolean {
        return message == null
    }
}