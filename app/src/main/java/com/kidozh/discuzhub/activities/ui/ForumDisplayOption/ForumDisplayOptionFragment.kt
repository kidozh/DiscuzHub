package com.kidozh.discuzhub.activities.ui.ForumDisplayOption

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kidozh.discuzhub.databinding.DialogForumDisplayOptionBinding
import com.kidozh.discuzhub.viewModels.ForumViewModel

class ForumDisplayOptionFragment : BottomSheetDialogFragment() {
    lateinit var binding: DialogForumDisplayOptionBinding
    lateinit var model:ForumViewModel;
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = super.onCreateDialog(savedInstanceState)
        binding = DialogForumDisplayOptionBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        return dialog

    }
}