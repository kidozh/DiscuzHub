package com.kidozh.discuzhub.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ForumActivity
import com.kidozh.discuzhub.adapter.FavoriteForumAdapter.FavoriteForumViewHolder
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.FavoriteForum
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.TimeDisplayUtils.Companion.getLocalePastTimeString
import com.kidozh.discuzhub.utilities.VibrateUtils

class FavoriteForumAdapter : PagedListAdapter<FavoriteForum?, FavoriteForumViewHolder>(FavoriteForum.DIFF_CALLBACK) {
    lateinit var context: Context
    lateinit var bbsInfo: Discuz
    var user: User? = null
    fun setInformation(bbsInfo: Discuz, userBriefInfo: User?) {
        this.bbsInfo = bbsInfo
        user = userBriefInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteForumViewHolder {
        context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        return FavoriteForumViewHolder(layoutInflater.inflate(R.layout.item_favorite_forum, parent, false))
    }

    override fun onBindViewHolder(holder: FavoriteForumViewHolder, position: Int) {
        val favoriteForum = getItem(position)
        if (favoriteForum != null) {
            holder.title.text = favoriteForum.title
            if (TextUtils.isEmpty(favoriteForum.description)) {
                holder.description.visibility = View.GONE
            } else {
                holder.description.visibility = View.VISIBLE
                holder.description.text = favoriteForum.description
            }
            holder.publishAt.text = getLocalePastTimeString(context, favoriteForum.date)
            Log.d(TAG, "get publish date " + favoriteForum.date)
            holder.todayPosts.text = context.getString(R.string.forum_today_post, favoriteForum.todayposts)
            if (favoriteForum.favid == 0) {
                holder.syncStatus.visibility = View.GONE
            } else {
                holder.syncStatus.visibility = View.VISIBLE
            }
            holder.cardView.setOnClickListener {
                val intent = Intent(context, ForumActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, favoriteForum.toForum())
                intent.putExtra("FID", favoriteForum.idKey)
                VibrateUtils.vibrateForClick(context)
                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity?,
                        Pair.create(holder.title, "bbs_thread_subject")
                )
                val bundle = options.toBundle()
                context.startActivity(intent, bundle)
            }
        }
    }

    class FavoriteForumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var publishAt: TextView = itemView.findViewById(R.id.favorite_forum_date)
        var title: TextView = itemView.findViewById(R.id.favorite_forum_title)
        var description: TextView = itemView.findViewById(R.id.favorite_forum_description)
        var cardView: CardView = itemView.findViewById(R.id.bbs_favorite_forum_cardview)
        var syncStatus: TextView = itemView.findViewById(R.id.favorite_forum_sync_status)
        var todayPosts: TextView = itemView.findViewById(R.id.favorite_forum_today_posts)

    }

    companion object {
        private val TAG = FavoriteForumAdapter::class.java.simpleName
    }
}