package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class LoginResult : BaseResult() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("Variables")
    var variables: LoginVariableResult = LoginVariableResult()

    class LoginVariableResult : VariableResults() {
        var loginUrl: String? = null
    }
}