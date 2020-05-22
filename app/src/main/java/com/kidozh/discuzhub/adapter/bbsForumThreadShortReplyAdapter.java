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
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.results.DisplayForumResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsForumThreadShortReplyAdapter extends RecyclerView.Adapter<bbsForumThreadShortReplyAdapter.ViewHolder> {

    List<DisplayForumResult.ShortReply> shortReplyInfoList;
    Context context;

    public bbsForumThreadShortReplyAdapter(Context context){
        this.context = context;
    }

    public void setShortReplyInfoList(List<DisplayForumResult.ShortReply> shortReplyInfoList) {
        this.shortReplyInfoList = shortReplyInfoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_forum_thread_short_reply;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayForumResult.ShortReply replyInfo = shortReplyInfoList.get(position);
        holder.mReplyerName.setText(replyInfo.author);
        holder.mReplyMessage.setText(replyInfo.message);

        // download avatar not regarding data save mode
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(context));
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        String source = URLUtils.getSmallAvatarUrlByUid(replyInfo.authorId);
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        Glide.with(context)
                .load(source)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_account_circle_24px).error(R.drawable.ic_account_circle_24px))
                .into(holder.mReplyerAvatar);
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

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.bbs_forum_thread_short_reply_user_avatar)
        ImageView mReplyerAvatar;
        @BindView(R.id.bbs_forum_thread_short_reply_user_name)
        TextView mReplyerName;
        @BindView(R.id.bbs_forum_thread_short_reply_user_reply_message)
        TextView mReplyMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
