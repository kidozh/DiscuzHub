package com.kidozh.discuzhub.adapter;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ItemManageBbsBinding;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ManageBBSAdapter extends PagedListAdapter<bbsInformation, ManageBBSAdapter.ManageBBSViewHolder> {
    Context context;

    public ManageBBSAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ManageBBSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemManageBbsBinding binding = ItemManageBbsBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ManageBBSViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageBBSViewHolder holder, int position) {
        bbsInformation forumInfo = getItem(position);
        if(forumInfo == null){
            return;
        }
        holder.binding.itemForumInformationHost.setText(forumInfo.base_url);
        holder.binding.itemForumInformationName.setText(forumInfo.site_name);
        if(forumInfo.base_url.startsWith("https://")){
            holder.binding.itemBbsHttps.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.itemBbsHttps.setVisibility(View.GONE);
        }

        if(forumInfo.getAPIVersion() > 4){
            holder.binding.itemBbsApiVersion.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.itemBbsApiVersion.setVisibility(View.GONE);
        }

        if(forumInfo.qqConnect){
            holder.binding.itemBbsQqLogin.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.itemBbsQqLogin.setVisibility(View.GONE);
        }

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context));
        URLUtils.setBBS(forumInfo);
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(context)
                .load(URLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(holder.binding.itemForumInformationAvatar);
    }


    public static class ManageBBSViewHolder extends RecyclerView.ViewHolder{
        ItemManageBbsBinding binding;
        public ManageBBSViewHolder(@NonNull ItemManageBbsBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private static DiffUtil.ItemCallback<bbsInformation> DIFF_CALLBACK = new DiffUtil.ItemCallback<bbsInformation>() {
        @Override
        public boolean areItemsTheSame(@NonNull bbsInformation oldItem, @NonNull bbsInformation newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull bbsInformation oldItem, @NonNull bbsInformation newItem) {
            return oldItem.equals(newItem);
        }
    };
}
