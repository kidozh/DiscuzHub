package com.kidozh.discuzhub.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.databinding.ItemAppThemeBinding
import com.kidozh.discuzhub.entities.AppTheme

class AppThemeAdapter: RecyclerView.Adapter<AppThemeAdapter.AppThemeViewHolder>() {
    val TAG = AppThemeAdapter::class.simpleName
    var appThemeList = ArrayList<AppTheme>()
    lateinit var context: Context
    var listener: OnThemeCardClicked? = null
    class AppThemeViewHolder : RecyclerView.ViewHolder {
        var binding : ItemAppThemeBinding
        constructor(binding: ItemAppThemeBinding) : super(binding.root) {
            this.binding = binding
        }

    }

    fun addAppTheme(appThemeList: ArrayList<AppTheme>){
        this.appThemeList = appThemeList
        notifyItemRangeInserted(0,appThemeList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppThemeViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        if(context is OnThemeCardClicked){
            listener = context as OnThemeCardClicked
        }
        return AppThemeViewHolder(ItemAppThemeBinding.inflate(inflater,parent,false))
    }

    override fun onBindViewHolder(holder: AppThemeViewHolder, position: Int) {
        val appTheme = appThemeList[position]
        Log.d(TAG,"GET theme position "+position)

        val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(appTheme.primaryColor,appTheme.primaryDarkColor,appTheme.accentColor)
        )
        holder.binding.filledLayout.background = gradientDrawable
        holder.binding.themeName.setText(appTheme.nameResource)

        if(listener != null){
            holder.binding.themeGradientCard.setOnClickListener { v->
                listener!!.onThemeCardSelected(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return appThemeList.size
    }

    interface OnThemeCardClicked{
        fun onThemeCardSelected(position: Int)
    }
}