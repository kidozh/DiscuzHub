package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.bbsShowThreadActivity;
import com.kidozh.discuzhub.activities.showPersonalInfoActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class bbsNotificationAdapter extends RecyclerView.Adapter<bbsNotificationAdapter.ViewHolder> {
    private static final String TAG = bbsNotificationAdapter.class.getSimpleName();
    private Context context;
    private List<bbsParseUtils.notificationDetailInfo> notificationDetailInfoList;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public bbsNotificationAdapter(bbsInformation bbsInfo, forumUserBriefInfo curUser){
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
    }

    public void setNotificationDetailInfoList(List<bbsParseUtils.notificationDetailInfo> notificationDetailInfoList) {
        this.notificationDetailInfoList = notificationDetailInfoList;
        notifyDataSetChanged();
    }

    public void addNotificationDetailInfoList(List<bbsParseUtils.notificationDetailInfo> notificationDetailInfoList) {
        if(notificationDetailInfoList == null){
            this.notificationDetailInfoList = notificationDetailInfoList;
        }
        else {
            this.notificationDetailInfoList.addAll(notificationDetailInfoList);
        }
        notifyDataSetChanged();

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bbs_notification_detail,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bbsParseUtils.notificationDetailInfo notificationDetailInfo = this.notificationDetailInfoList.get(position);
        if(notificationDetailInfo.author.length() == 0){
            holder.bbsNotificationAuthor.setVisibility(View.GONE);
        }
        else {
            holder.bbsNotificationAuthor.setVisibility(View.VISIBLE);
        }
        holder.bbsNotificationAuthor.setText(notificationDetailInfo.author);
        if(notificationDetailInfo.authorId!=0){
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(context));
            Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            Glide.with(context)
                    .load(bbsURLUtils.getDefaultAvatarUrlByUid(String.valueOf(notificationDetailInfo.authorId)))
                    .apply(RequestOptions
                            .placeholderOf(R.drawable.avatar_person_male)
                            .error(R.drawable.avatar_person_male))
                    .into(holder.bbsNotificationImageview);

            holder.bbsNotificationImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, showPersonalInfoActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);

                    intent.putExtra("UID",String.valueOf(notificationDetailInfo.authorId));
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) context, holder.bbsNotificationImageview, "user_info_avatar");

                    Bundle bundle = options.toBundle();

                    context.startActivity(intent,bundle);
                }
            });
        }
        else {
            holder.bbsNotificationImageview.setImageDrawable(context.getDrawable(R.drawable.vector_drawable_info_24px_outline));
        }
        holder.bbsNotificationPublishTime.setText(timeDisplayUtils.getLocalePastTimeString(context,notificationDetailInfo.date));
        Spanned sp = Html.fromHtml(notificationDetailInfo.note);
        SpannableString spannableString = new SpannableString(sp);
        holder.bbsNotificationNote.setText(spannableString, TextView.BufferType.SPANNABLE);
        holder.bbsNotificationNote.setTextColor(context.getColor(R.color.colorTextDefault));
        holder.bbsNotificationNote.setMovementMethod(LinkMovementMethod.getInstance());
        if(notificationDetailInfo.type.equals("post")){
            if(notificationDetailInfo.noteVariables!=null && notificationDetailInfo.noteVariables.containsKey("tid")){
                holder.bbsNotificationCardview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, bbsShowThreadActivity.class);
                        intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                        intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                        intent.putExtra("TID",notificationDetailInfo.noteVariables.get("tid"));
                        intent.putExtra("FID",String.valueOf(notificationDetailInfo.authorId));
                        intent.putExtra("SUBJECT",notificationDetailInfo.noteVariables.get("subject"));
                        context.startActivity(intent);
                    }
                });
            }
            else {
                // nothing
                holder.bbsNotificationCardview.setClickable(false);
            }

        }

    }

    @Override
    public int getItemCount() {
        if(notificationDetailInfoList == null){
            return 0;
        }
        else {
            return notificationDetailInfoList.size();
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.bbs_notification_imageView)
        ImageView bbsNotificationImageview;
        @BindView(R.id.bbs_notification_author)
        TextView bbsNotificationAuthor;
        @BindView(R.id.bbs_notification_note)
        TextView bbsNotificationNote;
        @BindView(R.id.bbs_notification_publish_time)
        TextView bbsNotificationPublishTime;
        @BindView(R.id.bbs_notification_cardview)
        CardView bbsNotificationCardview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
