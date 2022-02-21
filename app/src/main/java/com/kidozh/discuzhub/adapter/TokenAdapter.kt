package com.kidozh.discuzhub.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.databinding.ItemPushTokenBinding
import com.kidozh.discuzhub.results.TokenResult
import com.kidozh.discuzhub.utilities.TimeDisplayUtils
import java.util.*

class TokenAdapter: RecyclerView.Adapter<TokenAdapter.TokenViewHolder>() {
    final val TAG = TokenAdapter::class.simpleName
    lateinit var context: Context
    var tokenList: List<TokenResult.NotificationToken> = ArrayList()
    set(value) {
        val result = DiffUtil.calculateDiff(
            TokenResult.NotificationToken.Companion.TokenDiffCallback(
                field,
                value
            )
        )
        field = value
        result.dispatchUpdatesTo(this)
    }

    var currentDeviceToken = ""
    set(value) {
        field = value
        notifyItemRangeChanged(0, tokenList.size)
    }

    class TokenViewHolder(val binding: ItemPushTokenBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return TokenViewHolder(ItemPushTokenBinding.inflate(inflater,parent, false))
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val token = tokenList[position]
        holder.binding.deviceName.text = token.deviceName
        holder.binding.updateTime.text = TimeDisplayUtils.getLocalePastTimeString(context, Date(token.updateAt*1000))
        holder.binding.deviceIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_phone_android_24))
        Log.d(TAG,"${token.token == currentDeviceToken} -> ${token.token} and current ${currentDeviceToken} ")
        if(token.token == currentDeviceToken){
            holder.binding.deviceIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_check_24px))
            //holder.binding.tokenCard.setCardBackgroundColor()
            //holder.binding.deviceName.setTextColor(R.attr.colorControlNormal)
        }
        else{
            //holder.binding.tokenCard.setCardBackgroundColor(R.attr.colorBackgroundFloating)
            //holder.binding.deviceName.setTextColor(R.attr.colorControlNormal)
        }
        when(token.channel){
            "FCM" -> holder.binding.deviceChannel.text = context.getString(R.string.dhpush_channel_fcm)
            "APN" -> holder.binding.deviceChannel.text = context.getString(R.string.dhpush_channel_apns)
            "MI" -> holder.binding.deviceChannel.text = context.getString(R.string.dhpush_channel_mi)
            "HUAWEI" -> holder.binding.deviceChannel.text = context.getString(R.string.dhpush_channel_huawei)
            else -> holder.binding.deviceChannel.text = context.getString(R.string.dhpush_channel_unknown)
        }
        if(token.packageId.startsWith("com.kidozh.discuzhub")){
            holder.binding.devicePackage.text = context.getString(R.string.app_name)
        }
        else if(token.packageId.startsWith("com.kidozh.discuz-flutter")){

        }
    }

    override fun getItemCount(): Int {
        return tokenList.size
    }
}