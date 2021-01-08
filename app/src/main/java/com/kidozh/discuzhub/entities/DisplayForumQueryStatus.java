package com.kidozh.discuzhub.entities;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class DisplayForumQueryStatus {
    private static final String TAG = DisplayForumQueryStatus.class.getSimpleName();
    public int fid,page = 1,perPage=10;
    public boolean hasLoadAll = false;
    // orderby:[dateline,replies,views]
    @NonNull
    public String orderBy="",specialType="";
    // filter:
    @NonNull
    public String filter="";
    @NonNull
    public int filterTypeId = 0;
    public int dateline = 0;

    public DisplayForumQueryStatus(int fid, int page){
        this.fid = fid;
        this.page = page;
    }



    public void clear(){
        this.page=1;
        this.perPage = 15;
        this.orderBy = "";
        this.filterTypeId = 0;
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

        Log.d(TAG,"Type id "+this.filterTypeId);
        if(this.filterTypeId != -1){
            options.put("typeid",String.valueOf(this.filterTypeId));
            options.put("filter","typeid");
        }

        if(!this.orderBy.equals("")){
            options.put("orderby",this.orderBy);
            options.put("filter","reply");
        }



        if(!this.specialType.equals("")){
            options.put("specialtype",this.specialType);
            options.put("filter","specialtype");
        }

        if(this.dateline != 0){
            options.put("dateline",String.valueOf(this.dateline));
            options.put("filter","dateline");
        }

        if(!this.filter.equals("")){
            options.put("filter",this.filter);

        }

        return options;

    }
}
