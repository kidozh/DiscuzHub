package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.Thread;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreadCommentsResult extends BaseResult {
    @JsonProperty("Variables")
    public CommentsVariables commentsVariables;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentsVariables extends VariableResults{

        @JsonProperty("data")
        public List<Thread> forumThreadList = new ArrayList<>();
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("perpage")
        public int perPage;

    }

    public boolean isError(){
        return this.message == null;
    }
}
