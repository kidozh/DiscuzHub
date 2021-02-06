package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.InputStream;
import java.util.List;


public class PrivateDetailMessageAdapter extends RecyclerView.Adapter<PrivateDetailMessageAdapter.ViewHolder> {

    List<bbsParseUtils.privateDetailMessage> privateDetailMessageList;
    Context context;
    Discuz curBBS;
    User userBriefInfo;

    public PrivateDetailMessageAdapter(Discuz curBBS, User userBriefInfo) {
        this.curBBS = curBBS;
        this.userBriefInfo = userBriefInfo;
    }


    public void setPrivateDetailMessageList(List<bbsParseUtils.privateDetailMessage> privateDetailMessageList){
        this.privateDetailMessageList = privateDetailMessageList;
        notifyDataSetChanged();
    }

    public void addPrivateDetailMessageList(List<bbsParseUtils.privateDetailMessage> privateDetailMessageList){
        if(this.privateDetailMessageList == null){
            this.privateDetailMessageList = privateDetailMessageList;
        }
        else {
            privateDetailMessageList.addAll(this.privateDetailMessageList);
            this.privateDetailMessageList = privateDetailMessageList;
            //this.privateDetailMessageList.addAll(privateDetailMessageList);
        }
        notifyDataSetChanged();
    }

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
        bbsParseUtils.privateDetailMessage curPrivateDetailMessage = privateDetailMessageList.get(position);
        MyTagHandler myTagHandler = new MyTagHandler(context,holder.privateMessageDetailMessage,holder.privateMessageDetailMessage);

        Spanned sp = Html.fromHtml(curPrivateDetailMessage.message,
                new MyImageGetter(context,holder.privateMessageDetailMessage,holder.privateMessageDetailMessage,true),
                null);
//        Spanned sp = Html.fromHtml(curPrivateDetailMessage.message,
//                null,
//                null);
        SpannableString spannableString = new SpannableString(sp);

        holder.privateMessageDetailMessage.setText(spannableString, TextView.BufferType.SPANNABLE);
        holder.privateMessageDetailMessage.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned timeSp = Html.fromHtml(curPrivateDetailMessage.vDateline);

        holder.privateMessageDetailTime.setText(new SpannableString(timeSp));


        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.constraintLayout);
        if(curPrivateDetailMessage.isMyself){
            constraintSet.setHorizontalBias(holder.privateMessageDetailMessage.getId(),1.0f);
        }
        else {

            constraintSet.setHorizontalBias(holder.privateMessageDetailMessage.getId(),0.0f);
        }
        constraintSet.applyTo(holder.constraintLayout);

        holder.privateMessageDetailSenderAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);

                intent.putExtra("UID", String.valueOf(curPrivateDetailMessage.msgFromId));

                context.startActivity(intent);
            }
        });

        holder.privateMessageDetailRecvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);

                intent.putExtra("UID", String.valueOf(curPrivateDetailMessage.msgFromId));

                context.startActivity(intent);
            }
        });


        int avatar_num = position % 16;
        int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());
        if(curPrivateDetailMessage.isMyself){
            Glide.with(context)
                    .load(URLUtils.getSmallAvatarUrlByUid(String.valueOf(curPrivateDetailMessage.msgFromId)))
                    .centerInside()
                    .placeholder(avatarResource)
                    .error(avatarResource)
                    .into(holder.privateMessageDetailRecvAvatar);
            holder.privateMessageDetailMessage.setBackgroundColor(context.getColor(R.color.colorPrimary));
            holder.privateMessageDetailMessage.setTextColor(context.getColor(R.color.colorPureWhite));

            holder.privateMessageDetailSenderAvatar.setVisibility(View.INVISIBLE);
            holder.privateMessageDetailRecvAvatar.setVisibility(View.VISIBLE);
        }
        else {
            Glide.with(context)
                    .load(URLUtils.getSmallAvatarUrlByUid(String.valueOf(curPrivateDetailMessage.msgFromId)))
                    .centerInside()
                    .placeholder(avatarResource)
                    .error(avatarResource)
                    .into(holder.privateMessageDetailSenderAvatar);
            holder.privateMessageDetailMessage.setTextColor(context.getColor(R.color.colorTextDefault));
            holder.privateMessageDetailMessage.setBackgroundColor(context.getColor(R.color.colorBackgroundSecondaryDefault));
            holder.privateMessageDetailSenderAvatar.setVisibility(View.VISIBLE);
            holder.privateMessageDetailRecvAvatar.setVisibility(View.INVISIBLE);
        }





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

        TextView privateMessageDetailTime;
        TextView privateMessageDetailMessage;
        ImageView privateMessageDetailRecvAvatar;
        ImageView privateMessageDetailSenderAvatar;
        ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            privateMessageDetailTime = itemView.findViewById(R.id.item_private_message_detail_time);
            privateMessageDetailMessage = itemView.findViewById(R.id.item_private_message_detail_message);
            privateMessageDetailRecvAvatar = itemView.findViewById(R.id.item_private_message_detail_recv_avatar);
            privateMessageDetailSenderAvatar = itemView.findViewById(R.id.item_private_message_detail_sender_avatar);
            constraintLayout = itemView.findViewById(R.id.item_private_message_detail_constraint_layout);
        }
    }
}
