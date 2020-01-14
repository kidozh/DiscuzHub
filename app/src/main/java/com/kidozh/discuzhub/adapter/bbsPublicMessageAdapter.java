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
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsPublicMessageAdapter extends RecyclerView.Adapter<bbsPublicMessageAdapter.ViewHolder> {

    List<bbsParseUtils.publicMessage> publicMessageList;
    private Context context;




    public void setPublicMessageList(List<bbsParseUtils.publicMessage> publicMessageList) {
        this.publicMessageList = publicMessageList;
        notifyDataSetChanged();
    }

    public void addPublicMessageList(List<bbsParseUtils.publicMessage> publicMessageList) {
        if(this.publicMessageList == null){
            this.publicMessageList = publicMessageList;
        }
        else {
            this.publicMessageList.addAll(publicMessageList);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_public_message,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bbsParseUtils.publicMessage curPublicMessage = publicMessageList.get(position);
        holder.publicMessageContent.setText(curPublicMessage.message);
        holder.publicMessageUsername.setText(R.string.bbs_notification_public_pm);
        holder.publicMessageRecvTime.setText(timeDisplayUtils.getLocalePastTimeString(context,curPublicMessage.publishAt));
        holder.publicMessageAvatar.setImageDrawable(context.getDrawable(R.drawable.vector_drawable_info_24px_outline));
    }

    @Override
    public int getItemCount() {
        if(this.publicMessageList == null){
            return 0;
        }
        else {
            return this.publicMessageList.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_public_message_cardview)
        CardView publicMessageCardview;
        @BindView(R.id.item_public_message_avatar)
        ImageView publicMessageAvatar;
        @BindView(R.id.item_public_message_content)
        TextView publicMessageContent;
        @BindView(R.id.item_public_message_recv_time)
        TextView publicMessageRecvTime;
        @BindView(R.id.item_public_message_username)
        TextView publicMessageUsername;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
