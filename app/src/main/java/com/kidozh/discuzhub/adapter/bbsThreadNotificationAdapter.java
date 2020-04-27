package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsThreadNotificationAdapter extends RecyclerView.Adapter<bbsThreadNotificationAdapter.bbsThreadTypeViewHolder> {

    private Context context;

    List<threadNotification> threadNotificationList;

    @NonNull
    @Override
    public bbsThreadTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_thread_type;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsThreadTypeViewHolder(view);
    }

    public void setThreadNotificationList(List<threadNotification> threadNotificationList){
        this.threadNotificationList = threadNotificationList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull bbsThreadTypeViewHolder holder, int position) {

        threadNotification notification = threadNotificationList.get(position);

        if(notification.highlightColorRes == -1){
            holder.itemThreadTypeAvatar.setImageResource(notification.imageResource);
            holder.itemThreadTypeTextview.setText(notification.typeString);
        }
        else {
            holder.itemThreadTypeAvatar.setImageResource(notification.imageResource);
            holder.itemThreadTypeTextview.setText(notification.typeString);
            holder.itemThreadTypeCardview.setBackgroundColor(notification.highlightColorRes);
            holder.itemThreadTypeTextview.setTextColor(context.getColor(R.color.colorPureWhite));
            holder.itemThreadTypeAvatar.setColorFilter(context.getColor(R.color.colorPureWhite));
        }

    }

    @Override
    public int getItemCount() {
        if(threadNotificationList == null){
            return 0;
        }
        else {
            return threadNotificationList.size();
        }
    }

    public class bbsThreadTypeViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_bbs_thread_type_cardview)
        CardView itemThreadTypeCardview;
        @BindView(R.id.item_bbs_thread_type_avatar)
        ImageView itemThreadTypeAvatar;
        @BindView(R.id.item_bbs_thread_type_value)
        TextView itemThreadTypeTextview;

        public bbsThreadTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public static class threadNotification{
        public int imageResource;
        public String typeString;
        public String type = "";
        public int highlightColorRes = -1;


        public threadNotification(int imageResource, String typeString) {
            this.imageResource = imageResource;
            this.typeString = typeString;
        }

        public threadNotification(int imageResource, String typeString, String type) {
            this.imageResource = imageResource;
            this.typeString = typeString;
            this.type = type;
        }

        public threadNotification(int imageResource, String typeString, int colorRes) {
            this.imageResource = imageResource;
            this.typeString = typeString;
            this.highlightColorRes = colorRes;
        }
    }
}
