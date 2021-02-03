package com.kidozh.discuzhub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.SmileyDatabase
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.utilities.ListItemClickListener
import com.kidozh.discuzhub.utilities.URLUtils
import java.util.*

/**
 * Created by free2 on 16-5-1.
 * 表情adapter
 */
class SmileyAdapter(private val context: Context, private val itemListener: ListItemClickListener) : RecyclerView.Adapter<SmileyAdapter.SmileyViewHolder>() {
    var smileyInfos: List<Smiley>? = null
        private set

    fun setSmileyInfos(smileyInfos: List<Smiley>) {
        this.smileyInfos = smileyInfos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmileyViewHolder {
        return SmileyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bbs_smiley, parent, false))
    }

    override fun onBindViewHolder(holder: SmileyViewHolder, position: Int) {
        val smileyInfo = smileyInfos!![position]
        Glide.with(context).load(URLUtils.getSmileyImageUrl(smileyInfo.imageRelativePath))
                .into(holder.image)
        holder.image.setOnClickListener { v: View? ->
            itemListener.onListItemClick(holder.image, position)
            // insert smiley into database
            Thread{
                val database = SmileyDatabase.getInstance(context)
                val dao = database.getDao()
                smileyInfo.updateAt = Date()
                dao.insert(smileyInfo)
            }.start()
        }
    }

    override fun getItemCount(): Int {
        return if (smileyInfos != null) {
            smileyInfos!!.size
        } else {
            0
        }
    }

    class SmileyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView

        init {
            image = itemView.findViewById(R.id.item_bbs_smiley_imageview)
        }
    }
}