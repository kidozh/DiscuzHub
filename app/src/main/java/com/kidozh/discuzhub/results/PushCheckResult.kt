package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

class PushCheckResult {
    var result: String? = null
    var miPush: MiPushInformation? = null

    @JsonProperty("firebase")
    var fcm: FirebasePushInformation? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var uid = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var groupId = 0
    var groupTitle: String = ""
    var groupAllowed = false

    class MiPushInformation {
        var enabled = false
    }

    class FirebasePushInformation {
        var enabled = false
    }
}