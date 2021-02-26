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
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ShortPostAdapter extends RecyclerView.Adapter<ShortPostAdapter.ViewHolder> {

    List<ForumResult.ShortReply> shortReplyInfoList = new ArrayList<>();
    Context context;
    

    public void setShortReplyInfoList(@NonNull List<ForumResult.ShortReply> shortReplyInfoList) {
        int oldSize = this.shortReplyInfoList.size();
        this.shortReplyInfoList.clear();
        notifyItemRangeRemoved(0,oldSize);
        this.shortReplyInfoList.addAll(shortReplyInfoList);
        notifyItemRangeInserted(0,shortReplyInfoList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_short_post;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumResult.ShortReply replyInfo = shortReplyInfoList.get(position);
        holder.mReplyerName.setText(replyInfo.author);
        holder.mReplyMessage.setText(replyInfo.message);

        // download avatar not regarding data save mode
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        String source = URLUtils.getSmallAvatarUrlByUid(replyInfo.authorId);
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        GlideUrl glideUrl = new GlideUrl(source,
                new LazyHeaders.Builder().addHeader("referer",source).build()
        );
        int avatar_num = replyInfo.authorId % 16;
        if(avatar_num < 0){
            avatar_num = -avatar_num;
        }
        int avatarResource = context.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",context.getPackageName());
        if(NetworkUtils.canDownloadImageOrFile(context)){
            Glide.with(context)
                    .load(glideUrl)
                    .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                    .into(holder.mReplyerAvatar);
        }
        else {
            Glide.with(context)
                    .load(glideUrl)
                    .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                    .onlyRetrieveFromCache(true)
                    .into(holder.mReplyerAvatar);
        }


    }

    @Override
    public int getItemCount() {
        if(shortReplyInfoList==null){
            return 0;
        }
        else {
            return shortReplyInfoList.size();
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView mReplyerAvatar;
        TextView mReplyerName;
        TextView mReplyMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mReplyerAvatar = itemView.findViewById(R.id.bbs_forum_thread_short_reply_user_avatar);
            mReplyerName = itemView.findViewById(R.id.bbs_forum_thread_short_reply_user_name);
            mReplyMessage = itemView.findViewById(R.id.bbs_forum_thread_short_reply_user_reply_message);
        }
    }
}
