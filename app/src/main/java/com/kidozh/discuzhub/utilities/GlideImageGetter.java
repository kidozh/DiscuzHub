package com.kidozh.discuzhub.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.graphics.drawable.DrawableWrapper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;
import com.kidozh.discuzhub.adapter.PostAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import okhttp3.OkHttpClient;

public class GlideImageGetter implements Html.ImageGetter {
    private final static String TAG = GlideImageGetter.class.getSimpleName();
    @NonNull
    private TextView textView;
    @NonNull
    private Context context;
    private static OkHttpClient client = new OkHttpClient();
    @NonNull
    private static bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;


    public GlideImageGetter(@NonNull TextView textView, forumUserBriefInfo userBriefInfo){
        context = textView.getContext();
        this.textView = textView;
        this.userBriefInfo = userBriefInfo;
    }

    static Map<String, List<GlideDrawableTarget>> urlDrawableMapper = new HashMap<>();


    @Override
    public Drawable getDrawable(String source) {
        Drawable drawable = context.getDrawable(R.drawable.vector_drawable_image_wider_placeholder_stroke);
        GlideDrawableWrapper glideDrawableWrapper = new GlideDrawableWrapper(drawable);
        // myDrawable.setDrawable(drawable);
        client = NetworkUtils.getPreferredClientWithCookieJarByUserWithDefaultHeader(context,userBriefInfo);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        Glide.get(context)
                .getRegistry()
                .replace(GlideUrl.class, InputStream.class,factory);
        GlideDrawableTarget currentDrawable = new GlideDrawableTarget(glideDrawableWrapper,textView);
        // put image source in the key
        if(urlDrawableMapper.containsKey(source)){
            List<GlideDrawableTarget> targetList = urlDrawableMapper.get(source);
            targetList.add(currentDrawable);
            urlDrawableMapper.put(source, targetList);
        }
        else {
            List<GlideDrawableTarget> targetList = new ArrayList<>();
            targetList.add(currentDrawable);
            urlDrawableMapper.put(source, targetList);
        }
        // judge network status
        if(NetworkUtils.canDownloadImageOrFile(context)){
            Log.d(TAG,"load the picture from network "+source);
            GlideUrl glideUrl = new GlideUrl(source);
            Glide.with(context)
                    .load(glideUrl)
                    .error(R.drawable.vector_drawable_image_crash)
                    .placeholder(R.drawable.ic_loading_picture)
                    .into(currentDrawable);
        }
        else {
            Log.d(TAG,"load the picture from cache "+source);
            GlideUrl glideUrl = new GlideUrl(source);
            Glide.with(context)
                    .load(glideUrl)
                    .error(R.drawable.vector_drawable_image_wider_placeholder)
                    .placeholder(R.drawable.ic_loading_picture)
                    .onlyRetrieveFromCache(true)
                    .into(currentDrawable);
        }
        return glideDrawableWrapper;
    }

    @SuppressLint("RestrictedApi")
    private static class GlideDrawableWrapper extends DrawableWrapper {
        private Drawable drawable;

        public GlideDrawableWrapper(Drawable drawable) {
            super(drawable);
        }


        @Override
        public void draw(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }
        public Drawable getDrawable() {
            return drawable;
        }
        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }

    class GlideDrawableTarget extends CustomTarget<Drawable> {
        private final GlideDrawableWrapper myDrawable;
        TextView textView;
        public GlideDrawableTarget(GlideDrawableWrapper myDrawable, TextView textView) {
            this.myDrawable = myDrawable;
            this.textView = textView;
        }


        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);


