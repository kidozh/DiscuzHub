package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserFriendResult;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import static com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClient;

public class bbsUserFriendAdapter extends RecyclerView.Adapter<bbsUserFriendAdapter.ViewHolder> {
    private final static String TAG = bbsUserFriendAdapter.class.getSimpleName();
    @NonNull
    private List<UserFriendResult.UserFriend> userFriendList = new ArrayList<>();
    Context context;
    Discuz bbsInfo;
    forumUserBriefInfo curUser;

    public bbsUserFriendAdapter(Discuz bbsInfo, forumUserBriefInfo curUser){
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
    }

    @Override
    public long getItemId(int position) {
        return userFriendList.get(position).uid;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_user_friend;
    }

    public List<UserFriendResult.UserFriend> getUserFriendList() {
        return userFriendList;
    }

    public void setUserFriendList(@NonNull List<UserFriendResult.UserFriend> userFriendList){

        int oldSize = this.userFriendList.size();
        this.userFriendList.clear();
        notifyItemRangeRemoved(0,oldSize);

        this.userFriendList.addAll(userFriendList);
        notifyItemRangeInserted(0,userFriendList.size());
    }

    public void clearList(){
        int oldSize = userFriendList.size();
        userFriendList.clear();
        notifyItemRangeRemoved(0,oldSize);
    }

    public void addUserFriendList(@NonNull List<UserFriendResult.UserFriend> userFriendList){
        int oldSize = this.userFriendList.size();
        this.userFriendList.addAll(userFriendList);
        Log.d(TAG,"Old size "+oldSize+" insert count "+userFriendList.size());
        notifyItemRangeInserted(oldSize,userFriendList.size());
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_user_friend;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserFriendResult.UserFriend friend = userFriendList.get(position);
        holder.name.setText(friend.username);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        int avatar_num = position % 16;
        holder.idx.setText(String.valueOf(position+1));
        int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());

        Glide.with(context)
                .load(URLUtils.getDefaultAvatarUrlByUid(String.valueOf(friend.uid)))
                .apply(RequestOptions.placeholderOf(avatarResource)
                        .error(avatarResource))
                .into(holder.avatar);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"click friend cardview");
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,curUser);
                intent.putExtra("UID",friend.uid);
                VibrateUtils.vibrateForClick(context);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(userFriendList == null){
            return 0;
        }
        else {
            return userFriendList.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        
        ImageView avatar;
        TextView name;
        TextView idx;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.user_friend_avatar);
            name = itemView.findViewById(R.id.user_friend_username);
            idx = itemView.findViewById(R.id.user_friend_idx);
            cardView = itemView.findViewById(R.id.user_friend_cardview);
        }
    }
}
