package com.kidozh.discuzhub.entities;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class threadInfo implements Serializable {
    public String tid,fid;
    public String typeid;
    public String readperm;
    public String author,authorId;
    public String subject;
    public Date publishAt,UpdateAt;
    public String lastUpdator, lastUpdateTimeString;
    public String viewNum,repliesNum;
    public int price,attachment;
    public Boolean special, digest;
    public int recommendNum;
    public Boolean rushReply = false;
    public Boolean isTop = false;
    public String displayOrder = "0";
    public List<shortReplyInfo> shortReplyInfoList;
    public int replyCredit = 0;
    public threadInfo(){}

    public static class shortReplyInfo implements Serializable{
        public String pid;
        public String author,authorId;
        public String message;

        public shortReplyInfo(String pid,String author,String authorId,String message){
            this.pid= pid;
            this.author = author;
            this.authorId =authorId;
            this.message  = message;

        }
    }

}
