package com.kidozh.discuzhub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.results.ForumResult.ShortReply
import com.kidozh.discuzhub.utilities.NetworkUtils
import java.io.InputStream

class ShortPostAdapter(val discuz: Discuz) : RecyclerView.Adapter<ShortPostAdapter.ViewHolder>() {
    var shortReplyInfoList: MutableList<ShortReply> = ArrayList()
    lateinit var context: Context
    @JvmName("setShortReplyInfoList1")
    fun setShortReplyInfoList(shortReplyInfoList: List<ShortReply>) {
        val oldSize = this.shortReplyInfoList.size
        this.shortReplyInfoList.clear()
        notifyItemRangeRemoved(0, oldSize)
        this.shortReplyInfoList.addAll(shortReplyInfoList)
        notifyItemRangeInserted(0, shortReplyInfoList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val layoutIdForListItem = R.layout.item_short_post
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        val view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val replyInfo = shortReplyInfoList!![position]
        holder.mReplyerName.text = replyInfo.author
        holder.mReplyMessage.text = replyInfo.message

        // download avatar not regarding data save mode
        val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context))
        Glide.get(context).registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            factory
        )
        val source: String = discuz.getAvatarUrl(replyInfo.authorId)
        Glide.get(context).registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            factory
        )
        val glideUrl = GlideUrl(
            source,
            LazyHeaders.Builder().addHeader("referer", source).build()
        )
        var avatar_num = replyInfo.authorId % 16
        if (avatar_num < 0) {
            avatar_num = -avatar_num
        }
        val avatarResource = context.resources.getIdentifier(
            String.format("avatar_%s", avatar_num + 1),
            "drawable",
            context.packageName
        )
        if (NetworkUtils.canDownloadImageOrFile(context)) {
            Glide.with(context)
                .load(glideUrl)
                .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                .into(holder.mReplyerAvatar)
        } else {
            Glide.with(context)
                .load(glideUrl)
                .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                .onlyRetrieveFromCache(true)
                .into(holder.mReplyerAvatar)
        }
    }

    override fun getItemCount(): Int {
        return if (shortReplyInfoList == null) {
            0
        } else {
            shortReplyInfoList!!.size
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mReplyerAvatar: ImageView
        var mReplyerName: TextView
        var mReplyMessage: TextView

        init {
            mReplyerAvatar = itemView.findViewById(R.id.bbs_forum_thread_short_reply_user_avatar)
            mReplyerName = itemView.findViewById(R.id.bbs_forum_thread_short_reply_user_name)
            mReplyMessage =
                itemView.findViewById(R.id.bbs_forum_thread_short_reply_user_reply_message)
        }
    }
}