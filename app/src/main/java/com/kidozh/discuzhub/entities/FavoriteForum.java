package com.kidozh.discuzhub.entities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kidozh.discuzhub.utilities.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@TypeConverters(DateConverter.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteForum implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @JsonIgnore
    public int id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int favid, uid;
    @JsonProperty("id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int idKey;
    @NonNull
    @JsonProperty("idtype")
    public String idType = "";
    @JsonProperty("spaceuid")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int spaceUid;
    @NonNull
    public String title="", description = "", author="";
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
    public int threads, posts, todayposts, yesterdayposts;
    @JsonIgnore
    public int belongedBBSId, userId;

    public static DiffUtil.ItemCallback<FavoriteForum> DIFF_CALLBACK = new DiffUtil.ItemCallback<FavoriteForum>() {
        @Override
        public boolean areItemsTheSame(@NonNull FavoriteForum oldItem, @NonNull FavoriteForum newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FavoriteForum oldItem, @NonNull FavoriteForum newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteForum)) return false;
        FavoriteForum that = (FavoriteForum) o;
        return id == that.id &&
                favid == that.favid &&
                uid == that.uid &&
                idKey == that.idKey &&
                spaceUid == that.spaceUid &&
                threads == that.threads &&
                posts == that.posts &&
                todayposts == that.todayposts &&
                yesterdayposts == that.yesterdayposts &&
                belongedBBSId == that.belongedBBSId &&
                userId == that.userId &&
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
        return Objects.hash(id, favid, uid, idKey, idType, spaceUid, title, description, author, date, iconLabel, url, threads, posts, todayposts, yesterdayposts, belongedBBSId, userId);
    }

    public ForumInfo toForum(){
        ForumInfo forum = new ForumInfo();
        forum.fid = idKey;
        forum.name = title;
        forum.todayPosts = todayposts;
        forum.description = description;
        forum.threadCount = threads;
        forum.posts = posts;
        return forum;
    }
}
