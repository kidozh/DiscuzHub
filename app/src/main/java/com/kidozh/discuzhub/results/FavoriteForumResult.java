package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.FavoriteForum;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteForumResult extends BaseResult {
    @JsonProperty("Variables")
    public FavoriteForumVariable FavoriteForumVariable;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FavoriteForumVariable extends VariableResults{
        @JsonProperty("list")
        public List<FavoriteForum> FavoriteForumList;
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
