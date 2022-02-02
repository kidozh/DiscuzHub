package com.kidozh.discuzhub.adapter

import android.content.Context
import android.text.Html
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.kidozh.discuzhub.databinding.ItemPostCommentBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.results.Comment
import com.kidozh.discuzhub.utilities.NetworkUtils
import java.io.InputStream

class CommentAdapter(val discuz : Discuz) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    var commentList: List<Comment> = ArrayList()
    lateinit var context: Context

    fun setComments(commentList: List<Comment>) {
        this.commentList = commentList
        notifyItemRangeInserted(0, commentList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false

        return ViewHolder(ItemPostCommentBinding.inflate(inflater,parent,shouldAttachToParentImmediately))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]
        holder.binding.message.text = comment.comment
        holder.binding.username.text = comment.author
        val sp = Html.fromHtml(comment.dateline, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannableString = SpannableString(sp)
        holder.binding.publishDate.setText(spannableString, TextView.BufferType.SPANNABLE)

        // download avatar not regarding data save mode
        val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context))
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        val source: String = discuz.getAvatarUrl(comment.authorId)
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        val glideUrl = GlideUrl(source,
                LazyHeaders.Builder().addHeader("referer", source).build()
        )
        var avatar_num: Int = comment.authorId % 16
        if (avatar_num < 0) {
            avatar_num = -avatar_num
        }
        val avatarResource = context.resources.getIdentifier(String.format("avatar_%s", avatar_num + 1), "drawable", context.packageName)
        if (NetworkUtils.canDownloadImageOrFile(context)) {
            Glide.with(context)
                    .load(glideUrl)
                    .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                    .into(holder.binding.avatar)
        } else {
            Glide.with(context)
                    .load(glideUrl)
                    .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                    .onlyRetrieveFromCache(true)
                    .into(holder.binding.avatar)
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    class ViewHolder(val binding: ItemPostCommentBinding) : RecyclerView.ViewHolder(binding.root) {



    }
}