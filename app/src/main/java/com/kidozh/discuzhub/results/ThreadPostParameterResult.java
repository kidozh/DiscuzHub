package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class ThreadPostParameterResult extends BaseResult {
    @JsonProperty("Variables")
    public PermissionVariablesResult permissionVariables;

    public static class PermissionVariablesResult extends VariableResults{
        @JsonProperty("allowperm")
        public AllowPermission allowPerm;


    }
    public static class AllowPermission{
        @JsonProperty("allowpost")
        @JsonDeserialize(using= OneZeroBooleanJsonDeserializer.class)
        public boolean allowPost;
        @JsonProperty("allowreply")
        @JsonDeserialize(using= OneZeroBooleanJsonDeserializer.class)
        public boolean allowReply;
        @JsonProperty("allowupload")
        public UploadSize uploadSize;
        @JsonProperty("attachremain")
        public RemainedAttachment remainedAttachment;
        @JsonProperty("uploadhash")
        public String uploadHash;

    }

    public static class UploadSize{
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int jpg, jpeg, gif, png, mp3, txt, zip, rar, pdf;

        public List<String> getAllowableFileSuffix() {
            List<String> fileSuffixList = new ArrayList<>();
            Field[] fields = getClass().getDeclaredFields();
            try{
                for (Field field : fields) {
                    field.setAccessible(true);
                    int value = field.getInt(this);
                    if (value != 0) {
                        fileSuffixList.add(field.getName());
                    }
                }
                return fileSuffixList;
            }
            catch (IllegalAccessException e){
                return fileSuffixList;
            }

        }
    }

    public static class RemainedAttachment{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int size, count;
    }
}
