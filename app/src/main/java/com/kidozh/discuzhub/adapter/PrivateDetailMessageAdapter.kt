package com.kidozh.discuzhub.adapter

import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.UserProfileActivity
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.PrivateMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.*
import java.io.InputStream

class PrivateDetailMessageAdapter(var curBBS: Discuz, var user: User?) : RecyclerView.Adapter<PrivateDetailMessageAdapter.ViewHolder>() {
    var privateDetailMessageList: List<PrivateMessage> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val layoutIdForListItem = R.layout.item_private_message_detail
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        val view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curPrivateDetailMessage = privateDetailMessageList[position]

        val sp = HtmlCompat.fromHtml(curPrivateDetailMessage.message,HtmlCompat.FROM_HTML_MODE_LEGACY,
                MyImageGetter(context, holder.privateMessageDetailMessage, holder.privateMessageDetailMessage, true),
                null)

        val spannableString = SpannableString(sp)
        holder.privateMessageDetailMessage.setText(spannableString, TextView.BufferType.SPANNABLE)
        holder.privateMessageDetailMessage.movementMethod = LinkMovementMethod.getInstance()
        val timeSp = HtmlCompat.fromHtml(curPrivateDetailMessage.dateString,HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.privateMessageDetailTime.text = SpannableString(timeSp)
        val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context))
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.constraintLayout)
        if (curPrivateDetailMessage.self(user)) {
            constraintSet.setHorizontalBias(holder.privateMessageDetailMessage.id, 1.0f)
        } else {
            constraintSet.setHorizontalBias(holder.privateMessageDetailMessage.id, 0.0f)
        }
        constraintSet.applyTo(holder.constraintLayout)
        holder.privateMessageDetailSenderAvatar.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, curBBS)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra("UID", java.lang.String.valueOf(curPrivateDetailMessage.msgFromId))
            context.startActivity(intent)
        }
        holder.privateMessageDetailRecvAvatar.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, curBBS)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra("UID", java.lang.String.valueOf(curPrivateDetailMessage.msgFromId))
            context.startActivity(intent)
        }
        val avatar_num = position % 16
        val avatarResource = context.resources.getIdentifier(String.format("avatar_%s", avatar_num + 1), "drawable", context.packageName)
        if (curPrivateDetailMessage.self(user)) {
            Glide.with(context)
                    .load(URLUtils.getSmallAvatarUrlByUid(java.lang.String.valueOf(curPrivateDetailMessage.msgFromId)))
                    .centerInside()
                    .placeholder(avatarResource)
                    .error(avatarResource)
                    .into(holder.privateMessageDetailRecvAvatar)
            holder.privateMessageDetailMessage.setBackgroundColor(context.getColor(R.color.colorPrimary))
            holder.privateMessageDetailMessage.setTextColor(context.getColor(R.color.colorPureWhite))
            holder.privateMessageDetailSenderAvatar.visibility = View.INVISIBLE
            holder.privateMessageDetailRecvAvatar.visibility = View.VISIBLE
        } else {
            Glide.with(context)
                    .load(URLUtils.getSmallAvatarUrlByUid(java.lang.String.valueOf(curPrivateDetailMessage.msgFromId)))
                    .centerInside()
                    .placeholder(avatarResource)
                    .error(avatarResource)
                    .into(holder.privateMessageDetailSenderAvatar)
            holder.privateMessageDetailMessage.setTextColor(context.getColor(R.color.colorTextDefault))
            holder.privateMessageDetailMessage.setBackgroundColor(context.getColor(R.color.colorBackgroundSecondaryDefault))
            holder.privateMessageDetailSenderAvatar.visibility = View.VISIBLE
            holder.privateMessageDetailRecvAvatar.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return privateDetailMessageList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var privateMessageDetailTime: TextView
        var privateMessageDetailMessage: TextView
        var privateMessageDetailRecvAvatar: ImageView
        var privateMessageDetailSenderAvatar: ImageView
        var constraintLayout: ConstraintLayout

        init {
            privateMessageDetailTime = itemView.findViewById(R.id.item_private_message_detail_time)
            privateMessageDetailMessage = itemView.findViewById(R.id.item_private_message_detail_message)
            privateMessageDetailRecvAvatar = itemView.findViewById(R.id.item_private_message_detail_recv_avatar)
            privateMessageDetailSenderAvatar = itemView.findViewById(R.id.item_private_message_detail_sender_avatar)
            constraintLayout = itemView.findViewById(R.id.item_private_message_detail_constraint_layout)
        }
    }
}