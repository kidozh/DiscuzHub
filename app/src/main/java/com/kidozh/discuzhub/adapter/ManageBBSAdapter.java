package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.SingleDiscuzActivity;
import com.kidozh.discuzhub.databinding.ItemManageBbsBinding;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.InputStream;


public class ManageBBSAdapter extends PagedListAdapter<Discuz, ManageBBSAdapter.ManageBBSViewHolder> {
    private static final String TAG = ManageBBSAdapter.class.getSimpleName();
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
        Discuz forumInfo = getItem(position);
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

        if(forumInfo.getApiVersion() > 4){
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
        holder.binding.itemBbsCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, SingleDiscuzActivity.class);
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
            context.startActivity(intent);
        });
    }


    public static class ManageBBSViewHolder extends RecyclerView.ViewHolder{
        ItemManageBbsBinding binding;
        public ManageBBSViewHolder(@NonNull ItemManageBbsBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private static DiffUtil.ItemCallback<Discuz> DIFF_CALLBACK = new DiffUtil.ItemCallback<Discuz>() {
        @Override
        public boolean areItemsTheSame(@NonNull Discuz oldItem, @NonNull Discuz newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Discuz oldItem, @NonNull Discuz newItem) {
            return oldItem.equals(newItem);
        }
    };
}
