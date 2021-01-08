package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumResult extends BaseResult {
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
        public List<SubForumInfo> subForumLists = new ArrayList<>();
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
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public boolean required = false, listable = false;
        public String prefix;
        @JsonProperty("types")
        @JsonDeserialize(using = ForumThreadTypeDeserializer.class)
        public HashMap<String, String> idNameMap = new HashMap<>();
        @JsonProperty("icons")
        @JsonDeserialize(using = ForumThreadTypeDeserializer.class)
        public Map<String, String> idIconMap= new HashMap<>();
        @JsonProperty("moderators")
        @JsonDeserialize(using = ForumThreadTypeDeserializer.class)
        public Map<String, String> idModeratorMap= new HashMap<>();

    }

    public static class ForumThreadTypeDeserializer extends JsonDeserializer<Map<String,String>>{

        @Override
        public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.START_OBJECT)) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(p,  new TypeReference<Map<String,String>>(){});
            }
            else {
                return new HashMap<>();
            }
        }
    }

    public boolean isError(){
        return this.message != null;
    }
}
