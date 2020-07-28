package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.HotForum;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotForumsResult extends BaseResult {
    @JsonProperty("Variables")
    public HotForumVariables variables;

    public static class HotForumVariables extends VariableResults{


        @JsonProperty("data")
        public List<HotForum> hotForumList;

    }

    public boolean isError(){
        return this.message != null;
    }
}
