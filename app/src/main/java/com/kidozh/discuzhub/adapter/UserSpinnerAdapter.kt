package com.kidozh.discuzhub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.databinding.SpinnerItemUserBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User

class UserSpinnerAdapter(val discuz: Discuz) : BaseAdapter() {
    var userList : List<User>



    lateinit var context: Context

    init {
        userList = emptyList<User>()
    }

    @JvmName("setUserList1")
    fun setUserList(users : List<User>){
        userList = users
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return userList.size;
    }

    override fun getItem(position: Int): Any {
        return userList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return userList.get(position).id.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (parent != null) {
            context = parent.context
        }
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val binding: SpinnerItemUserBinding = SpinnerItemUserBinding.inflate(inflater)
        val user = userList[position]
        binding.name.text = user.username
        Glide.with(binding.root)
                .load(discuz.getAvatarUrl(user.uid))
                .error(R.drawable.avatar_1)
                .into(binding.avatar)
        return binding.root

    }

}