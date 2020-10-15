package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ViewThreadActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;


import java.io.InputStream;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;


public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ThreadAdapter.class.getSimpleName();
    public List<ThreadInfo> threadInfoList;
    Context mContext;
    public String fid;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    Map<String,String> threadType;
    public static int THREAD_TYPE_PINNED = 0, THREAD_TYPE_NORMAL = 1;
    ViewHistoryDao viewHistoryDao;

    public ThreadAdapter(Map<String,String> threadType, String fid, bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;

        this.threadType = threadType;
        this.fid = fid;
    }

    public void setThreadInfoList(List<ThreadInfo> threadInfoList, Map<String,String> threadType){
        this.threadType = threadType;
        this.threadInfoList = threadInfoList;
        notifyDataSetChanged();
    }

    public void addThreadInfoList(List<ThreadInfo> threadInfoList, Map<String,String> threadType){
        this.threadType = threadType;
        if(this.threadInfoList == null){
            this.threadInfoList = threadInfoList;
        }
        else {
            this.threadInfoList.addAll(threadInfoList);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ThreadInfo threadInfo = threadInfoList.get(position);
        if(threadInfo.displayOrder <= 0){
            return THREAD_TYPE_NORMAL;
        }
        else {
            return THREAD_TYPE_PINNED;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        mContext = context;
        viewHistoryDao = ViewHistoryDatabase.getInstance(mContext).getDao();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        if(viewType == THREAD_TYPE_PINNED){
            View view = inflater.inflate(R.layout.item_thread_pinned, parent, shouldAttachToParentImmediately);
            return new PinnedViewHolder(view);
        }
        else {
            // normal item
            if(!UserPreferenceUtils.conciseRecyclerView(context)){
                View view = inflater.inflate(R.layout.item_thread, parent, shouldAttachToParentImmediately);
                return new ThreadViewHolder(view);
            }
            else {
                View view = inflater.inflate(R.layout.item_thread_concise, parent, shouldAttachToParentImmediately);
                return new ConciseThreadViewHolder(view);
            }
        }



    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderRaw, int position) {
        ThreadInfo threadInfo = threadInfoList.get(position);
        if(holderRaw instanceof PinnedViewHolder){
            PinnedViewHolder holder = (PinnedViewHolder) holderRaw;
            Spanned sp = Html.fromHtml(threadInfo.subject);
            SpannableString spannableString = new SpannableString(sp);
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);

            // thread type
            if(threadInfo.displayOrder !=0){
                int textResource = R.string.bbs_forum_pinned;
                switch(threadInfo.displayOrder){
                    case 3:
                        textResource = R.string.display_order_3;
                        break;
                    case 2:
                        textResource = R.string.display_order_2;
                        break;
                    case 1:
                        textResource = R.string.display_order_1;
                        break;
                    case -1:
                        textResource = R.string.display_order_n1;
                        break;
                    case -2:
                        textResource = R.string.display_order_n2;
                        break;
                    case -3:
                        textResource = R.string.display_order_n3;
                        break;
                    case -4:
                        textResource = R.string.display_order_n4;
                        break;
                    default:
                        textResource = R.string.bbs_forum_pinned;
                }
                holder.mThreadType.setText(textResource);
                //holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorAccent));
            }
            else {
                if(threadType == null){
                    holder.mThreadType.setVisibility(View.GONE);

                }
                else {
                    // provided by label
                    holder.mThreadType.setVisibility(View.VISIBLE);
                    String type = threadType.get(String.valueOf(threadInfo.typeId));

                    if(type !=null){
                        Spanned threadSpanned = Html.fromHtml(type);
                        SpannableString threadSpannableString = new SpannableString(threadSpanned);
                        holder.mThreadType.setText(threadSpannableString);
                    }
                    else {
                        holder.mThreadType.setText(R.string.bbs_forum_pinned);
                    }

                }


            }
            holder.mCardview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, threadInfo);
                    intent.putExtra("FID",threadInfo.fid);
                    intent.putExtra("TID",threadInfo.tid);
                    intent.putExtra("SUBJECT",threadInfo.subject);
                    VibrateUtils.vibrateForClick(mContext);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                            Pair.create(holder.mTitle, "bbs_thread_subject")
                    );

                    Bundle bundle = options.toBundle();
                    mContext.startActivity(intent,bundle);
                }
            });
        }
        else if(holderRaw instanceof ThreadViewHolder){
            ThreadViewHolder holder = (ThreadViewHolder) holderRaw;
            holder.mContent.setVisibility(View.GONE);
            Spanned sp = Html.fromHtml(threadInfo.subject);
            SpannableString spannableString = new SpannableString(sp);
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);
            holder.mThreadViewNum.setText(numberFormatUtils.getShortNumberText(threadInfo.views));
            holder.mThreadReplyNum.setText(numberFormatUtils.getShortNumberText(threadInfo.replies));

            holder.mPublishDate.setText(timeDisplayUtils.getLocalePastTimeString(mContext,threadInfo.publishAt));

            if(threadInfo.displayOrder !=0){
                int textResource = R.string.bbs_forum_pinned;
                switch(threadInfo.displayOrder){
                    case 3:
                        textResource = R.string.display_order_3;
                        break;
                    case 2:
                        textResource = R.string.display_order_2;
                        break;
                    case 1:
                        textResource = R.string.display_order_1;
                        break;
                    case -1:
                        textResource = R.string.display_order_n1;
                        break;
                    case -2:
                        textResource = R.string.display_order_n2;
                        break;
                    case -3:
                        textResource = R.string.display_order_n3;
                        break;
                    case -4:
                        textResource = R.string.display_order_n4;
                        break;
                    default:
                        textResource = R.string.bbs_forum_pinned;
                }
                holder.mThreadType.setText(textResource);
                //holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorAccent));
            }
            else {
                if(threadType == null){
                    holder.mThreadType.setVisibility(View.GONE);

                }
                else {
                    // provided by label
                    holder.mThreadType.setVisibility(View.VISIBLE);
                    String type = threadType.get(String.valueOf(threadInfo.typeId));

                    if(type !=null){
                        Spanned threadSpanned = Html.fromHtml(type);
                        SpannableString threadSpannableString = new SpannableString(threadSpanned);
                        holder.mThreadType.setText(threadSpannableString);
                    }
                    else {
                        holder.mThreadType.setText(String.format("%s",position+1));
                    }

                }

                //holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.ThreadTypeBackgroundColor));
            }

            holder.mThreadPublisher.setText(threadInfo.author);

            int avatar_num = threadInfo.authorId % 16;
            if(avatar_num < 0){
                avatar_num = -avatar_num;
            }

            int avatarResource = mContext.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",mContext.getPackageName());

            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            String source = URLUtils.getSmallAvatarUrlByUid(threadInfo.authorId);
            RequestOptions options = new RequestOptions()
                    .placeholder(mContext.getDrawable(avatarResource))
                    .error(mContext.getDrawable(avatarResource))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

                    .priority(Priority.HIGH);
            GlideUrl glideUrl = new GlideUrl(source,
                    new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
            );

            if(NetworkUtils.canDownloadImageOrFile(mContext)){
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .into(holder.mAvatarImageview);
            }
            else {
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .onlyRetrieveFromCache(true)
                        .into(holder.mAvatarImageview);
            }
            // set short reply
            if(threadInfo.recommendNum !=0){
                holder.mRecommendationNumber.setVisibility(View.VISIBLE);
                holder.mRecommendationNumber.setText(numberFormatUtils.getShortNumberText(threadInfo.recommendNum));

            }
            else {
                holder.mRecommendationNumber.setVisibility(View.GONE);

            }

            if(threadInfo.readPerm == 0){
                holder.mReadPerm.setVisibility(View.GONE);

            }
            else {

                holder.mReadPerm.setVisibility(View.VISIBLE);
                holder.mReadPerm.setText(String.valueOf(threadInfo.readPerm));
                //holder.mReadPerm.setText(numberFormatUtils.getShortNumberText(threadInfo.readPerm));
                int readPermissionVal = threadInfo.readPerm;
                if(userBriefInfo == null || userBriefInfo.readPerm < readPermissionVal){
                    holder.mReadPerm.setTextColor(mContext.getColor(R.color.colorWarn));
                }
                else {
                    holder.mReadPerm.setTextColor(mContext.getColor(R.color.colorTextDefault));
                }
            }

            if(threadInfo.attachment == 0){
                holder.mAttachmentIcon.setVisibility(View.GONE);
            }
            else {
                holder.mAttachmentIcon.setVisibility(View.VISIBLE);
                if(threadInfo.attachment == 1){
                    holder.mAttachmentIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_thread_attachment_24px));
                }
                else {
                    holder.mAttachmentIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_image_outlined_24px));
                }
            }

            if(threadInfo.price !=0 ){

                holder.mPriceNumber.setText(String.valueOf(threadInfo.price));
                holder.mPriceNumber.setVisibility(View.VISIBLE);
            }
            else {
                // holder.mPriceNumber.setText(String.valueOf(threadInfo.price));
                holder.mPriceNumber.setVisibility(View.GONE);
            }


            if(threadInfo.shortReplyList!=null && threadInfo.shortReplyList.size()>0){
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                holder.mReplyRecyclerview.setFocusable(false);
                holder.mReplyRecyclerview.setNestedScrollingEnabled(false);
                holder.mReplyRecyclerview.setLayoutManager(linearLayoutManager);
                holder.mReplyRecyclerview.setClickable(false);
                ShortPostAdapter adapter = new ShortPostAdapter(mContext);
                adapter.setShortReplyInfoList(threadInfo.shortReplyList);
                holder.mReplyRecyclerview.setAdapter(adapter);
                holder.mReplyRecyclerview.setNestedScrollingEnabled(false);
            }
            else {

            }

            holder.mCardview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, threadInfo);
                    intent.putExtra("FID",threadInfo.fid);
                    intent.putExtra("TID",threadInfo.tid);
                    intent.putExtra("SUBJECT",threadInfo.subject);
                    VibrateUtils.vibrateForClick(mContext);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                            Pair.create(holder.mTitle, "bbs_thread_subject")
                    );

                    Bundle bundle = options.toBundle();
                    mContext.startActivity(intent,bundle);
                }
            });

            holder.mAvatarImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra("UID",threadInfo.authorId);

                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) mContext, holder.mAvatarImageview, "user_info_avatar");

                    Bundle bundle = options.toBundle();

                    mContext.startActivity(intent,bundle);
                }
            });
        }
        else if(holderRaw instanceof ConciseThreadViewHolder){
            ConciseThreadViewHolder holder = (ConciseThreadViewHolder) holderRaw;

            Spanned sp = Html.fromHtml(threadInfo.subject);
            SpannableString spannableString = new SpannableString(sp);
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);

            holder.mThreadReplyNum.setText(numberFormatUtils.getShortNumberText(threadInfo.replies));

            holder.mPublishDate.setText(timeDisplayUtils.getLocalePastTimeString(mContext,threadInfo.publishAt));

            //holder.mPublishDate.setText(df.format(threadInfo.publishAt));
            if(threadInfo.displayOrder !=0){
                int textResource = R.string.bbs_forum_pinned;
                switch(threadInfo.displayOrder){
                    case 3:
                        textResource = R.string.display_order_3;
                        break;
                    case 2:
                        textResource = R.string.display_order_2;
                        break;
                    case 1:
                        textResource = R.string.display_order_1;
                        break;
                    case -1:
                        textResource = R.string.display_order_n1;
                        break;
                    case -2:
                        textResource = R.string.display_order_n2;
                        break;
                    case -3:
                        textResource = R.string.display_order_n3;
                        break;
                    case -4:
                        textResource = R.string.display_order_n4;
                        break;
                    default:
                        textResource = R.string.bbs_forum_pinned;
                }
                holder.mThreadType.setText(textResource);
                holder.mThreadType.setTextColor(mContext.getColor(R.color.colorAccent));
                holder.mThreadType.setVisibility(View.VISIBLE);
            }
            else {
                holder.mThreadType.setVisibility(View.GONE);

            }

            int avatar_num = threadInfo.authorId % 16;
            if(avatar_num < 0){
                avatar_num = -avatar_num;
            }

            int avatarResource = mContext.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",mContext.getPackageName());

            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            String source = URLUtils.getDefaultAvatarUrlByUid(threadInfo.authorId);
            RequestOptions options = new RequestOptions()
                    .placeholder(mContext.getDrawable(avatarResource))
                    .error(mContext.getDrawable(avatarResource));
            GlideUrl glideUrl = new GlideUrl(source,
                    new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
            );

            if(NetworkUtils.canDownloadImageOrFile(mContext)){
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .into(holder.mAvatarImageview);
            }
            else {
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .onlyRetrieveFromCache(true)
                        .into(holder.mAvatarImageview);
            }


            holder.mThreadPublisher.setText(threadInfo.author);
            holder.mCardview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, threadInfo);
                    intent.putExtra("FID",threadInfo.fid);
                    intent.putExtra("TID",threadInfo.tid);
                    intent.putExtra("SUBJECT",threadInfo.subject);
                    VibrateUtils.vibrateForClick(mContext);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                            Pair.create(holder.mTitle, "bbs_thread_subject")
                    );

                    Bundle bundle = options.toBundle();
                    mContext.startActivity(intent,bundle);
                }
            });


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
    
    public class PinnedViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_thread_title)
        TextView mTitle;
        @BindView(R.id.bbs_thread_type)
        TextView mThreadType;
        @BindView(R.id.bbs_thread_cardview)
        CardView mCardview;
        public PinnedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_post_publisher)
        TextView mThreadPublisher;
        @BindView(R.id.bbs_post_publish_date)
        TextView mPublishDate;
        @BindView(R.id.bbs_thread_title)
        TextView mTitle;
        @BindView(R.id.bbs_thread_content)
        TextView mContent;
        @BindView(R.id.bbs_thread_view_textview)
        TextView mThreadViewNum;
        @BindView(R.id.bbs_thread_reply_number)
        TextView mThreadReplyNum;
        @BindView(R.id.bbs_thread_type)
        TextView mThreadType;
        @BindView(R.id.bbs_post_avatar_imageView)
        ShapedImageView mAvatarImageview;
        @BindView(R.id.bbs_thread_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_thread_short_reply_recyclerview)
        RecyclerView mReplyRecyclerview;

        @BindView(R.id.bbs_thread_recommend_number)
        TextView mRecommendationNumber;

        @BindView(R.id.bbs_thread_read_perm_number)
        TextView mReadPerm;
        @BindView(R.id.bbs_thread_attachment_image)
        ImageView mAttachmentIcon;


        @BindView(R.id.bbs_thread_price_number)
        TextView mPriceNumber;
        public ThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public class ConciseThreadViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_post_publisher)
        TextView mThreadPublisher;
        @BindView(R.id.bbs_post_publish_date)
        TextView mPublishDate;
        @BindView(R.id.bbs_thread_title)
        TextView mTitle;
        @BindView(R.id.bbs_thread_reply_number)
        TextView mThreadReplyNum;
        @BindView(R.id.bbs_thread_type)
        TextView mThreadType;
        @BindView(R.id.bbs_thread_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_post_avatar_imageView)
        ShapedImageView mAvatarImageview;



        public ConciseThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
