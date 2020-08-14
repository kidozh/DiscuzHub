package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.bbsShowForumThreadActivity;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class SubForumAdapter extends RecyclerView.Adapter<SubForumAdapter.SubForumViewHolder> {
    final String TAG = SubForumAdapter.class.getSimpleName();
    Context context;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;


    List<ForumResult.SubForumInfo> subForumInfoList;

    public SubForumAdapter(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
    }

    public void setSubForumInfoList(List<ForumResult.SubForumInfo> subForumInfoList) {
        this.subForumInfoList = subForumInfoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_sub_forum;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new SubForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubForumViewHolder holder, int position) {
        ForumResult.SubForumInfo forum = subForumInfoList.get(position);
        Spanned sp = Html.fromHtml(forum.name,null,null);
        holder.mForumName.setText(sp, TextView.BufferType.SPANNABLE);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        URLUtils.setBBS(bbsInfo);
        holder.mBBSForumImage.setImageResource(R.drawable.ic_sub_forum_24px);
//        Glide.with(context)
//                .load(URLUtils.getForumImageUrl(forum.fid))
//                .apply(RequestOptions
//                        .placeholderOf(R.drawable.ic_forum_24px)
//                        .error(R.drawable.ic_forum_24px))
//                .into(holder.mBBSForumImage);
        Log.d(TAG,"fid image "+URLUtils.getForumImageUrl(forum.fid));

        holder.mCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForumInfo putForum = new ForumInfo();
                putForum.fid = forum.fid;
                putForum.name = forum.name;
                putForum.description = forum.name;
                putForum.posts = forum.posts;
                putForum.todayPosts = forum.todayPosts;
                putForum.threadCount = forum.threads;
                Intent intent = new Intent(context, bbsShowForumThreadActivity.class);
                intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,putForum);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);

                VibrateUtils.vibrateForClick(context);
                context.startActivity(intent);
            }
        });
        if(forum.todayPosts != 0){
            holder.mTodayPosts.setVisibility(View.VISIBLE);
            if(forum.todayPosts >= 100){
                holder.mTodayPosts.setText(R.string.forum_today_posts_over_much);
                holder.mTodayPosts.setBackgroundColor(context.getColor(R.color.colorAlizarin));
            }
            else {
                holder.mTodayPosts.setText(String.valueOf(forum.todayPosts));
                holder.mTodayPosts.setBackgroundColor(context.getColor(R.color.colorPrimary));
            }

        }
        else {
            holder.mTodayPosts.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(subForumInfoList == null){
            return 0;
        }
        else {
            return subForumInfoList.size();
        }

    }

    public static class SubForumViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_forum_imageview)
        ImageView mBBSForumImage;
        @BindView(R.id.bbs_forum_name)
        TextView mForumName;
        @BindView(R.id.bbs_forum_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_forum_today_posts)
        TextView mTodayPosts;
        public SubForumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
