package com.kidozh.discuzhub.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kidozh.discuzhub.databinding.DialogAdminThreadBinding
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.viewModels.AdminThreadViewModel

class AdminThreadDialogFragment(thread: Thread): BottomSheetDialogFragment() {
    val TAG = AdminThreadDialogFragment::class.simpleName
    lateinit var binding: DialogAdminThreadBinding
    lateinit var viewModel: AdminThreadViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogAdminThreadBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        Log.d(TAG,"Create dialog")
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogAdminThreadBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
    }

    fun bindViewModel(){
        Log.d(TAG,"Bind view model")
        viewModel = ViewModelProvider(this)[AdminThreadViewModel::class.java]
        // bind data with model
        viewModel.adminStatusMutableLiveData.observe(viewLifecycleOwner, {
            if(it.pinnedLevel in 0..3){
                binding.pinSpinner.setSelection(it.pinnedLevel)
            }

            if(it.digestLevel in 0..3){
                binding.digestSpinner.setSelection(it.digestLevel)
            }

            binding.promoteCheckbox.isChecked = it.promote
            binding.pinCheckbox.isChecked = it.operatePin
            binding.digestCheckbox.isChecked = it.operateDigest
            if(it.operatePin){
                binding.pinSpinner.visibility = View.VISIBLE
            }
            else{
                binding.pinSpinner.visibility = View.GONE
            }

            if(it.operateDigest){
                binding.digestSpinner.visibility = View.VISIBLE
            }
            else{
                binding.digestSpinner.visibility = View.GONE
            }
        })
        // bind widget with data
        binding.pinCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
            adminStatus.operatePin = isChecked
            Log.d(TAG,"get pin status ${isChecked}")
            viewModel.adminStatusMutableLiveData.postValue(adminStatus)
        }


        binding.pinSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 in 0..3){
                    val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
                    adminStatus.pinnedLevel = p2
                    viewModel.adminStatusMutableLiveData.postValue(adminStatus)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
                adminStatus.pinnedLevel = 0
                viewModel.adminStatusMutableLiveData.postValue(adminStatus)
            }

        }

        binding.digestCheckbox.setOnCheckedChangeListener { _, b ->
            val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
            adminStatus.operateDigest = b
            viewModel.adminStatusMutableLiveData.postValue(adminStatus)

        }

        binding.digestSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 in 0..3){
                    val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
                    adminStatus.digestLevel = p2
                    viewModel.adminStatusMutableLiveData.postValue(adminStatus)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
                adminStatus.digestLevel = 0
                viewModel.adminStatusMutableLiveData.postValue(adminStatus)
            }

        }

        binding.promoteCheckbox.setOnCheckedChangeListener { _, b ->
            run{
                val adminStatus: AdminThreadViewModel.AdminStatus = viewModel.adminStatusMutableLiveData.value!!
                adminStatus.promote = b
                viewModel.adminStatusMutableLiveData.postValue(adminStatus)
            }
        }



    }

    fun bindSpinnerAndCheckBox(){

    }
}