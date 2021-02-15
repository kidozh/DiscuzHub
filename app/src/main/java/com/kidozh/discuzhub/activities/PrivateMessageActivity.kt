package com.kidozh.discuzhub.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment.Companion.newInstance
import com.kidozh.discuzhub.adapter.PrivateDetailMessageAdapter
import com.kidozh.discuzhub.adapter.SmileyViewPagerAdapter
import com.kidozh.discuzhub.databinding.ActivityBbsPrivateMessageDetailBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.*
import com.kidozh.discuzhub.utilities.bbsParseUtils.privateMessage
import com.kidozh.discuzhub.viewModels.PrivateMessageViewModel
import com.kidozh.discuzhub.viewModels.SmileyViewModel
import es.dmoral.toasty.Toasty
import okhttp3.*
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class PrivateMessageActivity : BaseStatusActivity(), SmileyFragment.OnSmileyPressedInteraction {
    lateinit var privateMessageInfo: privateMessage
    lateinit var adapter: PrivateDetailMessageAdapter
    
    private var smileyPicker: SmileyPicker? = null
    private var handler: EmotionInputHandler? = null
    lateinit var binding: ActivityBbsPrivateMessageDetailBinding
    lateinit var model : SmileyViewModel
    lateinit var smileyViewPagerAdapter : SmileyViewPagerAdapter
    lateinit var privateMessageViewModel: PrivateMessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBbsPrivateMessageDetailBinding.inflate(layoutInflater)
        model = ViewModelProvider(this).get(SmileyViewModel::class.java)
        privateMessageViewModel = ViewModelProvider(this).get(PrivateMessageViewModel::class.java)
        setContentView(binding.root)
        configureIntent()
        bindViewModel()
        configureActionBar()
        configureSmileyLayout()
        configureRecyclerview()
        configureSwipeLayout()
        configureSendBtn()
    }

    private fun configureSmileyLayout() {
        handler = EmotionInputHandler(binding.bbsPrivateMessageCommentEditText) { enable: Boolean, s: String? -> }
        smileyPicker = SmileyPicker(this, bbsInfo)
        smileyPicker!!.setListener { str: String?, a: Drawable? -> handler!!.insertSmiley(str, a) }
        binding.bbsPrivateMessageCommentSmileyTabLayout.setupWithViewPager(binding.bbsPrivateMessageCommentSmileyViewPager)
        binding.bbsPrivateMessageCommentSmileyViewPager.adapter = smileyViewPagerAdapter
    }

    private fun configureSwipeLayout() {
        binding.bbsPrivateMessageDetailSwipeRefreshLayout.setOnRefreshListener {
            if(privateMessageViewModel.networkState.value == ConstUtils.NETWORK_STATUS_LOADED_ALL||
                    privateMessageViewModel.networkState.value == ConstUtils.NETWORK_STATUS_LOADING){
                binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false
            }
            else{
                privateMessageViewModel.queryPrivateMessage()
            }


        }
    }

    private fun configureRecyclerview() {
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        binding.bbsPrivateMessageDetailRecyclerview.layoutManager = linearLayoutManager
        adapter = PrivateDetailMessageAdapter(bbsInfo!!, user)
        binding.bbsPrivateMessageDetailRecyclerview.adapter = adapter
        privateMessageViewModel.queryPrivateMessage()
    }

    private fun configureSendBtn() {
        binding.bbsPrivateMessageCommentButton.setOnClickListener {
            val sendMessage = binding.bbsPrivateMessageCommentEditText.text.toString()
            if (sendMessage.length != 0) {
                var message = binding.bbsPrivateMessageCommentEditText.text.toString()
                when(charsetType){
                    CHARSET_GBK ->{
                        message = URLEncoder.encode(message,"GBK")
                    }
                    CHARSET_BIG5 ->{
                        message = URLEncoder.encode(message,"BIG5")
                    }
                    else->{
                    }
                }

                privateMessageViewModel.sendPrivateMessage(privateMessageInfo.plid, privateMessageInfo.pmid,message)

            } else {
                Toasty.warning(application, getString(R.string.bbs_pm_is_required), Toast.LENGTH_SHORT).show()
            }
        }
        binding.bbsPrivateMessageCommentEmoij.setOnClickListener {
            if (binding.bbsPrivateMessageCommentSmileyConstraintLayout.visibility == View.GONE) {
                // smiley picker not visible
                binding.bbsPrivateMessageCommentEmoij.setImageDrawable(getDrawable(R.drawable.vector_drawable_keyboard_24px))
                binding.bbsPrivateMessageCommentEditText.clearFocus()
                // close keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bbsPrivateMessageCommentEditText.windowToken, 0)
                binding.bbsPrivateMessageCommentSmileyConstraintLayout.visibility = View.VISIBLE

                // tab layout binding...
                querySmileyInfo()
            } else {
                binding.bbsPrivateMessageCommentSmileyConstraintLayout.visibility = View.GONE
                binding.bbsPrivateMessageCommentEmoij.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp))
            }
        }
        binding.bbsPrivateMessageCommentEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && binding.bbsPrivateMessageCommentSmileyConstraintLayout.visibility == View.VISIBLE) {
                binding.bbsPrivateMessageCommentSmileyConstraintLayout.visibility = View.GONE
                binding.bbsPrivateMessageCommentEmoij.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp))
            }
        }
    }

    private fun bindViewModel(){
        model.smileyResultLiveData.observe(this,  { it->
            if(it != null){
                val smileyList = it.variables.smileyList
                smileyViewPagerAdapter.smileyList = smileyList
                binding.bbsPrivateMessageCommentSmileyTabLayout.getTabAt(0)?.icon = ContextCompat.getDrawable(this,R.drawable.ic_baseline_history_24)
            }
        })

        privateMessageViewModel.totalPrivateMessageListMutableLiveData.observe(this, {
            adapter.privateDetailMessageList = it
        })

        privateMessageViewModel.networkState.observe(this,{
            when(it){
                ConstUtils.NETWORK_STATUS_LOADING ->{
                    binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = true
                }
                ConstUtils.NETWORK_STATUS_LOADED_ALL ->{
                    binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false
                    binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false
                }
                else->{
                    binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false
                }
            }
        })

        privateMessageViewModel.sendNetworkState.observe(this,{
            when(it){
                ConstUtils.NETWORK_STATUS_LOADING->binding.bbsPrivateMessageCommentButton.isEnabled = false
                ConstUtils.NETWORK_STATUS_SUCCESSFULLY -> {
                    binding.bbsPrivateMessageCommentButton.isEnabled = true
                    binding.bbsPrivateMessageCommentEditText.text.clear()
                    val message = privateMessageViewModel.errorMessageMutableLiveData.value
                    if(message != null){
                        Toasty.success(this,getString(R.string.discuz_api_message_template,message.key,message.content)).show()
                    }

                }
                ConstUtils.NETWORK_STATUS_FAILED -> {
                    binding.bbsPrivateMessageCommentButton.isEnabled = true
                    val message = privateMessageViewModel.errorMessageMutableLiveData.value
                    if(message != null){
                        Toasty.error(this,getString(R.string.discuz_api_message_template,message.key,message.content)).show()
                    }

                }
                else->{
                    binding.bbsPrivateMessageCommentButton.isEnabled = true
                }
            }
        })
    }

    private fun querySmileyInfo(){
        model.getSmileyList()
    }

    override fun onSmileyPress(str: String, a: Drawable) {
        // remove \ and /
        val decodeStr = str.replace("/", "")
                .replace("\\", "")
        handler!!.insertSmiley(decodeStr, a)
        Log.d(TAG, "Press string $decodeStr")
    }

    // parse client
    fun configureIntent(){
        val intent = intent
        val discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        this.bbsInfo = discuz
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        model.configureDiscuz(discuz,user)
        smileyViewPagerAdapter = SmileyViewPagerAdapter(supportFragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,discuz,this)
        privateMessageInfo = intent.getSerializableExtra(ConstUtils.PASS_PRIVATE_MESSAGE_KEY) as privateMessage
        // parse client
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this, user)
        privateMessageViewModel.configure(discuz,user, privateMessageInfo.toUid)
        URLUtils.setBBS(discuz)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.bbs_notification_my_pm)
            supportActionBar!!.subtitle = privateMessageInfo.toUsername
        }

    }


    private fun configureActionBar() {
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == android.R.id.home) {
            finishAfterTransition()
            false
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = PrivateMessageActivity::class.java.simpleName
    }
}