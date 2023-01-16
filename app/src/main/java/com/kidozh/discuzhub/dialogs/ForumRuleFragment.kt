package com.kidozh.discuzhub.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.databinding.DialogForumInformationBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.ForumResult
import com.kidozh.discuzhub.utilities.GlideImageGetter
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod
import com.kidozh.discuzhub.utilities.numberFormatUtils
import com.kidozh.discuzhub.viewModels.ForumViewModel

class ForumRuleFragment(val discuz: Discuz, val user: User?, val forum: Forum) : BottomSheetDialogFragment() ,
    bbsLinkMovementMethod.OnLinkClickedListener {

    val TAG = ForumDisplayOptionFragment::class.simpleName



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var forumViewModel: ForumViewModel;
    lateinit var binding: DialogForumInformationBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        forumViewModel = (activity?.run {
            ViewModelProvider(this)[ForumViewModel::class.java]
        }!!)
        binding = DialogForumInformationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        bindViewModel()
        renderForum()
        return dialog
    }

    fun renderForum(){
        binding.bbsForumThreadNumberTextview.text =
            numberFormatUtils.getShortNumberText(forum.threads)
        binding.bbsForumPostNumberTextview.text =
            numberFormatUtils.getShortNumberText(forum.posts)
    }

    fun bindViewModel(){
        forumViewModel.displayForumResultMutableLiveData.observe(
            this
        ) { result: ForumResult? ->
            if (result != null) {
                val forum = result.forumVariables.forum

                if (binding.bbsForumRuleTextview.text != forum.rules) {
                    val s = forum.rules
                    if (s != null && s.isNotEmpty()) {
                        val glideImageGetter =
                            user?.let { GlideImageGetter(binding.bbsForumRuleTextview, it) }
                        val htmlTagHandler = GlideImageGetter.HtmlTagHandler(
                            requireContext(),
                            binding.bbsForumRuleTextview
                        )
                        val sp = Html.fromHtml(
                            s,
                            HtmlCompat.FROM_HTML_MODE_COMPACT, glideImageGetter, htmlTagHandler
                        )
                        val spannableString = SpannableString(sp)
                        // binding.bbsForumAlertTextview.setAutoLinkMask(Linkify.ALL);
                        binding.bbsForumRuleTextview.movementMethod =
                            bbsLinkMovementMethod(this)
                        binding.bbsForumRuleTextview.setText(
                            spannableString,
                            TextView.BufferType.SPANNABLE
                        )
                        //collapseTextView(binding.bbsForumRuleTextview,3);
                    } else {
                        binding.bbsForumRuleTextview.setText(R.string.bbs_rule_not_set)
                        binding.bbsForumRuleTextview.visibility = View.GONE
                    }
                }


                // for description
                if (binding.bbsForumAlertTextview.text != forum.description) {
                    val s = forum.description
                    if (s != null && s.isNotEmpty()) {
                        val glideImageGetter =
                            user?.let { GlideImageGetter(binding.bbsForumAlertTextview, it) }
                        val htmlTagHandler = GlideImageGetter.HtmlTagHandler(
                            requireContext(),
                            binding.bbsForumRuleTextview
                        )
                        val sp = Html.fromHtml(
                            s,
                            HtmlCompat.FROM_HTML_MODE_COMPACT,
                            glideImageGetter,
                            htmlTagHandler
                        )
                        val spannableString = SpannableString(sp)
                        // binding.bbsForumAlertTextview.setAutoLinkMask(Linkify.ALL);
                        binding.bbsForumAlertTextview.movementMethod =
                            bbsLinkMovementMethod(this)
                        binding.bbsForumAlertTextview.setText(
                            spannableString,
                            TextView.BufferType.SPANNABLE
                        )
                    } else {
                        binding.bbsForumAlertTextview.setText(R.string.bbs_forum_description_not_set)
                        binding.bbsForumAlertTextview.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onLinkClicked(url: String): Boolean {
        bbsLinkMovementMethod.onLinkClicked(requireContext(), discuz, user,url)
        return true
    }

}