package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;

import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;

public class MyTagHandler implements Html.TagHandler {
    private static String TAG = MyTagHandler.class.getSimpleName();
    private Context mContext;
    TextView textView;
    View rootView;

    public MyTagHandler(Context context, TextView textView,View rootView) {
        mContext = context;
        this.textView = textView;
        this.rootView = rootView;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

        // 处理标签<img>
        if (tag.toLowerCase(Locale.getDefault()).equals("img")) {
            // 获取长度
            int len = output.length();
            // 获取图片地址
            ImageSpan[] images = output.getSpans(len - 1, len, ImageSpan.class);
            String imgURL = images[0].getSource();

            // 使图片可点击并监听点击事件
            Log.d(TAG,"set Onclick span "+imgURL+" length "+len);

            output.setSpan(new MyTagHandler.ClickableImage(mContext, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private class ClickableImage extends ClickableSpan {

        private String url;
        private Context context;

        public ClickableImage(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        @Override
        public void onClick(View widget) {
            // 进行图片点击之后的处理 usually textview
            Log.d(TAG,"You pressed image "+widget.toString()+" URL "+url);
            OkHttpClient client = networkUtils.getPreferredClient(mContext);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);

            Glide.get(mContext)
                    .getRegistry()
                    .replace(GlideUrl.class, InputStream.class,factory);
            MyDrawableWrapper myDrawable = new MyDrawableWrapper();

            Glide.with(context)
                    .load(url)
                    .error(R.drawable.vector_drawable_image_failed)
                    .placeholder(R.drawable.vector_drawable_loading_image)
                    .into(new GlideImageTarget(context,myDrawable,textView,rootView));


            Glide.with(mContext)
                    .load(url)
                    .onlyRetrieveFromCache(true)
                    .error(R.drawable.vector_drawable_image_failed)
                    .placeholder(R.drawable.vector_drawable_loading_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d(TAG,"The resource is not loaded...");
                            Handler mainHandler = new Handler(context.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(context)
                                            .load(url)
                                            .error(R.drawable.vector_drawable_image_failed)
                                            .placeholder(R.drawable.vector_drawable_loading_image)
                                            .into(new GlideImageTarget(context,myDrawable,textView,rootView));
                                }
                            };
                            mainHandler.post(myRunnable);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG,"The resource is loaded and ready to open in external activity...");
                            Intent intent = new Intent(mContext, showImageFullscreenActivity.class);
                            intent.putExtra("URL",url);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            mContext.startActivity(intent);
                            return false;
                        }
                    }).into(new GlideImageTarget(context,myDrawable,textView,rootView));






//            Handler mHandler = new Handler(Looper.getMainLooper());
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });

        }
    }
}
