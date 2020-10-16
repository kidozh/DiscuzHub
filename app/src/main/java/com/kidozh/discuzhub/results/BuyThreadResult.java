package com.kidozh.discuzhub.results;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BuyThreadResult extends BaseResult {
    @JsonProperty("Variables")
    @NonNull
    public BuyThreadVariableResult variableResults;

    public static class BuyThreadVariableResult extends VariableResults{
        @JsonProperty("authorid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int authorId;
        public String author;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int price, balance;
        public Credit credit;

    }

    public static class Credit{
        public String title = "";
        @JsonIgnoreProperties(ignoreUnknown = true)
        public String unit = "";
    }
}
