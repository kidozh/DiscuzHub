package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.TimeDisplayUtils;

import java.util.List;



public class PublicMessageAdapter extends RecyclerView.Adapter<PublicMessageAdapter.ViewHolder> {

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

        Spanned sp = Html.fromHtml(curPublicMessage.message);
        SpannableString spannableString = new SpannableString(sp);

        holder.publicMessageContent.setText(spannableString, TextView.BufferType.SPANNABLE);
        holder.publicMessageUsername.setText(R.string.bbs_notification_public_pm);
        holder.publicMessageRecvTime.setText(TimeDisplayUtils.getLocalePastTimeString(context,curPublicMessage.publishAt));
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

    public static class ViewHolder extends RecyclerView.ViewHolder{
        
        CardView publicMessageCardview;
        ImageView publicMessageAvatar;
        TextView publicMessageContent;
        TextView publicMessageRecvTime;
        TextView publicMessageUsername;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            publicMessageCardview = itemView.findViewById(R.id.item_public_message_cardview);
            publicMessageAvatar = itemView.findViewById(R.id.item_public_message_avatar);
            publicMessageContent = itemView.findViewById(R.id.item_public_message_content);
            publicMessageRecvTime = itemView.findViewById(R.id.item_public_message_recv_time);
            publicMessageUsername = itemView.findViewById(R.id.item_public_message_username);
        }
    }
}
