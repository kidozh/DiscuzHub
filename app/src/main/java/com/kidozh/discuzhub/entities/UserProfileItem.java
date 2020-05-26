package com.kidozh.discuzhub.entities;

public class UserProfileItem {
    public String name, value;
    public int resourceId, color;

    public UserProfileItem(String name, String value, int resourceId, int color) {
        this.name = name;
        this.value = value;
        this.resourceId = resourceId;
        this.color = color;
    }

    public UserProfileItem(String name, String value, int resourceId) {
        this.name = name;
        this.value = value;
        this.resourceId = resourceId;
    }
}
