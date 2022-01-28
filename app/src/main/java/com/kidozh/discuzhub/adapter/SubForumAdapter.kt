package com.kidozh.discuzhub.adapter

import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ForumActivity
import com.kidozh.discuzhub.adapter.SubForumAdapter.SubForumViewHolder
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.ForumResult.SubForumInfo
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClient
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.VibrateUtils
import java.io.InputStream
import java.lang.String
import kotlin.Int

class SubForumAdapter(var bbsInfo: Discuz, var userBriefInfo: User?) :
    RecyclerView.Adapter<SubForumViewHolder>() {
    val TAG = SubForumAdapter::class.java.simpleName
    var context: Context? = null
    var subForumInfoList: List<SubForumInfo> = ArrayList()
    @JvmName("setSubForumInfoList1")
    fun setSubForumInfoList(subForumInfoList: List<SubForumInfo>) {
        this.subForumInfoList = subForumInfoList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubForumViewHolder {
        context = parent.context
        val layoutIdForListItem = R.layout.item_sub_forum
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        val view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately)
        return SubForumViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubForumViewHolder, position: Int) {
        val forum = subForumInfoList!![position]
        val sp = Html.fromHtml(forum.name, null, null)
        holder.mForumName.setText(sp, TextView.BufferType.SPANNABLE)
        val factory = OkHttpUrlLoader.Factory(getPreferredClient(context!!))
        Glide.get(context!!).registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            factory
        )
        URLUtils.setBBS(bbsInfo)
        holder.mBBSForumImage.setImageResource(R.drawable.ic_sub_forum_24px)
        holder.mCardview.setOnClickListener { v: View? ->
            val putForum = Forum()
            putForum.fid = forum.fid
            putForum.name = forum.name
            putForum.description = forum.name
            putForum.posts = forum.posts
            putForum.todayPosts = forum.todayPosts
            putForum.threadCount = forum.threads
            val intent = Intent(context, ForumActivity::class.java)
            intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, putForum)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            VibrateUtils.vibrateForClick(context)
            context!!.startActivity(intent)
        }
        if (forum.todayPosts != 0) {
            holder.mTodayPosts.visibility = View.VISIBLE
            if (forum.todayPosts >= 100) {
                holder.mTodayPosts.setText(R.string.forum_today_posts_over_much)
                holder.mTodayPosts.setBackgroundColor(context!!.getColor(R.color.colorAlizarin))
            } else {
                holder.mTodayPosts.text = String.valueOf(forum.todayPosts)
                holder.mTodayPosts.setBackgroundColor(context!!.getColor(R.color.colorPrimary))
            }
        } else {
            holder.mTodayPosts.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return if (subForumInfoList == null) {
            0
        } else {
            subForumInfoList!!.size
        }
    }

    class SubForumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mBBSForumImage: ImageView
        var mForumName: TextView
        var mCardview: CardView
        var mTodayPosts: TextView

        init {
            mBBSForumImage = itemView.findViewById(R.id.bbs_forum_imageview)
            mForumName = itemView.findViewById(R.id.bbs_forum_name)
            mCardview = itemView.findViewById(R.id.bbs_forum_cardview)
            mTodayPosts = itemView.findViewById(R.id.bbs_forum_today_posts)
        }
    }
}