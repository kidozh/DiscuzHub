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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.bbsShowThreadActivity;
import com.kidozh.discuzhub.activities.showPersonalInfoActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.threadInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;


import java.io.InputStream;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;


public class bbsForumThreadAdapter extends RecyclerView.Adapter<bbsForumThreadAdapter.bbsForumThreadViewHolder> {
    private static final String TAG = bbsForumThreadAdapter.class.getSimpleName();
    public List<threadInfo> threadInfoList;
    Context mContext;
    public String jsonString,fid;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public bbsForumThreadAdapter(Context context, String jsonString, String fid, bbsInformation bbsInfo, forumUserBriefInfo curUser){
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
        this.mContext = context;
        this.jsonString = jsonString;
        this.fid = fid;
    }

    public void setThreadInfoList(List<threadInfo> threadInfoList, String jsonString){
        this.jsonString = jsonString;
        this.threadInfoList = threadInfoList;
        notifyDataSetChanged();
    }

    public void addThreadInfoList(List<threadInfo> threadInfoList, String jsonString){
        this.jsonString = jsonString;
        if(this.threadInfoList == null){
            this.threadInfoList = threadInfoList;
        }
        else {
            this.threadInfoList.addAll(threadInfoList);
        }

        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public bbsForumThreadAdapter.bbsForumThreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_forum_thread;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsForumThreadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bbsForumThreadAdapter.bbsForumThreadViewHolder holder, int position) {
        threadInfo threadInfo = threadInfoList.get(position);
        holder.mThreadPublisher.setText(threadInfo.author);
        holder.mContent.setVisibility(View.GONE);
        Spanned sp = Html.fromHtml(threadInfo.subject);
        SpannableString spannableString = new SpannableString(sp);
        holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);
        holder.mThreadViewNum.setText(numberFormatUtils.getShortNumberText(threadInfo.viewNum));
        holder.mThreadReplyNum.setText(numberFormatUtils.getShortNumberText(threadInfo.repliesNum));
//        if(threadInfo.lastUpdator != null){
//            holder.mPublishDate.setText(threadInfo.lastUpdator);
//        }

        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
//        if(threadInfo.lastUpdateTimeString!=null){
//            // String lastUpdateTimeString = TextUtils.htmlEncode(threadInfo.lastUpdateTimeString);
//            SpannableString spannableString = new SpannableString(Html.fromHtml(threadInfo.lastUpdateTimeString));
//            holder.mPublishDate.setText(spannableString, TextView.BufferType.SPANNABLE);
//        }
//        else {
//            holder.mPublishDate.setText(timeDisplayUtils.getLocalePastTimeString(mContext,threadInfo.publishAt));
//        }

        holder.mPublishDate.setText(timeDisplayUtils.getLocalePastTimeString(mContext,threadInfo.publishAt));

