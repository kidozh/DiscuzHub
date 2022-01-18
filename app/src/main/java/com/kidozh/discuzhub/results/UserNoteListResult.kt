package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer
import java.util.*
import kotlin.collections.ArrayList

@JsonIgnoreProperties(ignoreUnknown = true)
class UserNoteListResult : BaseResult() {
    @JvmField
    @JsonProperty("Variables")
    var noteListVariableResult: NoteListVariableResult = NoteListVariableResult()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class NoteListVariableResult : VariableResults() {
        @JvmField
        @JsonProperty("list")
        var notificationList: List<UserNotification> = ArrayList()

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var count = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var page = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var perPage = 0
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class UserNotification {
        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var id = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var uid = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("authorid")
        var authorId = 0
        @JvmField
        var type: String = ""
        @JvmField
        var note: String = ""
        @JvmField
        var author = ""

        @JvmField
        @JsonProperty("new")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var isNew = false

        @JvmField
        @JsonProperty("dateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var date: Date = Date()

        @JsonProperty("from_id")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fromId = 0

        @JsonProperty("from_idtype")
        var fromIdType: String = ""

        @JsonProperty("from_num")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fromNum = 0

        @JvmField
        @JsonProperty("notevar")
        @JsonIgnoreProperties(ignoreUnknown = true)
        var notificationExtraInfo: UserNotificationExtraInfo? = null
    }

    class UserNotificationExtraInfo {
        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var tid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var pid = 0
        @JvmField
        var subject: String = ""

        @JsonProperty("actoruid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var actorUid = 0

        @JsonProperty("actorusername")
        var actorUsername: String? = null
    }

    override fun isError(): Boolean {
        return message == null
    }
}