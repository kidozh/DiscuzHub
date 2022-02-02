package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

class PushDevicesResult {
    var result: String = ""
    var miPush: MiPushInformation = MiPushInformation()

    @JsonProperty("firebase")
    var fcm: FirebasePushInformation? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var uid = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var groupId = 0
    var groupTitle: String? = null
    var groupAllowed = false

    class MiPushInformation {
        var enabled = false
    }

    class FirebasePushInformation {
        var enabled = false
    }

    class Device {
        var pushProvider: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var forbidden = false
        var token: String? = null
        var clientPackage: String? = null
        var dateline: String? = null
    }
}