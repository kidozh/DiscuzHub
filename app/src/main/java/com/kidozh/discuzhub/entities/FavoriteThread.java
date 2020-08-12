package com.kidozh.discuzhub.entities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.utilities.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@TypeConverters(DateConverter.class)
public class FavoriteThread implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @JsonIgnore
    public int id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int favid, uid;
    @JsonProperty("id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int idKey;
    @NonNull
    @JsonProperty("spaceuid")
    public String idType = "";
    @JsonProperty("id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int spaceUid;
    @NonNull
    public String title, description = "", author;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="s")
    @JsonProperty("dateline")
    public Date date;
    @NonNull
    @JsonProperty("icon")
    public String iconLabel = "";
    @NonNull
    @JsonProperty("url")
    public String url = "";
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int replies;
    @JsonIgnore
    public int belongedBBSId, userId;

    public static DiffUtil.ItemCallback<FavoriteThread> DIFF_CALLBACK = new DiffUtil.ItemCallback<FavoriteThread>() {
        @Override
        public boolean areItemsTheSame(@NonNull FavoriteThread oldItem, @NonNull FavoriteThread newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FavoriteThread oldItem, @NonNull FavoriteThread newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteThread that = (FavoriteThread) o;
        return id == that.id &&
                favid == that.favid &&
                uid == that.uid &&
                idKey == that.idKey &&
                spaceUid == that.spaceUid &&
                replies == that.replies &&
                idType.equals(that.idType) &&
                title.equals(that.title) &&
                description.equals(that.description) &&
                author.equals(that.author) &&
                Objects.equals(date, that.date) &&
                iconLabel.equals(that.iconLabel) &&
                url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, favid, uid, idKey, idType, spaceUid, title, description, author, date, iconLabel, url, replies);
    }

    public ThreadInfo toThread(){
        ThreadInfo threadInfo = new ThreadInfo();
        threadInfo.tid = idKey;
        threadInfo.publishAt = date;
        threadInfo.subject = title;
        threadInfo.author = author;
        threadInfo.replies = String.valueOf(replies);
        return threadInfo;
    }
}
