package com.kidozh.discuzhub.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.databinding.DialogForumDisplayOptionBinding
import com.kidozh.discuzhub.viewModels.ForumViewModel
import es.dmoral.toasty.Toasty

class ForumDisplayOptionFragment : BottomSheetDialogFragment() {
    val TAG = ForumDisplayOptionFragment::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var binding: DialogForumDisplayOptionBinding
    lateinit var model:ForumViewModel;
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogForumDisplayOptionBinding.inflate(layoutInflater)
        model = (activity?.run {
            ViewModelProvider(this)[ForumViewModel::class.java]
        }?:throw Exception("Invalid activity"))
        dialog.setContentView(binding.root)
        bindViewModel()
        listenChipGroup()
        configureButton()
        return dialog

    }

    fun bindViewModel(){
        model.forumStatusMutableLiveData.observe(this, Observer { status ->
            // render thread category
            if (status.filterTypeId == -1) {
                binding.categoryGroup.clearCheck()
            } else {
                for (i in 0 until binding.categoryGroup.getChildCount()) {
                    val chip:Chip = binding.categoryGroup.getChildAt(i) as Chip
                    if(chip.id == status.filterTypeId){
                        chip.isChecked = true
                    }
                }
            }

            if(status.orderBy.equals("")){
                binding.sortKeyGroup.clearCheck()
            }
            else{
                when(status.orderBy){
                    "lastpost" -> binding.sortKeyReply.isChecked = true
                    "dateline" -> binding.sortKeyPublish.isChecked = true
                    "views" -> binding.sortKeyView.isChecked = true
                    "heats" -> binding.sortKeyHeat.isChecked = true
                }
            }
            when(status.specialType){
                "poll" -> binding.typePoll.isChecked = true
                "reward" -> binding.typeReward.isChecked = true
                "activity" -> binding.typeActivity.isChecked = true
                "debate" -> binding.typeDebate.isChecked = true
                else -> binding.typeGroup.clearCheck()
            }

            when(status.filter){
                "digest" -> binding.statusDigest.isChecked = true
                "hot" -> binding.statusHot.isChecked = true
                else -> binding.statusGroup.clearCheck()
            }

            when(status.dateline){
                86400 -> binding.timeFilterToday.isChecked = true
                604800 -> binding.timeFilterWeek.isChecked = true
                2592000 -> binding.timeFilterMonth.isChecked = true
                7948800 -> binding.timeFilterQuarter.isChecked = true
                31536000 -> binding.timeFilterYear.isChecked = true
                else -> binding.timeFilterGroup.clearCheck()
            }

        })



        model.displayForumResultMutableLiveData.observe(this, Observer { result ->
            if (result != null) {
                val threadCategory = result.forumVariables.threadTypeInfo
                // parse it

                if(threadCategory==null || threadCategory.idNameMap == null){
                    binding.categoryGroup.visibility = View.GONE
                    binding.categoryText.visibility = View.GONE
                    return@Observer
                }
                else{
                    binding.categoryGroup.visibility = View.VISIBLE
                    binding.categoryText.visibility = View.VISIBLE
                }
                val threadTypeMap = threadCategory.idNameMap
                binding.categoryGroup.removeAllViews()
                Log.d(TAG, "Get thread category type " + threadTypeMap.keys)

                for (key in threadTypeMap.keys) {
                    // add to chip
                    Log.d(TAG, "GET key " + key)
                    val name = threadTypeMap.get(key)
                    var chip = Chip(context)
                    chip.text = name
                    chip.isCheckable = true
                    chip.id = key.toInt()
                    binding.categoryGroup.addView(chip)

                }
                // reactive it
                val status = model.forumStatusMutableLiveData.value
                if (status != null) {
                    if (status.filterTypeId == -1) {
                        binding.categoryGroup.clearCheck()
                    } else {
                        for (i in 0 until binding.categoryGroup.getChildCount()) {
                            val chip: Chip = binding.categoryGroup.getChildAt(i) as Chip
                            if (chip.id == status.filterTypeId) {
                                chip.isChecked = true
                            }
                        }
                    }
                }
            }
        })

    }

    fun listenChipGroup(){
        binding.categoryGroup.setOnCheckedChangeListener { _, checkedId ->
            var status = model.forumStatusMutableLiveData.value
            Log.d(TAG,"GET category checked id "+checkedId)
            if(status != null){
                when(checkedId){
                    // clear category option
                    0 -> status.filterTypeId = 0
                    else -> status.filterTypeId = checkedId
                }
            }
            model.forumStatusMutableLiveData.postValue(status)
        }

        binding.sortKeyGroup.setOnCheckedChangeListener { _, checkedId ->
            var status = model.forumStatusMutableLiveData.value
            if(status != null){
                when(checkedId){
                    // clear category option
                    R.id.sort_key_reply -> status.orderBy = "lastpost"
                    R.id.sort_key_publish -> status.orderBy = "dateline"
                    R.id.sort_key_view -> status.orderBy = "views"
                    R.id.sort_key_heat -> status.orderBy = "heats"
                    else -> status.orderBy = ""
                }
                model.forumStatusMutableLiveData.postValue(status)
            }
        }

        binding.typeGroup.setOnCheckedChangeListener{ _, checkedId ->
            var status = model.forumStatusMutableLiveData.value
            if(status != null){
                when(checkedId){
                    // clear category option
                    R.id.type_poll -> status.specialType = "poll"
                    R.id.type_reward -> status.specialType = "reward"
                    R.id.type_activity -> status.specialType = "activity"
                    R.id.type_debate -> status.specialType = "debate"
                    else -> status.specialType = ""
                }
                model.forumStatusMutableLiveData.postValue(status)
            }
        }

        binding.statusGroup.setOnCheckedChangeListener { group, checkedId ->
            var status = model.forumStatusMutableLiveData.value
            if(status != null){
                when(checkedId){
                    // clear category option
                    R.id.status_hot -> status.filter = "hot"
                    R.id.status_digest -> status.filter = "digest"
                    else -> status.filter = ""
                }
                model.forumStatusMutableLiveData.postValue(status)
            }

        }

        binding.timeFilterGroup.setOnCheckedChangeListener { group, checkedId ->
            var status = model.forumStatusMutableLiveData.value
            if(status != null){
                when(checkedId){
                    // clear category option

                    R.id.time_filter_today -> status.dateline = 86400
                    R.id.time_filter_week -> status.dateline = 604800
                    R.id.time_filter_month -> status.dateline = 2592000
                    R.id.time_filter_quarter -> status.dateline = 7948800
                    R.id.time_filter_year -> status.dateline = 31536000
                    else -> status.dateline = 0
                }
                model.forumStatusMutableLiveData.postValue(status)
            }

        }
    }

    fun configureButton(){
        binding.okButton.setOnClickListener { v->
            var status = model.forumStatusMutableLiveData.value
            if(status != null){
                status.hasLoadAll = false
                status.page = 1
                model.forumStatusMutableLiveData.postValue(status)
                model.setForumStatusAndFetchThread(model.forumStatusMutableLiveData.getValue())
                dismiss()
            }
        }
        binding.resetButton.setOnClickListener { v->
            binding.categoryGroup.clearCheck()
            binding.sortKeyGroup.clearCheck()
            binding.typeGroup.clearCheck()
            binding.statusGroup.clearCheck()
            binding.timeFilterGroup.clearCheck()

            context?.let { Toasty.normal(it, getString(R.string.reset_all_option), Toast.LENGTH_SHORT).show() }
        }
    }


}
