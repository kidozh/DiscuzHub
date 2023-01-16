package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer


@JsonIgnoreProperties(ignoreUnknown = true)
class AddCheckResult() {
    @JsonProperty("discuzversion")
    var discuz_version: String = ""
    var charset: String = ""

    @JsonProperty("version")
    var apiVersion = 0

    @JsonProperty("pluginversion")
    var pluginVersion: String=""

    @JsonProperty("regname")
    var registerName: String = ""

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    @JsonProperty("qqconnect")
    var qqConnect = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    @JsonProperty("wsqqqconnect")
    var wsqQQConnect = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    @JsonProperty("wsqhideregister")
    var wsqHideRegister = false


    @JsonProperty("sitename")
    var siteName: String = ""

    @JsonProperty("mysiteid")
    var mySiteId: String = ""

    @JsonProperty("ucenterurl")
    var uCenterURL: String = ""

    @JsonProperty("defaultfid")
    var defaultFid = 0

    @JsonProperty("totalposts")
    var totalPosts = 0L

    @JsonProperty("totalmembers")
    var totalMembers = 0L

    @JsonProperty("testcookie")
    var testCookie: String? = null
    fun toBBSInformation(baseUrl: String): Discuz {
        return Discuz(baseUrl, siteName, discuz_version, charset, apiVersion, pluginVersion, totalPosts, totalMembers, mySiteId,
                defaultFid, uCenterURL, registerName, "", wsqHideRegister, qqConnect
        )
    }
}