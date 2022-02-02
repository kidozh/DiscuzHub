package com.kidozh.discuzhub.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.FullImageActivity
import com.kidozh.discuzhub.entities.Poll
import com.kidozh.discuzhub.utilities.URLUtils
import java.lang.String
import kotlin.Int

class PollOptionAdapter : RecyclerView.Adapter<PollOptionAdapter.ViewHolder>() {
    private val TAG = PollOptionAdapter::class.java.simpleName
    var pollOptions: MutableList<Poll.Option> = ArrayList()
    private var context: Context? = null
    @JvmName("setPollOptions1")
    fun setPollOptions(pollOptions: List<Poll.Option>) {
        val oldSize = this.pollOptions.size
        this.pollOptions.clear()
        notifyItemRangeRemoved(0, oldSize)
        this.pollOptions.addAll(pollOptions)
        notifyItemRangeInserted(0, pollOptions.size)
    }

    fun refreshOptionStatus(position: Int) {
        notifyItemChanged(position)
    }

    @JvmName("getPollOptions1")
    fun getPollOptions(): List<Poll.Option> {
        return pollOptions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_poll_option, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = pollOptions[position]
        holder.pollOptionName.text = option.name
        holder.pollOptionProgressBar.max = 100
        holder.pollOptionProgressBar.progress = option.percent.toInt()
        holder.pollOptionPosition.text = (position + 1).toString()
        //Resources res = context.getResources();
        //String votersNumberString = res.getQuantityString(R.plurals.poll_voter_number, option.voteNumber, option.voteNumber);
        holder.pollOptionVotePercent.text =
            context!!.getString(R.string.bbs_poll_vote_percent, String.valueOf(option.percent))
        holder.pollOptionVoteNumber.text = String.valueOf(option.voteNumber)
        var colorString = option.colorName
        if (!colorString!!.startsWith("#")) {
            colorString = "#$colorString"
        }
        Log.d(TAG, "color $colorString")
        val colorDrawable = ColorDrawable(Color.parseColor(colorString))
        holder.pollOptionProgressBar.progressTintList = ColorStateList.valueOf(colorDrawable.color)
        if (option.imageInfo == null) {

            //holder.pollOptionImage.setImageDrawable(colorDrawable);
            // holder.pollOptionProgressBar.setProgressDrawable(colorDrawable);
            holder.pollOptionWatchPicture.visibility = View.GONE
        } else {
            val imageUrl = URLUtils.getBaseUrl() + "/" + option.imageInfo!!.bigURL
            holder.pollOptionWatchPicture.visibility = View.VISIBLE
            holder.pollOptionWatchPicture.isClickable = true
            holder.pollOptionWatchPicture.setOnClickListener {
                val intent = Intent(context, FullImageActivity::class.java)
                intent.putExtra("URL", imageUrl)
                context!!.startActivity(intent)
            }
        }
        // check the vote status
        if (option.checked) {
            holder.pollOptionCheckIcon.setColorFilter(context!!.getColor(R.color.colorPrimary))
            holder.pollOptionCheckIcon.visibility = View.VISIBLE
        } else {
            holder.pollOptionCheckIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return pollOptions.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pollOptionCardview: CardView
        var pollOptionVoteNumber: TextView
        var pollOptionWatchPicture: Button
        var pollOptionName: TextView
        var pollOptionProgressBar: ProgressBar
        var pollOptionVotePercent: TextView
        var pollOptionCheckIcon: ImageView
        var pollOptionPosition: TextView

        init {
            pollOptionCardview = itemView.findViewById(R.id.item_poll_option_cardview)
            pollOptionVoteNumber = itemView.findViewById(R.id.item_poll_option_vote_number)
            pollOptionWatchPicture = itemView.findViewById(R.id.item_poll_option_watch_picture)
            pollOptionName = itemView.findViewById(R.id.item_poll_option_name)
            pollOptionProgressBar = itemView.findViewById(R.id.item_poll_option_vote_progressBar)
            pollOptionVotePercent = itemView.findViewById(R.id.item_poll_option_vote_percent)
            pollOptionCheckIcon = itemView.findViewById(R.id.item_poll_option_check_icon)
            pollOptionPosition = itemView.findViewById(R.id.item_poll_option_position)
        }
    }
}