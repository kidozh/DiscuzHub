package com.kidozh.discuzhub.entities

import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.results.ForumResult.ShortReply
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class Thread : Serializable {
    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var tid = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var price = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var recommend = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var fid = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var heat = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var status = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var favtimes = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var sharetimes = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var stamp = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var icon = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var comments = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var pages = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var heatlevel = 0

    // sometimes replies becomes -
    @JvmField
    var views: String = ""
    @JvmField
    var replies: String = ""

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("pushedaid")
    var pushedAid = 0

    @JsonProperty("relatebytag")
    var relateByTag: String? = null

    @JsonProperty("maxposition")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var maxPostion = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("typeid")
    var typeId = 0

    @JsonProperty("posttableid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var postTableId = 0

    @JsonProperty("sortid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var sortId = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("readperm")
    var readPerm = 0
    @JvmField
    var author: String? = null
    @JvmField
    var subject: String = ""
    var dateline: String? = null

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("authorid")
    var authorId = 0

    @JsonProperty("lastpost")
    var lastPost: String? = null

    @JsonProperty("lastposter")
    var lastPoster: String? = null

    @JvmField
    @JsonProperty("displayorder")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var displayOrder = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var digest = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var special = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var moderated = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var stickreply = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var isgroup = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var hidden = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var moved = false

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("new")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var isNew = false

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var attachment = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var closed = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("recommend_add")
    var recommendNum = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("recommend_sub")
    var recommendSubNum = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("recommends")
    var recommends = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("replycredit")
    var replyCredit = 0

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
    @JsonProperty("dbdateline")
    var publishAt: Date? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
    @JsonProperty("dblastpost")
    var updateAt: Date? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("rushreply")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var rushReply = false

    @JvmField
    @JsonProperty("reply")
    @NonNull
    var shortReplyList: List<ShortReply> = ArrayList()
    var highlight: String? = null
    var folder: String? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("icontid")
    var iconTid = 0

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("istoday")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
    var isToday = false
    var id: String? = null

    @JsonProperty("avatar")
    var avatarURL: String? = null





    companion object{
        class ThreadDiffCallback(val oldList: List<Thread>, val newList: List<Thread>) : DiffUtil.Callback() {

            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].tid == newList[newItemPosition].tid

            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].equals(newList[newItemPosition])

            }

        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Thread

        if (tid != other.tid) return false
        if (price != other.price) return false
        if (recommend != other.recommend) return false
        if (fid != other.fid) return false
        if (heat != other.heat) return false
        if (status != other.status) return false
        if (favtimes != other.favtimes) return false
        if (sharetimes != other.sharetimes) return false
        if (stamp != other.stamp) return false
        if (icon != other.icon) return false
        if (comments != other.comments) return false
        if (pages != other.pages) return false
        if (heatlevel != other.heatlevel) return false
        if (views != other.views) return false
        if (replies != other.replies) return false
        if (pushedAid != other.pushedAid) return false
        if (relateByTag != other.relateByTag) return false
        if (maxPostion != other.maxPostion) return false
        if (typeId != other.typeId) return false
        if (postTableId != other.postTableId) return false
        if (sortId != other.sortId) return false
        if (readPerm != other.readPerm) return false
        if (author != other.author) return false
        if (subject != other.subject) return false
        if (dateline != other.dateline) return false
        if (authorId != other.authorId) return false
        if (lastPost != other.lastPost) return false
        if (lastPoster != other.lastPoster) return false
        if (displayOrder != other.displayOrder) return false
        if (digest != other.digest) return false
        if (special != other.special) return false
        if (moderated != other.moderated) return false
        if (stickreply != other.stickreply) return false
        if (isgroup != other.isgroup) return false
        if (hidden != other.hidden) return false
        if (moved != other.moved) return false
        if (isNew != other.isNew) return false
        if (attachment != other.attachment) return false
        if (closed != other.closed) return false
        if (recommendNum != other.recommendNum) return false
        if (recommendSubNum != other.recommendSubNum) return false
        if (recommends != other.recommends) return false
        if (replyCredit != other.replyCredit) return false
        if (publishAt != other.publishAt) return false
        if (updateAt != other.updateAt) return false
        if (rushReply != other.rushReply) return false
        if (shortReplyList != other.shortReplyList) return false
        if (highlight != other.highlight) return false
        if (folder != other.folder) return false
        if (iconTid != other.iconTid) return false
        if (isToday != other.isToday) return false
        if (id != other.id) return false
        if (avatarURL != other.avatarURL) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tid
        result = 31 * result + price
        result = 31 * result + recommend
        result = 31 * result + fid
        result = 31 * result + heat
        result = 31 * result + status
        result = 31 * result + favtimes
        result = 31 * result + sharetimes
        result = 31 * result + stamp
        result = 31 * result + icon
        result = 31 * result + comments
        result = 31 * result + pages
        result = 31 * result + heatlevel
        result = 31 * result + views.hashCode()
        result = 31 * result + replies.hashCode()
        result = 31 * result + pushedAid
        result = 31 * result + (relateByTag?.hashCode() ?: 0)
        result = 31 * result + maxPostion
        result = 31 * result + typeId
        result = 31 * result + postTableId
        result = 31 * result + sortId
        result = 31 * result + readPerm
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (subject?.hashCode() ?: 0)
        result = 31 * result + (dateline?.hashCode() ?: 0)
        result = 31 * result + authorId
        result = 31 * result + (lastPost?.hashCode() ?: 0)
        result = 31 * result + (lastPoster?.hashCode() ?: 0)
        result = 31 * result + displayOrder
        result = 31 * result + digest.hashCode()
        result = 31 * result + special.hashCode()
        result = 31 * result + moderated.hashCode()
        result = 31 * result + stickreply.hashCode()
        result = 31 * result + isgroup.hashCode()
        result = 31 * result + hidden.hashCode()
        result = 31 * result + moved.hashCode()
        result = 31 * result + isNew.hashCode()
        result = 31 * result + attachment
        result = 31 * result + closed
        result = 31 * result + recommendNum
        result = 31 * result + recommendSubNum
        result = 31 * result + recommends
        result = 31 * result + replyCredit
        result = 31 * result + (publishAt?.hashCode() ?: 0)
        result = 31 * result + (updateAt?.hashCode() ?: 0)
        result = 31 * result + rushReply.hashCode()
        result = 31 * result + (shortReplyList?.hashCode() ?: 0)
        result = 31 * result + (highlight?.hashCode() ?: 0)
        result = 31 * result + (folder?.hashCode() ?: 0)
        result = 31 * result + iconTid
        result = 31 * result + isToday.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (avatarURL?.hashCode() ?: 0)
        return result
    }
}