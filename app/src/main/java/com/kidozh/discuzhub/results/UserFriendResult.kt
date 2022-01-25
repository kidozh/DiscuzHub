package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

class UserFriendResult : BaseResult() {
    @JsonProperty("Variables")
    var friendVariables: FriendVariables? = null

    class FriendVariables : VariableResults() {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var count = 0

        @JsonProperty("list")
        var friendList: List<UserFriend> = ArrayList()
    }

    class UserFriend {
        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var uid = 0
        @JvmField
        var username = ""
    }

    override fun isError(): Boolean {
        return message != null
    }
}