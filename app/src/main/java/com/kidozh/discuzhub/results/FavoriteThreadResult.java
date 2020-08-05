package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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





    public boolean isError(){
        return this.message != null;
    }

}