        //holder.mPublishDate.setText(df.format(threadInfo.publishAt));
        if(threadInfo.isTop){
            int textResource = R.string.bbs_forum_pinned;
            switch(threadInfo.displayOrder){
                case "3":
                    textResource = R.string.display_order_3;
                    break;
                case "2":
                    textResource = R.string.display_order_2;
                    break;
                case "1":
                    textResource = R.string.display_order_1;
                    break;
                case "-1":
                    textResource = R.string.display_order_n1;
                    break;
                case "-2":
                    textResource = R.string.display_order_n2;
                    break;
                case "-3":
                    textResource = R.string.display_order_n3;
                    break;
                case "-4":
                    textResource = R.string.display_order_n4;
                    break;
                    default:
                        textResource = R.string.bbs_forum_pinned;

            }
            holder.mThreadType.setText(textResource);
            holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorAccent));
        }
        else {

            Map<String,String> threadType = bbsParseUtils.parseThreadType(jsonString);
            if(threadType == null){
                Log.d(TAG,"Cannot parse thread type "+jsonString);
                holder.mThreadType.setText(String.format("%s",position+1));
            }
            else {
                holder.mThreadType.setText(threadType.get(threadInfo.typeid));
            }

            holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorPrimary));
        }
        int avatar_num = position % 16;

        int avatarResource = mContext.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",mContext.getPackageName());
        //holder.mAvatarImageview.setImageDrawable(mContext.getDrawable(avatarResource));

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(mContext));
        Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        String source = bbsURLUtils.getSmallAvatarUrlByUid(threadInfo.authorId);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(mContext.getDrawable(avatarResource))
                .error(mContext.getDrawable(avatarResource))
                .diskCacheStrategy(DiskCacheStrategy.ALL)

                .priority(Priority.HIGH);

        Glide.with(mContext)
                .load(source)
                .apply(options)
                .into(holder.mAvatarImageview);
        // set short reply
        if(threadInfo.recommendNum !=0){
            holder.mRecommendationNumber.setVisibility(View.VISIBLE);
            holder.mRecommendationNumber.setText(numberFormatUtils.getShortNumberText(threadInfo.recommendNum));
            holder.mRecommendationIcon.setVisibility(View.VISIBLE);
        }
        else {
            holder.mRecommendationNumber.setVisibility(View.GONE);
            holder.mRecommendationIcon.setVisibility(View.GONE);
        }

        if(threadInfo.readperm.equals("0")){
            holder.mReadPerm.setVisibility(View.GONE);
            holder.mReadPermIcon.setVisibility(View.GONE);
        }
        else {
            holder.mReadPermIcon.setVisibility(View.VISIBLE);
            holder.mReadPerm.setVisibility(View.VISIBLE);
            holder.mReadPerm.setText(numberFormatUtils.getShortNumberText(threadInfo.readperm));
            int readPermissionVal = Integer.parseInt(threadInfo.readperm);
            if(curUser == null || curUser.readPerm < readPermissionVal){
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
                holder.mAttachmentIcon.setImageDrawable(mContext.getDrawable(R.drawable.vector_drawable_attachment));
            }
            else {
                holder.mAttachmentIcon.setImageDrawable(mContext.getDrawable(R.drawable.vector_drawable_image_24px));
            }
        }


        holder.mAttachmentNumber.setText(numberFormatUtils.getShortNumberText(threadInfo.attachment));
        if(threadInfo.shortReplyInfoList!=null && threadInfo.shortReplyInfoList.size()>0){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            holder.mReplyRecyclerview.setFocusable(false);
            holder.mReplyRecyclerview.setNestedScrollingEnabled(false);
            holder.mReplyRecyclerview.setLayoutManager(linearLayoutManager);
            holder.mReplyRecyclerview.setClickable(false);
            bbsForumThreadShortReplyAdapter adapter = new bbsForumThreadShortReplyAdapter(mContext);
            adapter.setShortReplyInfoList(threadInfo.shortReplyInfoList);
            holder.mReplyRecyclerview.setAdapter(adapter);
        }

        holder.mCardview.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, bbsShowThreadActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);

                intent.putExtra("TID",threadInfo.tid);
                intent.putExtra("SUBJECT",threadInfo.subject);
                if(threadInfo.fid!=null){
                    intent.putExtra("FID",threadInfo.fid);
                }
                else {
                    intent.putExtra("FID",fid);
                }

                mContext.startActivity(intent);
            }
        });

        holder.mAvatarImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, showPersonalInfoActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);

                intent.putExtra("UID",threadInfo.authorId);

//                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
//                        mContext, Pair.create(holder.mAvatarImageview, "user_info_avatar")
//                );

                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation((Activity) mContext, holder.mAvatarImageview, "user_info_avatar");

                Bundle bundle = options.toBundle();

                mContext.startActivity(intent,bundle);
            }
        });

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

    public class bbsForumThreadViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_thread_publisher)
        TextView mThreadPublisher;
        @BindView(R.id.bbs_thread_publish_date)
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
        @BindView(R.id.bbs_thread_avatar_imageView)
        ShapedImageView mAvatarImageview;
        @BindView(R.id.bbs_thread_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_thread_short_reply_recyclerview)
        RecyclerView mReplyRecyclerview;
        @BindView(R.id.bbs_thread_recommend_image)
        ImageView mRecommendationIcon;
        @BindView(R.id.bbs_thread_recommend_number)
        TextView mRecommendationNumber;
        @BindView(R.id.bbs_thread_read_perm_image)
        ImageView mReadPermIcon;
        @BindView(R.id.bbs_thread_read_perm_number)
        TextView mReadPerm;
        @BindView(R.id.bbs_thread_attachment_image)
        ImageView mAttachmentIcon;
        @BindView(R.id.bbs_thread_attachment_number)
        TextView mAttachmentNumber;
        public bbsForumThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
