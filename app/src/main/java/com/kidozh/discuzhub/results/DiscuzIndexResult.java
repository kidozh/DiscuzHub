package com.kidozh.discuzhub.results;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.Forum;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscuzIndexResult extends BaseResult implements Serializable{
    @JsonProperty("Variables")
    public ForumVariables forumVariables;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForumVariables extends VariableResults{
        @JsonProperty("member_email")
        public String memberEmail;
        @JsonProperty("member_credits")
        public String memberCredits;
        @JsonProperty("setting_bbclosed")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public Boolean bbClosed;
        @JsonProperty("group")
        public GroupInfo groupInfo;
        @JsonProperty("catlist")
        public List<ForumCategory> forumCategoryList;
        @JsonProperty("forumlist")
        public List<Forum> forumList;


    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupInfo implements Serializable {
        @JsonProperty("groupid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int groupId;
        @JsonProperty("grouptitle")
        public String groupTitle;
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public Boolean allowvisit,allowsendpm,allowinvite,allowmailinvite,allowpost, allowreply
                ,allowpostpoll,allowpostreward,allowposttrade,allowpostactivity,allowdirectpost,allowgetattach,
                allowgetimage,allowpostattach,allowpostimage,allowvote,allowsearch,allowcstatus,allowinvisible,
                allowtransfer,allowsetreadperm,allowsetattachperm,allowposttag,allowhidecode,allowhtml,allowanonymous,
                allowsigbbcode,allowsigimgcode,allowmagics,allowpostdebate,allowposturl,allowrecommend,
                allowpostrushreply, allowcomment,allowcommentarticle,allowblog,allowdoing,allowupload,allowshare,
                allowblogmod,allowdoingmod, allowuploadmod,allowsharemod,allowcss,allowpoke,allowfriend,allowclick,
                allowmagic,allowstat,allowstatdata,
                allowviewvideophoto,allowmyop,allowbuildgroup,allowgroupdirectpost,allowgroupposturl,allowpostarticle,
                allowdownlocalimg,allowdownremoteimg,allowpostarticlemod,allowspacediyhtml,allowspacediybbcode,
                allowspacediyimgcode,allowcommentpost,allowcommentitem,allowcommentreply,allowreplycredit,
                allowsendallpm,allowsendpmmaxnum,allowmediacode,allowbegincode,allowat,allowsave,allowsavereply,
                allowsavenum,allowsetpublishdate,allowfollowcollection,allowcommentcollection,allowcreatecollection,
                allowimgcontent;
        @JsonProperty("allowthreadplugin")
        List<String> allowThreadPlugins;

    }

    public static class ForumCategory implements Serializable{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fid;
        public String name;
        @JsonProperty("forums")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public List<Integer> forumIdList;

        public Forum getForumByFid(int fid, @NonNull List<Forum> allForumList){
            for(int j = 0; j < allForumList.size(); j++){
                Forum currentForum = allForumList.get(j);
                if(currentForum.fid == fid){
                    return currentForum;

                }
            }
            return null;
        }

        public List<Forum> getForumListInTheCategory(@NonNull List<Forum> allForumList){
            if(forumIdList == null){
                return new ArrayList<>();
            }
            List<Forum> returnedForumList = new ArrayList<>();
            for(int i=0; i<forumIdList.size(); i++){
                int searchedFid = forumIdList.get(i);
                // search from other infolist
                for(int j = 0; j < allForumList.size(); j++){
                    Forum currentForum = allForumList.get(j);
                    if(currentForum.fid == searchedFid){
                        returnedForumList.add(currentForum);
                        break;
                    }
                }
            }
            return returnedForumList;
        }
    }


    public boolean isError(){
        return this.message == null;
    }
}
