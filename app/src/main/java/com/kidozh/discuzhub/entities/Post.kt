package com.kidozh.discuzhub.entities

import androidx.recyclerview.widget.DiffUtil
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer
import java.io.IOException
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class Post : Serializable {
    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var pid = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var tid = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var first = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var anonymous = false
    @JvmField
    var author = ""
    var dateline = ""
    @JvmField
    var message = ""
    var username = ""

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("authorid")
    var authorId = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var attachment = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var status = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var replycredit = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var number = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var position: Long = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("adminid")
    var adminId = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("groupid")
    var groupId = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("memberstatus")
    var memberStatus = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
    @JsonProperty("dbdateline")
    var publishAt: Date = Date()

    @JsonProperty("attachments")
    @JsonDeserialize(using = AttachmentMapperDeserializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var attachmentMapper: Map<String, Attachment> = HashMap()

    @JsonProperty("attachlist")
    @JsonIgnoreProperties(ignoreUnknown = true)
    var attachmentIdList: List<String> = ArrayList()

    @JsonProperty("imagelist")
    @JsonIgnoreProperties(ignoreUnknown = true)
    var imageIdList: List<String> = ArrayList()

    @JsonProperty("groupiconid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var groupIconId: String? = null

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Attachment : Serializable {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var aid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var tid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var pid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var uid = 0
        var dateline: String? = null
        @JvmField
        var filename: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("filesize")
        var fileSize = 0
        @JvmField
        var attachment: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var remote = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var thumb = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var payed = false
        var description: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("readperm")
        var readPerm = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var price = 0

        @JsonProperty("isimage")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var isImageNum = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var width = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("picid")
        var picId = 0
        @JvmField
        var ext: String? = null

        @JsonProperty("attachicon")
        var attachIcon: String? = null

        @JvmField
        @JsonProperty("attachsize")
        var attachSize: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("attachimg")
        var attachImg = false
        @JvmField
        var url: String? = null

        @JvmField
        @JsonProperty("dbdateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var updateAt: Date? = null

        @JvmField
        @JsonProperty("aidencode")
        var aidEncode: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var downloads = 0

        @JsonProperty("imgalt")
        var imageAlt: String? = null
        val isImage: Boolean
            get() = isImageNum != 0
    }

    val allAttachments: List<Attachment?>
        get() {
            val attachmentList: MutableList<Attachment?> = ArrayList()
            val attachmentIdList = attachmentIdList
            val imageIdList = imageIdList
            val totalIdList: MutableList<String> = ArrayList()
            if (imageIdList != null) {
                totalIdList.addAll(imageIdList)
            }
            if (attachmentIdList != null) {
                totalIdList.addAll(attachmentIdList)
            }
            for (key in totalIdList) {
                if (attachmentMapper.containsKey(key)) {
                    val attachment = attachmentMapper[key]
                    attachmentList.add(attachment)
                }
            }
            return attachmentList
        }

    class AttachmentMapperDeserializer : JsonDeserializer<Map<String, Attachment>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Map<String, Attachment> {
            val currentToken = p.currentToken
            // Log.d(TAG,"Token "+p.getText());
            return if (currentToken == JsonToken.START_OBJECT) {
                val mapper = ObjectMapper()
                mapper.readValue<Map<String, Attachment>>(p,object: TypeReference<Map<String, Attachment>>(){})
            } else {
                HashMap()
            }
        }
    }

    companion object {
        private val TAG = Post::class.java.simpleName

        class DiffCallback(val oldList: List<Post>, val newList: List<Post>)  : DiffUtil.Callback(){
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]

            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]

            }

        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (pid != other.pid) return false
        if (tid != other.tid) return false
        if (first != other.first) return false
        if (anonymous != other.anonymous) return false
        if (author != other.author) return false
        if (dateline != other.dateline) return false
        if (message != other.message) return false
        if (username != other.username) return false
        if (authorId != other.authorId) return false
        if (attachment != other.attachment) return false
        if (status != other.status) return false
        if (replycredit != other.replycredit) return false
        if (number != other.number) return false
        if (position != other.position) return false
        if (adminId != other.adminId) return false
        if (groupId != other.groupId) return false
        if (memberStatus != other.memberStatus) return false
        if (publishAt != other.publishAt) return false
        if (attachmentMapper != other.attachmentMapper) return false
        if (attachmentIdList != other.attachmentIdList) return false
        if (imageIdList != other.imageIdList) return false
        if (groupIconId != other.groupIconId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pid
        result = 31 * result + tid
        result = 31 * result + first.hashCode()
        result = 31 * result + anonymous.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + dateline.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + authorId
        result = 31 * result + attachment
        result = 31 * result + status
        result = 31 * result + replycredit
        result = 31 * result + number
        result = 31 * result + position.hashCode()
        result = 31 * result + adminId
        result = 31 * result + groupId
        result = 31 * result + memberStatus
        result = 31 * result + publishAt.hashCode()
        result = 31 * result + attachmentMapper.hashCode()
        result = 31 * result + attachmentIdList.hashCode()
        result = 31 * result + imageIdList.hashCode()
        result = 31 * result + (groupIconId?.hashCode() ?: 0)
        return result
    }
}


