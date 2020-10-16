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
import com.kidozh.discuzhub.entities.ThreadCount;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadPropertiesAdapter extends RecyclerView.Adapter<ThreadPropertiesAdapter.bbsThreadPropertiesViewHolder> {
    private Context context;

    List<ThreadCount> threadNotificationList;
    OnThreadPropertyClicked mListener;

    @NonNull
    @Override
    public ThreadPropertiesAdapter.bbsThreadPropertiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if(context instanceof OnThreadPropertyClicked){
            mListener = (OnThreadPropertyClicked) context;
        }

        int layoutIdForListItem = R.layout.item_bbs_thread_property;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ThreadPropertiesAdapter.bbsThreadPropertiesViewHolder(view);
    }

    public void setThreadNotificationList(List<ThreadCount> threadNotificationList){
        this.threadNotificationList = threadNotificationList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadPropertiesAdapter.bbsThreadPropertiesViewHolder holder, int position) {

        ThreadCount notification = threadNotificationList.get(position);

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
            holder.itemThreadTypeCardview.getBackground().setAlpha(25);
        }
        // bind information
        if(mListener != null){
            if(notification.property == ThreadCount.PROPERTY_BUY){
                holder.itemThreadTypeCardview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.buyThreadPropertyClicked();
                    }
                });
            }
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

    public interface OnThreadPropertyClicked{
        public void buyThreadPropertyClicked();
    }
}
