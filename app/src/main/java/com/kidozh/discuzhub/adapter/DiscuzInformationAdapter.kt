package com.kidozh.discuzhub.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R

class DiscuzInformationAdapter(private val bbsInformationList: List<DiscuzInfoItem>) : RecyclerView.Adapter<DiscuzInformationAdapter.ViewHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val layoutIdForListItem = R.layout.item_bbs_information
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        val view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bbsInfo = bbsInformationList[position]

        holder.bbsInformationAvatar.setImageDrawable(bbsInfo.drawable)
        holder.bbsInformationTitle.text = bbsInfo.key
        if (bbsInfo.value == null || bbsInfo.value == "") {
            holder.bbsInformationValue.visibility = View.GONE
        } else {
            holder.bbsInformationValue.visibility = View.VISIBLE
        }
        holder.bbsInformationValue.text = bbsInfo.value
    }

    override fun getItemCount(): Int {
        return bbsInformationList.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bbsInformationAvatar: ImageView
        var bbsInformationTitle: TextView
        var bbsInformationValue: TextView

        init {
            bbsInformationAvatar = itemView.findViewById(R.id.item_bbs_information_avatar)
            bbsInformationTitle = itemView.findViewById(R.id.item_bbs_information_title)
            bbsInformationValue = itemView.findViewById(R.id.item_bbs_information_value)
        }
    }

    class DiscuzInfoItem(var drawable:Drawable?, var key: String, var value: String?){

    }
}