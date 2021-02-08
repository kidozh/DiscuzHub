package com.kidozh.discuzhub.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.SmileyDatabase
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.utilities.ListItemClickListener
import com.kidozh.discuzhub.utilities.URLUtils
import java.util.*

/**
 * Created by free2 on 16-5-1.
 * 表情adapter
 */
class SmileyAdapter(private val context: Context, private val discuz: Discuz, private val itemListener: ListItemClickListener) : RecyclerView.Adapter<SmileyAdapter.SmileyViewHolder>() {
    val TAG = SmileyAdapter::class.simpleName
    var smileys: List<Smiley>? = null

    fun setsmileys(smileys: List<Smiley>) {
        this.smileys = smileys
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmileyViewHolder {
        return SmileyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bbs_smiley, parent, false))
    }

    override fun onBindViewHolder(holder: SmileyViewHolder, position: Int) {
        val smiley = smileys!![position]
        Glide.with(context).load(URLUtils.getSmileyImageUrl(smiley.imageRelativePath))
                .into(holder.image)
        holder.image.setOnClickListener {
            itemListener.onListItemClick(holder.image, position)
            // insert smiley into database
            Thread{
                val database = SmileyDatabase.getInstance(context)
                val dao = database.getDao()
                Log.d(TAG,"GET simley id "+smiley.id+" CODE "+ smiley.code)
                val savedSmiley = dao.simleybyCode(smiley.code)
                if(savedSmiley == null){
                    smiley.updateAt = Date()
                    smiley.discuzId = discuz.id
                    val insertedId = dao.insert(smiley)
                    Log.d(TAG, "GET inserted id $insertedId")
                }
                else{
                    savedSmiley.updateAt = Date()
                    dao.update(savedSmiley)
                }

            }.start()
        }
    }

    override fun getItemCount(): Int {
        return if (smileys != null) {
            smileys!!.size
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