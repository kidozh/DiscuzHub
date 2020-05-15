package com.kidozh.discuzhub.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.bbsShowThreadActivity;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;
import com.kidozh.discuzhub.activities.showPersonalInfoActivity;
import com.kidozh.discuzhub.activities.ui.bbsPollFragment.bbsPollFragment;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import org.w3c.dom.Text;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;

public class bbsForumThreadCommentAdapter extends RecyclerView.Adapter<bbsForumThreadCommentAdapter.bbsForumThreadCommentViewHolder> {
    private final static String TAG = bbsForumThreadCommentAdapter.class.getSimpleName();
    private List<threadCommentInfo> threadInfoList;
    private Context mContext,context;
    public String subject;
    private OkHttpClient client = new OkHttpClient();
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;
    bbsURLUtils.ThreadStatus threadStatus;

    private onFilterChanged mListener;

    private AdapterView.OnItemClickListener listener;
    private onAdapterReply replyListener;




    public bbsForumThreadCommentAdapter(Context context, bbsInformation bbsInfo, forumUserBriefInfo curUser, bbsURLUtils.ThreadStatus threadStatus){
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
        this.mContext = context;
        client = networkUtils.getPreferredClient(context);
        this.threadStatus = threadStatus;
    }

    public void setThreadInfoList(List<threadCommentInfo> threadInfoList, bbsURLUtils.ThreadStatus threadStatus){
        this.threadInfoList = threadInfoList;
        this.threadStatus = threadStatus;
        notifyDataSetChanged();
    }

    public List<threadCommentInfo> getThreadInfoList() {
        return threadInfoList;
    }

