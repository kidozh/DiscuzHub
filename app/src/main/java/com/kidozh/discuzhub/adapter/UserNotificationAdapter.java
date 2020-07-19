package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.activities.bbsShowPostActivity;
import com.kidozh.discuzhub.activities.showWebPageActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class UserNotificationAdapter extends RecyclerView.Adapter<UserNotificationAdapter.ViewHolder> {
    private static final String TAG = UserNotificationAdapter.class.getSimpleName();
    private Context context;
    private List<UserNoteListResult.UserNotification> notificationDetailInfoList;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    public List<UserNoteListResult.UserNotification> getNotificationDetailInfoList() {
        return notificationDetailInfoList;
    }

    public UserNotificationAdapter(bbsInformation bbsInfo, forumUserBriefInfo curUser){
        this.bbsInfo = bbsInfo;
        this.curUser = curUser;
    }

    public void setNotificationDetailInfoList(List<UserNoteListResult.UserNotification> notificationDetailInfoList) {
        this.notificationDetailInfoList = notificationDetailInfoList;
        notifyDataSetChanged();
    }

    public void addNotificationDetailInfoList(List<UserNoteListResult.UserNotification> notificationDetailInfoList) {
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
        UserNoteListResult.UserNotification notificationDetailInfo = this.notificationDetailInfoList.get(position);
        // remove author if not named
        if(notificationDetailInfo.authorId == 0){
            holder.bbsNotificationAuthor.setVisibility(View.GONE);
            holder.bbsNotificationImageview.setImageDrawable(context.getDrawable(R.drawable.vector_drawable_info_24px_outline));
        }
        else {
            holder.bbsNotificationAuthor.setVisibility(View.VISIBLE);
            holder.bbsNotificationAuthor.setText(notificationDetailInfo.author);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(context));
            Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            // determine avatar placeholder
            int avatar_num = notificationDetailInfo.authorId % 16;
            if(avatar_num < 0){
                avatar_num = -avatar_num;
            }

            int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());

            Glide.with(context)
                    .load(URLUtils.getDefaultAvatarUrlByUid(String.valueOf(notificationDetailInfo.authorId)))
                    .apply(RequestOptions
                            .placeholderOf(avatarResource)
                            .error(avatarResource)
                    )
                    .into(holder.bbsNotificationImageview);

            holder.bbsNotificationImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                    intent.putExtra("UID",notificationDetailInfo.authorId);
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) context, holder.bbsNotificationImageview, "user_info_avatar");

                    Bundle bundle = options.toBundle();
                    context.startActivity(intent,bundle);
                }
            });
        }

        holder.bbsNotificationPublishTime.setText(timeDisplayUtils.getLocalePastTimeString(context,notificationDetailInfo.date));
        Spanned sp = Html.fromHtml(notificationDetailInfo.note);
        SpannableString spannableString = new SpannableString(sp);
        holder.bbsNotificationNote.setText(spannableString, TextView.BufferType.SPANNABLE);

        if(notificationDetailInfo.notificationExtraInfo != null){
            holder.bbsNotificationCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, bbsShowPostActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                    intent.putExtra("TID",notificationDetailInfo.notificationExtraInfo.tid);
                    intent.putExtra("FID",notificationDetailInfo.authorId);
                    intent.putExtra("SUBJECT",notificationDetailInfo.notificationExtraInfo.subject);
                    VibrateUtils.vibrateForClick(context);
                    context.startActivity(intent);
                }
            });
        }
        else {
            // nothing
            holder.bbsNotificationNote.setClickable(false);
            // holder.bbsNotificationCardview.setClickable(false);
            // create popup menu first
            holder.bbsNotificationCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context,holder.bbsNotificationCardview);
                    Menu menu = popupMenu.getMenu();

                    // parse link href
                    Document document = Jsoup.parse(notificationDetailInfo.note);
                    Elements elements = document.select("a");
                    List<String> urlLinkString = new ArrayList<>();
                    for(int i=0; i<elements.size();i++){
                        Element element = elements.get(i);
                        String href = element.attr("href");
                        String displayText = element.text();
                        Log.d(TAG,"href "+href+" display text "+displayText);
                        menu.add(Menu.NONE,Menu.FIRST+i,i,displayText);
                        if(href.startsWith("http://")||href.startsWith("https://")){

                        }
                        else {
                            // need to add protocol
                            Uri baseUri = Uri.parse(bbsInfo.base_url);
                            href = baseUri.getScheme()+"://" +baseUri.getHost()+"/" + href;
                            Log.d(TAG, "Changed href to "+href);
                        }
                        urlLinkString.add(href);
                    }
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();
                            int position = itemId - Menu.FIRST;
                            String urlLink = urlLinkString.get(position);
                            bbsLinkMovementMethod.parseURLAndOpen(context,bbsInfo,curUser,urlLink);

//                            Intent intent = new Intent(context, showWebPageActivity.class);
//                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
//                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
//                            intent.putExtra(bbsConstUtils.PASS_URL_KEY,urlLink);
//                            //Log.d(TAG,"Inputted URL "+currentUrl);
//                            context.startActivity(intent);
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
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
