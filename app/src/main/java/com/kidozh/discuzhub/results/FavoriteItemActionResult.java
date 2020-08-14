package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FavoriteItemActionResult extends BaseResult {
    @JsonProperty("Variables")
    public VariableResults variableResults;
}
