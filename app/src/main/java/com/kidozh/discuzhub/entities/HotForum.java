package com.kidozh.discuzhub.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class HotForum implements Serializable {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int fid;
    public String name = "";
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int threads, posts;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("todayposts")
    public int todayPosts;
    public String lastpost = "";
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("lastpost_tid")
    public int lastPostTid;
    @JsonProperty("lastpost_subject")
    public String lastPostSubject = "";
    @JsonProperty("lastposter")
    public String lastPoster ="";
}
