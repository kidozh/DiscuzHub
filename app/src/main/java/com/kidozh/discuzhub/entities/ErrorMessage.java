package com.kidozh.discuzhub.entities;

import androidx.annotation.NonNull;

public class ErrorMessage {
    @NonNull public String key = "",content = "";

    public ErrorMessage(@NonNull String key, @NonNull String content) {
        this.key = key;
        this.content = content;
    }
}
