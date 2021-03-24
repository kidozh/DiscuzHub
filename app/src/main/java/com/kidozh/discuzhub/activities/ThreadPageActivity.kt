package com.kidozh.discuzhub.activities

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.method.DigitsKeyListener
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter
import com.kidozh.discuzhub.adapter.PostAdapter
import com.kidozh.discuzhub.adapter.SmileyViewPagerAdapter
import com.kidozh.discuzhub.databinding.ActivityThreadPageBinding
import com.kidozh.discuzhub.entities.*
import com.kidozh.discuzhub.utilities.*
import com.kidozh.discuzhub.viewModels.SmileyViewModel
import com.kidozh.discuzhub.viewModels.ThreadViewModel
import es.dmoral.toasty.Toasty
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern
import kotlin.concurrent.thread


class ThreadPageActivity : BaseStatusActivity() , SmileyFragment.OnSmileyPressedInteraction, PostAdapter.onFilterChanged, PostAdapter.onAdapterReply, PostAdapter.OnLinkClicked{
    val TAG = ThreadPageActivity::class.simpleName
    lateinit var binding: ActivityThreadPageBinding
    lateinit var threadViewModel: ThreadViewModel
    lateinit var smileyViewModel: SmileyViewModel
    lateinit var discuz: Discuz
    var tid = 0
    var fid = 0
    lateinit var thread : Thread
    var forum : Forum? = null
    lateinit var postAdapter: PostAdapter
    lateinit var concatAdapter: ConcatAdapter
    private val networkIndicatorAdapter: NetworkIndicatorAdapter = NetworkIndicatorAdapter()
    lateinit var smileyViewPagerAdapter: SmileyViewPagerAdapter
    lateinit var smileyHandler: EmotionInputHandler
    lateinit var smileyPicker: SmileyPicker
    var jumpedPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerViewModel()
        configureSpinner()
        configureToolbar()
        configureIntentData()
        configureRecyclerview()
        configureAdvancedIcon()
        configureSmileyHandler()
        configureSwipeRefreshLayout()
        bindViewModel()
        configureReplyLayout()
        Log.d(TAG, "start to get thread " + threadViewModel.threadStatusMutableLiveData.value!!.tid)
        threadViewModel.getThreadDetail(threadViewModel.threadStatusMutableLiveData.value!!)

    }

    private fun configureToolbar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = getString(R.string.paging_view)
    }

    fun registerViewModel(){
        threadViewModel = ViewModelProvider(this).get(ThreadViewModel::class.java)
        smileyViewModel = ViewModelProvider(this).get(SmileyViewModel::class.java)
    }

    fun configureIntentData(){
        discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        thread = intent.getSerializableExtra(ConstUtils.PASS_THREAD_KEY) as Thread
        forum = intent.getParcelableExtra(ConstUtils.PASS_FORUM_THREAD_KEY)
        val page: Int = intent.getIntExtra(ConstUtils.PASS_PAGE_KEY, 1)
        tid = thread.tid
        fid = intent.getIntExtra("FID", 0)
        // configure view model

        threadViewModel.setBBSInfo(discuz, user, forum, thread.tid)
        smileyViewModel.configureDiscuz(discuz, user)
        // init toolbar
        val sp = Html.fromHtml(thread.subject, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannableString = SpannableString(sp)
        binding.threadSubject.setText(spannableString, TextView.BufferType.SPANNABLE)
        smileyViewPagerAdapter = SmileyViewPagerAdapter(supportFragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, discuz, this)

        // init adapter
        postAdapter = PostAdapter(discuz, user, ViewThreadQueryStatus(tid, threadViewModel.threadStatusMutableLiveData.value!!.page))
        networkIndicatorAdapter.successPageShown = false
        concatAdapter = ConcatAdapter(postAdapter, networkIndicatorAdapter)
        Log.d(TAG, "Get tid " + thread.tid)
        threadViewModel.threadStatusMutableLiveData.value = ViewThreadQueryStatus(tid, page)

    }

    fun configureRecyclerview(){
        // configure post recyclerview
        val linearLayoutManager = LinearLayoutManager(this)
        binding.postsRecyclerview.layoutManager = linearLayoutManager
        binding.postsRecyclerview.itemAnimator = AnimationUtils.getRecyclerviewAnimation(this)
        binding.postsRecyclerview.adapter = AnimationUtils.getAnimatedAdapter(this, concatAdapter)
    }

    fun configureSwipeRefreshLayout(){
        binding.postsSwipeRefreshLayout.setOnRefreshListener {
            // clear it first
            postAdapter.clearList()
            val status = threadViewModel.threadStatusMutableLiveData.value as ViewThreadQueryStatus
            threadViewModel.getThreadDetail(status)
        }
    }

    fun bindViewModel(){
        threadViewModel.networkStatus.observe(this, { integer: Int ->
            Log.d(TAG, "network changed $integer")
            when (integer) {
                ConstUtils.NETWORK_STATUS_LOADING -> {
                    binding.postsSwipeRefreshLayout.isRefreshing = true
                    networkIndicatorAdapter.setLoadingStatus()
                    // clear list
                    postAdapter.clearList()
                }
                ConstUtils.NETWORK_STATUS_LOADED_ALL -> {
                    binding.postsSwipeRefreshLayout.isRefreshing = false
                    //Log.d(TAG,"Network changed "+integer);
                    networkIndicatorAdapter.setLoadedAllStatus()
                }
                ConstUtils.NETWORK_STATUS_SUCCESSFULLY -> {
                    binding.postsSwipeRefreshLayout.isRefreshing = false
                    networkIndicatorAdapter.setLoadSuccessfulStatus()
                }
                else -> {
                    binding.postsSwipeRefreshLayout.isRefreshing = false
                }
            }
        })

        threadViewModel.errorMessageMutableLiveData.observe(this, { errorMessage: ErrorMessage? ->
            if (errorMessage != null) {
                Toasty.error(application,
                        getString(R.string.discuz_api_message_template, errorMessage.key, errorMessage.content),
                        Toast.LENGTH_LONG).show()
                networkIndicatorAdapter.setErrorStatus(errorMessage)
                VibrateUtils.vibrateForError(application)
            }
        })

        threadViewModel.threadPostResultMutableLiveData.observe(this, {
            if (it != null) {
                // rendering subject
                val threadInfo = it.threadPostVariables.detailedThreadInfo
                val sp = Html.fromHtml(threadInfo.subject, HtmlCompat.FROM_HTML_MODE_LEGACY)
                val spannableString = SpannableString(sp)
                binding.threadSubject.setText(spannableString, TextView.BufferType.SPANNABLE)
                // rendering list
                val posts = it.threadPostVariables.postList
                val status = threadViewModel.threadStatusMutableLiveData.value as ViewThreadQueryStatus
                postAdapter.setPosts(posts as MutableList<Post>, status, status.authorId)
                Log.d(TAG, "GET page post " + posts.size + " returned ppp " + it.threadPostVariables.ppp)
                // deal with page
                val allReplies = it.threadPostVariables.detailedThreadInfo.replies
                // hardcoded for stable performance
                var cntPerpage = it.threadPostVariables.ppp

                if (cntPerpage == 0) {
                    cntPerpage = 15
                }
                Log.d(TAG, "Get all replies " + allReplies + " spinner pages " + allReplies / cntPerpage)
                // divide by 0 check
                val spinnerPages = allReplies / cntPerpage + 1
                // scroll if position is given
                if(jumpedPosition != 0){
                    val destinationLayer = jumpedPosition % cntPerpage
                    Toasty.info(this,getString(R.string.scroll_to_pid_successfully,jumpedPosition)).show()
                    binding.postsRecyclerview.smoothScrollToPosition(destinationLayer)

                    jumpedPosition = 0
                }

                binding.pageSpinner.visibility = View.VISIBLE
                // check if || spinnerPages != binding.pageSpinner.adapter.count
                // original comparision
                if (binding.pageSpinner.adapter == null) {
                    val pageList: MutableList<String> = ArrayList()

                    for (i in 0 until spinnerPages) {
                        pageList.add(getString(R.string.per_page, i + 1))
                    }
                    binding.pageSpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pageList)
                }

                binding.pageSpinner.setSelection(status.page - 1)


                // checking comment
                if (it.threadPostVariables.member_uid == 0) {
                    binding.replyLayout.visibility = View.GONE
                } else {
                    binding.replyLayout.visibility = View.VISIBLE
                }
            } else {
                binding.pageSpinner.visibility = View.GONE
            }
        })


        // for secure reason
        threadViewModel.secureInfo.observe(this, { secureInfoResult ->
            if (secureInfoResult != null) {
                if (secureInfoResult.secureVariables == null) {
                    // don't need a code
                    binding.captchaLayout.visibility = View.GONE

                } else {
                    binding.captchaLayout.visibility = View.VISIBLE

                    binding.captchaImageview.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_captcha_placeholder_24px))
                    // need a captcha
                    val captchaURL = secureInfoResult.secureVariables.secCodeURL
                    val captchaImageURL = URLUtils.getSecCodeImageURL(secureInfoResult.secureVariables.secHash)
                    // load it
                    if (captchaURL == null) {
                        return@observe
                    }
                    val captchaRequest = Request.Builder()
                            .url(captchaURL)
                            .build()
                    // get first
                    client = threadViewModel.client
                    client.newCall(captchaRequest).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {}

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful && response.body() != null) {
                                // get the session
                                binding.captchaImageview.post {
                                    val factory = OkHttpUrlLoader.Factory(client)
                                    Glide.get(application).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)

                                    // forbid cache captcha
                                    val options = RequestOptions()
                                            .fitCenter()
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .placeholder(R.drawable.ic_captcha_placeholder_24px)
                                            .error(R.drawable.ic_post_status_warned_24px)
                                    val pictureGlideURL = GlideUrl(captchaImageURL,
                                            LazyHeaders.Builder()
                                                    .addHeader("Referer", captchaURL)
                                                    .build()
                                    )
                                    Glide.with(application)
                                            .load(pictureGlideURL)
                                            .apply(options)
                                            .into(binding.captchaImageview)
                                }
                            }
                        }
                    })
                }
            } else {
                // don't know the situation
                binding.captchaLayout.visibility = View.GONE
            }
        })

        smileyViewModel.smileyResultLiveData.observe(this, { it ->
            if (it != null) {
                val smileyList = it.variables.smileyList
                val smileyCategoryCnt = smileyList.size
                binding.smileyTablayout.removeAllTabs()
                for (i in 0 until smileyCategoryCnt) {
                    binding.smileyTablayout.addTab(
                            binding.smileyTablayout.newTab().setText((i + 1).toString())
                    )
                }

                smileyViewPagerAdapter.smileyList = smileyList
                binding.smileyTablayout.getTabAt(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_history_24)

            }
        })

        threadViewModel.replyPostMutableLiveData.observe(this,{
            if(it == null){
                binding.replyPersonContent.visibility = View.GONE
            }
            else{
                binding.replyPersonContent.visibility = View.VISIBLE
                binding.replyPostAuthorChip.text = it.author
                binding.replyPostContent.text = it.message
            }
        })

        threadViewModel.interactErrorMutableLiveData.observe(this,{
            if(it != null){
                Toasty.error(this,
                        getString(R.string.discuz_api_message_template, it.key, it.content),
                        Toast.LENGTH_LONG).show()
            }

        })



        threadViewModel.replyResultMutableLiveData.observe(this,{
            if(it?.message != null){
                if (it.message!!.key == "post_reply_succeed") {
                    Toasty.success(this,
                            getString(R.string.discuz_api_message_template, it.message!!.key, it.message!!.content),
                            Toast.LENGTH_LONG).show()
                    // clear the status
                    threadViewModel.replyPostMutableLiveData.postValue(null)
                    binding.replyEdittext.text.clear()
                } else {
                    Toasty.error(this,
                            getString(R.string.discuz_api_message_template, it.message!!.key, it.message!!.content),
                            Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    private fun configureSpinner(){
        binding.pageSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val page = position + 1
                val status = threadViewModel.threadStatusMutableLiveData.value as ViewThreadQueryStatus
                if(page != status.page){
                    Log.d(TAG, "Page $page is selected ")
                    status.page = page
                    threadViewModel.getThreadDetail(status)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }
    
    private fun configureSmileyHandler(){
        smileyHandler = EmotionInputHandler(binding.replyEdittext) { _: Boolean, _: String? -> }
        smileyPicker = SmileyPicker(this, discuz)
        smileyPicker.setListener { str: String?, a: Drawable? -> smileyHandler.insertSmiley(str, a) }
    }

    private fun configureAdvancedIcon(){

        binding.advancePostIcon.setOnClickListener {
            val message = binding.replyEdittext.text.toString()
            val intent = Intent(this, PublishActivity::class.java)
            intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, forum)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra(ConstUtils.PASS_POST_TYPE, ConstUtils.TYPE_POST_REPLY)
            intent.putExtra(ConstUtils.PASS_POST_MESSAGE, message)
            //intent.putExtra(ConstUtils.PASS_REPLY_POST, selectedThreadComment)
            intent.putExtra("tid", thread.tid)


            startActivity(intent)
        }
    }

    private fun configureReplyLayout(){
        binding.replyPostAuthorChip.setOnCloseIconClickListener {
            threadViewModel.replyPostMutableLiveData.postValue(null)
        }
        binding.replyButton.setOnClickListener{
            val replyMessage = binding.replyEdittext.text.toString()
            val captcha = binding.captchaEdittext.text.toString()
            if(replyMessage.isNotEmpty()){
                threadViewModel.sendReplyRequest(fid,replyMessage,captcha)
            }

        }

        binding.replyEdittext.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val inputString = s.toString();
                binding.replyButton.isEnabled = inputString.isNotEmpty()
            }

        })

        binding.captchaImageview.setOnClickListener { // update it
            threadViewModel.secureInfo
        }
    }

    override fun onSmileyPress(str: String, a: Drawable) {
        val decodeStr = str.replace("/", "")
                .replace("\\", "")
        smileyHandler.insertSmiley(decodeStr, a)
        
    }

    private fun invalidateResponse(){
        // clear spinner for better selection
        binding.pageSpinner.adapter = null
    }

    override fun setAuthorId(authorId: Int) {
        val viewThreadQueryStatus = threadViewModel.threadStatusMutableLiveData.value as ViewThreadQueryStatus
        viewThreadQueryStatus.setInitAuthorId(authorId)
        invalidateResponse()
        threadViewModel.getThreadDetail(viewThreadQueryStatus)

    }

    override fun replyToSomeOne(post: Post) {
        // need further notice
        threadViewModel.replyPostMutableLiveData.postValue(post)
    }

    private fun parseURLAndOpen(url: String) {
        Log.d(TAG, "Parse and open URL $url")
        val uri = Uri.parse(url)
        if (uri != null && uri.path != null) {
            if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "redirect" && uri.getQueryParameter("goto") != null && uri.getQueryParameter("goto") == "findpost" && uri.getQueryParameter("pid") != null && uri.getQueryParameter("ptid") != null) {
                val pidString = uri.getQueryParameter("pid")
                val tidString = uri.getQueryParameter("ptid")
                var redirectTid = 0
                var redirectPid = 0
                try {
                    redirectTid = tidString!!.toInt()
                    redirectPid = pidString!!.toInt()
                } catch (e: Exception) {
                }
                Log.d(TAG, "Find the current $redirectPid tid $redirectTid")
                if (redirectTid != tid) {
                    val putThreadInfo = Thread()
                    putThreadInfo.tid = redirectTid
                    val intent = Intent(this, ThreadActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                    //intent.putExtra("FID", fid)
                    intent.putExtra("TID", redirectTid)
                    intent.putExtra("SUBJECT", url)
                    VibrateUtils.vibrateForClick(this)
                    startActivity(intent)
                    return
                } else {
                    // scroll it
                    val postInfos = postAdapter.getPosts()
                    for (i in postInfos.indices) {
                        val curPost = postInfos[i]
                        if (curPost.pid == redirectPid) {
                            binding.postsRecyclerview.smoothScrollToPosition(i)
                            VibrateUtils.vibrateForClick(this)
                            val postPostion = postInfos[i].position
                            Toasty.success(this, getString(R.string.scroll_to_pid_successfully, postPostion), Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    Toasty.info(this, getString(R.string.scroll_to_pid_failed, pidString), Toast.LENGTH_SHORT).show()
                }
            } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "viewthread" && uri.getQueryParameter("tid") != null) {
                val tidString = uri.getQueryParameter("tid")
                var redirectTid = 0
                try {
                    redirectTid = tidString!!.toInt()
                } catch (e: Exception) {
                }
                val putThreadInfo = Thread()
                putThreadInfo.tid = redirectTid
                val intent = Intent(this, ThreadActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                //intent.putExtra("FID", fid)
                intent.putExtra("TID", redirectTid)
                intent.putExtra("SUBJECT", url)
                VibrateUtils.vibrateForClick(this)
                startActivity(intent)
                return
            } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "forumdisplay" && uri.getQueryParameter("fid") != null) {
                val fidString = uri.getQueryParameter("fid")
                var fid = 0
                fid = try {
                    fidString!!.toInt()
                } catch (e: Exception) {
                    0
                }
                val intent = Intent(this, ForumActivity::class.java)
                val clickedForum = Forum()
                clickedForum.fid = fid
                intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                Log.d(TAG, "put base url " + discuz.base_url)
                VibrateUtils.vibrateForClick(this)
                startActivity(intent)
                return
            } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "space" && uri.getQueryParameter("uid") != null) {
                val uidStr = uri.getQueryParameter("uid")
                var uid = 0
                uid = try {
                    uidStr!!.toInt()
                } catch (e: Exception) {
                    0
                }
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                intent.putExtra("UID", uid)
                startActivity(intent)
                return
            }
            val intent = Intent(this, InternalWebViewActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra(ConstUtils.PASS_URL_KEY, url)
            Log.d(TAG, "Inputted URL $url")
            startActivity(intent)
        } else {
            val intent = Intent(this, InternalWebViewActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra(ConstUtils.PASS_URL_KEY, url)
            Log.d(TAG, "Inputted URL $url")
            startActivity(intent)
        }
    }

    override fun onLinkClicked(url: String) {
        val context: Context = this
        val unescapedURL = url
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
        // judge the host
        val baseURL = URLUtils.getBaseUrl()
        val baseUri = Uri.parse(baseURL)
        val clickedUri = Uri.parse(unescapedURL)
        if (clickedUri.host == null || clickedUri.host == baseUri.host) {
            // internal link
            val result = threadViewModel.threadPostResultMutableLiveData.value
            if (result != null) {
                val rewriteRules = result.threadPostVariables.rewriteRule
                var clickedURLPath = clickedUri.path
                if (clickedURLPath == null) {
                    parseURLAndOpen(unescapedURL)
                }
                val basedURLPath = baseUri.path
                if (clickedURLPath != null && basedURLPath != null) {
                    if (clickedURLPath.matches(Regex("^$basedURLPath.*"))) {
                        clickedURLPath = clickedURLPath.substring(basedURLPath.length)
                    }
                }
                // only catch two type : forum_forumdisplay & forum_viewthread
                // only 8.0+ support reverse copy
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (rewriteRules != null) {
                        if (rewriteRules.containsKey("forum_forumdisplay")) {
                            var rewriteRule = rewriteRules["forum_forumdisplay"]
                            UserPreferenceUtils.saveRewriteRule(context, discuz, UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY, rewriteRule)
                            if (rewriteRule == null || clickedURLPath == null) {
                                parseURLAndOpen(unescapedURL)
                                return
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{fid}", "(?<fid>\\d+)")
                            rewriteRule = rewriteRule.replace("{page}", "(?<page>\\d+)")
                            val pattern = Pattern.compile(rewriteRule)
                            val matcher = pattern.matcher(clickedURLPath)
                            if (matcher.find()) {
                                val fidStr = matcher.group("fid")
                                val pageStr = matcher.group("page")
                                // handle it
                                if (fidStr != null) {
                                    var fid = 0
                                    fid = try {
                                        fidStr.toInt()
                                    } catch (e: Exception) {
                                        0
                                    }

            //                                    int page = Integer.parseInt(pageStr);
                                    val intent = Intent(context, ForumActivity::class.java)
                                    val clickedForum = Forum()
                                    clickedForum.fid = fid
                                    intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                                    Log.d(TAG, "put base url " + discuz.base_url)
                                    VibrateUtils.vibrateForClick(context)
                                    context.startActivity(intent)
                                    return
                                }
                            }
                        }
                    }
                    if (rewriteRules != null) {
                        if (rewriteRules.containsKey("forum_viewthread")) {
                            // match template such as t{tid}-{page}-{prevpage}
                            var rewriteRule = rewriteRules["forum_viewthread"]
                            UserPreferenceUtils.saveRewriteRule(context, discuz, UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY, rewriteRule)
                            if (rewriteRule == null || clickedURLPath == null) {
                                parseURLAndOpen(unescapedURL)
                                return
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{tid}", "(?<tid>\\d+)")
                            rewriteRule = rewriteRule.replace("{page}", "(?<page>\\d+)")
                            rewriteRule = rewriteRule.replace("{prevpage}", "(?<prevpage>\\d+)")
                            val pattern = Pattern.compile(rewriteRule)
                            val matcher = pattern.matcher(clickedURLPath)
                            if (matcher.find()) {
                                val tidStr = matcher.group("tid")
                                val pageStr = matcher.group("page")
                                // handle it
                                if (tidStr != null) {
                                    val putThreadInfo = Thread()
                                    var tid = 0
                                    tid = try {
                                        tidStr.toInt()
                                    } catch (e: Exception) {
                                        0
                                    }
                                    putThreadInfo.tid = tid
                                    val intent = Intent(context, ThreadActivity::class.java)
                                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                                    //intent.putExtra("FID", fid)
                                    intent.putExtra("TID", tid)
                                    intent.putExtra("SUBJECT", url)
                                    VibrateUtils.vibrateForClick(context)
                                    val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                    val bundle = options.toBundle()
                                    context.startActivity(intent, bundle)
                                    return
                                }
                            }
                        }
                    }
                    if (rewriteRules != null) {
                        if (rewriteRules.containsKey("home_space")) {
                            // match template such as t{tid}-{page}-{prevpage}
                            var rewriteRule = rewriteRules["home_space"]
                            Log.d(TAG, "get home space url $rewriteRule")
                            UserPreferenceUtils.saveRewriteRule(context, discuz, UserPreferenceUtils.REWRITE_HOME_SPACE, rewriteRule)
                            if (rewriteRule == null || clickedURLPath == null) {
                                parseURLAndOpen(unescapedURL)
                                return
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{user}", "(?<user>\\d+)")
                            rewriteRule = rewriteRule.replace("{value}", "(?<value>\\d+)")
                            val pattern = Pattern.compile(rewriteRule)
                            val matcher = pattern.matcher(clickedURLPath)
                            if (matcher.find()) {
                                val userString = matcher.group("user")
                                val uidString = matcher.group("value")
                                // handle it
                                if (uidString != null) {
                                    var uid = 0
                                    uid = try {
                                        uidString.toInt()
                                    } catch (e: Exception) {
                                        0
                                    }
                                    val intent = Intent(context, UserProfileActivity::class.java)
                                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                                    intent.putExtra("UID", uid)
                                    VibrateUtils.vibrateForClick(context)
                                    val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                    val bundle = options.toBundle()
                                    context.startActivity(intent, bundle)
                                    return
                                }
                            }
                        }
                    }
                    parseURLAndOpen(unescapedURL)
                } else {
                    parseURLAndOpen(unescapedURL)
                }
            } else {
                // parse the URL
                parseURLAndOpen(unescapedURL)
            }
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val outLinkWarn = prefs.getBoolean(getString(R.string.preference_key_outlink_warn), true)
            if (outLinkWarn) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.outlink_warn_title)
                        .setMessage(getString(R.string.outlink_warn_message, clickedUri.host, baseUri.host))
                        .setNeutralButton(R.string.bbs_show_in_internal_browser) { dialog, which ->
                            val intent = Intent(context, InternalWebViewActivity::class.java)
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                            intent.putExtra(ConstUtils.PASS_URL_KEY, unescapedURL)
                            Log.d(TAG, "Inputted URL $unescapedURL")
                            startActivity(intent)
                        }
                        .setPositiveButton(R.string.bbs_show_in_external_browser) { dialog, which ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(unescapedURL))
                            startActivity(intent)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
                        .show()
            } else {
                val intent = Intent(this, InternalWebViewActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                intent.putExtra(ConstUtils.PASS_URL_KEY, unescapedURL)
                Log.d(TAG, "Inputted URL $unescapedURL")
                startActivity(intent)
                return
            }
        }
        Log.d(TAG, "You click $unescapedURL")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_thread_page, menu)
        return true
    }

    private fun openJumpDialog(){
        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.jump_to_position)
        val input = EditText(this)
        input.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.keyListener = DigitsKeyListener.getInstance("0123456789");
        builder.setView(input)
        // configure view
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val result = threadViewModel.threadPostResultMutableLiveData.value
            if(result!= null){
                val allReplies = result.threadPostVariables.detailedThreadInfo.replies
                var position = 0
                try{
                    position = input.text.toString().toInt()
                }
                catch (e:Exception){
                    Toasty.error(this,getString(R.string.invalid_position)).show()
                    return@setPositiveButton
                }


                if(position> allReplies){
                    Toasty.warning(this,getString(R.string.jump_pos_over,allReplies)).show()
                }
                else{
                    var ppp = result.threadPostVariables.ppp
                    Log.d(TAG,"get ppp "+ppp)
                    if(ppp == 0){
                        ppp = 15
                    }
                    // analysis the page it should be
                    val destinationPage:Int = position / ppp + 1
                    val destinationPosition = position % ppp
                    val status = threadViewModel.threadStatusMutableLiveData.value as ViewThreadQueryStatus
                    Log.d(TAG,"get destination page "+destinationPage+" ppp "+ppp)
                    // check if this is current page
                    if(destinationPage == status.page){
                        Toasty.success(this,getString(R.string.scroll_to_pid_successfully,position)).show()
                        binding.postsRecyclerview.smoothScrollToPosition(destinationPosition)
                    }
                    else{
                        status.page = destinationPage
                        jumpedPosition = position
                        threadViewModel.getThreadDetail(status)
                    }
                }
            }
        }

        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finishAfterTransition()
                return true
            }
            R.id.jump_to_position -> {
                // trigger a dialog
                val result = threadViewModel.threadPostResultMutableLiveData.value
                if(result != null){
                    openJumpDialog()
                }
                else{
                    Toasty.info(this,getString(R.string.jump_not_ready)).show()
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
