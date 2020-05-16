package com.kidozh.discuzhub.entities;

import android.content.SharedPreferences;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostInfo implements Serializable {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int pid, tid;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    public boolean first,anonymous;
    public String author, dateline, message, username;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("authorid")
    public int authorId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int attachment, status, replycredit, position, number;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("adminid")
    public int adminId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("groupid")
    public int groupId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("memeberstatus")
    public int memberStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "s")
    @JsonProperty("dbdateline")
    public Date publishAt;


    @JsonProperty("attachments")
    public Map<String, Attachment> attachmentMapper = new HashMap<>();
    @JsonProperty("attachlist")
    public List<String> attachmentIdList;
    @JsonProperty("groupiconid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int groupIconId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment implements Serializable{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int aid, tid, pid, uid;
        public String dateline, filename;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("filesize")
        public int fileSize;
        public String attachment;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean remote, thumb, payed;
        public String description;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("readperm")
        public int readPerm;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int price;
        @JsonProperty("isimage")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int isImageNum;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int width;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("picid")
        public int picId;
        public String ext;
        @JsonProperty("attachicon")
        public String attachIcon;
        @JsonProperty("attachsize")
        public String attachSize;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("attachimg")
        public boolean attachImg;
        public String url;
        @JsonProperty("dbdateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        public Date updateAt;
        @JsonProperty("aidencode")
        public String aidEncode;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int downloads;
        @JsonProperty("imgalt")
        public String imageAlt;


        public boolean isImage(){
            return isImageNum != 0;
        }


    }

    public List<Attachment> getAllAttachments(){
        List<Attachment> attachmentList = new ArrayList<>();
        for(String key : this.attachmentMapper.keySet()){
            Attachment attachment = attachmentMapper.get(key);
            attachmentList.add(attachment);
        }
        return attachmentList;
    }


}
