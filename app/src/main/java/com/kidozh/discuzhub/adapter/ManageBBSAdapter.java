package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManageBBSAdapter extends RecyclerView.Adapter<ManageBBSAdapter.ManageBBSViewHolder> {
    private List<bbsInformation> bbsInformationList;
    Context context;

    public void setBbsInformationList(List<bbsInformation> bbsInformationList) {
        this.bbsInformationList = bbsInformationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ManageBBSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ManageBBSViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_bbs,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ManageBBSViewHolder holder, int position) {
        bbsInformation forumInfo = bbsInformationList.get(position);
        holder.forumHost.setText(forumInfo.base_url);
        holder.forumName.setText(forumInfo.site_name);
        holder.forumSiteId.setText(forumInfo.mysite_id);
        holder.forumPostNumber.setText(numberFormatUtils.getShortNumberText(forumInfo.total_posts));
        holder.forumMemberNumber.setText(numberFormatUtils.getShortNumberText(forumInfo.total_members));

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context));
        URLUtils.setBBS(forumInfo);
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(context)
                .load(URLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(holder.forumAvatar);
    }

    @Override
    public int getItemCount() {
        if(bbsInformationList == null){
            return 0;
        }
        else {
            return bbsInformationList.size();
        }

    }

    public static class ManageBBSViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_forum_information_avatar)
        ImageView forumAvatar;
        @BindView(R.id.item_forum_information_name)
        TextView forumName;
        @BindView(R.id.item_forum_information_host)
        TextView forumHost;
        @BindView(R.id.item_forum_information_siteid)
        TextView forumSiteId;
        @BindView(R.id.item_forum_information_post_number)
        TextView forumPostNumber;
        @BindView(R.id.item_forum_information_posts_icon)
        ImageView forumPostIcon;
        @BindView(R.id.item_forum_information_member_number)
        TextView forumMemberNumber;
        @BindView(R.id.item_forum_information_member_icon)
        ImageView forumMemberIcon;
        public ManageBBSViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
