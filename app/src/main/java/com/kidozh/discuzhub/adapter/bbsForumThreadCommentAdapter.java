package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
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
import java.util.List;
import java.util.Locale;

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
        decodeString =decodeString
                .replace("&amp;","&")
                .replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&quot;","“");
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

    class MyDrawableWrapper extends DrawableWrapper {
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

    class MyImageGetter implements Html.ImageGetter {
        TextView textView;

        MyImageGetter(TextView textView){
            this.textView = textView;
        }

        @Override
        public Drawable getDrawable(String source) {

            Drawable drawable = mContext.getDrawable(R.drawable.vector_drawable_loading_image);
            MyDrawableWrapper myDrawable = new MyDrawableWrapper(drawable);
            myDrawable.setDrawable(drawable);
            client = networkUtils.getPreferredClient(mContext);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
            Glide.get(mContext)
                    .getRegistry()
                    .replace(GlideUrl.class,InputStream.class,factory);
            Log.d(TAG,"Load image from "+source);
            if(networkUtils.canDownloadImageOrFile(mContext)){

                Glide.with(mContext)
                        .load(source)
                        .error(R.drawable.vector_drawable_image_crash)
                        .placeholder(R.drawable.vector_drawable_loading_image)
                        .into(new drawableTarget(myDrawable,textView));
            }
            else {
                Glide.with(mContext)
                        .load(source)
                        .error(R.drawable.vector_drawable_image_wider_placeholder)
                        .placeholder(R.drawable.vector_drawable_loading_image)
                        .onlyRetrieveFromCache(true)
                        .into(new drawableTarget(myDrawable,textView));
            }
            return myDrawable;
        }
    }
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
            Log.d(TAG,"Failed to load the image in the textview..." + textView.getText()+" width "+width+" height "+height);
            myDrawable.setBounds(0,0,width,height);
            errorDrawable.setBounds(0,0,width,height);
            myDrawable.setDrawable(errorDrawable);
            tv.setText(tv.getText());
            tv.invalidate();
        }

        @Override
        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
            Drawable drawable = resource;
            //获取原图大小
            int width=drawable.getIntrinsicWidth() ;
            int height=drawable.getIntrinsicHeight();
            // Rescale to image
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            // int screenWidth = outMetrics.widthPixels - textView.getPaddingLeft() - textView.getPaddingRight();
            int screenWidth =  textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
            Log.d(TAG,"Screen width "+screenWidth+" image width "+width);
            if (screenWidth / width < 3){
                double rescaleFactor = ((double) screenWidth) / width;
                int newHeight = (int) (height * rescaleFactor);
                Log.d(TAG,"rescaleFactor "+rescaleFactor+" image new height "+newHeight);
                myDrawable.setBounds(0,0,screenWidth,newHeight);
                drawable.setBounds(0,0,screenWidth,newHeight);
            }
            else {
                myDrawable.setBounds(0,0,width*2,height*2);
                drawable.setBounds(0,0,width*2,height*2);
            }


            myDrawable.setDrawable(drawable);
            TextView tv = textView;
            tv.setText(tv.getText());
            tv.invalidate();
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

            public ClickableImage(Context context, String url) {
                this.context = context;
                this.url = url;
            }

            @Override
            public void onClick(View widget) {
                // 进行图片点击之后的处理
                Log.d(TAG,"You pressed image "+widget.toString()+" URL "+url);
                client = networkUtils.getPreferredClient(mContext);
                OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
                Drawable drawable = mContext.getDrawable(R.drawable.vector_drawable_loading_image);
                MyDrawableWrapper myDrawable = new MyDrawableWrapper(drawable);
                myDrawable.setDrawable(drawable);
                Glide.get(mContext)
                        .getRegistry()
                        .replace(GlideUrl.class,InputStream.class,factory);

                Glide.with(mContext)
                        .load(url)
                        .placeholder(R.drawable.vector_drawable_image_wider_placeholder)
                        .error(R.drawable.vector_drawable_image_crash)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d(TAG,"Failed to load image");
                                notifyDataSetChanged();

                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                              // call to redraw the picture
                                Log.d(TAG,"Resource is ready");
                                notifyDataSetChanged();

                                return false;
                            }
                        })
                        .into(new drawableTarget(myDrawable,textView));

                Glide.with(mContext)
                        .load(url)
                        .onlyRetrieveFromCache(true)
                        .error(R.drawable.vector_drawable_image_failed)
                        .placeholder(R.drawable.vector_drawable_loading_image)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d(TAG,"The resource is not loaded...");
                                Glide.with(context)
                                        .load(url)
                                        .error(R.drawable.vector_drawable_image_failed)
                                        .placeholder(R.drawable.vector_drawable_loading_image)
                                        .into(new drawableTarget(myDrawable,textView));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d(TAG,"The resource is loaded and ready to open in external activity...");
                                Intent intent = new Intent(mContext, showImageFullscreenActivity.class);
                                intent.putExtra("URL",url);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                mContext.startActivity(intent);
                                return true;
                            }
                        }).into(new drawableTarget(myDrawable,textView));
            }
        }
    }  

    public interface onAdapterReply{
        public void replyToSomeOne(int position);
    }
    

}
