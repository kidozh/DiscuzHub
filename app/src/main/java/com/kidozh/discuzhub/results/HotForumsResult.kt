package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.HotForum

@JsonIgnoreProperties(ignoreUnknown = true)
class HotForumsResult : BaseResult() {
    @JvmField
    @JsonProperty("Variables")
    var variables: HotForumVariables = HotForumVariables()

    class HotForumVariables : VariableResults() {
        @JvmField
        @JsonProperty("data")
        var hotForumList: List<HotForum> = ArrayList()
    }

    override fun isError(): Boolean {
        return message != null
    }
}