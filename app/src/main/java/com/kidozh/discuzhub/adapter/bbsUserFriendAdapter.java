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
import com.kidozh.discuzhub.activities.showPersonalInfoActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class bbsUserFriendAdapter extends RecyclerView.Adapter<bbsUserFriendAdapter.ViewHolder> {
    private final static String TAG = bbsUserFriendAdapter.class.getSimpleName();
    private List<bbsParseUtils.userFriend> userFriendList;
    Context context;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public bbsUserFriendAdapter(List<bbsParseUtils.userFriend> userFriendList, bbsInformation bbsInfo, forumUserBriefInfo curUser){
        this.userFriendList = userFriendList;
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
    }

    public List<bbsParseUtils.userFriend> getUserFriendList() {
        return userFriendList;
    }

    public void setUserFriendList(List<bbsParseUtils.userFriend> userFriendList){
        this.userFriendList = userFriendList;
        notifyDataSetChanged();
    }

    public void addUserFriendList(List<bbsParseUtils.userFriend> userFriendList){
        if(this.userFriendList == null){
            this.userFriendList = userFriendList;
        }
        else {
            this.userFriendList.addAll(userFriendList);
        }

        notifyDataSetChanged();
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
        bbsParseUtils.userFriend friend = userFriendList.get(position);
        holder.name.setText(friend.username);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        int avatar_num = position % 16;

        int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());

        Glide.with(context)
                .load(bbsURLUtils.getDefaultAvatarUrlByUid(String.valueOf(friend.uid)))
                .apply(RequestOptions.placeholderOf(avatarResource)
                        .error(avatarResource))
                .into(holder.avatar);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"click friend cardview");
                Intent intent = new Intent(context, showPersonalInfoActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                intent.putExtra("UID",String.valueOf(friend.uid));
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
        @BindView(R.id.user_friend_avatar)
        ImageView avatar;
        @BindView(R.id.user_friend_username)
        TextView name;
        @BindView(R.id.user_friend_cardview)
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
