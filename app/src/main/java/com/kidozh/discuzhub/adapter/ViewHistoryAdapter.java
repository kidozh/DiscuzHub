package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ViewThreadActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.GlideImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewHistoryAdapter extends PagedListAdapter<ViewHistory, ViewHistoryAdapter.ViewHistoryViewHolder> {

    public ViewHistoryAdapter(){
        super(DIFF_CALLBACK);
    }

    public void setInfo(bbsInformation bbsInfo, forumUserBriefInfo forumUserBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = forumUserBriefInfo;
    }

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;

    Context context;

    @NonNull
    @Override
    public ViewHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_history,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHistoryViewHolder holder, int position) {
        ViewHistory history = getItem(position);
        if(history!=null){
            holder.name.setText(history.name);
            if(history.description!=null){
                Spanned sp = Html.fromHtml(history.description,
                        new GlideImageGetter(holder.description,userBriefInfo),
                        new GlideImageGetter.HtmlTagHandler(context,holder.description));
                SpannableString spannableString = new SpannableString(sp);
                holder.description.setText(spannableString, TextView.BufferType.SPANNABLE);

            }

            holder.time.setText(timeDisplayUtils.getLocalePastTimeString(context,history.recordAt));


            if(history.type == ViewHistory.VIEW_TYPE_FORUM){
                Glide.with(context)
                        .load(history.avatarURL)
                        .placeholder(R.drawable.ic_forum_outlined_24px)
                        .error(R.drawable.ic_forum_outlined_24px)
                        .into(holder.icon);
            }
            else if(history.type == ViewHistory.VIEW_TYPE_THREAD) {
                Glide.with(context)
                        .load(history.avatarURL)
                        .placeholder(R.drawable.ic_thread_outlined_24px)
                        .error(R.drawable.ic_thread_outlined_24px)
                        .into(holder.icon);
            }
            else {
                Glide.with(context)
                        .load(history.avatarURL)
                        .placeholder(R.drawable.ic_history_24px)
                        .error(R.drawable.ic_history_24px)
                        .into(holder.icon);
            }
            // click things
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (history.type){
                        case ViewHistory.VIEW_TYPE_FORUM:{
                            ForumInfo forumInfo = new ForumInfo();
                            forumInfo.fid = history.fid;
                            forumInfo.description = history.description;
                            Intent intent = new Intent(context, ForumActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forumInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            intent.putExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,true);
                            // Log.d(TAG,"put base url "+bbsInfo.base_url);

                            context.startActivity(intent);
                            VibrateUtils.vibrateForClick(context);
                            break;
                        }
                        case ViewHistory.VIEW_TYPE_THREAD:{
                            ThreadInfo threadInfo = new ThreadInfo();
                            threadInfo.fid = history.fid;
                            threadInfo.tid = history.tid;
                            threadInfo.subject = history.description;
                            Intent intent = new Intent(context, ViewThreadActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, threadInfo);
                            intent.putExtra("FID",threadInfo.fid);
                            intent.putExtra("TID",threadInfo.tid);
                            intent.putExtra("SUBJECT",threadInfo.subject);
                            intent.putExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,true);

                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                                    (Activity) context,
                                    Pair.create(holder.name, "bbs_thread_subject")
                            );
                            VibrateUtils.vibrateForClick(context);

                            Bundle bundle = options.toBundle();
                            context.startActivity(intent,bundle);
                            break;
                        }
                        case ViewHistory.VIEW_TYPE_USER_PROFILE:{
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            intent.putExtra("UID",history.fid);
                            intent.putExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,true);

                            ActivityOptions options = ActivityOptions
                                    .makeSceneTransitionAnimation((Activity) context, holder.icon, "user_info_avatar");

                            Bundle bundle = options.toBundle();

                            context.startActivity(intent,bundle);
                            VibrateUtils.vibrateForClick(context);
                            break;
                        }
                    }
                }
            });

        }
        else {

        }


    }

    public static class ViewHistoryViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.view_history_icon)
        ImageView icon;
        @BindView(R.id.view_history_name)
        TextView name;
        @BindView(R.id.view_history_time)
        TextView time;
        @BindView(R.id.view_history_description)
        TextView description;
        @BindView(R.id.view_history_cardview)
        CardView cardView;
        public ViewHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }


    }

    private static DiffUtil.ItemCallback<ViewHistory> DIFF_CALLBACK = new DiffUtil.ItemCallback<ViewHistory>() {
        @Override
        public boolean areItemsTheSame(@NonNull ViewHistory oldItem, @NonNull ViewHistory newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ViewHistory oldItem, @NonNull ViewHistory newItem) {
            return oldItem.equals(newItem);
        }
    };
}
