package com.kidozh.discuzhub.adapter;

import android.content.Context;
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
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.InputStream;
import java.util.List;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    List<forumUserBriefInfo> userList;
    private Discuz bbsInfo;
    Context context;

    public List<forumUserBriefInfo> getUserList() {
        return userList;
    }

    public Context getContext() {
        return context;
    }

    public UsersAdapter(Context context, Discuz Discuz){
        this.bbsInfo = Discuz;
        this.context = context;
    }

    public void setUserList(List<forumUserBriefInfo> userList){
        this.userList = userList;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_forum_user,parent,false));
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
        ImageView mUserAvatar;
        TextView mUserName;
        CardView mUserCardview;
        TextView mUserIdx;

        ViewHolder(View view){
            super(view);
            mUserAvatar = view.findViewById(R.id.forum_user_avatar);
            mUserName = view.findViewById(R.id.forum_user_name);
            mUserCardview = view.findViewById(R.id.forum_user_cardview);
            mUserIdx = view.findViewById(R.id.forum_user_index);
        }
    }
}
