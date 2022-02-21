package com.kidozh.discuzhub.pushes

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class ReplyThreadPush {
    @JsonProperty("site_url")
    var siteURL:String = ""
    @JsonProperty("site_host")
    var siteHost:String = ""

    var type:String = ""
    @JsonProperty("sender_name")
    var senderName = ""
    @JsonProperty("sender_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var senderId = 0

    var message = ""
    var title = ""
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var tid = 0
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var pid = 0
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var fid = 0
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var uid = 0


}