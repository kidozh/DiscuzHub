package com.kidozh.discuzhub.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ViewHistory(avatarURL: String, name: String, belongedBBSId: Int, description: String, type: Int, fid: Int, tid: Int, recordAt: Date) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @JvmField
    var avatarURL = ""
    @JvmField
    var name = ""
    @JvmField
    var belongedBBSId: Int
    @JvmField
    var description = ""
    @JvmField
    var type: Int
    @JvmField
    var fid: Int
    @JvmField
    var tid: Int
    @JvmField
    var recordAt: Date
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ViewHistory
        return id == that.id && belongedBBSId == that.belongedBBSId && type == that.type && fid == that.fid && tid == that.tid &&
                (avatarURL == that.avatarURL) && name == that.name && description == that.description && recordAt == that.recordAt
    }

    override fun hashCode(): Int {
        return Objects.hash(id, avatarURL, name, belongedBBSId, description, type, fid, tid, recordAt)
    }

    companion object {
        @Ignore
        const val VIEW_TYPE_FORUM = 0

        @Ignore
        const val VIEW_TYPE_THREAD = 1

        @Ignore
        const val VIEW_TYPE_USER_PROFILE = 2
    }

    init {
        this.avatarURL = avatarURL
        this.name = name
        this.belongedBBSId = belongedBBSId
        this.description = description
        this.type = type
        this.fid = fid
        this.tid = tid
        this.recordAt = recordAt
    }
}