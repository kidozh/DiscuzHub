package com.kidozh.discuzhub.entities;

import android.util.Log;

import java.util.HashMap;

public class DisplayForumQueryStatus {
    private static final String TAG = DisplayForumQueryStatus.class.getSimpleName();
    public int fid,page = 1,perPage=10;
    public boolean hasLoadAll = false;
    // orderby:[dateline,replies,views]
    public String orderBy="";
    // filter:
    public String filter="",filterId="";

    public DisplayForumQueryStatus(int fid, int page){
        this.fid = fid;
        this.page = page;
    }



    public void clear(){
        this.page=1;
        this.perPage = 15;
        this.orderBy = "";
        this.filterId = "";
        this.filter = "";
    }

    public void setInitAuthorId(int authorId){
        this.page = 1;
        this.hasLoadAll = false;
    }

    public void setInitPage(int page){
        this.page = page;
        this.hasLoadAll = false;
    }

    public HashMap<String,String> generateQueryHashMap(){
        HashMap<String,String> options = new HashMap<>();
        options.put("fid",String.valueOf(fid));
        options.put("page",String.valueOf(page));
        options.put("ppp",String.valueOf(perPage));
        if(!orderBy.equals("")){
            options.put("orderby",this.orderBy);
        }

        if(!this.filter.equals("")){

            options.put("filter",this.filter);
            switch (this.filter){
                case ("specialtype"):{
                    options.put("specialtype","poll");
                    break;
                }
                case ("lastpost"):{
                    options.put("orderby","lastpost");
                    break;
                }
                case ("heat"):{
                    options.put("orderby","heats");
                    break;
                }
                case ("digest"):{
                    options.put("digest","1");
                    break;
                }
            }
        }
        Log.d(TAG,"Type id "+this.filterId);
        if(!this.filterId.equals("")){

            options.put("filter","typeid");
            options.put("typeid",this.filterId);
        }
        return options;

    }
}
