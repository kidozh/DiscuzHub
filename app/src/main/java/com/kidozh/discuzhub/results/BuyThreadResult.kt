package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

class BuyThreadResult : BaseResult() {
    @JsonProperty("Variables")
    var variableResults: BuyThreadVariableResult = BuyThreadVariableResult()

    class BuyThreadVariableResult : VariableResults() {
        @JsonProperty("authorid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var authorId = 0
        var author: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var price = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var balance = 0
        var credit: Credit = Credit()
    }

    class Credit {
        var title = ""

        @JsonIgnoreProperties(ignoreUnknown = true)
        var unit = ""
    }
}