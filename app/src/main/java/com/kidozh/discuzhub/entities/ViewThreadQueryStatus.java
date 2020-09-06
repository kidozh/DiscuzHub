package com.kidozh.discuzhub.entities;

import java.util.HashMap;

public class ViewThreadQueryStatus {
    public int tid,page=1,perPage=15;
    public int authorId = -1;
    public boolean hasLoadAll = false;
    // ordertype:1 -> descend 2:-? ascend
    public boolean datelineAscend = true;

    public ViewThreadQueryStatus(int tid, int page) {
        this.tid = tid;
        this.page = page;
    }

    public void clear(){
        this.page=1;
        this.perPage = 15;
    }

    public void setInitAuthorId(int authorId){
        this.page = 1;
        this.authorId = authorId;
        this.hasLoadAll = false;
    }

    public void setInitPage(int page){
        this.page = page;
        this.hasLoadAll = false;
    }

    public HashMap<String,String> generateQueryHashMap(){
        HashMap<String,String> options = new HashMap<>();
        options.put("tid",String.valueOf(this.tid));
        options.put("page",String.valueOf(this.page));
        options.put("ppp",String.valueOf(this.perPage));
        options.put("pollsubmit","1");
        if(this.authorId != -1){
            options.put("authorid",String.valueOf(this.authorId));
        }

        if(this.datelineAscend){
            options.put("ordertype","2");
        }
        else {
            options.put("ordertype","1");
        }
        return options;
    }
}
