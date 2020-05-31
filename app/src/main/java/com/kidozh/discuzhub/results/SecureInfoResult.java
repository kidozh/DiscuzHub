package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureInfoResult extends BaseResult {
    @JsonProperty("Variables")
    public SecureVariables secureVariables;

    public static class SecureVariables extends VariableResults{

        @JsonProperty("sechash")
        public String secHash;
        @JsonProperty("seccode")
        public String secCodeURL;

    }

    public boolean isError(){
        return this.message == null;
    }
}
