package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.kidozh.discuzhub.activities.bbsShowPortalActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class forumUsersAdapter extends RecyclerView.Adapter<forumUsersAdapter.ViewHolder> {

    List<forumUserBriefInfo> userList;
    private bbsInformation bbsInfo;
    Context context;

    public List<forumUserBriefInfo> getUserList() {
        return userList;
    }

    public Context getContext() {
        return context;
    }

    public forumUsersAdapter(Context context, bbsInformation bbsInformation){
        this.bbsInfo = bbsInformation;
        this.context = context;
    }

    public void setUserList(List<forumUserBriefInfo> userList){
        this.userList = userList;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new forumUsersAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_forum_user,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        forumUserBriefInfo userInfo = userList.get(position);
        holder.mUserName.setText(userInfo.username);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        holder.mUserIdx.setText(String.valueOf(position+1));
        Glide.with(context)
                .load(userInfo.avatarUrl)
                .error(R.drawable.avatar_person_male)
                .placeholder(R.drawable.avatar_person_male)
                .centerInside()
                .into(holder.mUserAvatar);
        holder.mUserCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context, bbsShowCategoryForumActivity.class);
                Intent intent = new Intent(context, bbsShowPortalActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, userInfo);
                VibrateUtils.vibrateForClick(context);
                context.startActivity(intent);
            }
        });
//        holder.mUserCardview.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Intent intent = new Intent(context, LoginActivity.class);
//                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
//                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userInfo);
//                context.startActivity(intent);
//                VibrateUtils.vibrateForNotice(context);
//                return true;
//            }
//        });

        //List<Integer> colorStateResourceList = colorUtils.colorStateList;
        //int resourceListLength = colorStateResourceList.size();
        //holder.mUserCardview.setBackgroundColor(context.getColor(colorStateResourceList.get(position % resourceListLength)));

    }

    @Override
    public int getItemCount() {
        if(userList == null){
            return 0;
        }
        else {
            return userList.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.forum_user_avatar)
        ImageView mUserAvatar;
        @BindView(R.id.forum_user_name)
        TextView mUserName;
        @BindView(R.id.forum_user_cardview)
        CardView mUserCardview;
        @BindView(R.id.forum_user_index)
        TextView mUserIdx;

        ViewHolder(View view){
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
