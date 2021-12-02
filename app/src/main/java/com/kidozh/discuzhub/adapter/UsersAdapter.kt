package com.kidozh.discuzhub.adapter

import android.content.Context
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
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.NetworkUtils
import java.io.InputStream

class UsersAdapter(var context: Context, private val bbsInfo: Discuz) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    var userList: MutableList<User> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_forum_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userInfo = userList!![position]
        holder.mUserName.text = userInfo.username
        val factory = OkHttpUrlLoader.Factory(
            NetworkUtils.getPreferredClient(
                context
            )
        )
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        holder.mUserIdx.text = (position + 1).toString()
        Glide.with(context)
            .load(userInfo.avatarUrl)
            .error(R.drawable.avatar_person_male)
            .placeholder(R.drawable.avatar_person_male)
            .centerInside()
            .into(holder.mUserAvatar)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var mUserAvatar: ImageView
        var mUserName: TextView
        var mUserCardview: CardView
        var mUserIdx: TextView

        init {
            mUserAvatar = view.findViewById(R.id.forum_user_avatar)
            mUserName = view.findViewById(R.id.forum_user_name)
            mUserCardview = view.findViewById(R.id.forum_user_cardview)
            mUserIdx = view.findViewById(R.id.forum_user_index)
        }
    }
}