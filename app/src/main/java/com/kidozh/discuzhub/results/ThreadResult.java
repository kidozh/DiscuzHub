package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.Post;
import com.kidozh.discuzhub.entities.Poll;
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreadResult extends BaseResult {
    @JsonProperty("Variables")
    public ThreadPostVariable threadPostVariables;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadPostVariable extends VariableResults{
        @JsonProperty("thread")
        public bbsParseUtils.DetailedThreadInfo detailedThreadInfo;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fid;
        @JsonProperty("postlist")
        public List<Post> postList;
        @JsonProperty("allowpostcomment")
        public List<String> allowPostCommentList;
        @JsonIgnore
        @JsonProperty("comments")
        @JsonDeserialize(using = CommentListJsonDeserializer.class)
        public Map<String, List<Comment>> commentList;
        @JsonIgnore
        @JsonProperty("commentcount")
        @JsonDeserialize(using = CommentCountJsonDeserializer.class)
        public Map<String, String> commentCount;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int ppp;
        @JsonProperty("setting_rewriterule")
        public Map<String, String> rewriteRule;
        @JsonProperty("setting_rewritestatus")
        @JsonDeserialize(using = SettingRewriteStatusJsonDeserializer.class)
        public List<String> rewriteList;
        @JsonProperty("forum_threadpay")
        public String threadPay;
        @JsonProperty("cache_custominfo_postno")
        public List<String> customPostNoList;
        // for poll
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonProperty("special_poll")
        public Poll pollInfo;

    }





    public boolean isError(){
        return this.message != null;
    }

    public static class SettingRewriteStatusJsonDeserializer extends JsonDeserializer<List<String>>{

        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.VALUE_STRING)){
                return new ArrayList<>();
            }
            else if(currentToken.equals(JsonToken.START_ARRAY)) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(p,  new TypeReference<List<String>>(){});
            }
            else {
                return new ArrayList<>();
            }

        }
    }

    public static class CommentListJsonDeserializer extends JsonDeserializer<Map<String, List<Comment>>>{

        @Override
        public Map<String, List<Comment>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.VALUE_STRING)){
                return new HashMap<>();
            }
            else if(currentToken.equals(JsonToken.START_OBJECT)) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(p,  new TypeReference<Map<String, List<Comment>>>(){});
            }
            else {
                return new HashMap<>();
            }

        }
    }

    public static class CommentCountJsonDeserializer extends JsonDeserializer<Map<String, String>>{

        @Override
        public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.VALUE_STRING)){
                return new HashMap<>();
            }
            else if(currentToken.equals(JsonToken.START_OBJECT)) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(p,  new TypeReference<Map<String, String>>(){});
            }
            else {
                return new HashMap<>();
            }

        }
    }

    public static class Comment{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int id, tid, pid;
        public String author, dateline, comment, avatar;
        @JsonProperty("authorid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int authorId;
    }

}
