package com.kidozh.discuzhub.entities;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

@Entity
public class forumUserBriefInfo implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    public String auth,saltkey,uid,username,avatarUrl, groupId;
    public int readPerm;
    public int belongedBBSID;
    @JsonIgnore
    public int position = 0;

    public forumUserBriefInfo(String auth, String saltkey, String uid, String username, String avatarUrl, int readPerm, String groupId){
        this.auth = auth;
        this.saltkey = saltkey;
        this.uid = uid;
        this.username = username;
        this.readPerm = readPerm;
        this.avatarUrl = avatarUrl;
        this.groupId = groupId;
    }

    public int getUid(){
        return Integer.parseInt(uid);
    }

//    public forumUserBriefInfo(String auth, String saltkey, String uid, String username, String avatarUrl, int readPerm, String groupId, int belongedBBSID){
//        this.auth = auth;
//        this.saltkey = saltkey;
//        this.uid = uid;
//        this.username = username;
//        this.readPerm = readPerm;
//        this.avatarUrl = avatarUrl;
//        this.groupId = groupId;
//        this.belongedBBSID = belongedBBSID;
//    }


    public boolean isValid(){
        return this.auth !=null && (!this.auth.equals("null"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
