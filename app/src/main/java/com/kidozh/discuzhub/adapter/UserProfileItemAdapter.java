package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.UserProfileItem;

import java.util.List;


public class UserProfileItemAdapter extends RecyclerView.Adapter<UserProfileItemAdapter.UserProfileItemViewHolder> {
    private List<UserProfileItem> userProfileItemList;
    private Context context;

    public void setUserProfileItemList(List<UserProfileItem> userProfileItemList) {
        this.userProfileItemList = userProfileItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserProfileItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_user_profile_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new UserProfileItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserProfileItemViewHolder holder, int position) {
        UserProfileItem userProfileItem = userProfileItemList.get(position);
        holder.userProfileIcon.setImageDrawable(context.getDrawable(userProfileItem.resourceId));
        holder.userProfileTitle.setText(userProfileItem.name);
        holder.userProfileContent.setText(userProfileItem.value);
    }

    @Override
    public int getItemCount() {
        if(userProfileItemList == null){
            return 0;
        }
        else {
            return userProfileItemList.size();
        }

    }

    public static class UserProfileItemViewHolder extends RecyclerView.ViewHolder{
        
        ImageView userProfileIcon;
        TextView userProfileTitle;
        TextView userProfileContent;

        public UserProfileItemViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileIcon = itemView.findViewById(R.id.user_profile_icon);
            userProfileTitle = itemView.findViewById(R.id.user_profile_title);
            userProfileContent = itemView.findViewById(R.id.user_profile_content);
        }
    }
}
