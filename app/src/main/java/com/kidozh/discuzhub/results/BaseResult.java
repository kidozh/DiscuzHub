package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class BaseResult {
    @JsonProperty("Version")
    public String apiVersion;
    @JsonProperty("Charset")
    public String Charset;
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("Message")
    public MessageResult message;


}
