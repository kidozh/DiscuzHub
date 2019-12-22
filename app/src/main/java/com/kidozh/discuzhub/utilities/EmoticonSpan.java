package com.kidozh.discuzhub.utilities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class EmoticonSpan extends ImageSpan {

    private float scale = 1.0f;

    public EmoticonSpan(Drawable drawable) {
        super(drawable);
    }

    //这个行数设置行的大小，可以设置fontMetricsInt的top/bottom/accent/decent等
    //设置完成之后一行的大小就会改变
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = getDrawable();
        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            float lineHeight = fontMetricsInt.bottom - fontMetricsInt.top;
            int imgHeight = drawable.getIntrinsicHeight();
            if (imgHeight > 0) {
                scale = lineHeight / imgHeight;
            }
        }
        //返回表情的宽度
        return (int) (rect.right * scale) + 5;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable drawable = getDrawable();
        canvas.save();
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        int transY = y + (int) ((fm.descent + fm.ascent) / 2 - drawable.getIntrinsicHeight() * scale / 2);
        //int transY = ((bottom - top) - drawable.getBounds().bottom) / 2 + top;
        canvas.translate(x, transY);
        canvas.scale(scale, scale);
        drawable.draw(canvas);
        canvas.restore();
    }
}

