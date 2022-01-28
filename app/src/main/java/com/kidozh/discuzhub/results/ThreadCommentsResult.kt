package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.Thread

@JsonIgnoreProperties(ignoreUnknown = true)
class ThreadCommentsResult : BaseResult() {
    @JsonProperty("Variables")
    var commentsVariables: CommentsVariables = CommentsVariables()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class CommentsVariables : VariableResults() {
        @JsonProperty("data")
        var forumThreadList: List<Thread> = ArrayList()

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("perpage")
        var perPage = 0
    }

    override fun isError(): Boolean {
        return message == null
    }
}