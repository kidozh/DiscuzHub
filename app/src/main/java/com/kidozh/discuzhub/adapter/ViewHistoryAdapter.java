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
import com.kidozh.discuzhub.activities.ThreadActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.entities.Forum;
import com.kidozh.discuzhub.entities.Thread;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.GlideImageGetter;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;


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
            Spanned sp = Html.fromHtml(history.description,
                    new GlideImageGetter(holder.description,userBriefInfo),
                    new GlideImageGetter.HtmlTagHandler(context,holder.description));
            SpannableString spannableString = new SpannableString(sp);
            holder.description.setText(spannableString, TextView.BufferType.SPANNABLE);

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
                        .placeholder(R.drawable.ic_baseline_history_24)
                        .error(R.drawable.ic_baseline_history_24)
                        .into(holder.icon);
            }
            // click things
            holder.cardView.setOnClickListener(v -> {
                switch (history.type){
                    case ViewHistory.VIEW_TYPE_FORUM:{
                        Forum forum = new Forum();
                        forum.fid = history.fid;
                        forum.description = history.description;
                        Intent intent = new Intent(context, ForumActivity.class);
                        intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, forum);
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                        intent.putExtra(ConstUtils.PASS_IS_VIEW_HISTORY,true);
                        // Log.d(TAG,"put base url "+bbsInfo.base_url);

                        context.startActivity(intent);
                        VibrateUtils.vibrateForClick(context);
                        break;
                    }
                    case ViewHistory.VIEW_TYPE_THREAD:{
                        Thread thread = new Thread();
                        thread.fid = history.fid;
                        thread.tid = history.tid;
                        thread.subject = history.description;
                        Intent intent = new Intent(context, ThreadActivity.class);
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                        intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread);
                        intent.putExtra("FID", thread.fid);
                        intent.putExtra("TID", thread.tid);
                        intent.putExtra("SUBJECT", thread.subject);
                        intent.putExtra(ConstUtils.PASS_IS_VIEW_HISTORY,true);

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
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                        intent.putExtra("UID",history.fid);
                        intent.putExtra(ConstUtils.PASS_IS_VIEW_HISTORY,true);

                        ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation((Activity) context, holder.icon, "user_info_avatar");

                        Bundle bundle = options.toBundle();

                        context.startActivity(intent,bundle);
                        VibrateUtils.vibrateForClick(context);
                        break;
                    }
                }
            });


        }


    }

    public static class ViewHistoryViewHolder extends RecyclerView.ViewHolder{
        
        ImageView icon;
        TextView name;
        TextView time;
        TextView description;
        CardView cardView;

        public ViewHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.view_history_icon);
            name = itemView.findViewById(R.id.view_history_name);
            time = itemView.findViewById(R.id.view_history_time);
            description = itemView.findViewById(R.id.view_history_description);
            cardView = itemView.findViewById(R.id.view_history_cardview);
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
