package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayForumResult extends BaseResult {
    @JsonProperty("Variables")
    public ForumVariables forumVariables;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForumVariables extends VariableResults{
        @JsonProperty("forum")
        public ForumInfo forumInfo;
        @JsonProperty("group")
        public GroupInfo groupInfo;
        @JsonProperty("forum_threadlist")
        public List<ThreadInfo> forumThreadList;
        @JsonProperty("groupiconid")
        public Map<String, String> groupIconId;
        @JsonProperty("sublist")
        public List<SubForumInfo> subForumLists;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int tpp, page;
        @JsonProperty("reward_unit")
        public String rewardUnit;
        @JsonProperty("threadtypes")
        public ThreadTypeInfo threadTypeInfo;

    }



    public static class GroupInfo implements Serializable{
        @JsonProperty("groupid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int groupId;
        @JsonProperty("grouptitle")
        public String groupTitle;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShortReply implements Serializable {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int pid;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("authorid")
        public int authorId;
        public String author, message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubForumInfo implements Serializable{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fid, threads, posts;
        public String name;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("todayposts")
        public int todayPosts;
        @JsonProperty("icon")
        public String iconURL;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadTypeInfo implements Serializable{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean required, listable, prefix;
        @JsonProperty("types")
        public Map<String, String> idNameMap;
        @JsonProperty("icons")
        public Map<String, String> idIconMap;
        @JsonProperty("moderators")
        public Map<String, String> idModeratorMap;
    }

    public boolean isError(){
        return this.message != null;
    }
}
