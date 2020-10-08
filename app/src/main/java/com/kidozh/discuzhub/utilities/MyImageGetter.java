package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;

import java.io.InputStream;

import okhttp3.OkHttpClient;

public class MyImageGetter implements Html.ImageGetter {
    private TextView textView;
    private Context context;
    private static String TAG = MyImageGetter.class.getSimpleName();
    View rootView;
    Boolean isFileDownloadAllowed = false;

    public MyImageGetter(Context context,TextView textView, View rootView){
        this.textView = textView;
        this.context = context;
        this.rootView = rootView;
    }

    public MyImageGetter(Context context,TextView textView, View rootView, Boolean isFileAllowed){
        this.textView = textView;
        this.context = context;
        this.rootView = rootView;
        this.isFileDownloadAllowed = isFileAllowed;
    }

    @Override
    public Drawable getDrawable(String source) {
        //Log.d(TAG,"Get drawable "+source);
        MyDrawableWrapper myDrawable = new MyDrawableWrapper();
        Drawable drawable = context.getDrawable(R.drawable.vector_drawable_loading_image);
        myDrawable.setDrawable(drawable);
        OkHttpClient client = NetworkUtils.getPreferredClient(context);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        Glide.get(context)
                .getRegistry()
                .replace(GlideUrl.class, InputStream.class,factory);
        //Log.d(TAG,"Load image from "+source);
        if(NetworkUtils.canDownloadImageOrFile(context) || isFileDownloadAllowed){


            Glide.with(context)
                    .load(source)
                    .error(R.drawable.vector_drawable_image_failed)
                    .placeholder(R.drawable.vector_drawable_loading_image)
                    .into(new GlideImageTarget(context,myDrawable,textView,rootView));

        }
        else {
            Glide.with(context)
                    .load(source)
                    .error(R.drawable.vector_drawable_image_download_wider_placeholder)
                    .placeholder(R.drawable.vector_drawable_loading_image)
                    .onlyRetrieveFromCache(true)
                    .into(new GlideImageTarget(context,myDrawable,textView,rootView));

        }
        return myDrawable;
    }
}
