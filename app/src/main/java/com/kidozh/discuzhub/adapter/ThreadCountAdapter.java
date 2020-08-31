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

public class ThreadCountAdapter extends RecyclerView.Adapter<ThreadCountAdapter.ThreadCountHolder> {

    private Context context;

    List<ThreadCount> ThreadCountList;

    @NonNull
    @Override
    public ThreadCountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_thread_type;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ThreadCountHolder(view);
    }

    public void setThreadCountList(List<ThreadCount> ThreadCountList){
        this.ThreadCountList = ThreadCountList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadCountHolder holder, int position) {

        ThreadCount notification = ThreadCountList.get(position);

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
        if(ThreadCountList == null){
            return 0;
        }
        else {
            return ThreadCountList.size();
        }
    }

    public class ThreadCountHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_bbs_thread_type_cardview)
        CardView itemThreadTypeCardview;
        @BindView(R.id.item_bbs_thread_type_avatar)
        ImageView itemThreadTypeAvatar;
        @BindView(R.id.item_bbs_thread_type_value)
        TextView itemThreadTypeTextview;

        public ThreadCountHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }


}
