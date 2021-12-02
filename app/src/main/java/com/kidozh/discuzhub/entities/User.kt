package com.kidozh.discuzhub.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Entity
class User(
    @JvmField
    var auth: String?,
    @JvmField
    var saltkey: String,
    @JvmField
    var uid: Int,
    @JvmField
    var username: String,
    @JvmField
    var avatarUrl: String,
    @JvmField
    var readPerm: Int,
    @field:JsonProperty(
        "groupid"
    ) var groupId: Int
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @JvmField
    var belongedBBSID = 0

    @JvmField
    @JsonIgnore
    var position = 0
    val isValid: Boolean
        get() = auth != null && auth != "null"
}