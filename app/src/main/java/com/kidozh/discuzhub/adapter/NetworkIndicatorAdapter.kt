package com.kidozh.discuzhub.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorFailedBinding
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorLoadAllBinding
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorLoadSuccessfullyBinding
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorLoadingBinding

class NetworkIndicatorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var networkStatus = 0
    private var context: Context? = null
    private var errorMessage: ErrorMessage? = null
    private var mListener: OnRefreshBtnListener? = null
    public var successPageShown = true
    override fun getItemId(position: Int): Long {
        return networkStatus.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return when (networkStatus) {
            ConstUtils.NETWORK_STATUS_LOADING -> {
                R.layout.item_network_indicator_loading
            }
            ConstUtils.NETWORK_STATUS_LOADED_ALL -> {
                R.layout.item_network_indicator_load_all
            }
            ConstUtils.NETWORK_STATUS_FAILED -> {
                R.layout.item_network_indicator_failed
            }
            else -> {
                0
            }
        }
    }

    fun setLoadingStatus() {
        if (networkStatus == ConstUtils.NETWORK_STATUS_SUCCESSFULLY) {
            networkStatus = ConstUtils.NETWORK_STATUS_LOADING
            notifyItemChanged(0)
        } else {
            networkStatus = ConstUtils.NETWORK_STATUS_LOADING
            notifyItemChanged(0)
        }
    }

    fun setLoadSuccessfulStatus() {
        networkStatus = ConstUtils.NETWORK_STATUS_SUCCESSFULLY
        notifyItemChanged(0)
    }

    fun setLoadedAllStatus() {
        if (networkStatus == ConstUtils.NETWORK_STATUS_SUCCESSFULLY) {
            networkStatus = ConstUtils.NETWORK_STATUS_LOADED_ALL
            notifyItemChanged(0)
        } else {
            networkStatus = ConstUtils.NETWORK_STATUS_LOADED_ALL
            notifyItemChanged(0)
        }
    }

    fun setErrorStatus(errorMessage: ErrorMessage) {
        if (networkStatus == ConstUtils.NETWORK_STATUS_SUCCESSFULLY) {
            networkStatus = ConstUtils.NETWORK_STATUS_FAILED
            this.errorMessage = errorMessage
            notifyItemChanged(0)
        } else {
            networkStatus = ConstUtils.NETWORK_STATUS_FAILED
            this.errorMessage = errorMessage
            notifyItemChanged(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        if (context is OnRefreshBtnListener) {
            mListener = context as OnRefreshBtnListener?
        }
        val layoutInflater = LayoutInflater.from(context)
        when (networkStatus) {
            ConstUtils.NETWORK_STATUS_LOADING -> {
                val binding = ItemNetworkIndicatorLoadingBinding.inflate(layoutInflater, parent, false)
                return NetworkIndicatorLoadingViewHolder(binding)
            }
            ConstUtils.NETWORK_STATUS_LOADED_ALL -> {
                val binding = ItemNetworkIndicatorLoadAllBinding.inflate(layoutInflater, parent, false)
                return NetworkIndicatorLoadAllViewHolder(binding)
            }
            ConstUtils.NETWORK_STATUS_SUCCESSFULLY -> {
                val binding = ItemNetworkIndicatorLoadSuccessfullyBinding.inflate(layoutInflater, parent, false)
                if(!successPageShown){
                    binding.loadAllLayout.visibility = View.GONE
                }
                else{
                    binding.loadAllLayout.visibility = View.VISIBLE
                }
                return NetworkIndicatorLoadSuccessfullyViewHolder(binding)
            }
            ConstUtils.NETWORK_STATUS_FAILED -> {
                val binding = ItemNetworkIndicatorFailedBinding.inflate(layoutInflater, parent, false)
                return NetworkIndicatorLoadFailedViewHolder(binding)
            }
        }
        val binding = ItemNetworkIndicatorLoadingBinding.inflate(layoutInflater, parent, false)
        return NetworkIndicatorLoadingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NetworkIndicatorLoadFailedViewHolder && networkStatus == ConstUtils.NETWORK_STATUS_FAILED) {
            holder.binding.errorValue.text = errorMessage!!.key
            holder.binding.errorContent.text = errorMessage!!.content
            if (errorMessage!!.errorIconResource != 0) {
                holder.binding.errorIcon.setImageResource(errorMessage!!.errorIconResource)
            } else {
                holder.binding.errorIcon.setImageResource(ErrorMessage.getDefaultErrorIconResource())
            }
            holder.binding.retryButton.setOnClickListener { v: View? ->
                if (mListener != null) {
                    mListener!!.onRefreshBtnClicked()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    class NetworkIndicatorLoadingViewHolder internal constructor(var binding: ItemNetworkIndicatorLoadingBinding) : RecyclerView.ViewHolder(binding.root)
    class NetworkIndicatorLoadAllViewHolder internal constructor(var binding: ItemNetworkIndicatorLoadAllBinding) : RecyclerView.ViewHolder(binding.root)
    class NetworkIndicatorLoadFailedViewHolder internal constructor(var binding: ItemNetworkIndicatorFailedBinding) : RecyclerView.ViewHolder(binding.root)
    class NetworkIndicatorLoadSuccessfullyViewHolder internal constructor(var binding: ItemNetworkIndicatorLoadSuccessfullyBinding) : RecyclerView.ViewHolder(binding.root)
    interface OnRefreshBtnListener {
        fun onRefreshBtnClicked()
    }

    init {
        setHasStableIds(true)
    }
}