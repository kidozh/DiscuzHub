package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.FavoriteItem;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteThreadResult extends BaseResult {
    @JsonProperty("Variables")
    public FavoriteThreadVariable favoriteThreadVariable;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FavoriteThreadVariable extends VariableResults{
        @JsonProperty("list")
        public List<FavoriteItem> favoriteItemList;
        @JsonProperty("perpage")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int perpage;
        @JsonProperty("count")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int count;

    }





    public boolean isError(){
        return this.message != null;
    }

}
