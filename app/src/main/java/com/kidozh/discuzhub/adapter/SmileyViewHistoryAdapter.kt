package com.kidozh.discuzhub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.SmileyDatabase
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.utilities.ListItemClickListener
import com.kidozh.discuzhub.utilities.URLUtils
import java.util.*

class SmileyViewHistoryAdapter: PagedListAdapter<Smiley, SmileyViewHistoryAdapter.SmileyViewHolder>(Smiley.DIFF_CALLBACK) {

    lateinit var context: Context
    private var itemListener: ListItemClickListener? = null

    class SmileyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView

        init {
            image = itemView.findViewById(R.id.item_bbs_smiley_imageview)
            // image.setOnClickListener { view: View? -> itemListener.onListItemClick(image, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmileyViewHolder {
        context = parent.context
        if(context is ListItemClickListener){
            itemListener = context as ListItemClickListener
        }
        return SmileyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bbs_smiley, parent, false))
    }

    override fun onBindViewHolder(holder: SmileyViewHolder, position: Int) {
        val smiley = getItem(position)
        if(smiley != null){
            Glide.with(context).load(URLUtils.getSmileyImageUrl(smiley.imageRelativePath))
                    .into(holder.image)
            holder.image.setOnClickListener { v: View? ->
                itemListener?.onListItemClick(holder.image, position)
                Thread{
                    val database = SmileyDatabase.getInstance(context)
                    val dao = database.getDao()
                    smiley.updateAt = Date()
                    dao.insert(smiley)
                }.start()
            }
        }
    }



}