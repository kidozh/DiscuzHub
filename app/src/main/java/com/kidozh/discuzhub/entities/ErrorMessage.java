package com.kidozh.discuzhub.entities;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.R;

public class ErrorMessage {
    @NonNull public String key = "",content = "";
    public int errorIconResource = R.drawable.ic_error_outline_24px;

    public ErrorMessage(@NonNull String key, @NonNull String content) {
        this.key = key;
        this.content = content;
    }

    public ErrorMessage(@NonNull String key, @NonNull String content, int errorIconResource) {
        this.key = key;
        this.content = content;
        this.errorIconResource = errorIconResource;
    }

    public static int getDefaultErrorIconResource(){
        return R.drawable.ic_error_outline_24px;
    }
}
