package com.kidozh.discuzhub.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.OnPostAdmined
import com.kidozh.discuzhub.databinding.DialogAdminPostBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Post
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.VibrateUtils
import com.kidozh.discuzhub.viewModels.AdminPostViewModel
import es.dmoral.toasty.Toasty


class AdminPostDialogFragment(val discuz: Discuz, val user: User, val fid: Int, val tid: Int, val post: Post, val formhash: String): BottomSheetDialogFragment() {
    lateinit var binding: DialogAdminPostBinding
    val TAG = AdminPostDialogFragment::class.simpleName
    lateinit var viewModel: AdminPostViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogAdminPostBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setTitle(getString(R.string.admin_post, post.author))
        Log.d(TAG,"Create dialog")
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogAdminPostBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
        bindButton()
    }

    fun bindViewModel(){
        binding.dialogAdminPostTitle.text = getString(R.string.admin_post, post.author)
        viewModel = ViewModelProvider(this)[AdminPostViewModel::class.java]
        viewModel.initPostStatus(discuz,user,post,fid,tid,formhash)

        viewModel.blockState.observe(viewLifecycleOwner,{
            if(it){
                binding.blockPostButton.text = getString(R.string.unblock_post)
            }
            else{
                binding.blockPostButton.text = getString(R.string.block_post)
            }
        })

        viewModel.warnState.observe(viewLifecycleOwner,{
            if(it){
                binding.warnPostButton.text = getString(R.string.undo_warn_post)
            }
            else{
                binding.warnPostButton.text = getString(R.string.warn_post)
            }
        })

        viewModel.stickState.observe(viewLifecycleOwner,{
            if(it){
                binding.stickPostButton.text = getString(R.string.unstick_post)
            }
            else{
                binding.stickPostButton.text = getString(R.string.stick_post)
            }
        })

        viewModel.sendingBlockRequest.observe(viewLifecycleOwner,{
            binding.blockPostButton.isEnabled = !it
        })

        viewModel.sendingWarnRequest.observe(viewLifecycleOwner,{
            binding.warnPostButton.isEnabled = !it
        })

        viewModel.sendingStickRequest.observe(viewLifecycleOwner,{
            binding.stickPostButton.isEnabled = !it
        })

        viewModel.sendingDeleteRequest.observe(viewLifecycleOwner,{
            binding.deletePostButton.isEnabled = !it
        })

        viewModel.returnedMessage.observe(viewLifecycleOwner,{
            if(it!= null){
                if(it.key == "admin_succeed"){
                    VibrateUtils.vibrateSlightly(requireContext())
                    Toasty.success(requireContext(),getString(R.string.discuz_api_message_template, it.key, it.content), Toast.LENGTH_LONG).show()
                    // callback to activity to change item

                }
                else{
                    VibrateUtils.vibrateForError(requireContext())
                    Toasty.error(requireContext(),getString(R.string.discuz_api_message_template, it.key, it.content), Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.newPostMutableLiveData.observe(viewLifecycleOwner,{
            if(context is OnPostAdmined){
                val listener : OnPostAdmined = context as OnPostAdmined

                listener.onPostSuccessfullyAdmined(it)
            }
        })
    }

    private fun bindButton(){
        binding.adminPostReason.addTextChangedListener(object : TextWatcher{
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

        binding.warnPostButton.setOnClickListener {
            viewModel.sendWarnRequest()
        }

        binding.blockPostButton.setOnClickListener {
            viewModel.sendBlockRequest()
        }

        binding.stickPostButton.setOnClickListener {
            viewModel.sendStickRequest()
        }

        binding.deletePostButton.setOnClickListener {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.apply {
                setTitle(R.string.delete_post)
                setMessage(R.string.delete_post_description)
                setPositiveButton(R.string.delete_post) { _, _ ->
                    viewModel.deleteStickRequest()
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    dismiss()
                }
            }
            alertDialogBuilder.create().show()

        }
    }
}