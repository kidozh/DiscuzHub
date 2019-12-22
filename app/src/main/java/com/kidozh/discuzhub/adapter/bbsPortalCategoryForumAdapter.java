package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kidozh.discuzhub.utilities.bbsParseUtils.getForumInfoByFid;
import static com.kidozh.discuzhub.utilities.networkUtils.getPreferredClient;

public class bbsPortalCategoryForumAdapter extends RecyclerView.Adapter<bbsPortalCategoryForumAdapter.bbsPortalCatagoryViewHolder> {
    private String TAG = bbsPortalCategoryForumAdapter.class.getSimpleName();
    Context mContext;
    List<Integer> mCateList;
    String jsonString;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    bbsPortalCategoryForumAdapter(Context context, String jsonObject, bbsInformation bbsInformation){
        this.mContext = context;
        this.jsonString = jsonObject;
        this.bbsInfo = bbsInformation;
    }

    bbsPortalCategoryForumAdapter(Context context, String jsonObject, bbsInformation bbsInformation, forumUserBriefInfo curUser){
        this.mContext = context;
        this.jsonString = jsonObject;
        this.bbsInfo = bbsInformation;
        this.curUser = curUser;
    }

    public void setmCateList(List<Integer> mCateList){
        this.mCateList = mCateList;
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
        int fid = mCateList.get(position);
        forumInfo forum = getForumInfoByFid(jsonString,fid);
        if(forum!=null){
            holder.mForumName.setText(forum.name);
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(getPreferredClient(this.mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            Glide.with(mContext)
                    .load(forum.iconURL)
                    .apply(RequestOptions.placeholderOf(R.drawable.vector_drawable_forum).error(R.drawable.vector_drawable_forum))
                    .into(holder.mBBSForumImage);
            holder.mCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, bbsShowForumThreadActivity.class);
                    intent.putExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY,forum);
                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                    Log.d(TAG,"put base url "+bbsInfo.base_url);
                    mContext.startActivity(intent);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        if(mCateList == null){
            return 0;
        }
        else {
            return mCateList.size();
        }

    }

    public class bbsPortalCatagoryViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_forum_imageview)
        ImageView mBBSForumImage;
        @BindView(R.id.bbs_forum_name)
        TextView mForumName;
        @BindView(R.id.bbs_forum_cardview)
        CardView mCardview;
        public bbsPortalCatagoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
