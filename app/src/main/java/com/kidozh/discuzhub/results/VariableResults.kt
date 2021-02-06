package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

public class VariableResults extends BaseResult {
    public String cookiepre, auth, saltkey, member_username, member_avatar;
    public int member_uid;
    @JsonProperty("groupid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int groupId;
    @JsonProperty("formhash")
    public String formHash;
    @JsonProperty("ismoderator")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    public Boolean moderator;
    @JsonProperty("readaccess")
    public int readAccess;
    @JsonProperty("notice")
    public newNoticeNumber noticeNumber;

    public static class newNoticeNumber{
        @JsonProperty("newpush")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int push;
        @JsonProperty("newpm")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int privateMessage;
        @JsonProperty("newprompt")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int prompt;
        @JsonProperty("newmypost")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int myPost;
    }

    public forumUserBriefInfo getUserBriefInfo(){
        return new forumUserBriefInfo(auth,saltkey,String.valueOf(member_uid),member_username,member_avatar,readAccess,String.valueOf(groupId));
    }
}
