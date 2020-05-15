package com.kidozh.discuzhub.results;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BBSIndexResult extends BaseResult implements Serializable{
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
        public List<ForumInfo> forumInfoList;


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

        public ForumInfo getForumByFid(int fid,@NonNull List<ForumInfo> allForumInfoList){
            for(int j=0;j <allForumInfoList.size();j++){
                ForumInfo currentForum = allForumInfoList.get(j);
                if(currentForum.fid == fid){
                    return currentForum;

                }
            }
            return null;
        }

        public List<ForumInfo> getForumListInTheCategory(@NonNull List<ForumInfo> allForumInfoList){
            if(forumIdList == null){
                return new ArrayList<>();
            }
            List<ForumInfo> returnedForumInfoList = new ArrayList<>();
            for(int i=0; i<forumIdList.size(); i++){
                int searchedFid = forumIdList.get(i);
                // search from other infolist
                for(int j=0;j <allForumInfoList.size();j++){
                    ForumInfo currentForum = allForumInfoList.get(j);
                    if(currentForum.fid == searchedFid){
                        returnedForumInfoList.add(currentForum);
                        break;
                    }
                }
            }
            return returnedForumInfoList;
        }
    }


    public boolean isError(){
        return this.message == null;
    }
}