            int width=errorDrawable.getIntrinsicWidth() ;
            int height=errorDrawable.getIntrinsicHeight();
            Log.d(TAG,"Unable to get the image "+errorDrawable+" W "+width+" H "+height);
            myDrawable.setBounds(0,0,width,height);
            errorDrawable.setBounds(0,0,width,height);
            myDrawable.setDrawable(errorDrawable);
            textView.setText(textView.getText());
            textView.invalidate();

        }

        public Bitmap DrawableToBitmap(Drawable drawable) {

            // 获取 drawable 长宽
            int width = drawable.getIntrinsicWidth();
            int heigh = drawable.getIntrinsicHeight();

            drawable.setBounds(0, 0, width, heigh);

            // 获取drawable的颜色格式
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                    : Bitmap.Config.RGB_565;
            // 创建bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, heigh, config);
            // 创建bitmap画布
            Canvas canvas = new Canvas(bitmap);
            // 将drawable 内容画到画布中
            drawable.draw(canvas);
            return bitmap;
        }

        @Override
        public void onResourceReady(final Drawable resource, Transition<? super Drawable> transition) {

            //获取原图大小
            textView.post(new Runnable() {
                @Override
                public void run() {
                    Drawable drawable = resource;
                    int width=drawable.getIntrinsicWidth() ;
                    int height=drawable.getIntrinsicHeight();
                    final int DRAWABLE_COMPRESS_THRESHOLD = 250000;
                    final int DRAWABLE_SIMLEY_THRESHOLD = 10000;
                    // Rescale to image
                    int screenWidth = textView.getMeasuredWidth();
                    Log.d(TAG,"Screen width "+screenWidth+" image width "+width);


                    if (screenWidth !=0 && width * height > DRAWABLE_SIMLEY_THRESHOLD){
                        double rescaleFactor = ((double) screenWidth) / width;
                        int newHeight = (int) (height * rescaleFactor);
                        Log.d(TAG,"rescaleFactor "+rescaleFactor+" image new height "+newHeight);
                        if(width * height > DRAWABLE_COMPRESS_THRESHOLD){
                            // compress it for swift display
                            Bitmap bitmap = DrawableToBitmap(drawable);
                            // scale it first
                            bitmap = Bitmap.createScaledBitmap(bitmap,screenWidth, newHeight, true);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG,80, out);
                            Bitmap compressedBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                            drawable =  new BitmapDrawable(context.getResources(), compressedBitmap);

                        }

                        myDrawable.setBounds(0,0,screenWidth,newHeight);
                        drawable.setBounds(0,0,screenWidth,newHeight);
                        resource.setBounds(0,0,screenWidth,newHeight);

                    }
                    else if(screenWidth == 0){
                        Log.d(TAG, "Get textview width : 0");
                        myDrawable.setBounds(0,0,width,height);
                        drawable.setBounds(0,0,width,height);
                        resource.setBounds(0,0,width,height);
                    }
                    else {
                        myDrawable.setBounds(0,0,width*2,height*2);
                        drawable.setBounds(0,0,width*2,height*2);
                        resource.setBounds(0,0,width*2,height*2);
                    }

                    myDrawable.setDrawable(drawable);
                    TextView tv = textView;
                    tv.setText(tv.getText());
                    tv.invalidate();
                }
            });


        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }
    }

    public static class HtmlTagHandler implements Html.TagHandler {

        private Context mContext;
        TextView textView;

        public HtmlTagHandler(Context context, TextView textView) {
            mContext = context.getApplicationContext();
            this.textView = textView;
        }

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            // 处理标签<img>
            if (tag.equalsIgnoreCase("img")) {
                // 获取长度
                int len = output.length();
                // 获取图片地址
                ImageSpan[] images = output.getSpans(len - 1, len, ImageSpan.class);
                String imgURL = images[0].getSource();

                // 使图片可点击并监听点击事件
                output.setSpan(new ClickableImage(mContext, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else if (tag.equalsIgnoreCase("del")){
                int startTag = 0, endTag = 0;
                if(opening){
                    startTag = output.length();
                }else{
                    endTag = output.length();
                    output.setSpan(new StrikethroughSpan(),startTag,endTag, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

        }

        private class ClickableImage extends ClickableSpan {

            private String url;
            private Context context;
            private boolean isLoaded = false, isLoading = false;
            //private MyDrawableWrapper myDrawable;

            public ClickableImage(Context context, String url) {
                this.context = context;
                this.url = url;
            }

            @Override
            public void onClick(View widget) {
                // 进行图片点击之后的处理
                if(isLoading){
                    return;
                }
                isLoading = true;
                Log.d(TAG,"You pressed image URL "+url);
                client = NetworkUtils.getPreferredClient(mContext);
                OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);

                Glide.get(mContext)
                        .getRegistry()
                        .replace(GlideUrl.class,InputStream.class,factory);
                // need to judge whether the image is cached or not
                // find from imageGetter!
                Log.d(TAG,"You press the image ");
                if(urlDrawableMapper.containsKey(url) && urlDrawableMapper.get(url)!=null){
                    List<GlideDrawableTarget> drawableTargetList = urlDrawableMapper.get(url);
                    // update all target
                    GlideUrl glideUrl = new GlideUrl(url);
                    Glide.with(mContext)
                            .load(glideUrl)
                            .error(R.drawable.vector_drawable_image_failed)
                            .placeholder(R.drawable.vector_drawable_loading_image)
                            .onlyRetrieveFromCache(true)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.d(TAG,"Can't find the image! ");
                                    isLoading = false;
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    // update all drawable target
                                    for(GlideDrawableTarget drawTarget: drawableTargetList){
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                GlideUrl glideUrl = new GlideUrl(url);
                                                Glide.with(mContext)
                                                        .load(glideUrl)
                                                        .error(R.drawable.vector_drawable_image_failed)
                                                        .placeholder(R.drawable.vector_drawable_loading_image)
                                                        .into(drawTarget);

                                            }
                                        });

                                    }


                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d(TAG,"Find the image! Goes to other activity");
                                    isLoading = false;
                                    Intent intent = new Intent(mContext, showImageFullscreenActivity.class);
                                    intent.putExtra("URL",url);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    mContext.startActivity(intent);
                                    return false;
                                }
                            })
                            .submit();


                }
                else {
                    Log.d(TAG,"Can not find the drawable via URL "+url);
                }


            }
        }
    }


}
