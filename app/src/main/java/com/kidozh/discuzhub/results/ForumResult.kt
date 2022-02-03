package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer
import java.io.IOException
import java.io.Serializable

class ForumResult : BaseResult() {

    @JsonProperty("Variables")
    lateinit var forumVariables: ForumVariables


    class ForumVariables : VariableResults() {

        @JsonProperty("forum")
        lateinit var forum: Forum

        @JsonProperty("group")
        var groupInfo: GroupInfo? = null

        @JsonProperty("forum_threadlist", defaultValue = "[]")
        var forumThreadList: List<Thread> = ArrayList()

        @JsonProperty("groupiconid")
        var groupIconId: Map<String, String>? = null

        @JvmField
        @JsonProperty("sublist")
        var subForumLists: List<SubForumInfo> = ArrayList()

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var tpp = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var page = 0

        @JsonProperty("reward_unit")
        var rewardUnit: String? = null


        @JsonProperty("threadtypes")
        var threadTypeInfo: ThreadTypeInfo? = null
    }

    class GroupInfo : Serializable {
        @JsonProperty("groupid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var groupId = 0

        @JsonProperty("grouptitle")
        var groupTitle: String? = null
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ShortReply : Serializable {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var pid = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("authorid")
        var authorId = 0

        lateinit var author: String

        lateinit var message: String
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class SubForumInfo : Serializable {
        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fid = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var threads = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var posts = 0
        @JvmField
        var name: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("todayposts")
        var todayPosts = 0

        @JsonProperty("icon")
        var iconURL: String? = null
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ThreadTypeInfo : Serializable {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var required = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var listable = false
        var prefix: String? = null

        @JvmField
        @JsonProperty("types")
        @JsonDeserialize(using = ForumThreadTypeDeserializer::class)
        var idNameMap = HashMap<String, String>()

        @JsonProperty("icons")
        @JsonDeserialize(using = ForumThreadTypeDeserializer::class)
        var idIconMap: Map<String, String> = HashMap()

        @JsonProperty("moderators")
        @JsonDeserialize(using = ForumThreadTypeDeserializer::class)
        var idModeratorMap: Map<String, String> = HashMap()
    }

    class ForumThreadTypeDeserializer : JsonDeserializer<Map<String, String>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Map<String, String> {
            val currentToken = p.currentToken
            return if (currentToken == JsonToken.START_OBJECT) {
                val mapper = ObjectMapper()
                mapper.readValue<Map<String, String>>(p, object : TypeReference<Map<String, String>>() {})
            } else {
                HashMap()
            }
        }
    }

    override fun isError(): Boolean {
        return message != null
    }
}