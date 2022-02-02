package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.entities.FavoriteForum

@JsonIgnoreProperties(ignoreUnknown = true)
class FavoriteForumResult : BaseResult() {
    @JsonProperty("Variables")
    var favoriteForumVariable: FavoriteForumVariable = FavoriteForumVariable()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class FavoriteForumVariable : VariableResults() {
        @JsonProperty("list")
        var FavoriteForumList: List<FavoriteForum> = ArrayList()

        @JsonProperty("perpage")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var perpage = 0

        @JsonProperty("count")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var count = 0
    }

    override fun isError(): Boolean {
        return message != null
    }
}