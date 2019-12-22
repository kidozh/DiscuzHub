package com.kidozh.discuzhub.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class forumInfo implements Parcelable {
    public String name;
    public int fid;
    public String iconURL;
    public String description;
    public String todayPost,totalPost,threads;
    forumInfo(){}
    public forumInfo(String name, int fid,String iconURL, String description, String todayPost,String totalPost, String threads){
        this.name = name;
        this.fid = fid;
        this.iconURL = iconURL;
        this.description =description;
        this.todayPost = todayPost;
        this.totalPost = totalPost;
        this.threads = threads;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.fid);
        dest.writeString(this.iconURL);
        dest.writeString(this.description);
        dest.writeString(this.todayPost);
        dest.writeString(this.totalPost);
        dest.writeString(this.threads);
    }

    public static final Creator<forumInfo> CREATOR = new Creator<forumInfo>() {
        @Override
        public forumInfo createFromParcel(Parcel source) {
            forumInfo forumInfo = new forumInfo();
            forumInfo.name = source.readString();
            forumInfo.fid = source.readInt();
            forumInfo.iconURL = source.readString();
            forumInfo.description = source.readString();
            forumInfo.todayPost = source.readString();
            forumInfo.totalPost = source.readString();
            forumInfo.threads = source.readString();
            return forumInfo;
        }

        @Override
        public forumInfo[] newArray(int size) {
            return new forumInfo[size];
        }
    };
}
