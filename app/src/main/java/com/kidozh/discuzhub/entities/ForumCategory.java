package com.kidozh.discuzhub.entities;

import java.util.List;

public class ForumCategory {
    public String name;
    public int fid;
    public List<Integer> forumFidList;

    public ForumCategory(String name, int fid, List<Integer> forumFidList){
        this.name = name;
        this.fid = fid;
        this.forumFidList = forumFidList;
    }
}
