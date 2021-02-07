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

    // private OkHttpClient client;
    private var globalPage = -1
    var formHash: String? = null
    var pmid: String? = null
    var hasLoadAll = false
    var allSmileyInfos: List<Smiley>? = null
    var smileyCateNum = 0
    private var smileyPicker: SmileyPicker? = null
    private var handler: EmotionInputHandler? = null
    lateinit var binding: ActivityBbsPrivateMessageDetailBinding
    lateinit var model : SmileyViewModel
    lateinit var smileyViewPagerAdapter : SmileyViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBbsPrivateMessageDetailBinding.inflate(layoutInflater)
        model = ViewModelProvider(this).get(SmileyViewModel::class.java)
        setContentView(binding.root)
        configureIntent()
        bindViewModel()
        configureActionBar()
        configureSmileyLayout()
        configureRecyclerview()
        configureSwipeLayout()
        getPageInfo(globalPage)
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
        val mHandler = Handler(Looper.getMainLooper())
        binding.bbsPrivateMessageDetailSwipeRefreshLayout.setOnRefreshListener {
            if (globalPage != 0) {
                mHandler.post { getPageInfo(globalPage) }
            } else {
                binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun configureRecyclerview() {
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.bbsPrivateMessageDetailRecyclerview.layoutManager = linearLayoutManager
        adapter = PrivateDetailMessageAdapter(bbsInfo, user)
        binding.bbsPrivateMessageDetailRecyclerview.adapter = adapter
    }

    private fun configureSendBtn() {
        binding.bbsPrivateMessageCommentButton.setOnClickListener {
            val sendMessage = binding.bbsPrivateMessageCommentEditText.text.toString()
            if (sendMessage.length != 0) {
                sendPrivateMessage()
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
    }

    // update the UI
    // viewpager
    //adapter.setSmileyInfos(smileyInfoList);
    // interface with tab
    // bind tablayout and viewpager
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

    private fun getPageInfo(page: Int) {
        binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = true
        val apiStr = URLUtils.getPrivatePMDetailApiUrlByTouid(privateMessageInfo!!.toUid, page)
        val request = Request.Builder()
                .url(apiStr)
                .build()
        Log.d(TAG, "get public message in page $page $apiStr")
        val mHandler = Handler(Looper.getMainLooper())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                hasLoadAll = true
                mHandler.post { binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                mHandler.post { binding.bbsPrivateMessageDetailSwipeRefreshLayout.isRefreshing = false }
                if (response.isSuccessful && response.body() != null) {
                    val s = response.body()!!.string()
                    val privateDetailMessages = bbsParseUtils.parsePrivateDetailMessage(s, user!!.uid)
                    val messagePerPage = bbsParseUtils.parsePrivateDetailMessagePerPage(s)
                    formHash = bbsParseUtils.parseFormHash(s)
                    pmid = bbsParseUtils.parsePrivateDetailMessagePmid(s)
                    globalPage = bbsParseUtils.parsePrivateDetailMessagePage(s)
                    globalPage -= 1
                    if (privateDetailMessages != null) {
                        Log.d(TAG, "get PM " + privateDetailMessages.size)
                        mHandler.post {
                            if (page == -1) {
                                adapter.setPrivateDetailMessageList(privateDetailMessages)
                                //binding.bbsPrivateMessageDetailRecyclerview.scrollToPosition(privateDetailMessages.size()-1);
                            } else {
                                adapter.addPrivateDetailMessageList(privateDetailMessages)
                                //binding.bbsPrivateMessageDetailRecyclerview.scrollToPosition(privateDetailMessages.size()-1);
                            }
                        }
                    } else {
                        hasLoadAll = true
                    }
                } else {
                    hasLoadAll = false
                }
            }
        })
    }

    // parse client
    fun configureIntent(){
        val intent = intent
        val bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        this.bbsInfo = bbsInfo
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        model.configureDiscuz(bbsInfo,user)
        smileyViewPagerAdapter = SmileyViewPagerAdapter(supportFragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,bbsInfo,this)
        privateMessageInfo = intent.getSerializableExtra(ConstUtils.PASS_PRIVATE_MESSAGE_KEY) as privateMessage
        // parse client
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this, user)
        URLUtils.setBBS(bbsInfo)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.bbs_notification_my_pm)
            supportActionBar!!.subtitle = privateMessageInfo!!.toUsername
        }
    }


    private fun sendPrivateMessage() {
        val builder = FormBody.Builder()
                .add("formhash", formHash)
                .add("topmuid", privateMessageInfo!!.toUid.toString())
        when (charsetType) {
            CHARSET_GBK -> {
                try {
                    builder.addEncoded("message", URLEncoder.encode(binding.bbsPrivateMessageCommentEditText.text.toString(), "GBK"))

                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    builder.add("message", binding.bbsPrivateMessageCommentEditText.text.toString())
                }
            }
            CHARSET_BIG5 -> {
                try {
                    builder.addEncoded("message", URLEncoder.encode(binding.bbsPrivateMessageCommentEditText.text.toString(), "BIG5"))

                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    builder.add("message", binding.bbsPrivateMessageCommentEditText.text.toString())
                }
            }
            else -> {
                builder.add("message", binding.bbsPrivateMessageCommentEditText.text.toString())
            }
        }
        val formBody = builder.build()
        val apiStr = URLUtils.getSendPMApiUrl(privateMessageInfo!!.plid, pmid!!.toInt())
        Log.d(TAG, "Send PM " + apiStr + " topmuid " + privateMessageInfo!!.toUid + " formhash " + formHash)
        val request = Request.Builder()
                .url(apiStr)
                .post(formBody)
                .build()
        binding.bbsPrivateMessageCommentButton.isEnabled = false
        val mHandler = Handler(Looper.getMainLooper())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.post {
                    binding.bbsPrivateMessageCommentEditText.setText("")
                    Toasty.error(applicationContext,
                            getString(R.string.network_failed),
                            Toast.LENGTH_SHORT
                    ).show()
                    binding.bbsPrivateMessageCommentButton.isEnabled = true
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                mHandler.post {
                    binding.bbsPrivateMessageCommentEditText.setText("")
                    binding.bbsPrivateMessageCommentButton.isEnabled = true
                }
                if (response.isSuccessful && response.body() != null) {
                    val s = response.body()!!.string()
                    Log.d(TAG, "Recv PM $s")
                    globalPage = -1
                    // need to post a delay to get information
                    mHandler.postDelayed({
                        hasLoadAll = false
                        getPageInfo(globalPage)
                    }, 500)
                } else {
                    mHandler.post {
                        Toasty.error(applicationContext,
                                getString(R.string.network_failed),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
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