package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class PostTokenResult {
    var result: String = ""
    val formhash: String = ""
    @JsonProperty(required = false)
    var data: TokenResult.NotificationToken? = null
}