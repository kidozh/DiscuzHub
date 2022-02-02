package com.kidozh.discuzhub.entities;

import android.provider.ContactsContract;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Poll implements Serializable {
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    @JsonDeserialize(using= OneZeroBooleanDeserializer.class)
    public boolean multiple;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "s")
    public Date expirations;
    @JsonProperty("maxchoices")
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public int maxChoices;
    @JsonProperty("voterscount")
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public int votersCount;
    @JsonProperty("visiblepoll")
    @JsonDeserialize(using= OneZeroBooleanDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public boolean resultVisible;

    @JsonProperty("allowvote")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using= OneZeroBooleanDeserializer.class)
    public boolean allowVote;
    @JsonProperty("polloptions")
    @JsonDeserialize(using = optionsDeserializer.class)
    public List<option> options;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonIgnore
    public List<String> remaintime;

    public int getCheckedOptionNumber(){
        if(options == null){
            return 0;
        }
        else {
            int count = 0;
            for(int i=0;i<options.size();i++){
                if(options.get(i).checked){
                    count +=1;
                }
            }
            return count;

        }

    }

    public static class option implements Serializable{
        @JsonProperty("polloptionid")
        public String id;
        @JsonProperty("polloption")
        public String name;
        @JsonProperty("votes")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int voteNumber;
        public String width;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public float percent;
        @JsonProperty("color")
        public String colorName;
        @JsonProperty("imginfo")
        @JsonDeserialize(using = imageInfoDeserializer.class)
        public imageInfo imageInfo;
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonIgnore
        public boolean checked = false;
    }

    public static class imageInfo implements Serializable{
        public String aid,poid,tid,pid,uid,filename,filesize,attachment,remote,width,thumb;

        @JsonProperty("dateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "s")
        public Date updateAt;
        @JsonProperty("small")
        public String smallURL;
        @JsonProperty("big")
        public String bigURL;
    }

    public static class optionsDeserializer extends JsonDeserializer<List<option>>{

        @Override
        public List<option> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.START_OBJECT)){
                ObjectCodec codec = p.getCodec();
                JsonNode optionMapperNode = codec.readTree(p);
                int cnt = 1;
                ObjectMapper objectMapper = new ObjectMapper();
                List<option> options = new ArrayList<>();
                while(true){
                    String cntString = String.valueOf(cnt);
                    if(optionMapperNode.has(cntString)){
                        JsonNode optionObj = optionMapperNode.get(cntString);
                        option parsedOption = objectMapper.treeToValue(optionObj,option.class);
                        options.add(parsedOption);
                    }
                    else {
                        break;
                    }


                    cnt +=1;
                }
                return options;
            }
            return null;
        }
    }

    public static class imageInfoDeserializer extends JsonDeserializer<imageInfo>{

        @Override
        public imageInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.START_ARRAY)){
                return null;
            }
            else if(currentToken.equals(JsonToken.START_OBJECT)){
                ObjectCodec codec = p.getCodec();
                return codec.readValue(p,imageInfo.class);
            }

            return null;
        }
    }

    public static class OneZeroBooleanDeserializer extends JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonToken currentToken = jp.getCurrentToken();

            if (currentToken.equals(JsonToken.VALUE_STRING)) {
                String text = jp.getText();

                if ("0".equals(text) ||("").equals(text)) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }

            } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
                return Boolean.FALSE;
                //return null
            }

            throw ctxt.mappingException("Can't parse boolean value: " + jp.getText());
        }
    }
}
