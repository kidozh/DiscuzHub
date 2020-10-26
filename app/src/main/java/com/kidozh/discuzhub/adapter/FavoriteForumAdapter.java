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
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ForumActivity;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;


public class FavoriteForumAdapter extends PagedListAdapter<FavoriteForum, FavoriteForumAdapter.FavoriteForumViewHolder> {
    private static final String TAG = FavoriteForumAdapter.class.getSimpleName();
    Context context;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public FavoriteForumAdapter() {
        super(FavoriteForum.DIFF_CALLBACK);
    }

    public void setInformation(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.curUser = userBriefInfo;
    }

    @NonNull
    @Override
    public FavoriteForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        return new FavoriteForumViewHolder(layoutInflater.inflate(R.layout.item_favorite_forum,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteForumViewHolder holder, int position) {
        FavoriteForum FavoriteForum = getItem(position);
        if(FavoriteForum !=null){

            holder.title.setText(FavoriteForum.title);
            if(TextUtils.isEmpty(FavoriteForum.description)){
                holder.description.setVisibility(View.GONE);
            }
            else {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(FavoriteForum.description);
            }
            holder.publishAt.setText(timeDisplayUtils.getLocalePastTimeString(context, FavoriteForum.date));
            Log.d(TAG,"get publish date "+ FavoriteForum.date);
            holder.todayPosts.setText(context.getString(R.string.forum_today_post, FavoriteForum.todayposts));
            if(FavoriteForum.favid == 0){
                holder.syncStatus.setVisibility(View.GONE);
            }
            else {
                holder.syncStatus.setVisibility(View.VISIBLE);
            }


            if(bbsInfo!=null){
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(context, ForumActivity.class);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                        intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY, FavoriteForum.toForum());
                        intent.putExtra("FID", FavoriteForum.idKey);
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

    public static class FavoriteForumViewHolder extends RecyclerView.ViewHolder{


        
        TextView publishAt;
        TextView title;
        TextView description;
        CardView cardView;
        TextView syncStatus;
        TextView todayPosts;

        public FavoriteForumViewHolder(@NonNull View itemView) {
            super(itemView);
            publishAt = itemView.findViewById(R.id.favorite_forum_date);
            title = itemView.findViewById(R.id.favorite_forum_title);
            description = itemView.findViewById(R.id.favorite_forum_description);
            cardView = itemView.findViewById(R.id.bbs_favorite_forum_cardview);
            syncStatus = itemView.findViewById(R.id.favorite_forum_sync_status);
            todayPosts = itemView.findViewById(R.id.favorite_forum_today_posts);
        }
    }
}
