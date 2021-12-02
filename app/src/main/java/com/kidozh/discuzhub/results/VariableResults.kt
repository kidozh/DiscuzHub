package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer

open class VariableResults : BaseResult() {
    @JsonProperty(defaultValue = "", required = false)
    var cookiepre: String = ""
    @JsonProperty(defaultValue = "", required = false)
    var auth: String? = ""
    @JsonProperty(defaultValue = "", required = false)
    var saltkey: String = ""
    @JsonProperty(defaultValue = "", required = false)
    var member_username: String = ""
    @JsonProperty(defaultValue = "", required = false)
    var member_avatar: String = ""
    var member_uid = 0

    @JsonProperty("groupid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var groupId = 0


    @JsonProperty("formhash")
    lateinit var formHash: String

    @JsonProperty("ismoderator", defaultValue = "0", required = false)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var moderator: Boolean = false

    @JsonProperty("readaccess")
    var readAccess = 0

    @JsonProperty("notice")
    var noticeNumber: newNoticeNumber? = null

    class newNoticeNumber {
        @JsonProperty("newpush")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var push = 0

        @JsonProperty("newpm")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var privateMessage = 0

        @JsonProperty("newprompt")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var prompt = 0

        @JsonProperty("newmypost")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var myPost = 0
    }

    val userBriefInfo: User
        get() = User(auth, saltkey, member_uid, member_username, member_avatar, readAccess, groupId)
}