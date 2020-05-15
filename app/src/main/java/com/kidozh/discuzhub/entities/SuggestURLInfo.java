package com.kidozh.discuzhub.entities;



public class SuggestURLInfo {
    public String url,name;
    public Boolean valid = false;

    public SuggestURLInfo(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public SuggestURLInfo(String url, String name, Boolean valid) {
        this.url = url;
        this.name = name;
        this.valid = valid;
    }
}
