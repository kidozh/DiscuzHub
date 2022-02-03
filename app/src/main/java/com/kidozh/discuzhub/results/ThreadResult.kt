package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.entities.FavoriteThread
import com.kidozh.discuzhub.entities.Poll
import com.kidozh.discuzhub.entities.Post
import com.kidozh.discuzhub.utilities.bbsParseUtils.OneZeroDeserializer
import java.io.IOException
import java.util.*

//@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class ThreadResult : BaseResult() {
    @JsonProperty("Variables")
    @JsonIgnoreProperties(ignoreUnknown = true)
    var threadPostVariables: ThreadVariable = ThreadVariable()

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    class ThreadVariable : VariableResults() {


        @JsonProperty("special_poll")
        //@JsonInclude(JsonInclude.Include.ALWAYS)
        //@JsonDeserialize(using = PollJsonDeserializer::class)
        var poll: Poll? = null

        @JsonProperty("thread")
//    @JsonIgnoreProperties(ignoreUnknown = true)
        var detailedThreadInfo: DetailedThreadInfo = DetailedThreadInfo()

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fid = 0

        @JsonProperty("postlist")
        var postList: List<Post> = ArrayList()



//        @JsonProperty("allowpostcomment")
//        var allowPostCommentList: List<String>? = ArrayList()

        @JsonProperty("comments")
        @JsonDeserialize(using = CommentListJsonDeserializer::class)
        var commentList: Map<String, List<Comment>>? = HashMap()

//        @JsonIgnore
//        @JsonProperty("commentcount")
//        @JsonDeserialize(using = CommentCountJsonDeserializer::class)
//        var commentCount: Map<String, String> = HashMap()

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var ppp = 0

        @JsonProperty("setting_rewriterule")
        var rewriteRule: Map<String, String>? = HashMap()

//        @JsonProperty("setting_rewritestatus")
//        @JsonDeserialize(using = SettingRewriteStatusJsonDeserializer::class)
//        var rewriteList: List<String>? = ArrayList()
//
//        @JsonProperty("forum_threadpay")
//        var threadPay: String? = null
//
//        @JsonProperty("cache_custominfo_postno")
//        var customPostNoList: List<String>? = ArrayList()


    }

    @JsonProperty("special_poll", required = false)
    var poll: Poll? = null


    @JsonIgnore
    override fun isError(): Boolean {
        return message != null
    }

    companion object{
        val TAG = ThreadResult::class.simpleName
    }

    class SettingRewriteStatusJsonDeserializer : JsonDeserializer<List<String>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<String> {
            return when (p.currentToken) {
                JsonToken.VALUE_STRING -> {
                    ArrayList()
                }
                JsonToken.START_ARRAY -> {
                    val mapper = ObjectMapper()
                    mapper.readValue<List<String>>(p, object : TypeReference<List<String>>() {})
                }
                else -> {
                    ArrayList()
                }
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
            return when (p.currentToken) {
                JsonToken.VALUE_STRING -> {
                    HashMap()
                }
                JsonToken.START_OBJECT -> {
                    val mapper = ObjectMapper()
                    mapper.readValue<Map<String, String>>(p, object : TypeReference<Map<String, String>>() {})
                }
                else -> {
                    HashMap()
                }
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    class DetailedThreadInfo {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var tid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fid = 0

        @JsonProperty("posttableid")
        var postableId = ""

        @JsonProperty("typeid")
        var typeId = ""
        var author = ""
        var subject = ""

        @JsonProperty("authorid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var authorId = 0

        @JsonProperty("sortid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var sortId = 0

        @JsonProperty("dateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var lastPostTime = Date()

        @JsonProperty("lastpost")
        var lastPostTimeString: String? = null
        var lastposter = ""

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("displayorder")
        var displayOrder = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var views: Long = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var replies: Long = 0
        var highlight: String? = null

        @JsonDeserialize(using = OneZeroDeserializer::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var special = false

        @JsonDeserialize(using = OneZeroDeserializer::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var moderated = false

        @JsonDeserialize(using = OneZeroDeserializer::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var is_archived = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var rate = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var status = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var readperm = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var price = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var digest = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var closed = 0
        var attachment: String? = null

        @JsonDeserialize(using = OneZeroDeserializer::class)
        @JsonProperty("stickreply")
        var stickReply = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var recommends = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var recommend_add = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var recommend_sub = 0
        var isgroup: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var favtimes = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var sharetimes = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var heats = 0
        var stamp: String? = null
        var icon: String? = null
        var pushedaid: String? = null
        var cover: String? = null

        @JsonProperty("replycredit")
        var replyCredit: String? = null
        var relatebytag: String? = null
        var bgcolor: String? = null

        @JsonProperty("maxposition")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var maxPosition: Long = 0

        @JsonProperty("comments")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var comments = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroDeserializer::class)
        var hidden = false
        var threadtable: String? = null
        var threadtableid: String? = null
        var posttable: String? = null

        @JsonProperty("allreplies")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var allreplies = 0
        var archiveid: String? = null
        var subjectenc: String? = null
        var short_subject: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var relay = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var ordertype = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var recommend = 0

        @JsonProperty("recommendlevel")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var recommendLevel = 0

        @JsonProperty("heatlevel")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var heatLevel = 0

        @JsonProperty("freemessage")
        var freeMessage: String? = null

//    @JsonProperty("replycredit_rule")
//    var creditRule: replyCreditRule? = null

//    @JsonProperty("starttime")
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
//    var startTime = Date()
//
//    @JsonProperty("remaintime", defaultValue = "0")
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
//    var remainTime = Date()

        fun toFavoriteThread(bbsId: Int, userId: Int): FavoriteThread {
            val favoriteThread = FavoriteThread()
            favoriteThread.belongedBBSId = bbsId
            favoriteThread.uid = authorId
            favoriteThread.idKey = tid
            favoriteThread.idType = "tid"
            favoriteThread.title = subject
            favoriteThread.description = if (freeMessage == null) "" else freeMessage!!
            favoriteThread.author = author
            favoriteThread.date = lastPostTime
            favoriteThread.replies = replies.toInt()
            favoriteThread.userId = userId
            return favoriteThread
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ReplyCreditRule {
        var tid: String? = null

        @JsonProperty("extcredits")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extCredits = 0

        @JsonProperty("extcreditstype")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extCreditsType = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var times = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var membertimes = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var random = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var remaining = 0
    }





}





