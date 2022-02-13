package com.kidozh.discuzhub.results

class TokenResult {
    var result: String = ""
    var maxToken: Int = 0
    var formhash = ""
    var notificationTokenList: List<NotificationToken> = ArrayList()

    class NotificationToken{
        var id: Int = 0
        var uid: Int = 0
        var username: String = ""
        var token: String = ""
        var allowPush: Boolean = true
        var deviceName: String = ""
        var updateAt : Int = 0
    }
}