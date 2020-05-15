package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class MessageResult implements Serializable {
    @JsonProperty("messagestr")
    public String content;
    @JsonProperty("messageval")
    public String key;
}
