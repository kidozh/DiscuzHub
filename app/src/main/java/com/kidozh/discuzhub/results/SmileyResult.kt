package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.entities.Thread
import java.util.*

class SmileyResult : BaseResult() {
    @JsonProperty("Variables")
    lateinit var variables: SmileyVariables

    @JsonIgnoreProperties(ignoreUnknown = true)
    class SmileyVariables : VariableResults() {
        @JsonProperty("smilies")
        var smileyList: List<List<Smiley>> = ArrayList()
    }

    override fun isError(): Boolean {
        return message == null
    }
}