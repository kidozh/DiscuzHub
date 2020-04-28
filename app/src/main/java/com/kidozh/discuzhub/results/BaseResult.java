package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class BaseResult {
    @JsonProperty("Version")
    public String apiVersion;
    @JsonProperty("Charset")
    public String Charset;
}
