package com.kidozh.discuzhub.entities

import com.fasterxml.jackson.annotation.JsonProperty

class PrivateMessage() {
    @JsonProperty("plid")
    var plId : Int = 0
    @JsonProperty("pmid")
    var pmId : Int = 0
    var subject : String = ""
    var message : String = ""
    @JsonProperty("touid")
    var toUid = 0
    @JsonProperty("msgfromid")
    var msgFromId = 0
    @JsonProperty("msgfrom")
    var fromUsername : String = ""
    @JsonProperty("vdateline")
    var dateString : String = ""

    fun self(user: User?): Boolean{
        if(user == null){
            return false
        }
        return msgFromId == user.uid
    }
}