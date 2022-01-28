package com.kidozh.discuzhub.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Html
import android.text.SpannableString
import android.util.Log
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
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.PrivateMessageActivity
import com.kidozh.discuzhub.activities.UserProfileActivity
import com.kidozh.discuzhub.adapter.PrivateMessageAdapter
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClient
import com.kidozh.discuzhub.utilities.bbsParseUtils.privateMessage
import java.io.InputStream

class PrivateMessageAdapter(var discuz: Discuz, var userBriefInfo: User) :
    RecyclerView.Adapter<PrivateMessageAdapter.ViewHolder>() {
    private var privateMessageList: MutableList<privateMessage>? = null
    private var context: Context? = null
    fun setPrivateMessageList(privateMessageList: MutableList<privateMessage>?) {
        this.privateMessageList = privateMessageList
        notifyDataSetChanged()
    }

    fun addPrivateMessageList(privateMessageList: MutableList<privateMessage>?) {
        if (this.privateMessageList == null) {
            this.privateMessageList = privateMessageList
        } else {
            this.privateMessageList!!.addAll((privateMessageList)!!)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_private_message, parent, false)
        )
    }

    @SuppressLint("RestrictedApi", "UnsafeOptInUsageError")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val privateM = privateMessageList!![position]
        holder.privateMessageContent.text = privateM.message
        val timeSp = Html.fromHtml(privateM.vdateLine)
        if (privateM.isNew) {
            Log.d(TAG, "THIS IS NEW PRIVATE MESSAGE " + privateM.message)
            val badgeDrawable = BadgeDrawable.create((context)!!)
            badgeDrawable.number = 1
            badgeDrawable.isVisible = true
            BadgeUtils.attachBadgeDrawable(badgeDrawable, holder.privateMessageAvatar, null)
            holder.privateMessageUsername.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        } else {
            holder.privateMessageUsername.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        }
        holder.privateMessageRecvTime.text = SpannableString(timeSp)

        //holder.privateMessageRecvTime.setText(privateM.vdateLine);
        holder.privateMessageUsername.text = privateM.toUsername
        val factory = OkHttpUrlLoader.Factory(
            getPreferredClient(
                (context)!!
            )
        )
        Glide.get((context)!!).registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            factory
        )
        val avatar_num = position % 16
        val avatarResource = context!!.resources.getIdentifier(
            String.format("avatar_%s", avatar_num + 1),
            "drawable",
            context!!.packageName
        )
        Glide.with((context)!!)
            .load(discuz.getAvatarUrl(privateM.toUid))
            .centerInside()
            .placeholder(avatarResource)
            .error(avatarResource)
            .into(holder.privateMessageAvatar)
        holder.privateMessageCardview.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, PrivateMessageActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            intent.putExtra(ConstUtils.PASS_PRIVATE_MESSAGE_KEY, privateM)
            context!!.startActivity(intent)
        })
        holder.privateMessageAvatar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, UserProfileActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                intent.putExtra("UID", privateM.toUid.toString())
                context!!.startActivity(intent)
            }
        })
    }

    override fun getItemCount(): Int {
        return if (privateMessageList == null) {
            0
        } else {
            privateMessageList!!.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var privateMessageCardview: CardView
        var privateMessageAvatar: ImageView
        var privateMessageContent: TextView
        var privateMessageRecvTime: TextView
        var privateMessageUsername: TextView

        init {
            privateMessageCardview = itemView.findViewById(R.id.item_private_message_cardview)
            privateMessageAvatar = itemView.findViewById(R.id.item_private_message_avatar)
            privateMessageContent = itemView.findViewById(R.id.item_private_message_content)
            privateMessageRecvTime = itemView.findViewById(R.id.item_private_message_recv_time)
            privateMessageUsername = itemView.findViewById(R.id.item_private_message_username)
        }
    }

    companion object {
        private val TAG = PrivateMessageAdapter::class.java.simpleName
    }
}