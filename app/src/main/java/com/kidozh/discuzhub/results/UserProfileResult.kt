package com.kidozh.discuzhub.results

import android.util.Log
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kidozh.discuzhub.results.UserProfileResult
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer
import com.kidozh.discuzhub.utilities.URLUtils
import java.io.IOException
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class UserProfileResult : BaseResult() {
    @JsonProperty("Variables")
    var userProfileVariableResult: UserProfileVariableResult? = null

    //@JsonIgnoreProperties(ignoreUnknown = true)
    class UserProfileVariableResult : VariableResults() {
        @JsonProperty("space")
        var space: SpaceVariables = SpaceVariables()

        @JsonProperty("extcredits")
        var extendCreditMap: Map<String, extendCredit>? = HashMap()
        val extendCredits: List<extendCredit?>
            get() {
                val extendCreditList: MutableList<extendCredit?> = ArrayList()
                Log.d(TAG, "GET extend credit hash map $extendCreditMap $space")
                if (space != null && extendCreditMap != null) {
                    val creditIndexKey = extendCreditMap!!.keys
                    for (creditIndex in creditIndexKey) {
                        val extFieldName = "extcredits$creditIndex"
                        try {
                            val extField = SpaceVariables::class.java.getField(extFieldName)
                            val fieldValue = extField[space] as Int
                            val extCredit = extendCreditMap!![creditIndex]
                            extCredit!!.value = fieldValue
                            extendCreditList.add(extCredit)
                        } catch (ignored: Exception) {
                        }
                    }
                }
                return extendCreditList
            }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class SpaceVariables {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var uid = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var status = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var credits = 0
        var username: String = ""

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("emailstatus")
        var emailStatus = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("avatarstatus")
        var avatarStatus = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("videophotostatus")
        var videoPhotoStatus = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("adminid")
        var adminId = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("groupid")
        var groupId = 0
        var groupexpiry: String? = null
        var regdate: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("extgroupids")
        var extendGroup: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("notifysound")
        var notifySound = false

        @JsonProperty("timeoffset")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var timeZone = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var newpm = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var newprompt = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("accessmasks")
        var accessMasks = false
        var allowadmincp: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("onlyacceptfriendpm")
        var onlyAcceptFriendPM = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        @JsonProperty("conisbind")
        var connectBinded = false

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var freeze = false

        // need to rewrite
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits1 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits2 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits3 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits4 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits5 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits6 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits7 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var extcredits8 = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var friends = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var posts = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var threads = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var digestposts = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var doings = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var blogs = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var albums = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var sharings = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var views = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var feeds = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var follower = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var following = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var newfollower = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var blacklist = 0

        @JsonProperty("attachsize")
        var attachmentSize: String? = null

        @JsonProperty("oltime")
        var onlineHours = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var todayattachs = 0

        @JsonProperty("todayattachsize")
        var todayAttachSize: String? = null
        var videophoto: String? = null
        var spacename: String? = null
        var spacedescription: String? = null
        var domain: String? = null
        var addsize: String? = null
        var addfriend: String? = null
        var menunum: String? = null
        var theme: String? = null
        var spacecss: String? = null
        var blockposition: String? = null
        var feedfriend: String? = null
        var magicgift: String? = null

        @JsonProperty("recentnote")
        var recentNote = ""

        @JsonProperty("spacenote")
        var spaceNotification: String? = null

        @JsonProperty("sightml")
        var sigatureHtml = ""
        var publishfeed: String? = null
        var authstr: String? = null
        var groupterms: String? = null
        var groups: String? = null
        var attentiongroup: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var gender = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var birthyear = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var birthmonth = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var birthday = 0
        var constellation = ""
        var zodiac: String? = null
        var nationality: String? = null
        var birthprovince = ""
        var birthcity = ""
        var birthdist = ""
        var birthcommunity = ""
        var resideprovince = ""
        var residecity = ""
        var residedist = ""
        var residecommunity = ""
        var residesuite = ""
        var graduateschool = ""

        @JsonProperty("position")
        var workPosition = ""
        var company = ""
        var education = ""
        var occupation = ""
        var revenue = ""
        var lookingfor = ""
        var bloodtype = ""

        @JsonProperty("affectivestatus")
        var marriedStatus = ""
        var height = ""
        var weight = ""
        var site = ""
        var bio = ""
        var interest = ""

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var port = 0
        var lastvisit: String? = null
        var lastactivity = ""
        var lastpost: String? = null
        var lastsendmail: String? = null
        var field1: String? = null
        var field2: String? = null
        var field3: String? = null
        var field4: String? = null
        var field5: String? = null
        var field6: String? = null
        var field7: String? = null
        var field8: String? = null
        var regipport: String? = null
        var lastipport: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var invisible = false
        var buyercredit: String? = null
        var sellercredit: String? = null

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var favtimes = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var sharetimes = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var profileprogress = 0

        @JsonProperty("admingroup")
        var adminGroup: AdminGroupVariables? = null

        @JsonProperty("group")
        var group: GroupVariables? = null

        @JsonProperty("lastactivitydb")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var lastActivityDate: Date? = null
        var buyerrank: String? = null
        var sellerrank: String? = null
        var groupiconid: String? = null
        var upgradecredit = 0
        var upgradeprogress = 0

        @JsonDeserialize(using = MedalInfoJsonDeserializer::class)
        var medals: List<Medal> = ArrayList()

        @JsonProperty("privacy")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonIgnore
        var privacySetting = PrivacySetting()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class PrivacySetting {
        @JsonProperty("feed")
        var feedPrivacy = FeedPrivacySetting()

        @JsonProperty("view")
        var viewPrivacySetting = ViewPrivacySetting()

        @JsonProperty("profile")
        @JsonIgnoreProperties(ignoreUnknown = true)
        var profilePrivacySetting = ProfilePrivacySetting()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class FeedPrivacySetting {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var doing = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var blog = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var upload = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var poll = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var newthread = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var share = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var friend = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var comment = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var show = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var credit = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var invite = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var task = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var profile = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var click = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var newreply = 0
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ViewPrivacySetting {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var friend = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var wall = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var home = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var doing = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var blog = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var album = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var share = 2

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var index = 2
    }

    //@JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = ProfilePrivacyJsonDeserializer::class)
    @JsonIgnoreProperties
    class ProfilePrivacySetting {
        var realname = 0
        var gender = 0
        var birthday = 0
        var birthcity = 0
        var residecity = 0
        var affectivestatus = 0
        var lookingfor = 0
        var bloodtype = 0
        var telephone = 0
        var mobile = 0
        var qq = 0
        var msn = 0
        var taobao = 0
        var graduateschool = 0
        var education = 0
        var company = 0
        var occupation = 0
        var position = 0
        var revenue = 0
        var idcardtype = 0
        var idcard = 0
        var address = 0
        var zipcode = 0
        var site = 0
        var bio = 0
        var interest = 0
    }

    //@JsonIgnoreProperties(ignoreUnknown = true)
    class AdminGroupVariables {
        var type: String? = null

        @JvmField
        @JsonProperty("grouptitle")
        var groupTitle = ""

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var stars = 0
        @JvmField
        var color: String? = null
        @JvmField
        var icon: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("readaccess")
        var readAccess = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetattach")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowGetAttach = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetimage")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowGetImage = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowmediacode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowMediaCode = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("maxsigsize")
        var maxSignatureSize = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowbegincode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowBeginCode = false
        var userstatusby: String? = null
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class GroupVariables {
        var type: String? = null

        @JvmField
        @JsonProperty("grouptitle")
        var groupTitle: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var stars = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("creditshigher")
        var upperBoundCredit = -1

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("creditslower")
        var lowerBoundCredit = -1
        @JvmField
        var color: String? = null
        @JvmField
        var icon: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("readaccess")
        var readAccess = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetattach")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowGetAttach = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetimage")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowGetImage = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowmediacode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowMediaCode = false

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("maxsigsize")
        var maxSignatureSize = 0

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowbegincode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowBeginCode = false
        var userstatusby: String? = null
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class extendCredit {
        var img: String? = null
        var title: String = ""
        var unit: String = ""
        var ratio: String = ""
        var showinthread: String? = null
        var allowexchangein: String? = null
        var allowexchangeout: String? = null

        @JsonIgnore
        var value = 0
    }

    class Medal {
        @JvmField
        var name: String? = null
        var image: String? = null
        @JvmField
        var description: String? = null

        @JsonProperty("medalid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var medalId = 0
        val medalImageURL: String
            get() = URLUtils.getBBSMedalImageURL(image)
    }

    class MedalInfoJsonDeserializer : JsonDeserializer<List<Medal>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Medal> {
            val currentToken = p.currentToken
            return if (currentToken == JsonToken.VALUE_STRING) {
                ArrayList()
            } else if (currentToken == JsonToken.START_ARRAY) {
                val mapper = jacksonObjectMapper()

                mapper.readValue<List<Medal>>(
                    p,
                    object :
                        TypeReference<List<Medal>>() {})
            } else {
                ArrayList()
            }
        }
    }

    inner class JsonNullAwareDeserializer : JsonNodeDeserializer() {
        override fun getNullValue(ctxt: DeserializationContext): JsonNode {
            return NullNode.getInstance()
        }
    }

    class ProfilePrivacyJsonDeserializer : JsonDeserializer<ProfilePrivacySetting>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): ProfilePrivacySetting {
            val currentToken = p.currentToken
            return if (currentToken == JsonToken.START_OBJECT) {
                ProfilePrivacySetting()
                //                ObjectMapper mapper = new ObjectMapper();
//
//                return mapper.readValue(p,  ProfilePrivacySetting.class);
            } else {
                ProfilePrivacySetting()
            }
        }
    }

    override fun isError(): Boolean {
        return message == null
    }

    companion object {
        private val TAG = UserProfileResult::class.java.simpleName
    }
}