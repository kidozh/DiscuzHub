package com.kidozh.discuzhub.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.FullImageActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.entities.Post;
import com.kidozh.discuzhub.entities.ViewThreadQueryStatus;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.results.ThreadResult;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.TimeDisplayUtils;

import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import okhttp3.OkHttpClient;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final static String TAG = PostAdapter.class.getSimpleName();
    private List<Post> postList = new ArrayList<>();
    private Map<String, List<ThreadResult.Comment>> postCommentList = new HashMap<>();
    private Context context;
    
    private OkHttpClient client = new OkHttpClient();
    private final Discuz bbsInfo;
    private final User curUser;
    private ViewThreadQueryStatus viewThreadQueryStatus;

    private onFilterChanged mListener;

    private AdapterView.OnItemClickListener listener;
    private onAdapterReply replyListener;
    private OnLinkClicked onLinkClickedListener;
    private OnAdvanceOptionClicked onAdvanceOptionClickedListener;
    private int authorId = 0;
    
    
    
    public PostAdapter(Context context, Discuz bbsInfo, User curUser, ViewThreadQueryStatus viewThreadQueryStatus){
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
        this.context = context;
        client = NetworkUtils.getPreferredClient(context);
        this.viewThreadQueryStatus = viewThreadQueryStatus;
        setHasStableIds(true);
    }

    public void clearList(){
        int oldSize = this.postList.size();
        this.postList.clear();
        notifyItemRangeRemoved(0,oldSize);
    }

    public void addThreadInfoList(List<Post> postList, ViewThreadQueryStatus viewThreadQueryStatus, int authorId){
        int oldSize = this.postList.size();
        Iterator<Post> iterator = postList.iterator();
        while (iterator.hasNext()){
            Post post = iterator.next();
            // remove nullable message
            if(post.message.equals("")){
                iterator.remove();
            }
        }
        this.postList.addAll(postList);
        this.viewThreadQueryStatus = viewThreadQueryStatus;
        this.authorId = authorId;
        notifyItemRangeInserted(oldSize, postList.size());
    }

    public List<Post> getThreadInfoList() {
        return postList;
    }

    @Override
    public long getItemId(int position) {
        Post post = postList.get(position);
        return post.pid;
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_post;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        replyListener = (onAdapterReply) context;
        onLinkClickedListener = (OnLinkClicked) context;
        if(context instanceof OnAdvanceOptionClicked){
            onAdvanceOptionClickedListener = (OnAdvanceOptionClicked) context;
        }

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        Post post = postList.get(position);
        if(post.author.equals("")){
            return;
        }
        holder.mThreadPublisher.setText(post.author);
        if(post.authorId == authorId){
            holder.isAuthorLabel.setVisibility(View.VISIBLE);
        }
        else {
            holder.isAuthorLabel.setVisibility(View.GONE);

        }
        // parse status
        int status = post.status;
        int BACKGROUND_ALPHA = 25;
        final int POST_HIDDEN = 1, POST_WARNED = 2, POST_REVISED = 4, POST_MOBILE = 8;
        if((status & POST_HIDDEN) != 0){
            holder.mPostStatusBlockedView.setVisibility(View.VISIBLE);
            holder.mPostStatusBlockedView.setBackgroundColor(context.getColor(R.color.colorPomegranate));
            holder.mPostStatusBlockedView.getBackground().setAlpha(BACKGROUND_ALPHA);
        }
        else {
            holder.mPostStatusBlockedView.setVisibility(View.GONE);
        }

        if((status & POST_WARNED) != 0){
            holder.mPostStatusWarnedView.setVisibility(View.VISIBLE);
            holder.mPostStatusWarnedView.setBackgroundColor(context.getColor(R.color.colorOrange));
            holder.mPostStatusWarnedView.getBackground().setAlpha(BACKGROUND_ALPHA);
        }
        else {
            holder.mPostStatusWarnedView.setVisibility(View.GONE);
        }

        if((status & POST_REVISED) != 0){
            holder.mPostStatusEditedView.setVisibility(View.VISIBLE);
            holder.mPostStatusEditedView.setBackgroundColor(context.getColor(R.color.colorPrimary));
            holder.mPostStatusEditedView.getBackground().setAlpha(BACKGROUND_ALPHA);
        }
        else {
            holder.mPostStatusEditedView.setVisibility(View.GONE);
        }

        if((status & POST_MOBILE) != 0 && !UserPreferenceUtils.conciseRecyclerView(context)){
            holder.mPostStatusMobileIcon.setVisibility(View.VISIBLE);
        }
        else {
            holder.mPostStatusMobileIcon.setVisibility(View.GONE);
        }

        String decodeString = post.message;
        // extract quote message
        //String quoteRegexInVer4 = "^<div class=.reply_wrap.>.*?</div><br />";
        String quoteRegexInVer4 = "^<div class=\"reply_wrap\">(.+?)</div><br .>";

        // remove it if possible
        Pattern quotePatternInVer4 = Pattern.compile(quoteRegexInVer4,Pattern.DOTALL);
        Matcher quoteMatcherInVer4 = quotePatternInVer4.matcher(decodeString);
        // delete it first

        if(quoteMatcherInVer4.find()){

            //decodeString = quoteMatcherInVer4.replaceAll("");
            String quoteString = quoteMatcherInVer4.group(1);
            holder.mPostQuoteContent.setVisibility(View.VISIBLE);
            // set html
            HtmlTagHandler HtmlTagHandler = new HtmlTagHandler(context,holder.mPostQuoteContent);
            Spanned sp = Html.fromHtml(quoteString,new MyImageGetter(holder.mPostQuoteContent),HtmlTagHandler);
            SpannableString spannableString = new SpannableString(sp);

            holder.mPostQuoteContent.setText(spannableString, TextView.BufferType.SPANNABLE);
            holder.mPostQuoteContent.setFocusable(true);
            holder.mPostQuoteContent.setTextIsSelectable(true);
            holder.mPostQuoteContent.setMovementMethod(new bbsLinkMovementMethod(new bbsLinkMovementMethod.OnLinkClickedListener() {
                @Override
                public boolean onLinkClicked(String url) {
                    if(onLinkClickedListener !=null){
                        if(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")){
                            onLinkClickedListener.onLinkClicked(url);
                            return true;
                        }
                        else {
                            return false;
                        }


                    }
                    else {
                        return false;
                    }
                }
            }));
        }
        else {
            holder.mPostQuoteContent.setVisibility(View.GONE);
        }
        decodeString = quoteMatcherInVer4.replaceAll("");
        // handle jammer contents
        Log.d(TAG,"is removed contents "+UserPreferenceUtils.isJammerContentsRemoved(context));
        if(UserPreferenceUtils.isJammerContentsRemoved(context)){
            decodeString= decodeString.replaceAll("<font class=\"jammer\">.+</font>","");
            decodeString= decodeString.replaceAll("<span style=\"display:none\">.+</span>","");
            Log.d(TAG,"GET removed contents "+decodeString);
        }


        HtmlTagHandler HtmlTagHandler = new HtmlTagHandler(context,holder.mContent);
        Spanned sp = Html.fromHtml(decodeString,new MyImageGetter(holder.mContent),HtmlTagHandler);
        SpannableString spannableString = new SpannableString(sp);

        holder.mContent.setText(spannableString, TextView.BufferType.SPANNABLE);
        holder.mContent.setFocusable(true);
        holder.mContent.setTextIsSelectable(true);
        // handle links
        holder.mContent.setMovementMethod(new bbsLinkMovementMethod(new bbsLinkMovementMethod.OnLinkClickedListener() {
            @Override
            public boolean onLinkClicked(String url) {
                if(onLinkClickedListener !=null){
                    if(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")){
                        onLinkClickedListener.onLinkClicked(url);
                        return true;
                    }
                    else {
                        return false;
                    }


                }
                else {
                    return false;
                }
            }
        }));

        // some discuz may return a null dbdateline fields
        if(post.publishAt !=null){
            holder.mPublishDate.setText(TimeDisplayUtils.getLocalePastTimeString(context, post.publishAt));
        }
        else{
            holder.mPublishDate.setText(post.dateline);
        }


        holder.mThreadType.setText(context.getString(R.string.post_index_number, post.position));


        int avatar_num = post.authorId % 16;
        if(avatar_num < 0){
            avatar_num = -avatar_num;
        }

        int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        String source = URLUtils.getSmallAvatarUrlByUid(post.authorId);
        RequestOptions options = new RequestOptions()
                .placeholder(context.getDrawable(avatarResource))
                .error(context.getDrawable(avatarResource))
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                ;

        GlideUrl glideUrl = new GlideUrl(source,
                new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                );
        if(NetworkUtils.canDownloadImageOrFile(context)){
            Glide.with(context)
                    .load(glideUrl)
                    .apply(options)
                    .into(holder.mAvatarImageview);
        }
        else {
            Glide.with(context)
                    .load(glideUrl)
                    .apply(options)
                    .onlyRetrieveFromCache(true)
                    .into(holder.mAvatarImageview);
        }



        holder.mAvatarImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,curUser);

                intent.putExtra("UID", post.authorId);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation((Activity) context, holder.mAvatarImageview, "user_info_avatar");

                Bundle bundle = options.toBundle();

                context.startActivity(intent,bundle);
            }
        });

        holder.mReplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyListener.replyToSomeOne(position);
            }
        });

        // advance option
        if(bbsInfo.getApiVersion() > 4){
            holder.mPostAdvanceOptionImageView.setVisibility(View.VISIBLE);
            // bind option
            holder.mPostAdvanceOptionImageView.setOnClickListener(v -> {
                showPopupMenu(holder.mPostAdvanceOptionImageView, post);
            });
        }
        else {
            holder.mPostAdvanceOptionImageView.setVisibility(View.GONE);
        }


        if(post.getAllAttachments() != null){
            AttachmentAdapter attachmentAdapter = new AttachmentAdapter();
            attachmentAdapter.setAttachmentInfoList(post.getAllAttachments());
            holder.mRecyclerview.setNestedScrollingEnabled(false);

            holder.mRecyclerview.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
            holder.mRecyclerview.setAdapter(attachmentAdapter);
        }
        else {
            AttachmentAdapter attachmentAdapter = new AttachmentAdapter();
            attachmentAdapter.setAttachmentInfoList(post.getAllAttachments());
            holder.mRecyclerview.setNestedScrollingEnabled(false);
            holder.mRecyclerview.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
            holder.mRecyclerview.setAdapter(attachmentAdapter);
        }
        registerListener();
        if(viewThreadQueryStatus.authorId == -1){
            // no author is filtered
            holder.mFilterByAuthorIdBtn.setText(context.getString(R.string.bbs_post_only_see_him));
            holder.mFilterByAuthorIdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.setAuthorId(post.authorId);
                }
            });
        }
        else {
            holder.mFilterByAuthorIdBtn.setText(context.getString(R.string.bbs_post_see_all));
            holder.mFilterByAuthorIdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set it back
                    mListener.setAuthorId(-1);
                }
            });
        }

        // check with tid
        String pidString = String.valueOf(post.pid);
        if(postCommentList.containsKey(pidString)){
            holder.commentRecyclerview.setVisibility(View.VISIBLE);
            holder.commentRecyclerview.setLayoutManager(new LinearLayoutManager(context));
            CommentAdapter commentAdapter = new CommentAdapter();
            List<ThreadResult.Comment> comments = postCommentList.getOrDefault(pidString,new ArrayList<>());
            if(comments != null){
                commentAdapter.setCommentList(comments);
                Log.d(TAG,"Get comments size "+comments.size());
            }
            holder.mRecyclerview.setNestedScrollingEnabled(false);
            holder.commentRecyclerview.setAdapter(commentAdapter);
        }
        else {
            holder.commentRecyclerview.setVisibility(View.GONE);
        }




    }

    public void mergeCommentMap(Map<String, List<ThreadResult.Comment>> commentList){

        if(commentList != null){
            this.postCommentList.putAll(commentList);

            commentList.forEach(
                    (key,value) -> {
                        Log.d(TAG,"get comment key "+key+" value "+value);
                        int pid = Integer.parseInt(key);
                        // this.postCommentList.put(key,value);
                        // find adapter and change it
                        for(int i=0;i<postList.size();i++){
                            Post post = postList.get(i);
                            if(post.pid == pid){
                                notifyItemChanged(i);
                                break;
                            }
                        }
                    }
            );


        }
    }

    public void showPopupMenu(View view, Post post){
        if(onAdvanceOptionClickedListener ==null){
            return;
        }
        PopupMenu popupMenu = new PopupMenu(context,view);
        popupMenu.inflate(R.menu.post_option);
        // listen to this

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.action_report_post:{
                    onAdvanceOptionClickedListener.reportPost(post);
                    return true;
                }
            }
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }

        popupMenu.show();

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
        if(postList == null){
            return 0;
        }
        else {
            return postList.size();
        }

    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        TextView mThreadPublisher;
        TextView mPublishDate;
        TextView mContent;
        TextView mThreadType;
        ImageView mAvatarImageview;
        RecyclerView mRecyclerview;
        Button mReplyBtn;
        Button mFilterByAuthorIdBtn;
        ImageView mPostStatusMobileIcon;
        View mPostStatusBlockedView;
        View mPostStatusWarnedView;
        View mPostStatusEditedView;
        TextView isAuthorLabel;
        TextView mPostQuoteContent;
        ImageView mPostAdvanceOptionImageView;
        RecyclerView commentRecyclerview;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mThreadPublisher = itemView.findViewById(R.id.bbs_post_publisher);
            mPublishDate = itemView.findViewById(R.id.bbs_post_publish_date);
            mContent = itemView.findViewById(R.id.bbs_thread_content);
            mThreadType = itemView.findViewById(R.id.bbs_thread_type);
            mAvatarImageview = itemView.findViewById(R.id.bbs_post_avatar_imageView);
            mRecyclerview = itemView.findViewById(R.id.bbs_thread_attachment_recyclerview);
            mReplyBtn = itemView.findViewById(R.id.bbs_thread_reply_button);
            mFilterByAuthorIdBtn = itemView.findViewById(R.id.bbs_thread_only_see_him_button);
            mPostStatusMobileIcon = itemView.findViewById(R.id.bbs_post_status_mobile);
            mPostStatusBlockedView = itemView.findViewById(R.id.bbs_post_status_blocked_layout);
            mPostStatusWarnedView = itemView.findViewById(R.id.bbs_post_status_warned_layout);
            mPostStatusEditedView = itemView.findViewById(R.id.bbs_post_status_edited_layout);
            isAuthorLabel = itemView.findViewById(R.id.bbs_post_is_author);
            mPostQuoteContent = itemView.findViewById(R.id.bbs_thread_quote_content);
            mPostAdvanceOptionImageView = itemView.findViewById(R.id.bbs_post_advance_option);
            commentRecyclerview = itemView.findViewById(R.id.comment_recyclerview);
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

        MyImageGetter(TextView textView){
            this.textView = textView;
        }


        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = context.getDrawable(R.drawable.vector_drawable_image_wider_placeholder_stroke);
            myDrawable = new MyDrawableWrapper(drawable);
            // myDrawable.setDrawable(drawable);
            client = NetworkUtils.getPreferredClient(context);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
            Glide.get(context)
                    .getRegistry()
                    .replace(GlideUrl.class,InputStream.class,factory);
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
            if(NetworkUtils.canDownloadImageOrFile(context)){
                GlideUrl glideUrl = new GlideUrl(source,
                        new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                );
                Glide.with(context)
                        .load(glideUrl)
                        .error(R.drawable.vector_drawable_image_crash)
                        .placeholder(R.drawable.ic_loading_picture)
                        .into(currentDrawable);
            }
            else {
                GlideUrl glideUrl = new GlideUrl(source,
                        new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                );
                Glide.with(context)
                        .load(glideUrl)
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
            //errorDrawable = context.getDrawable(R.drawable.vector_drawable_image_crash);
            int width=errorDrawable.getIntrinsicWidth() ;
            int height=errorDrawable.getIntrinsicHeight();
            myDrawable.setBounds(0,0,width,height);
            errorDrawable.setBounds(0,0,width,height);
            myDrawable.setDrawable(errorDrawable);
            tv.setText(tv.getText());
            tv.invalidate();
            //tv.setText(tv.getText());

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


                    if (screenWidth !=0 && width * height > DRAWABLE_SIMLEY_THRESHOLD){
                        double rescaleFactor = ((double) screenWidth) / width;
                        int newHeight = (int) (height * rescaleFactor);
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
                    else{
                        myDrawable.setBounds(0,0,width,height);
                        drawable.setBounds(0,0,width,height);
                        resource.setBounds(0,0,width,height);
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
            myDrawable.setDrawable(placeholder);
        }
    }

    // make image clickable
    public class HtmlTagHandler implements Html.TagHandler {

        private Context context;
        TextView textView;

        public HtmlTagHandler(Context context, TextView textView) {
            context = context.getApplicationContext();
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
                output.setSpan(new ClickableImage(context, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                client = NetworkUtils.getPreferredClient(context);
                OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
                Drawable drawable = context.getDrawable(R.drawable.vector_drawable_loading_image);
                //myDrawable = new MyDrawableWrapper(drawable);
                myDrawable.setDrawable(drawable);
                Glide.get(context)
                        .getRegistry()
                        .replace(GlideUrl.class,InputStream.class,factory);
                // need to judge whether the image is cached or not
                // find from imageGetter!
                if(urlDrawableMapper.containsKey(url) && urlDrawableMapper.get(url)!=null){
                    List<drawableTarget> drawableTargetList = urlDrawableMapper.get(url);
                    // update all target
                    GlideUrl glideUrl = new GlideUrl(url,
                            new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                    );
                    Glide.with(context)
                            .load(glideUrl)
                            .error(R.drawable.vector_drawable_image_failed)
                            .placeholder(R.drawable.vector_drawable_loading_image)
                            .onlyRetrieveFromCache(true)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    isLoading = false;
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    // update all drawable target
                                    for(drawableTarget drawTarget: drawableTargetList){
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                GlideUrl glideUrl = new GlideUrl(url,
                                                        new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                                                );
                                                Glide.with(context)
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
                                    isLoading = false;
                                    Intent intent = new Intent(context, FullImageActivity.class);
                                    intent.putExtra("URL",url);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    context.startActivity(intent);
                                    return false;
                                }
                            })
                            .into(new drawableTarget(myDrawable,textView));


                }
                else {

                    drawableTarget target = new drawableTarget(myDrawable, textView);
                    GlideUrl glideUrl = new GlideUrl(url,
                            new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                    );
                    Glide.with(context)
                            .load(glideUrl)
                            .error(R.drawable.vector_drawable_image_failed)
                            .placeholder(R.drawable.vector_drawable_loading_image)
                            .onlyRetrieveFromCache(true)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    // load from network
                                    isLoading = false;
                                    GlideUrl glideUrl = new GlideUrl(url,
                                            new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
                                    );
                                    Glide.with(context)
                                            .load(glideUrl)
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
                                    isLoading = false;
                                    Intent intent = new Intent(context, FullImageActivity.class);
                                    intent.putExtra("URL",url);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    context.startActivity(intent);
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

    public interface OnLinkClicked{
        public void onLinkClicked(String url);
    }

    public interface OnAdvanceOptionClicked{
        public void reportPost(Post post);
    }
    

}
