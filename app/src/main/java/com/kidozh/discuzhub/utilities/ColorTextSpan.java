package com.kidozh.discuzhub.utilities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;


public class ColorTextSpan extends ReplacementSpan {

    private static final float PADDING = 10.0f;

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end) + PADDING);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        // Text
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#529ECC"));
        int xPos = Math.round(x + PADDING / 2);
        int yPos = (int) ((top + bottom) / 2 - (paint.descent() + paint.ascent()) / 2);
        canvas.drawText(text, start, end, xPos, yPos, paint);
    }
}
