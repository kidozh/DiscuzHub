package com.kidozh.discuzhub.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.OnThreadAdmined
import com.kidozh.discuzhub.databinding.DialogAdminThreadBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.VibrateUtils
import com.kidozh.discuzhub.viewModels.AdminThreadViewModel
import es.dmoral.toasty.Toasty

class AdminThreadDialogFragment(var discuz: Discuz,
                                var user: User, var fid: Int, var thread: Thread, var formhash: String
): BottomSheetDialogFragment() {
    val TAG = AdminThreadDialogFragment::class.simpleName
    lateinit var binding: DialogAdminThreadBinding
    lateinit var viewModel: AdminThreadViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogAdminThreadBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setTitle(getString(R.string.admin_thread_title, thread.subject))
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
        bindSubmitBotton()
    }

    fun bindViewModel(){
        Log.d(TAG,"Bind view model")
        viewModel = ViewModelProvider(this)[AdminThreadViewModel::class.java]
        viewModel.initParameter(discuz, user, fid, thread, formhash)
        viewModel.loadingStatusMutableLiveData.observe(viewLifecycleOwner,{
            binding.adminThreadButton.isEnabled = !it
            if(it){
                binding.adminNetworkProgressbar.visibility = View.VISIBLE
            }
            else{
                binding.adminNetworkProgressbar.visibility = View.GONE
            }
        })

        // bind data with model
        viewModel.adminStatusMutableLiveData.observe(viewLifecycleOwner, {
            if(it.operateDigest || it.operatePin || it.promote){
                binding.adminThreadButton.visibility = View.VISIBLE
            }
            else{
                binding.adminThreadButton.visibility = View.GONE
            }

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

        viewModel.returnedMessage.observe(viewLifecycleOwner,{
            if(it!= null){
                if(it.key == "admin_succeed"){
                    VibrateUtils.vibrateSlightly(requireContext())
                    Toasty.success(requireContext(),getString(R.string.discuz_api_message_template, it.key, it.content), Toast.LENGTH_LONG).show()
                    if (context is OnThreadAdmined && viewModel.adminStatusMutableLiveData.value!= null){
                        val mlistener: OnThreadAdmined = context as OnThreadAdmined
                        mlistener.onThreadSuccessfullyAdmined(thread, viewModel.adminStatusMutableLiveData.value!!)
                    }
                }
                else{
                    VibrateUtils.vibrateForError(requireContext())
                    Toasty.error(requireContext(),getString(R.string.discuz_api_message_template, it.key, it.content), Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.networkError.observe(viewLifecycleOwner,{
            if(it == true){
                Toasty.error(requireContext(),getString(R.string.network_failed), Toast.LENGTH_LONG).show()
            }
        })

        binding.adminThreadReason.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0!=null){
                    viewModel.reasonMutableLiveData.postValue(p0.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        viewModel.reasonMutableLiveData.observe(viewLifecycleOwner,{
            if(it.isNullOrEmpty()){
                binding.deleteThreadButton.visibility = View.GONE
            }
            else{
                binding.deleteThreadButton.visibility = View.VISIBLE
            }
        })



    }

    fun bindSubmitBotton(){
        binding.dialogAdminTitle.setText(getString(R.string.admin_thread_title, thread.subject))
        binding.adminThreadButton.setOnClickListener {
            VibrateUtils.vibrateForClick(requireContext())
            viewModel.adminThread()
        }
        binding.deleteThreadButton.setOnClickListener {
            VibrateUtils.vibrateForClick(requireContext())
            viewModel.deleteThread()
        }
    }
}