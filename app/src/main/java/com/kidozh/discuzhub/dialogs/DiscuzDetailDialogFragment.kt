package com.kidozh.discuzhub.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.DiscuzInformationAdapter
import com.kidozh.discuzhub.databinding.DialogDiscuzDetailBinding
import com.kidozh.discuzhub.entities.Discuz
import kotlin.collections.ArrayList

class DiscuzDetailDialogFragment(bbs: Discuz) : BottomSheetDialogFragment() {
    val TAG = DiscuzDetailDialogFragment::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    val bbs: Discuz
    init {
        this.bbs = bbs
    }

    lateinit var binding: DialogDiscuzDetailBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogDiscuzDetailBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        configureRecyclerview()
        return dialog

    }

    fun configureRecyclerview(){
        // generate item
        val context = requireContext()
        var infoList = ArrayList<DiscuzInformationAdapter.DiscuzInfoItem>()
        infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                ContextCompat.getDrawable(context,R.drawable.ic_format_quote_white_24dp),
                bbs.site_name,
                getString(R.string.site_name_description),
        ))

        if(bbs.version.equals("5")){
            infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                    ContextCompat.getDrawable(context,R.drawable.ic_bbs_5_api_outlined_24dp),
                    getString(R.string.api_5_title),
                    getString(R.string.api_5_description),
            ))
        }
        else{
            infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                    ContextCompat.getDrawable(context,R.drawable.ic_suggestion_check_circle_outline_24px),
                    getString(R.string.api_4_title),
                    getString(R.string.api_4_description),
            ))
        }

        infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                ContextCompat.getDrawable(context,R.drawable.ic_baseline_settings_input_composite_24)?.apply {
                    setTint(context.getColor(R.color.MaterialColorOrange))
                },
                getString(R.string.discuz_version_title,bbs.discuz_version),
                getString(R.string.discuz_version_description),
        ))

        infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                ContextCompat.getDrawable(context,R.drawable.ic_baseline_settings_input_composite_24)?.apply {
                    setTint(context.getColor(R.color.MaterialColorBlue))
                },
                getString(R.string.discuz_plugin_version_title,bbs.plugin_version),
                getString(R.string.discuz_plugin_version_description),
        ))
        var charsetDescription = ""
        when (bbs.charset.toLowerCase()){
            "utf-8" -> charsetDescription = getString(R.string.discuz_charset_description_utf8)
            "gbk" -> charsetDescription = getString(R.string.discuz_charset_description_gbk)
            "big-5" -> charsetDescription = getString(R.string.discuz_charset_description_big5)
            else -> charsetDescription = getString(R.string.discuz_charset_unknown)
        }


        infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                ContextCompat.getDrawable(context,R.drawable.ic_baseline_emoji_symbols_24)?.apply {
                    setTint(context.getColor(R.color.MaterialColorDeepOrange))
                },
                getString(R.string.discuz_charset_title,bbs.charset),
                charsetDescription
        ))
        if(!bbs.hideRegister){
            infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                    ContextCompat.getDrawable(context,R.drawable.ic_baseline_how_to_reg_24),
                    getString(R.string.bbs_allow_register),
                    getString(R.string.discuz_mobile_allow_register)
            ))
        }

        if(bbs.qqConnect){
            infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                    ContextCompat.getDrawable(context,R.drawable.ic_suggestion_check_ok_circle_24px),
                    getString(R.string.bbs_qq_connect_ok),
                    getString(R.string.discuz_allow_qq)
            ))
        }

        infoList.add(DiscuzInformationAdapter.DiscuzInfoItem(
                ContextCompat.getDrawable(context,R.drawable.ic_forum_outlined_24px),
                bbs.site_name,
                getString(R.string.discuz_post_members,bbs.total_members,bbs.total_posts),
        ))

        val adapter: DiscuzInformationAdapter
        adapter = DiscuzInformationAdapter(infoList)
        binding.discuzDetailRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.discuzDetailRecyclerview.adapter = adapter


    }


}
