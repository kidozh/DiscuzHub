package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResult extends BaseResult {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("Variables")
    public VariableResults variables;

}