    @NonNull
    @Override
    public bbsForumThreadCommentAdapter.bbsForumThreadCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_thread_comment_detail;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        replyListener = (onAdapterReply) context;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsForumThreadCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bbsForumThreadCommentAdapter.bbsForumThreadCommentViewHolder holder, int position) {
        threadCommentInfo threadInfo = threadInfoList.get(position);
        holder.mThreadPublisher.setText(threadInfo.author);
        holder.mTitle.setVisibility(View.GONE);
        //holder.mTitle.setText(threadInfo.subject);
        String decodeString = threadInfo.message;
        MyTagHandler myTagHandler = new MyTagHandler(mContext,holder.mContent);

        Spanned sp = Html.fromHtml(decodeString,new MyImageGetter(holder.mContent),myTagHandler);
        SpannableString spannableString = new SpannableString(sp);

        holder.mContent.setText(spannableString, TextView.BufferType.SPANNABLE);

        holder.mContent.setMovementMethod(LinkMovementMethod.getInstance());
        holder.mContent.setCompoundDrawablePadding(8);
        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
        //holder.mPublishDate.setText(df.format(threadInfo.publishAt));
        holder.mPublishDate.setText(timeDisplayUtils.getLocalePastTimeString(mContext,threadInfo.publishAt));
        if(threadInfo.first){
            holder.mThreadType.setText(R.string.bbs_thread_publisher);
            holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorAccent));
        }
        else {

            holder.mThreadType.setText(String.format("%s",position+1));
            holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorPrimaryDark));
        }
        int avatar_num = position % 16;

        int avatarResource = mContext.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",mContext.getPackageName());
        // holder.mAvatarImageview.setImageDrawable(mContext.getDrawable(avatarResource));

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        String source = bbsURLUtils.getSmallAvatarUrlByUid(threadInfo.authorId);
        RequestOptions options = new RequestOptions()

                .placeholder(mContext.getDrawable(avatarResource))
                .error(mContext.getDrawable(avatarResource))
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                ;

        Glide.with(mContext)
                .load(source)
                .apply(options)
                .into(holder.mAvatarImageview);
        holder.mAvatarImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, showPersonalInfoActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);

                intent.putExtra("UID",threadInfo.authorId);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation((Activity) mContext, holder.mAvatarImageview, "user_info_avatar");

                Bundle bundle = options.toBundle();

                mContext.startActivity(intent,bundle);
            }
        });

        holder.mReplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyListener.replyToSomeOne(position);
            }
        });
        holder.mCopyContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = threadInfo.message;
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", content);
                if(cm!=null){
                    cm.setPrimaryClip(mClipData);
                    Toasty.success(context,context.getString(R.string.bbs_post_copy_to_clipboard_successfully,threadInfo.author), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toasty.error(context,context.getString(R.string.bbs_post_copy_clipboard_manager_null), Toast.LENGTH_SHORT).show();
                }


            }
        });

        if(threadInfo.attachmentInfoList != null){
            bbsAttachmentAdapter attachmentAdapter = new bbsAttachmentAdapter(mContext);
            attachmentAdapter.attachmentInfoList = threadInfo.attachmentInfoList;
            //holder.mRecyclerview.setHasFixedSize(true);
            holder.mRecyclerview.setNestedScrollingEnabled(false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            holder.mRecyclerview.setLayoutManager(linearLayoutManager);
            holder.mRecyclerview.setAdapter(attachmentAdapter);
        }
        else {
            bbsAttachmentAdapter attachmentAdapter = new bbsAttachmentAdapter(mContext);
            attachmentAdapter.attachmentInfoList = threadInfo.attachmentInfoList;
            //holder.mRecyclerview.setHasFixedSize(true);
            holder.mRecyclerview.setNestedScrollingEnabled(false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            holder.mRecyclerview.setLayoutManager(linearLayoutManager);
            holder.mRecyclerview.setAdapter(attachmentAdapter);
        }
        registerListener();
        if(threadStatus.authorId == -1){
            // no author is filtered
            holder.mFilterByAuthorIdBtn.setText(mContext.getString(R.string.bbs_post_only_see_him));
            holder.mFilterByAuthorIdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.setAuthorId(Integer.parseInt(threadInfo.authorId));
                }
            });
        }
        else {
            holder.mFilterByAuthorIdBtn.setText(mContext.getString(R.string.bbs_post_see_all));
            holder.mFilterByAuthorIdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set it back
                    mListener.setAuthorId(-1);
                }
            });
        }




    }

    private void registerListener(){
        if (context instanceof onFilterChanged) {
            mListener = (onFilterChanged) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onFilterChanged");
        }
    }

    @Override
    public int getItemCount() {
        if(threadInfoList == null){
            return 0;
        }
        else {
            return threadInfoList.size();
        }

    }

    public class bbsForumThreadCommentViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_thread_publisher)
        TextView mThreadPublisher;
        @BindView(R.id.bbs_thread_publish_date)
        TextView mPublishDate;
        @BindView(R.id.bbs_thread_title)
        TextView mTitle;
        @BindView(R.id.bbs_thread_content)
        TextView mContent;
        @BindView(R.id.bbs_thread_type)
        TextView mThreadType;
        @BindView(R.id.bbs_thread_avatar_imageView)
        ImageView mAvatarImageview;
        @BindView(R.id.bbs_thread_attachment_recyclerview)
        RecyclerView mRecyclerview;
        @BindView(R.id.bbs_thread_reply_button)
        Button mReplyBtn;
        @BindView(R.id.bbs_thread_copy_content_button)
        Button mCopyContentBtn;
        @BindView(R.id.bbs_thread_only_see_him_button)
        Button mFilterByAuthorIdBtn;
        public bbsForumThreadCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public interface onFilterChanged{
        void setAuthorId(int authorId);
    }

    @SuppressLint("RestrictedApi")
    static class MyDrawableWrapper extends DrawableWrapper {
        private Drawable drawable;

        public MyDrawableWrapper(Drawable drawable) {
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

    private MyDrawableWrapper myDrawable;

    class MyImageGetter implements Html.ImageGetter {
        TextView textView;
        Boolean loadPictureDirectly = false;

        MyImageGetter(TextView textView){
            this.textView = textView;
        }


        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = context.getDrawable(R.drawable.vector_drawable_image_wider_placeholder_stroke);
            myDrawable = new MyDrawableWrapper(drawable);
            // myDrawable.setDrawable(drawable);
            client = networkUtils.getPreferredClient(mContext);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
            Glide.get(mContext)
                    .getRegistry()
                    .replace(GlideUrl.class,InputStream.class,factory);
            Log.d(TAG,"Load image from "+source);
            drawableTarget currentDrawable = new drawableTarget(myDrawable,textView);
            if(urlDrawableMapper.containsKey(source)){
                List<drawableTarget> targetList = urlDrawableMapper.get(source);
                targetList.add(currentDrawable);
                urlDrawableMapper.put(source, targetList);
            }
            else {
                List<drawableTarget> targetList = new ArrayList<>();
                targetList.add(currentDrawable);
                urlDrawableMapper.put(source, targetList);
            }
            if(networkUtils.canDownloadImageOrFile(mContext)){
                Log.d(TAG,"load the picture from network "+source);
                Glide.with(mContext)
                        .load(source)
                        .error(R.drawable.vector_drawable_image_crash)
                        .placeholder(R.drawable.ic_loading_picture)
                        .into(currentDrawable);
            }
            else {
                Log.d(TAG,"load the picture from cache "+source);
                Glide.with(mContext)
                        .load(source)
                        .error(R.drawable.vector_drawable_image_wider_placeholder)
                        .placeholder(R.drawable.ic_loading_picture)
                        .onlyRetrieveFromCache(true)
                        .into(currentDrawable);
            }
            return myDrawable;
        }
    }

    Map<String, List<drawableTarget>> urlDrawableMapper = new HashMap<>();


    class drawableTarget extends CustomTarget<Drawable> {
        private final MyDrawableWrapper myDrawable;
        TextView textView;
        public drawableTarget(MyDrawableWrapper myDrawable,TextView textView) {
            this.myDrawable = myDrawable;
            this.textView = textView;
        }


        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);

            TextView tv = textView;
            //errorDrawable = mContext.getDrawable(R.drawable.vector_drawable_image_crash);
            int width=errorDrawable.getIntrinsicWidth() ;
            int height=errorDrawable.getIntrinsicHeight();
            Log.d(TAG,"Failed to load the image in the textview..."+" width "+width+" height "+height);
            myDrawable.setBounds(0,0,width,height);
            errorDrawable.setBounds(0,0,width,height);
            myDrawable.setDrawable(errorDrawable);
            tv.setText(tv.getText());
            tv.invalidate();
            //tv.setText(tv.getText());

        }

        @Override
        public void onResourceReady(final Drawable resource, Transition<? super Drawable> transition) {
            final Drawable drawable = resource;
            //获取原图大小
            textView.post(new Runnable() {
                @Override
                public void run() {
                    int width=drawable.getIntrinsicWidth() ;
                    int height=drawable.getIntrinsicHeight();
                    // Rescale to image
                    // int screenWidth = outMetrics.widthPixels - textView.getPaddingLeft() - textView.getPaddingRight();
                    //int screenWidth =  textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
                    int screenWidth = textView.getMeasuredWidth();
                    Log.d(TAG,"Screen width "+screenWidth+" image width "+width);
                    if (screenWidth / width < 3 && screenWidth !=0 ){
                        double rescaleFactor = ((double) screenWidth) / width;
                        int newHeight = (int) (height * rescaleFactor);
                        Log.d(TAG,"rescaleFactor "+rescaleFactor+" image new height "+newHeight);
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

                    //myDrawable.invalidateSelf();
                    myDrawable.setDrawable(drawable);
                    TextView tv = textView;
                    tv.setText(tv.getText());
                    tv.invalidate();
                    //tv.setText(tv.getText(), TextView.BufferType.SPANNABLE);
                    //tv.invalidate();
                }
            });


        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }
    }

    // make image clickable
    public class MyTagHandler implements Html.TagHandler {

        private Context mContext;
        TextView textView;

        public MyTagHandler(Context context, TextView textView) {
            mContext = context.getApplicationContext();
            this.textView = textView;
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
                output.setSpan(new ClickableImage(mContext, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                client = networkUtils.getPreferredClient(mContext);
                OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
                Drawable drawable = mContext.getDrawable(R.drawable.vector_drawable_loading_image);
                //myDrawable = new MyDrawableWrapper(drawable);
                myDrawable.setDrawable(drawable);
                Glide.get(mContext)
                        .getRegistry()
                        .replace(GlideUrl.class,InputStream.class,factory);
                // need to judge whether the image is cached or not
                // find from imageGetter!
                Log.d(TAG,"You press the image ");
                if(urlDrawableMapper.containsKey(url) && urlDrawableMapper.get(url)!=null){
                    List<drawableTarget> drawableTargetList = urlDrawableMapper.get(url);
                    // update all target

                    for(drawableTarget target: drawableTargetList){
                        Glide.with(mContext)
                                .load(url)
                                .error(R.drawable.vector_drawable_image_failed)
                                .placeholder(R.drawable.vector_drawable_loading_image)
                                .onlyRetrieveFromCache(true)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Log.d(TAG,"Can't find the image! ");
                                        isLoading = false;
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Glide.with(mContext)
                                                        .load(url)
                                                        .error(R.drawable.vector_drawable_image_failed)
                                                        .placeholder(R.drawable.vector_drawable_loading_image)
                                                        .into(target);

                                            }
                                        });

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
                                .into(target);
                    }
                }
                else {
                    drawableTarget target = new drawableTarget(myDrawable, textView);
                    Glide.with(mContext)
                            .load(url)
                            .error(R.drawable.vector_drawable_image_failed)
                            .placeholder(R.drawable.vector_drawable_loading_image)
                            .onlyRetrieveFromCache(true)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    // load from network
                                    isLoading = false;
                                    Glide.with(mContext)
                                            .load(url)
                                            .error(R.drawable.vector_drawable_image_failed)
                                            .placeholder(R.drawable.vector_drawable_loading_image)
                                            .listener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                    return false;
                                                }
                                            })
                                            .into(target);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d(TAG,"The resource is loaded and ready to open in external activity...");
                                    isLoading = false;
                                    Intent intent = new Intent(mContext, showImageFullscreenActivity.class);
                                    intent.putExtra("URL",url);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    mContext.startActivity(intent);
                                    //textView.invalidateDrawable(resource);
                                    textView.invalidate();

                                    return false;
                                }
                            }).into(target);
                }


            }
        }
    }  

    public interface onAdapterReply{
        public void replyToSomeOne(int position);
    }
    

}
