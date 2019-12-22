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
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsPrivateDetailMessageAdapter extends RecyclerView.Adapter<bbsPrivateDetailMessageAdapter.ViewHolder> {

    List<bbsParseUtils.privateDetailMessage> privateDetailMessageList;
    Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_private_message_detail;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if(privateDetailMessageList == null){
            return 0;
        }
        else {
            return privateDetailMessageList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_private_message_detail_time)
        TextView privateMessageDetailTime;
        @BindView(R.id.item_private_message_detail_message)
        TextView privateMessageDetailMessage;
        @BindView(R.id.item_private_message_detail_recv_avatar)
        ImageView privateMessageDetailRecvAvatar;
        @BindView(R.id.item_private_message_detail_sender_avatar)
        ImageView privateMessageDetailSenderAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
