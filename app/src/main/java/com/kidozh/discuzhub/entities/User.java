package com.kidozh.discuzhub.entities;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@Entity
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    public String auth,saltkey,username,avatarUrl;
    @JsonProperty("groupid")
    public int groupId;
    public int uid;
    public int readPerm;
    public int belongedBBSID;
    @JsonIgnore
    public int position = 0;

    public User(String auth, String saltkey, int uid, String username, String avatarUrl, int readPerm, int groupId){
        this.auth = auth;
        this.saltkey = saltkey;
        this.uid = uid;
        this.username = username;
        this.readPerm = readPerm;
        this.avatarUrl = avatarUrl;
        this.groupId = groupId;
    }

    public int getUid(){
        return uid;
    }


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
