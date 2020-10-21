package com.kidozh.discuzhub.results;

import android.telecom.TelecomManager;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.FavoriteThread;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteThreadResult extends BaseResult {
    @JsonProperty("Variables")
    public FavoriteThreadVariable favoriteThreadVariable;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FavoriteThreadVariable extends VariableResults{
        @JsonProperty("list")
        public List<FavoriteThread> favoriteThreadList;
        @JsonProperty("perpage")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int perpage;
        @JsonProperty("count")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int count;

    }

}
