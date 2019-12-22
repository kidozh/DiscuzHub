package com.kidozh.discuzhub.utilities;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;


public class AttachImage extends ImageSpan {

    public String aid;

    public AttachImage(String aid, Drawable drawable, int maxWidth) {
        super(drawable);
        this.aid = aid;
        int width = (int) (drawable.getIntrinsicWidth() * 1.2f);
        int height = (int) (drawable.getIntrinsicHeight() * 1.2f);

        if (width > maxWidth) {
            height = (int) (height * maxWidth * 1.0f / width);
            width = maxWidth;
        }
        drawable.setBounds(0, 0, width, height);
    }
}
