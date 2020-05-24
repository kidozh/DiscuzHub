package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
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
import com.kidozh.discuzhub.activities.bbsShowForumThreadActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class bbsPortalCategoryForumAdapter extends RecyclerView.Adapter<bbsPortalCategoryForumAdapter.bbsPortalCatagoryViewHolder> {
    private String TAG = bbsPortalCategoryForumAdapter.class.getSimpleName();
    private Context mContext;
    private List<ForumInfo> forumInfoList;
    private String jsonString;
    private bbsInformation bbsInfo;
    private forumUserBriefInfo curUser;

    bbsPortalCategoryForumAdapter(Context context, String jsonObject, bbsInformation bbsInformation, forumUserBriefInfo curUser){
        this.mContext = context;
        this.jsonString = jsonObject;
        this.bbsInfo = bbsInformation;
        this.curUser = curUser;
    }

    public void setForumInfoList(List<ForumInfo> forumInfoList) {
        this.forumInfoList = forumInfoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public bbsPortalCatagoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_category_forum;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsPortalCatagoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bbsPortalCatagoryViewHolder holder, int position) {

        ForumInfo forum = forumInfoList.get(position);
        if(forum!=null){
            // support rich text
            Spanned sp = Html.fromHtml(forum.name,null,null);
            holder.mForumName.setText(sp, TextView.BufferType.SPANNABLE);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(this.mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            Glide.with(mContext)
                    .load(forum.iconUrl)
                    .apply(RequestOptions
                            .placeholderOf(R.drawable.ic_forum_24px)
                            .error(R.drawable.ic_forum_24px))
                    .into(holder.mBBSForumImage);

            holder.mCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, bbsShowForumThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forum);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                    Log.d(TAG,"put base url "+bbsInfo.base_url);
                    VibrateUtils.vibrateForClick(mContext);
                    mContext.startActivity(intent);
                }
            });
            if(forum.todayPosts != 0){
                holder.mTodayPosts.setVisibility(View.VISIBLE);
                if(forum.todayPosts >= 100){
                    holder.mTodayPosts.setText(R.string.forum_today_posts_over_much);
                    holder.mTodayPosts.setBackgroundColor(mContext.getColor(R.color.colorAccent));
                }
                else {
                    holder.mTodayPosts.setText(String.valueOf(forum.todayPosts));
                    holder.mTodayPosts.setBackgroundColor(mContext.getColor(R.color.colorPrimary));
                }

            }
            else {
                holder.mTodayPosts.setVisibility(View.GONE);
            }
        }


    }

    @Override
    public int getItemCount() {
        if(forumInfoList == null){
            return 0;
        }
        else {
            return forumInfoList.size();
        }

    }

    public class bbsPortalCatagoryViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_forum_imageview)
        ImageView mBBSForumImage;
        @BindView(R.id.bbs_forum_name)
        TextView mForumName;
        @BindView(R.id.bbs_forum_cardview)
        CardView mCardview;
        @BindView(R.id.bbs_forum_today_posts)
        TextView mTodayPosts;
        public bbsPortalCatagoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
