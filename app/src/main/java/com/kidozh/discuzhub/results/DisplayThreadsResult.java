package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayThreadsResult extends BaseResult {
    @JsonProperty("Variables")
    public ForumVariables forumVariables;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForumVariables extends VariableResults{

        @JsonProperty("data")
        public List<ThreadInfo> forumThreadList = new ArrayList<>();
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("perpage")
        public int perPage;

    }

    public boolean isError(){
        return this.message == null;
    }
}
