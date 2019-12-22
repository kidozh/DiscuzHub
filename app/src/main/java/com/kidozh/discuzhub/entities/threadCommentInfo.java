package com.kidozh.discuzhub.entities;

import java.util.Date;
import java.util.List;

public class threadCommentInfo{
    public String tid,pid;
    public Boolean first = false;
    public String author,authorId;
    public String message,lastPost;
    public Date publishAt;
    public List<attachmentInfo> attachmentInfoList;

    public threadCommentInfo(String tid,String pid,String author,String authorId, String message,Date publishAt){
        this.tid = tid;
        this.pid = pid;
        this.author = author;
        this.authorId = authorId;
        this.message = message;
        this.publishAt = publishAt;
        this.lastPost = null;
    }

    public threadCommentInfo(String tid,String pid,String author,String authorId, String message,Date publishAt, String lastPost){
        this.tid = tid;
        this.pid = pid;
        this.author = author;
        this.authorId = authorId;
        this.message = message;
        this.publishAt = publishAt;
        this.lastPost = lastPost;
    }

    public static class attachmentInfo{
        public String aid,tid,pid,uid;
        public Date publishAt;
        public String filename,relativeUrl;
        public String urlPrefix;

        public attachmentInfo(String aid, String tid,String pid,String uid,String relativeUrl,String filename, Date publishAt, String urlPrefix){
            this.aid = aid;
            this.tid = tid;
            this.pid = pid;
            this.uid = uid;
            this.filename = filename;
            this.publishAt = publishAt;
            this.relativeUrl = relativeUrl;
            this.urlPrefix = urlPrefix;
        }

    }

}
