package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ThreadActivity;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;


public class FavoriteThreadAdapter extends PagedListAdapter<FavoriteThread, FavoriteThreadAdapter.FavoriteThreadViewHolder> {
    private static final String TAG = FavoriteThreadAdapter.class.getSimpleName();
    Context context;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public FavoriteThreadAdapter() {
        super(FavoriteThread.DIFF_CALLBACK);
    }

    public void setInformation(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.curUser = userBriefInfo;
    }

    @NonNull
    @Override
    public FavoriteThreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        return new FavoriteThreadViewHolder(layoutInflater.inflate(R.layout.item_favorite_thread,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteThreadViewHolder holder, int position) {
        FavoriteThread favoriteThread = getItem(position);
        if(favoriteThread !=null){
            holder.author.setText(favoriteThread.author);
            holder.title.setText(favoriteThread.title);
            if(TextUtils.isEmpty(favoriteThread.description)){
                holder.description.setVisibility(View.GONE);
            }
            else {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(favoriteThread.description);
            }
            holder.publishAt.setText(timeDisplayUtils.getLocalePastTimeString(context, favoriteThread.date));
            Log.d(TAG,"get publish date "+ favoriteThread.date);
            holder.replyNumber.setText(context.getString(R.string.bbs_thread_reply_number, favoriteThread.replies));
            if(favoriteThread.favid == 0){
                holder.syncStatus.setVisibility(View.GONE);
            }
            else {
                holder.syncStatus.setVisibility(View.VISIBLE);
            }

            String avatarURL = URLUtils.getDefaultAvatarUrlByUid(favoriteThread.uid);
            int avatar_num = favoriteThread.uid;
            avatar_num = avatar_num % 16;
            if(avatar_num < 0){
                avatar_num = -avatar_num;
            }
            int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());
            RequestOptions options = new RequestOptions()
                    .placeholder(ContextCompat.getDrawable(context,avatarResource))
                    .error(ContextCompat.getDrawable(context,avatarResource));
            Glide.with(context)
                    .load(avatarURL)
                    .apply(options)
                    .into(holder.userAvatar);
            if(bbsInfo!=null){
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(context, ThreadActivity.class);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                        intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, favoriteThread.toThread());
                        intent.putExtra("FID", favoriteThread.favid);
                        intent.putExtra("TID", favoriteThread.idKey);
                        intent.putExtra("SUBJECT", favoriteThread.title);
                        VibrateUtils.vibrateForClick(context);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                                Pair.create(holder.title, "bbs_thread_subject")
                        );

                        Bundle bundle = options.toBundle();
                        context.startActivity(intent,bundle);
                    }
                });
            }

        }
        else {

        }
    }

    public static class FavoriteThreadViewHolder extends RecyclerView.ViewHolder{
        
        ShapeableImageView userAvatar;
        TextView author;
        TextView publishAt;
        TextView replyNumber;
        TextView title;
        TextView description;
        CardView cardView;
        TextView syncStatus;

        public FavoriteThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.favorite_thread_user_avatar);
            author = itemView.findViewById(R.id.favorite_thread_author);
            publishAt = itemView.findViewById(R.id.favorite_thread_date);
            replyNumber = itemView.findViewById(R.id.favorite_thread_reply);
            title = itemView.findViewById(R.id.favorite_thread_title);
            description = itemView.findViewById(R.id.favorite_thread_description);
            cardView = itemView.findViewById(R.id.bbs_favorite_thread_cardview);
            syncStatus = itemView.findViewById(R.id.favorite_thread_sync_status);
        }
    }
}
