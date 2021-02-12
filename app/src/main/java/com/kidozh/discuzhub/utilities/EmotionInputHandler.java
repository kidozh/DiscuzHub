package com.kidozh.discuzhub.utilities;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-3-20.
 * 表情处理
 * 格式处理 加粗 斜体等。。。
 */
public class EmotionInputHandler implements TextWatcher {

    private final EditText mEditor;
    private final TextChangeListener listener;
    private final ArrayList<ImageSpan> mEmoticonsToRemove = new ArrayList<>();
    private final ArrayList<ColorTextSpan> colorTextSpansToRemove = new ArrayList<>();
    private final ArrayList<AttachImage> imageSpansToRemove = new ArrayList<>();

    public EmotionInputHandler(EditText editor, TextChangeListener listener) {
        mEditor = editor;
        mEditor.addTextChangedListener(this);
        this.listener = listener;
    }


    public void insertString(String s) {
        int start = mEditor.getSelectionStart();
        int end = mEditor.getSelectionEnd();
        Editable editableText = mEditor.getEditableText();

        editableText.replace(start, end, s);
        editableText.setSpan(new ColorTextSpan(), start, start + s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void insertSmiley(String s, Drawable drawable) {
        if (drawable != null) {
            EmoticonSpan emoticonSpan = new EmoticonSpan(drawable);
            int start = mEditor.getSelectionStart();
            int end = mEditor.getSelectionEnd();
            Editable editableText = mEditor.getEditableText();
            // Insert the emoticon.
            editableText.replace(start, end, s);
            editableText.setSpan(emoticonSpan, start, start + s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public List<String> getImagesAids() {
        Editable message = mEditor.getEditableText();
        int end = mEditor.getText().toString().length();
        List<String> returns = new ArrayList<>();

        AttachImage[] list3 = message.getSpans(0, end, AttachImage.class);
        for (AttachImage span : list3) {
            int spanStart = message.getSpanStart(span);
            int spanEnd = message.getSpanEnd(span);
            if ((spanStart < end) && (spanEnd > 0)) {
                //[attachimg]12345[/attachimg] -> 12345
                String aid = span.aid.replace("[attachimg]", "").replace("[/attachimg]", "").trim();
                if (!TextUtils.isEmpty(aid)){
                    returns.add(aid);
                }

            }
        }

        return returns;
    }

    public void insertImage(String s, Drawable drawable, int maxWidth) {
        if (drawable != null) {
            s = "[attachimg]" + s + "[/attachimg]";
            AttachImage imageSpan = new AttachImage(s, drawable, maxWidth);
            int start = mEditor.getSelectionStart();
            int end = mEditor.getSelectionEnd();
            Editable editableText = mEditor.getEditableText();
            // Insert the emoticon.
            editableText.replace(start, end, s);
            editableText.setSpan(imageSpan, start, start + s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void backSpace() {
        int start = mEditor.getSelectionStart();
        int end = mEditor.getSelectionEnd();
        if (start == 0) {
            return;
        }
        if ((start == end) && start > 0) {
            start = start - 1;
        }
        mEditor.getText().delete(start, end);
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        if (count > 0) {
            int end = start + count;
            Editable message = mEditor.getEditableText();
            ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);
            for (ImageSpan span : list) {
                int spanStart = message.getSpanStart(span);
                int spanEnd = message.getSpanEnd(span);
                if ((spanStart < end) && (spanEnd > start)) {
                    // Add to remove list
                    mEmoticonsToRemove.add(span);
                }
            }

            ColorTextSpan[] list2 = message.getSpans(start, end, ColorTextSpan.class);
            for (ColorTextSpan span : list2) {
                int spanStart = message.getSpanStart(span);
                int spanEnd = message.getSpanEnd(span);
                if ((spanStart < end) && (spanEnd > start)) {
                    // Add to remove list
                    colorTextSpansToRemove.add(span);
                }
            }


            AttachImage[] list3 = message.getSpans(start, end, AttachImage.class);
            for (AttachImage span : list3) {
                int spanStart = message.getSpanStart(span);
                int spanEnd = message.getSpanEnd(span);
                if ((spanStart < end) && (spanEnd > start)) {
                    // Add to remove list
                    imageSpansToRemove.add(span);
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable text) {
        Editable message = mEditor.getEditableText();
        for (ImageSpan span : mEmoticonsToRemove) {
            int start = message.getSpanStart(span);
            int end = message.getSpanEnd(span);
            message.removeSpan(span);
            if (start != end) {
                message.delete(start, end);
            }
        }

        for (ColorTextSpan span : colorTextSpansToRemove) {
            int start = message.getSpanStart(span);
            int end = message.getSpanEnd(span);
            message.removeSpan(span);
            if (start != end) {
                message.delete(start, end);
            }
        }

        for (AttachImage span : imageSpansToRemove) {
            int start = message.getSpanStart(span);
            int end = message.getSpanEnd(span);
            message.removeSpan(span);
            if (start != end) {
                message.delete(start, end);
            }
        }


        mEmoticonsToRemove.clear();
        colorTextSpansToRemove.clear();
        imageSpansToRemove.clear();

        listener.onTextChange(!TextUtils.isEmpty(mEditor.getText().toString()), mEditor.getText().toString());
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {

    }

    public interface TextChangeListener {
        void onTextChange(boolean enable, String s);
    }

}