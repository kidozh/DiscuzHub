package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiMessageActionResult extends BaseResult {
    @JsonProperty("Variables")
    public VariableResults variableResults;
}
