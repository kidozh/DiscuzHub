package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class SecureInfoResult : BaseResult() {
    @JvmField
    @JsonProperty("Variables")
    var secureVariables: SecureVariables? = null

    class SecureVariables : VariableResults() {
        @JvmField
        @JsonProperty("sechash")
        var secHash: String = ""

        @JvmField
        @JsonProperty("seccode")
        var secCodeURL: String = ""
    }

    override fun isError(): Boolean {
        return message == null
    }
}