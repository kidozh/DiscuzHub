package com.kidozh.discuzhub.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity
public class bbsThreadDraft implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    public String subject,content;
    public Date lastUpdateAt;
    public int belongBBSId;
    public String fid,forumName,typeid,typeName,apiString;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public bbsThreadDraft(String subject, String content, Date lastUpdateAt, int belongBBSId, String fid, String forumName, String typeid, String typeName, String apiString) {
        this.subject = subject;
        this.content = content;
        this.lastUpdateAt = lastUpdateAt;
        this.belongBBSId = belongBBSId;
        this.fid = fid;
        this.forumName = forumName;
        this.typeid = typeid;
        this.typeName = typeName;
        this.apiString = apiString;
    }
}
