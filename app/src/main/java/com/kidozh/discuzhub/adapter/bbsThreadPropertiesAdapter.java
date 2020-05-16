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

import com.kidozh.discuzhub.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsThreadPropertiesAdapter extends RecyclerView.Adapter<bbsThreadPropertiesAdapter.bbsThreadPropertiesViewHolder> {
    private Context context;

    List<bbsThreadNotificationAdapter.threadNotification> threadNotificationList;

    @NonNull
    @Override
    public bbsThreadPropertiesAdapter.bbsThreadPropertiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_thread_property;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsThreadPropertiesAdapter.bbsThreadPropertiesViewHolder(view);
    }

    public void setThreadNotificationList(List<bbsThreadNotificationAdapter.threadNotification> threadNotificationList){
        this.threadNotificationList = threadNotificationList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull bbsThreadPropertiesAdapter.bbsThreadPropertiesViewHolder holder, int position) {

        bbsThreadNotificationAdapter.threadNotification notification = threadNotificationList.get(position);

        if(notification.highlightColorRes == -1){
            holder.itemThreadTypeAvatar.setImageResource(notification.imageResource);
            holder.itemThreadTypeTextview.setText(notification.typeString);
            holder.itemThreadTypeCardview.setAlpha(1);
        }
        else {
            holder.itemThreadTypeAvatar.setImageResource(notification.imageResource);
            holder.itemThreadTypeTextview.setText(notification.typeString);
            holder.itemThreadTypeTextview.setTextColor(notification.highlightColorRes);
            holder.itemThreadTypeCardview.setBackgroundColor(notification.highlightColorRes);
            holder.itemThreadTypeCardview.getBackground().setAlpha(20);
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

    public class bbsThreadPropertiesViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_bbs_thread_type_cardview)
        CardView itemThreadTypeCardview;
        @BindView(R.id.item_bbs_thread_type_avatar)
        ImageView itemThreadTypeAvatar;
        @BindView(R.id.item_bbs_thread_type_value)
        TextView itemThreadTypeTextview;

        public bbsThreadPropertiesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
