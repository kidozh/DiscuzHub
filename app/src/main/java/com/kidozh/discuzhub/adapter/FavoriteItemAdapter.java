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
import com.kidozh.discuzhub.activities.bbsShowPostActivity;
import com.kidozh.discuzhub.entities.FavoriteItem;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteItemAdapter extends PagedListAdapter<FavoriteItem, FavoriteItemAdapter.FavoriteThreadViewHolder> {
    private static final String TAG = FavoriteItemAdapter.class.getSimpleName();
    Context context;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public FavoriteItemAdapter() {
        super(FavoriteItem.DIFF_CALLBACK);
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
        FavoriteItem favoriteItem = getItem(position);
        if(favoriteItem !=null){
            holder.author.setText(favoriteItem.author);
            holder.title.setText(favoriteItem.title);
            if(TextUtils.isEmpty(favoriteItem.description)){
                holder.description.setVisibility(View.GONE);
            }
            else {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(favoriteItem.description);
            }
            holder.publishAt.setText(timeDisplayUtils.getLocalePastTimeString(context, favoriteItem.date));
            Log.d(TAG,"get publish date "+ favoriteItem.date);
            holder.replyNumber.setText(context.getString(R.string.bbs_thread_reply_number, favoriteItem.replies));
            if(favoriteItem.favid == 0){
                holder.syncStatus.setVisibility(View.GONE);
            }
            else {
                holder.syncStatus.setVisibility(View.VISIBLE);
            }

            String avatarURL = URLUtils.getDefaultAvatarUrlByUid(favoriteItem.uid);
            int avatar_num = favoriteItem.uid;
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

                        Intent intent = new Intent(context, bbsShowPostActivity.class);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                        intent.putExtra(bbsConstUtils.PASS_THREAD_KEY, favoriteItem.toThread());
                        intent.putExtra("FID", favoriteItem.favid);
                        intent.putExtra("TID", favoriteItem.idKey);
                        intent.putExtra("SUBJECT", favoriteItem.title);
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
        @BindView(R.id.favorite_thread_user_avatar)
        ShapeableImageView userAvatar;
        @BindView(R.id.favorite_thread_author)
        TextView author;
        @BindView(R.id.favorite_thread_date)
        TextView publishAt;
        @BindView(R.id.favorite_thread_reply)
        TextView replyNumber;
        @BindView(R.id.favorite_thread_title)
        TextView title;
        @BindView(R.id.favorite_thread_description)
        TextView description;
        @BindView(R.id.bbs_favorite_thread_cardview)
        CardView cardView;
        @BindView(R.id.favorite_thread_sync_status)
        TextView syncStatus;

        public FavoriteThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
