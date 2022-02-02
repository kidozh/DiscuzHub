package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
class DiscuzIndexResult : BaseResult(), Serializable {
    @JsonProperty("Variables")
    var forumVariables: ForumVariables = ForumVariables()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ForumVariables : VariableResults() {
//        @JsonProperty("member_email", defaultValue = "")
//        var memberEmail: String = ""
//
//        @JsonProperty("member_credits")
//        var memberCredits: String = ""
//
//        @JsonProperty("setting_bbclosed")
//        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
//        var bbClosed: Boolean = false

//        @JsonProperty("group")
//        var groupInfo: GroupInfo? = null

        @JsonProperty("catlist")
        var forumCategoryList: List<ForumCategory> = ArrayList()

        @JsonProperty("forumlist")
        var forumList: List<Forum> = ArrayList()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class GroupInfo : Serializable {
        @JsonProperty("groupid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var groupId = 0

        @JsonProperty("grouptitle")
        var groupTitle: String? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowvisit: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsendpm: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowinvite: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowmailinvite: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpost: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowreply: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostpoll: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostreward: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowposttrade: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostactivity: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowdirectpost: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowgetattach: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowgetimage: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostattach: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostimage: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowvote: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsearch: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcstatus: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowinvisible: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowtransfer: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsetreadperm: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsetattachperm: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowposttag: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowhidecode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowhtml: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowanonymous: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsigbbcode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsigimgcode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowmagics: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostdebate: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowposturl: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowrecommend: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostrushreply: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcomment: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcommentarticle: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowblog: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowdoing: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowupload: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowshare: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowblogmod: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowdoingmod: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowuploadmod: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsharemod: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcss: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpoke: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowfriend: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowclick: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowmagic: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowstat: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowstatdata: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowviewvideophoto: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowmyop: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowbuildgroup: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowgroupdirectpost: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowgroupposturl: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostarticle: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowdownlocalimg: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowdownremoteimg: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowpostarticlemod: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowspacediyhtml: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowspacediybbcode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowspacediyimgcode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcommentpost: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcommentitem: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcommentreply: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowreplycredit: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsendallpm: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsendpmmaxnum: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowmediacode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowbegincode: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowat: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsave: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsavereply: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsavenum: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowsetpublishdate: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowfollowcollection: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcommentcollection: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowcreatecollection: Boolean? = null

        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowimgcontent: Boolean? = null

        @JsonProperty("allowthreadplugin")
        var allowThreadPlugins: List<String> = ArrayList()
    }

    class ForumCategory : Serializable {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var fid = 0
        @JvmField
        var name: String = ""

        @JsonProperty("forums")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var forumIdList: List<Int> = ArrayList()
        fun getForumByFid(fid: Int, allForumList: List<Forum>): Forum? {
            for (j in allForumList.indices) {
                val currentForum = allForumList[j]
                if (currentForum.fid == fid) {
                    return currentForum
                }
            }
            return null
        }

        fun getForumListInTheCategory(allForumList: List<Forum>): List<Forum> {
            val returnedForumList: MutableList<Forum> = ArrayList()
            for (i in forumIdList.indices) {
                val searchedFid = forumIdList[i]
                // search from other infolist
                for (j in allForumList.indices) {
                    val currentForum = allForumList[j]
                    if (currentForum.fid == searchedFid) {
                        returnedForumList.add(currentForum)
                        break
                    }
                }
            }
            return returnedForumList
        }
    }

    override fun isError(): Boolean {
        return message == null
    }
}