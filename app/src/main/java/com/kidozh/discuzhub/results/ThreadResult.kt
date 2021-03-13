package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.kidozh.discuzhub.results.BaseResult
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.results.ThreadResult.ThreadPostVariable
import com.kidozh.discuzhub.results.VariableResults
import com.kidozh.discuzhub.utilities.bbsParseUtils.DetailedThreadInfo
import com.fasterxml.jackson.annotation.JsonFormat
import com.kidozh.discuzhub.entities.Post
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.results.ThreadResult.CommentListJsonDeserializer
import com.fasterxml.jackson.annotation.JsonIgnore
import com.kidozh.discuzhub.results.ThreadResult.CommentCountJsonDeserializer
import com.kidozh.discuzhub.results.ThreadResult.SettingRewriteStatusJsonDeserializer
import com.kidozh.discuzhub.entities.Poll
import com.fasterxml.jackson.databind.JsonDeserializer
import kotlin.Throws
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

@JsonIgnoreProperties(ignoreUnknown = true)
class ThreadResult : BaseResult() {
    @JsonProperty("Variables")
    lateinit var threadPostVariables: ThreadPostVariable

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ThreadPostVariable : VariableResults() {
        @JsonProperty("thread")
        lateinit var detailedThreadInfo: DetailedThreadInfo

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fid = 0

        @JsonProperty("postlist")
        var postList: List<Post> = ArrayList()

        @JsonProperty("allowpostcomment")
        var allowPostCommentList: List<String> = ArrayList()

        @JsonProperty("comments")
        @JsonDeserialize(using = CommentListJsonDeserializer::class)
        var commentList: Map<String, List<Comment>>? = HashMap()

        @JsonIgnore
        @JsonProperty("commentcount")
        @JsonDeserialize(using = CommentCountJsonDeserializer::class)
        var commentCount: Map<String, String>? = HashMap()

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var ppp = 0

        @JsonProperty("setting_rewriterule")
        var rewriteRule: Map<String, String>? = HashMap()

        @JsonProperty("setting_rewritestatus")
        @JsonDeserialize(using = SettingRewriteStatusJsonDeserializer::class)
        var rewriteList: List<String>? = ArrayList()

        @JsonProperty("forum_threadpay")
        var threadPay: String? = null

        @JsonProperty("cache_custominfo_postno")
        var customPostNoList: List<String>? = ArrayList()

        // for poll
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonProperty("special_poll")
        var pollInfo: Poll? = null
    }

    override fun isError(): Boolean {
        return message != null
    }

    class SettingRewriteStatusJsonDeserializer : JsonDeserializer<List<String>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<String> {
            val currentToken = p.currentToken
            return if (currentToken == JsonToken.VALUE_STRING) {
                ArrayList()
            } else if (currentToken == JsonToken.START_ARRAY) {
                val mapper = ObjectMapper()
                mapper.readValue<List<String>>(p, object : TypeReference<List<String>>() {})
            } else {
                ArrayList()
            }
        }
    }

    class CommentListJsonDeserializer : JsonDeserializer<Map<String, List<Comment>>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Map<String, List<Comment>> {
            val currentToken = p.currentToken
            return if (currentToken == JsonToken.VALUE_STRING) {
                HashMap()
            } else if (currentToken == JsonToken.START_OBJECT) {
                val mapper = ObjectMapper()
                mapper.readValue<Map<String, List<Comment>>>(p, object : TypeReference<Map<String, List<Comment>>>() {})
            } else {
                HashMap()
            }
        }
    }

    class CommentCountJsonDeserializer : JsonDeserializer<Map<String, String>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Map<String, String> {
            val currentToken = p.currentToken
            return if (currentToken == JsonToken.VALUE_STRING) {
                HashMap()
            } else if (currentToken == JsonToken.START_OBJECT) {
                val mapper = ObjectMapper()
                mapper.readValue<Map<String, String>>(p, object : TypeReference<Map<String, String>>() {})
            } else {
                HashMap()
            }
        }
    }

    class Comment {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var id = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var tid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var pid = 0
        var author: String = ""
        var dateline: String = ""
        var comment: String = ""
        var avatar: String = ""

        @JsonProperty("authorid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var authorId = 0
    }
}