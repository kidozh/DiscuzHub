package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserNoteListResult extends BaseResult {
    @JsonProperty("Variables")
    public NoteListVariableResult noteListVariableResult;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NoteListVariableResult extends VariableResults{

        @JsonProperty("list")
        public List<UserNotification> notificationList;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int count, page;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public int perPage;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserNotification{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int id, uid;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("authorid")
        public int authorId;
        public String type, note, author="";
        @JsonProperty("new")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean isNew;
        @JsonProperty("dateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        public Date date;
        @JsonProperty("from_id")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fromId;
        @JsonProperty("from_idtype")
        public String fromIdType;
        @JsonProperty("from_num")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fromNum;
        @JsonProperty("notevar")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public UserNotificationExtraInfo notificationExtraInfo;

    }

    public static class UserNotificationExtraInfo{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int tid, pid;
        public String subject;
        @JsonProperty("actoruid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int actorUid;
        @JsonProperty("actorusername")
        public String actorUsername;
    }

    public boolean isError(){
        return this.message == null;
    }
}
