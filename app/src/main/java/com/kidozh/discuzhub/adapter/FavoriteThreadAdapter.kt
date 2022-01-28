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
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ThreadActivity
import com.kidozh.discuzhub.adapter.FavoriteThreadAdapter.FavoriteThreadViewHolder
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.FavoriteThread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.TimeDisplayUtils.Companion.getLocalePastTimeString
import com.kidozh.discuzhub.utilities.VibrateUtils

class FavoriteThreadAdapter :
    PagingDataAdapter<FavoriteThread, FavoriteThreadViewHolder>(FavoriteThread.DIFF_CALLBACK) {
    lateinit var context: Context
    lateinit var discuz: Discuz
    var curUser: User? = null
    fun setInformation(discuz: Discuz, userBriefInfo: User?) {
        this.discuz = discuz
        curUser = userBriefInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteThreadViewHolder {
        context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        return FavoriteThreadViewHolder(
            layoutInflater.inflate(
                R.layout.item_favorite_thread,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FavoriteThreadViewHolder, position: Int) {
        val favoriteThread = getItem(position)
        if (favoriteThread != null) {
            holder.author.text = favoriteThread.author
            holder.title.text = favoriteThread.title
            if (TextUtils.isEmpty(favoriteThread.description)) {
                holder.description.visibility = View.GONE
            } else {
                holder.description.visibility = View.VISIBLE
                holder.description.text = favoriteThread.description
            }
            holder.publishAt.text = getLocalePastTimeString(context!!, favoriteThread.date)
            Log.d(TAG, "get publish date " + favoriteThread.date)
            holder.replyNumber.text =
                context.getString(R.string.bbs_thread_reply_number, favoriteThread.replies)
            if (favoriteThread.favid == 0) {
                holder.syncStatus.visibility = View.GONE
            } else {
                holder.syncStatus.visibility = View.VISIBLE
            }
            val avatarURL = discuz.getAvatarUrl(favoriteThread.uid)
            var avatar_num = favoriteThread.uid
            avatar_num = avatar_num % 16
            if (avatar_num < 0) {
                avatar_num = -avatar_num
            }
            val avatarResource = context!!.resources.getIdentifier(
                String.format("avatar_%s", avatar_num + 1),
                "drawable",
                context.packageName
            )
            val options = RequestOptions()
                .placeholder(ContextCompat.getDrawable(context, avatarResource))
                .error(ContextCompat.getDrawable(context, avatarResource))
            Glide.with(context)
                .load(avatarURL)
                .apply(options)
                .into(holder.userAvatar)
            holder.cardView.setOnClickListener {
                val intent = Intent(context, ThreadActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, curUser)
                intent.putExtra(ConstUtils.PASS_THREAD_KEY, favoriteThread.toThread())
                intent.putExtra("FID", favoriteThread.favid)
                intent.putExtra("TID", favoriteThread.idKey)
                intent.putExtra("SUBJECT", favoriteThread.title)
                VibrateUtils.vibrateForClick(context)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity?,
                    Pair.create(holder.title, "bbs_thread_subject")
                )
                val bundle = options.toBundle()
                context.startActivity(intent, bundle)
            }
        } else {
        }
    }

    class FavoriteThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userAvatar: ShapeableImageView
        var author: TextView
        var publishAt: TextView
        var replyNumber: TextView
        var title: TextView
        var description: TextView
        var cardView: CardView
        var syncStatus: TextView

        init {
            userAvatar = itemView.findViewById(R.id.favorite_thread_user_avatar)
            author = itemView.findViewById(R.id.favorite_thread_author)
            publishAt = itemView.findViewById(R.id.favorite_thread_date)
            replyNumber = itemView.findViewById(R.id.favorite_thread_reply)
            title = itemView.findViewById(R.id.favorite_thread_title)
            description = itemView.findViewById(R.id.favorite_thread_description)
            cardView = itemView.findViewById(R.id.bbs_favorite_thread_cardview)
            syncStatus = itemView.findViewById(R.id.favorite_thread_sync_status)
        }
    }

    companion object {
        private val TAG = FavoriteThreadAdapter::class.java.simpleName
    }
}