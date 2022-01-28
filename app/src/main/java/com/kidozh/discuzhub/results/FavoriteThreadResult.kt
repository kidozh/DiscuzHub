package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.FavoriteThread

@JsonIgnoreProperties(ignoreUnknown = true)
class FavoriteThreadResult : BaseResult() {
    @JvmField
    @JsonProperty("Variables")
    var favoriteThreadVariable: FavoriteThreadVariable = FavoriteThreadVariable()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class FavoriteThreadVariable : VariableResults() {
        @JvmField
        @JsonProperty("list")
        var favoriteThreadList: List<FavoriteThread> = ArrayList()

        @JsonProperty("perpage")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var perpage = 0

        @JsonProperty("count")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var count = 0
    }
}