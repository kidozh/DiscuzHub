package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonProperty

class ApiMessageActionResult : BaseResult() {
    @JsonProperty("Variables")
    var variableResults: VariableResults = VariableResults()
}