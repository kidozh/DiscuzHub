package com.kidozh.discuzhub.results

import androidx.recyclerview.widget.DiffUtil
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer

@JsonIgnoreProperties(ignoreUnknown = true)
class TokenResult {
    var result: String = ""
    @JsonProperty(required = false)
    var maxToken: Int = 0
    @JsonProperty(required = false)
    var formhash = ""
    @JsonProperty("list", defaultValue = "[]")
    var list: List<NotificationToken> = ArrayList()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class NotificationToken{
        var id: Int = 0
        var uid: Int = 0
        var username: String = ""
        var token: String = ""
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowPush: Boolean = true
        var deviceName: String = ""
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var updateAt : Long = 0
        var channel:String = ""
        var packageId: String = ""


        companion object{
            class TokenDiffCallback(val oldList: List<NotificationToken>, val newList: List<NotificationToken>) : DiffUtil.Callback(){
                override fun getOldListSize(): Int {
                    return oldList.size
                }

                override fun getNewListSize(): Int {
                    return newList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldItemPosition == newItemPosition
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldList[oldItemPosition].equals(newList[newItemPosition])
                }

            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NotificationToken

            if (id != other.id) return false
            if (uid != other.uid) return false
            if (username != other.username) return false
            if (token != other.token) return false
            if (allowPush != other.allowPush) return false
            if (deviceName != other.deviceName) return false
            if (updateAt != other.updateAt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + uid
            result = 31 * result + username.hashCode()
            result = 31 * result + token.hashCode()
            result = 31 * result + allowPush.hashCode()
            result = 31 * result + deviceName.hashCode()
            result = 31 * result + updateAt.hashCode()
            return result
        }
    }
}