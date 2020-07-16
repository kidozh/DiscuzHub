package com.kidozh.discuzhub.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity
public class ViewHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    public String avatarURL;
    public String name;
    public int belongedBBSId;
    public String description;
    public int type;
    public int fid;
    public int tid;
    public Date recordAt;
    @Ignore
    public final static int VIEW_TYPE_FORUM = 0, VIEW_TYPE_THREAD = 1, VIEW_TYPE_USER_PROFILE = 2;


    public ViewHistory(String avatarURL, String name, int belongedBBSId, String description, int type, int fid, int tid, Date recordAt) {

        this.avatarURL = avatarURL;
        this.name = name;
        this.belongedBBSId = belongedBBSId;
        this.description = description;
        this.type = type;
        this.fid = fid;
        this.tid = tid;
        this.recordAt = recordAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewHistory that = (ViewHistory) o;
        return id == that.id &&
                belongedBBSId == that.belongedBBSId &&
                type == that.type &&
                fid == that.fid &&
                tid == that.tid &&
                avatarURL.equals(that.avatarURL) &&
                name.equals(that.name) &&
                description.equals(that.description) &&
                recordAt.equals(that.recordAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, avatarURL, name, belongedBBSId, description, type, fid, tid, recordAt);
    }
}
