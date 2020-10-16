package com.kidozh.discuzhub.entities;

public class ThreadCount {
    public int imageResource;
    public String typeString;
    public String type = "";
    public int highlightColorRes = -1;
    public int property = 0;
    public static int PROPERTY_BUY = 1;


    public ThreadCount(int imageResource, String typeString) {
        this.imageResource = imageResource;
        this.typeString = typeString;
    }

    public ThreadCount(int imageResource, String typeString, String type) {
        this.imageResource = imageResource;
        this.typeString = typeString;
        this.type = type;
    }

    public ThreadCount(int imageResource, String typeString, int colorRes) {
        this.imageResource = imageResource;
        this.typeString = typeString;
        this.highlightColorRes = colorRes;
    }

    public ThreadCount(int imageResource, String typeString, int colorRes, int property) {
        this.imageResource = imageResource;
        this.typeString = typeString;
        this.highlightColorRes = colorRes;
        this.property = property;
    }


}
