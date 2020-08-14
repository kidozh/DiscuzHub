package com.kidozh.discuzhub.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreadInfo implements Serializable{
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int tid, price, recommend, fid,
            heat, status, favtimes, sharetimes, stamp ,icon,
            comments, pages, heatlevel;
    // sometimes replies becomes -
    public String views, replies;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("pushedaid")
    public int pushedAid;
    @JsonProperty("relatebytag")
    public String relateByTag;
    @JsonProperty("maxposition")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int maxPostion;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("typeid")
    public int typeId;
    @JsonProperty("posttableid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int postTableId = 0;
    @JsonProperty("sortid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int sortId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("readperm")
    public int readPerm;
    public String author, subject, dateline;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("authorid")
    public int authorId;
    @JsonProperty("lastpost")
    public String lastPost;
    @JsonProperty("lastposter")
    public String lastPoster;
    @JsonProperty("displayorder")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int displayOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    public boolean digest =false, special =false, moderated = false,
            stickreply = false, isgroup = false,
            hidden=false, moved = false;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("new")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    public boolean isNew;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int attachment, closed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("recommend_add")
    public int recommendNum;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("recommend_sub")
    public int recommendSubNum;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("recommends")
    public int recommends;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("replycredit")
    public int replyCredit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="s")
    @JsonProperty("dbdateline")
    public Date publishAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="s")
    @JsonProperty("dblastpost")
    public Date updateAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("rushreply")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    public boolean rushReply;
    @JsonProperty("reply")
    public List<ForumResult.ShortReply> shortReplyList;
    public String highlight;
    public String folder;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("icontid")
    public int iconTid;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("istoday")
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    public boolean isToday = false;
    public String id;
    @JsonProperty("avatar")
    public String avatarURL;

}
