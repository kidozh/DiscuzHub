package com.kidozh.discuzhub.utilities;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Selection;
import android.text.method.MovementMethod;


public class bbsLinkMovementMethod extends LinkMovementMethod {
    private static LinkMovementMethod sInstance;


    private OnLinkClickedListener mOnLinkClickedListener;

    public bbsLinkMovementMethod(OnLinkClickedListener onLinkClickedListener){
        mOnLinkClickedListener = onLinkClickedListener;
    }

    int x1;
    int x2;
    int y1;
    int y2;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN){
            x1 = (int) event.getX();
            y1 = (int) event.getY();
        }

        if (action == MotionEvent.ACTION_UP) {
            x2 = (int) event.getX();
            y2 = (int) event.getY();

            if (Math.abs(x1 - x2) < 10 && Math.abs(y1 - y2) < 10) {

                x2 -= widget.getTotalPaddingLeft();
                y2 -= widget.getTotalPaddingTop();

                x2 += widget.getScrollX();
                y2 += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y2);
                int off = layout.getOffsetForHorizontal(line, x2);
                /**
                 * get you interest span
                 */
                URLSpan[] spans = buffer.getSpans(off, off, URLSpan.class);
                if (spans.length != 0) {
                    String url = spans[0].getURL();
                    boolean handled = mOnLinkClickedListener.onLinkClicked(url);
                    if(handled){
                        return true;
                    }

                    return super.onTouchEvent(widget, buffer, event);
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    public boolean canSelectArbitrarily() {
        return true;
    }

    public boolean onKeyUp(TextView widget, Spannable buffer, int keyCode,
                           KeyEvent event) {
        return false;
    }

    public interface OnLinkClickedListener {
        boolean onLinkClicked(String url);
    }

}
