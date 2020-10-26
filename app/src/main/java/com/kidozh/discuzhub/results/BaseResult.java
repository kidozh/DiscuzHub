package com.kidozh.discuzhub.results;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.entities.ErrorMessage;


public class BaseResult {
    @JsonProperty("Version")
    public String apiVersion;
    @JsonProperty("Charset")
    public String Charset;
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("Message")
    public MessageResult message;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String error="";

    public boolean isError(){
        return this.message != null || !TextUtils.isEmpty(error);
    }

    public ErrorMessage getErrorMessage(){
        if(this.message!=null){
            return new ErrorMessage(message.key,message.content);

        }
        else if(!TextUtils.isEmpty(error)){
            return new ErrorMessage(error,error);
        }
        else {
            return null;
        }
    }

}
