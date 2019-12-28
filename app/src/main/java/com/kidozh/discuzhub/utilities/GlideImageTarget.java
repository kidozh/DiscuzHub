package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

class GlideImageTarget extends CustomTarget<Drawable> {
    private String TAG = GlideImageTarget.class.getSimpleName();
    private final MyDrawableWrapper myDrawable;
    TextView textView;
    Context context;
    View rootView;
    public GlideImageTarget(Context context, MyDrawableWrapper myDrawable, TextView textView, View rootView) {
        this.myDrawable = myDrawable;
        this.textView = textView;
        this.context = context;
        this.rootView = rootView;
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        super.onLoadStarted(placeholder);
        Drawable errorDrawable = placeholder;
        int width=errorDrawable.getIntrinsicWidth() ;
        int height=errorDrawable.getIntrinsicHeight();
        myDrawable.setBounds(0,0,width,height);
        errorDrawable.setBounds(0,0,width,height);

        myDrawable.setDrawable(errorDrawable);

        textView.setText(textView.getText());
        textView.invalidate();

    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
        TextView tv = textView;
        // errorDrawable = context.getDrawable(R.drawable.vector_drawable_image_placeholder);
        int width=errorDrawable.getIntrinsicWidth();
        int height=errorDrawable.getIntrinsicHeight();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        // int screenWidth = outMetrics.widthPixels - textView.getPaddingLeft() - textView.getPaddingRight();
        int screenWidth =  textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
        double rescaleFactor = ((double) screenWidth) / width;
        if(rescaleFactor <=0){
            rescaleFactor = 1;
        }
        int newHeight = (int) (height * rescaleFactor);
        myDrawable.setBounds(0,0,screenWidth,newHeight);
        errorDrawable.setBounds(0,0,screenWidth,newHeight);

        Log.d(TAG,"Failed to load the image in the textview "+width + " "+height+" Scale factor "+rescaleFactor);

        myDrawable.setDrawable(errorDrawable);

        tv.setText(tv.getText());
        tv.invalidate();
        rootView.invalidate();
        rootView.requestLayout();
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        Drawable drawable = resource;
        //获取原图大小
        int width=drawable.getIntrinsicWidth() ;
        int height=drawable.getIntrinsicHeight();
        myDrawable.invalidateSelf();
        //自定义drawable的高宽, 缩放图片大小最好用matrix变化，可以保证图片不失真
        //drawable.setBounds(0, 0, 500, 500);
        //myDrawable.setBounds(0, 0, 500, 500);
        // Rescale to image
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
////        DisplayMetrics outMetrics = new DisplayMetrics();
////        wm.getDefaultDisplay().getMetrics(outMetrics);
        // int screenWidth = outMetrics.widthPixels - textView.getPaddingLeft() - textView.getPaddingRight();
        textView.post(new Runnable() {
            @Override
            public void run() {
                int screenWidth =  textView.getWidth();
                Log.d(TAG,"Screen width "+screenWidth+" image width "+width);
                if (screenWidth / width < 3){
                    double rescaleFactor = ((double) screenWidth) / width;
                    if(rescaleFactor <=0){
                        rescaleFactor = 1;
                    }
                    int newHeight = (int) (height * rescaleFactor);
                    Log.d(TAG,"rescaleFactor "+rescaleFactor+" image new height "+newHeight);
                    myDrawable.setBounds(0,0,screenWidth,newHeight);
                    drawable.setBounds(0,0,screenWidth,newHeight);
                }
                else {
                    myDrawable.setBounds(0,0,width*2,height*2);
                    drawable.setBounds(0,0,width*2,height*2);
                }
                Log.d(TAG, "Get bit map "+width);
                myDrawable.setDrawable(drawable);
            }
        });


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Log.d("UI thread", "I am the UI thread");
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        CharSequence t = textView.getText();
                        Log.d(TAG,"refreshing textview...");
                        textView.refreshDrawableState();
                        textView.invalidateDrawable(myDrawable);
                        textView.setText(t, TextView.BufferType.SPANNABLE);
                        textView.invalidate();
//                        textView.requestLayout();
//                        rootView.invalidate();
//                        rootView.requestLayout();

                    }
                });
            }
        });





    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        int width=placeholder.getIntrinsicWidth() ;
        int height=placeholder.getIntrinsicHeight();
        Log.d(TAG,"placeholder width "+width+" image height "+height);
        myDrawable.setBounds(0,0,width,height);
        myDrawable.setDrawable(placeholder);
        textView.setText(textView.getText());
        textView.invalidate();
        textView.requestLayout();
    }
}
