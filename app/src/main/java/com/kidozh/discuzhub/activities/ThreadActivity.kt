package com.kidozh.discuzhub.activities


import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment.OnSmileyPressedInteraction
import com.kidozh.discuzhub.adapter.PostAdapter.onFilterChanged
import com.kidozh.discuzhub.adapter.PostAdapter.onAdapterReply
import com.kidozh.discuzhub.adapter.PostAdapter.OnLinkClicked
import com.kidozh.discuzhub.activities.ui.bbsPollFragment.bbsPollFragment
import com.kidozh.discuzhub.adapter.ThreadPropertiesAdapter.OnThreadPropertyClicked
import com.kidozh.discuzhub.adapter.PostAdapter.OnAdvanceOptionClicked
import com.kidozh.discuzhub.dialogs.ReportPostDialogFragment.ReportDialogListener
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter.OnRefreshBtnListener
import com.kidozh.discuzhub.adapter.ThreadCountAdapter.OnRecommendBtnPressed
import com.kidozh.discuzhub.adapter.PostAdapter
import com.kidozh.discuzhub.adapter.ThreadCountAdapter
import com.kidozh.discuzhub.utilities.SmileyPicker
import com.kidozh.discuzhub.utilities.EmotionInputHandler
import com.kidozh.discuzhub.viewModels.ThreadViewModel
import androidx.recyclerview.widget.ConcatAdapter
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.URLUtils
import android.text.Html
import android.text.SpannableString
import android.widget.TextView
import android.graphics.drawable.Drawable
import es.dmoral.toasty.Toasty
import com.kidozh.discuzhub.R
import android.widget.Toast
import com.kidozh.discuzhub.utilities.VibrateUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils
import kotlin.Throws
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.LazyHeaders
import com.kidozh.discuzhub.results.ApiMessageActionResult
import com.kidozh.discuzhub.results.BuyThreadResult
import android.content.DialogInterface
import com.kidozh.discuzhub.utilities.NetworkUtils
import android.view.View.OnFocusChangeListener
import com.kidozh.discuzhub.utilities.bbsParseUtils
import android.app.ActivityOptions
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.*
import com.kidozh.discuzhub.dialogs.ReportPostDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.EditText
import android.widget.LinearLayout
import android.text.TextUtils
import androidx.core.content.ContextCompat
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.kidozh.discuzhub.adapter.SmileyViewPagerAdapter
import com.kidozh.discuzhub.database.ViewHistoryDatabase
import com.kidozh.discuzhub.databinding.ActivityViewThreadBinding
import com.kidozh.discuzhub.entities.*
import com.kidozh.discuzhub.viewModels.SmileyViewModel
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URLEncoder
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class ThreadActivity : BaseStatusActivity(), OnSmileyPressedInteraction, onFilterChanged, onAdapterReply, OnLinkClicked, bbsPollFragment.OnFragmentInteractionListener, OnThreadPropertyClicked, OnAdvanceOptionClicked, ReportDialogListener, OnRefreshBtnListener, OnRecommendBtnPressed {
    lateinit var binding: ActivityViewThreadBinding
    var subject: String? = null
    var tid = 0
    var fid = 0

    lateinit var postAdapter: PostAdapter
    lateinit var countAdapter: ThreadCountAdapter
    var formHash: String? = null
    var forum: Forum? = null
    lateinit var discuz: Discuz
    var thread: Thread? = null
    private var hasLoadOnce = false
    private var notifyLoadAll = false
    var poll: Poll? = null
    private var selectedThreadComment: Post? = null
    private var smileyPicker: SmileyPicker? = null
    private var handler: EmotionInputHandler? = null
    lateinit var threadDetailViewModel: ThreadViewModel
    lateinit var smileyViewModel: SmileyViewModel
    private var concatAdapter: ConcatAdapter? = null
    val networkIndicatorAdapter = NetworkIndicatorAdapter()
    lateinit var smileyViewPagerAdapter: SmileyViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewThreadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        threadDetailViewModel = ViewModelProvider(this).get(ThreadViewModel::class.java)
        smileyViewModel = ViewModelProvider(this).get(SmileyViewModel::class.java)
        configureIntentData()
        initThreadStatus()
        configureClient()
        configureToolbar()
        bindViewModel()
        configureRecyclerview()
        threadDetailViewModel.threadStatusMutableLiveData.value?.let { threadDetailViewModel.getThreadDetail(it) }
        // getThreadComment();
        configureSwipeRefreshLayout()
        configureCommentBtn()
        configureSmileyLayout()
        configureChipGroup()

    }

    private fun configureIntentData() {
        val intent = intent
        forum = intent.getParcelableExtra(ConstUtils.PASS_FORUM_THREAD_KEY)
        discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        thread = intent.getSerializableExtra(ConstUtils.PASS_THREAD_KEY) as Thread?
        tid = intent.getIntExtra("TID", 0)
        fid = intent.getIntExtra("FID", 0)
        subject = intent.getStringExtra("SUBJECT")
        // hasLoadOnce = intent.getBooleanExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,false);
        URLUtils.setBBS(discuz)
        threadDetailViewModel.setBBSInfo(discuz, user, forum, tid)
        smileyViewModel.configureDiscuz(discuz, user)
        if (thread != null && thread!!.subject != null) {
            val sp = Html.fromHtml(thread!!.subject)
            val spannableString = SpannableString(sp)
            binding.bbsThreadSubject.setText(spannableString, TextView.BufferType.SPANNABLE)
        }
        smileyViewPagerAdapter = SmileyViewPagerAdapter(supportFragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,discuz,this)
    }

    private fun initThreadStatus() {
        val viewThreadQueryStatus = ViewThreadQueryStatus(tid, 1)
        Log.d(TAG, "Set status when init data")
        threadDetailViewModel.threadStatusMutableLiveData.value = viewThreadQueryStatus
    }

    private fun checkWithPerm(status: Int, perm: Int): Boolean {
        return status and perm != 0
    }



    private fun configureSmileyLayout() {
        handler = EmotionInputHandler(binding.bbsThreadDetailCommentEditText) { enable: Boolean, s: String? -> }
        smileyPicker = SmileyPicker(this, discuz)
        smileyPicker!!.setListener { str: String?, a: Drawable? -> handler!!.insertSmiley(str, a) }

        binding.bbsCommentSmileyTabLayout.setupWithViewPager(binding.bbsCommentSmileyViewPager)
        binding.bbsCommentSmileyViewPager.adapter = smileyViewPagerAdapter
    }

    fun configureChipGroup(){
        binding.threadProperty.setOnCheckedChangeListener { _, checkedId ->
            Log.d(TAG,"GET clicked id "+checkedId)
            if(checkedId == R.drawable.ic_price_outlined_24px){
                Toasty.info(this, getString(R.string.buy_thread_loading), Toast.LENGTH_SHORT).show()
                threadDetailViewModel.getThreadPriceInfo(tid)
            }
        }
    }

    fun getPropertyChip(res: Int, string: String, iconColor: Int ): Chip{

        return Chip(this).apply {
            text = string
            chipIcon = ContextCompat.getDrawable(context,res)
            id = res
            iconStartPadding = 8.0F
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun bindViewModel() {
        // for personal info
        threadDetailViewModel.bbsPersonInfoMutableLiveData.observe(this, { userBriefInfo ->
            Log.d(TAG, "User info $userBriefInfo")
            if (userBriefInfo == null || userBriefInfo.auth == null) {
                binding.bbsCommentConstraintLayout.visibility = View.GONE
            } else {
                binding.bbsCommentConstraintLayout.visibility = View.VISIBLE
            }
        })
        threadDetailViewModel.newPostList.observe(this, { posts: List<Post> ->
            var authorid = 0
            val threadResult = threadDetailViewModel.threadPostResultMutableLiveData.value
            val viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
            if (threadResult?.threadPostVariables != null && threadResult.threadPostVariables.detailedThreadInfo != null) {
                authorid = threadResult.threadPostVariables.detailedThreadInfo.authorId
            }
            Log.d(TAG, "queried page " + viewThreadQueryStatus!!.page)
            if (viewThreadQueryStatus.page == 1) {
                postAdapter.clearList()
                postAdapter.addThreadInfoList(posts, threadDetailViewModel.threadStatusMutableLiveData.value, authorid)
                binding.postsRecyclerview.scrollToPosition(0)
            } else {
                postAdapter.addThreadInfoList(posts, threadDetailViewModel.threadStatusMutableLiveData.value, authorid)
            }
        })
        threadDetailViewModel.networkStatus.observe(this, { integer: Int ->
            Log.d(TAG, "network changed $integer")
            when (integer) {
                ConstUtils.NETWORK_STATUS_LOADING -> {
                    binding.bbsThreadDetailSwipeRefreshLayout.isRefreshing = true
                    networkIndicatorAdapter!!.setLoadingStatus()
                }
                ConstUtils.NETWORK_STATUS_LOADED_ALL -> {
                    binding.bbsThreadDetailSwipeRefreshLayout.isRefreshing = false
                    //Log.d(TAG,"Network changed "+integer);
                    networkIndicatorAdapter.setLoadedAllStatus()
                }
                ConstUtils.NETWORK_STATUS_SUCCESSFULLY -> {
                    binding.bbsThreadDetailSwipeRefreshLayout.isRefreshing = false
                    networkIndicatorAdapter.setLoadSuccessfulStatus()
                }
                else -> {
                    binding.bbsThreadDetailSwipeRefreshLayout.isRefreshing = false
                }
            }
        })
        threadDetailViewModel.errorMessageMutableLiveData.observe(this, { errorMessage: ErrorMessage? ->
            if (errorMessage != null) {
                Toasty.error(application,
                        getString(R.string.discuz_api_message_template, errorMessage.key, errorMessage.content),
                        Toast.LENGTH_LONG).show()
                networkIndicatorAdapter.setErrorStatus(ErrorMessage(
                        errorMessage.key, errorMessage.content, R.drawable.ic_error_outline_24px
                ))
                VibrateUtils.vibrateForError(application)
            }
        })
        threadDetailViewModel.pollLiveData.observe(this, { bbsPollInfo ->
            if (bbsPollInfo != null) {
                Log.d(TAG, "get poll " + bbsPollInfo.votersCount)
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.bbs_thread_poll_fragment,
                        bbsPollFragment.newInstance(bbsPollInfo, user, tid, formHash))
                fragmentTransaction.commit()
            } else {
                Log.d(TAG, "get poll is null")
            }
        })
        threadDetailViewModel.formHash.observe(this, { s -> formHash = s })
        threadDetailViewModel.threadStatusMutableLiveData.observe(this, { viewThreadQueryStatus ->
            Log.d(TAG, "Livedata changed " + viewThreadQueryStatus.datelineAscend)
            if (supportActionBar != null) {
                if (viewThreadQueryStatus.datelineAscend) {
                    supportActionBar!!.setSubtitle(getString(R.string.bbs_thread_status_ascend))
                } else {
                    supportActionBar!!.setSubtitle(getString(R.string.bbs_thread_status_descend))
                }
            }
        })
        threadDetailViewModel.detailedThreadInfoMutableLiveData.observe(this, { detailedThreadInfo -> // closed situation
            // prepare notification list
            val threadNotificationList: MutableList<ThreadCount> = ArrayList()
            binding.threadProperty.removeAllViews()
            if (detailedThreadInfo.price != 0) {
                if (detailedThreadInfo.price > 0) {
                    val chip = getPropertyChip(R.drawable.ic_price_outlined_24px,
                            getString(R.string.thread_buy_price, detailedThreadInfo.price),
                            getColor(R.color.colorPumpkin),
                    )
                    chip.isClickable = true
                    chip.setOnClickListener{ v->
                        Toasty.info(this, getString(R.string.buy_thread_loading), Toast.LENGTH_SHORT).show()
                        threadDetailViewModel.getThreadPriceInfo(tid)
                    }


                    binding.threadProperty.addView(chip)
                } else {
                    binding.threadProperty.addView(
                            getPropertyChip(R.drawable.ic_price_outlined_24px,
                                    getString(R.string.thread_reward_price, detailedThreadInfo.price), getColor(R.color.colorPumpkin))
                    )
                }
            }
            if (detailedThreadInfo.subject != null) {
                val sp = Html.fromHtml(detailedThreadInfo.subject)
                val spannableString = SpannableString(sp)
                binding.bbsThreadSubject.setText(spannableString, TextView.BufferType.SPANNABLE)
            }
            if (detailedThreadInfo.closed != 0) {
                binding.bbsThreadDetailCommentEditText.isEnabled = false
                binding.bbsThreadDetailCommentButton.isEnabled = false
                binding.bbsThreadDetailCommentEditText.setHint(R.string.thread_is_closed)
                binding.bbsThreadDetailEmoijButton.isClickable = false
                binding.advancePostIcon.visibility = View.GONE
                if (!UserPreferenceUtils.conciseRecyclerView(applicationContext)
                        && detailedThreadInfo.closed == 1) {

                    binding.threadProperty.addView(
                            getPropertyChip(R.drawable.ic_highlight_off_outlined_24px,
                                    getString(R.string.thread_is_closed), getColor(R.color.colorPomegranate))
                    )
                }
            } else {
                binding.advancePostIcon.visibility = View.VISIBLE
                binding.bbsThreadDetailCommentEditText.isEnabled = true
                binding.bbsThreadDetailCommentButton.isEnabled = true
                binding.bbsThreadDetailCommentEditText.setHint(R.string.bbs_thread_say_something)
                binding.bbsThreadDetailEmoijButton.isClickable = true
            }

            if (detailedThreadInfo.readperm != 0) {
                val result = threadDetailViewModel.threadPostResultMutableLiveData.value
                if (result != null) {
                    val userBriefInfo = result.threadPostVariables.userBriefInfo
                }
                if (user != null && user!!.readPerm >= detailedThreadInfo.readperm) {
                    if (!UserPreferenceUtils.conciseRecyclerView(applicationContext)) {
                        // not to display in concise mode
                        binding.threadProperty.addView(
                                getPropertyChip(R.drawable.ic_verified_user_outlined_24px,
                                        getString(R.string.thread_readperm, detailedThreadInfo.readperm, user!!.readPerm), getColor(R.color.colorTurquoise))
                        )
                    }
                } else {
                    if (user == null) {
                        binding.threadProperty.addView(
                                getPropertyChip(R.drawable.ic_read_perm_unsatisfied_24px,
                                        getString(R.string.thread_anoymous_readperm, detailedThreadInfo.readperm), getColor(R.color.colorAsbestos))
                        )
                    } else {
                        binding.threadProperty.addView(
                                getPropertyChip(R.drawable.ic_read_perm_unsatisfied_24px,
                                        getString(R.string.thread_readperm, detailedThreadInfo.readperm, user!!.readPerm), getColor(R.color.colorAlizarin))
                        )
                    }
                }
            }
            if (detailedThreadInfo.hidden) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_thread_visibility_off_24px,
                                getString(R.string.thread_is_hidden), getColor(R.color.colorWisteria))
                )
            }

            // need another
            if (detailedThreadInfo.highlight != null && detailedThreadInfo.highlight != "0") {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_highlight_outlined_24px,
                                getString(R.string.thread_is_highlighted), getColor(R.color.colorPrimary))
                )
            }
            if (detailedThreadInfo.digest != 0) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_digest_outlined_24px,
                                getString(R.string.thread_is_digested), getColor(R.color.colorGreensea))
                )
            }
            if (detailedThreadInfo.is_archived) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_archive_outlined_24px,
                                getString(R.string.thread_is_archived), getColor(R.color.colorMidnightblue))
                )
            }
            if (!UserPreferenceUtils.conciseRecyclerView(applicationContext)) {
                // only see in not concise mode
                if (detailedThreadInfo.moderated) {
                    binding.threadProperty.addView(
                            getPropertyChip(R.drawable.ic_moderated_outlined_24px,
                                    getString(R.string.thread_is_moderated), getColor(R.color.colorOrange))
                    )
                }
                if (detailedThreadInfo.stickReply) {
                    binding.threadProperty.addView(
                            getPropertyChip(R.drawable.vector_drawable_reply_24px,
                                    getString(R.string.thread_stick_reply), getColor(R.color.colorWetasphalt))
                    )
                }
                // recommend?
                threadNotificationList.add(
                        ThreadCount(R.drawable.ic_thumb_up_outlined_24px, detailedThreadInfo.recommend_add.toString())
                )
                threadNotificationList.add(
                        ThreadCount(R.drawable.ic_thumb_down_outlined_24px, detailedThreadInfo.recommend_sub.toString())
                )
                threadNotificationList.add(
                        ThreadCount(R.drawable.ic_favorite_24px, detailedThreadInfo.favtimes.toString(), "FAVORITE")
                )
                threadNotificationList.add(
                        ThreadCount(R.drawable.ic_share_outlined_24px, detailedThreadInfo.sharedtimes.toString(), "SHARE")
                )
                if (detailedThreadInfo.heats != 0) {
                    threadNotificationList.add(
                            ThreadCount(R.drawable.ic_whatshot_outlined_24px, detailedThreadInfo.heats.toString())
                    )
                }
            }
            val status = detailedThreadInfo.status
            // check with perm
            val STATUS_CACHE_THREAD_LOCATION = 1
            val STATUS_ONLY_SEE_BY_POSTER = 2
            val STATUS_REWARD_LOTTO = 4
            val STATUS_DESCEND_REPLY = 8
            val STATUS_EXIST_ICON = 16
            val STATUS_NOTIFY_AUTHOR = 32
            if (checkWithPerm(status, STATUS_CACHE_THREAD_LOCATION)) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_cache_thread_location_24px,
                                getString(R.string.thread_cache_location), getColor(R.color.colorMidnightblue))
                )
            }
            if (checkWithPerm(status, STATUS_ONLY_SEE_BY_POSTER)) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_reply_only_see_by_poster_24px,
                                getString(R.string.thread_reply_only_see_by_poster), getColor(R.color.colorNephritis))
                )
            }
            if (checkWithPerm(status, STATUS_REWARD_LOTTO)) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_thread_reward_lotto_24px,
                                getString(R.string.thread_reward_lotto), getColor(R.color.colorSunflower))
                )
            }
            if (!UserPreferenceUtils.conciseRecyclerView(applicationContext)
                    && checkWithPerm(status, STATUS_NOTIFY_AUTHOR)) {
                binding.threadProperty.addView(
                        getPropertyChip(R.drawable.ic_thread_notify_author_24px,
                                getString(R.string.thread_notify_author), getColor(R.color.colorPrimaryDark))
                )
            }
            countAdapter.setThreadCountList(threadNotificationList)

            // for normal rendering
            binding.bbsThreadCommentNumber.text = getString(R.string.bbs_thread_reply_number, detailedThreadInfo.replies)
            binding.bbsThreadViewNumber.text = detailedThreadInfo.views.toString()
        })
        threadDetailViewModel.threadPostResultMutableLiveData.observe(this, { threadResult ->
            if (threadResult != null) {
                if (threadResult.threadPostVariables != null && threadResult.threadPostVariables.detailedThreadInfo != null && threadResult.threadPostVariables.detailedThreadInfo.subject != null) {
                    val sp = Html.fromHtml(threadResult.threadPostVariables.detailedThreadInfo.subject,HtmlCompat.FROM_HTML_MODE_COMPACT)
                    val spannableString = SpannableString(sp)
                    binding.bbsThreadSubject.setText(spannableString, TextView.BufferType.SPANNABLE)
                    if (supportActionBar != null) {
                        supportActionBar!!.setTitle(threadResult.threadPostVariables.detailedThreadInfo.subject)
                    }
                    // check with comments
                    Log.d(TAG,"get comments "+threadResult.threadPostVariables.commentList.keys.size+" "+threadResult.threadPostVariables.commentList)
                    postAdapter.mergeCommentMap(threadResult.threadPostVariables.commentList)

                    val detailedThreadInfo = threadResult.threadPostVariables.detailedThreadInfo
                    if (detailedThreadInfo != null && hasLoadOnce == false) {
                        hasLoadOnce = true
                        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        val recordHistory = prefs.getBoolean(getString(R.string.preference_key_record_history), false)
                        if (recordHistory) {
                            insertViewHistory(ViewHistory(
                                    URLUtils.getDefaultAvatarUrlByUid(detailedThreadInfo.authorId),
                                    detailedThreadInfo.author,
                                    discuz.id,
                                    detailedThreadInfo.subject,
                                    ViewHistory.VIEW_TYPE_THREAD,
                                    detailedThreadInfo.fid,
                                    tid,
                                    Date()
                            ))
                        }
                    }
                }

                //Log.d(TAG,"Thread post result error "+ threadResult.isError()+" "+ threadResult.threadPostVariables.message);
                val rewriteRule = threadResult.threadPostVariables.rewriteRule
                if (rewriteRule != null) {
                    getAndSaveRewriteRule(rewriteRule, UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY)
                    getAndSaveRewriteRule(rewriteRule, UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY)
                    getAndSaveRewriteRule(rewriteRule, UserPreferenceUtils.REWRITE_HOME_SPACE)
                }
            }
        })

        // for secure reason
        threadDetailViewModel.secureInfo.observe(this, Observer { secureInfoResult ->
            if (secureInfoResult != null) {
                if (secureInfoResult.secureVariables == null) {
                    // don't need a code
                    binding.bbsPostCaptchaEditText.visibility = View.GONE
                    binding.bbsPostCaptchaImageview.visibility = View.GONE
                } else {
                    binding.bbsPostCaptchaEditText.visibility = View.VISIBLE
                    binding.bbsPostCaptchaImageview.visibility = View.VISIBLE
                    binding.bbsPostCaptchaImageview.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_captcha_placeholder_24px))
                    // need a captcha
                    val captchaURL = secureInfoResult.secureVariables.secCodeURL
                    val captchaImageURL = URLUtils.getSecCodeImageURL(secureInfoResult.secureVariables.secHash)
                    // load it
                    if (captchaURL == null) {
                        return@Observer
                    }
                    val captchaRequest = Request.Builder()
                            .url(captchaURL)
                            .build()
                    // get first
                    client.newCall(captchaRequest).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {}

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful && response.body() != null) {
                                // get the session
                                binding.bbsPostCaptchaImageview.post {
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
                                            .into(binding.bbsPostCaptchaImageview)
                                }
                            }
                        }
                    })
                }
            } else {
                // don't know the situation
                binding.bbsPostCaptchaEditText.visibility = View.GONE
                binding.bbsPostCaptchaImageview.visibility = View.GONE
            }
        })
        threadDetailViewModel.favoriteThreadLiveData.observe(this, { favoriteThread ->
            Log.d(TAG, "Get favorite thread in observer$favoriteThread")
            invalidateOptionsMenu()
        })
        threadDetailViewModel.recommendResultMutableLiveData.observe(this, { apiMessageActionResult: ApiMessageActionResult? ->
            if (apiMessageActionResult != null && apiMessageActionResult.message != null) {
                val messageResult = apiMessageActionResult.message
                if (messageResult != null) {
                    if (messageResult.key == "recommend_succeed") {
                        Toasty.success(applicationContext, getString(R.string.discuz_api_message_template, messageResult.key, messageResult.content), Toast.LENGTH_LONG).show()
                    } else {
                        Toasty.error(applicationContext, getString(R.string.discuz_api_message_template, messageResult.key, messageResult.content), Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
        threadDetailViewModel.interactErrorMutableLiveData.observe(this, { errorMessage: ErrorMessage? ->
            if (errorMessage != null) {
                networkIndicatorAdapter.setErrorStatus(errorMessage)
                Toasty.error(applicationContext, getString(R.string.discuz_api_message_template, errorMessage.key, errorMessage.content), Toast.LENGTH_LONG).show()
            }
        })
        val context: Context = this
        threadDetailViewModel.threadPriceInfoMutableLiveData.observe(this, { buyThreadResult: BuyThreadResult? ->
            if (buyThreadResult != null) {
                val variableResult = buyThreadResult.variableResults
                val builder = AlertDialog.Builder(context)
                        .setTitle(R.string.buy_thread_title)
                        .setMessage(
                                getString(R.string.buy_thread_dialog_message,
                                        variableResult.author, variableResult.price.toString(),
                                        if (variableResult.credit == null) "" else variableResult.credit.title, variableResult.balance.toString())
                        )
                        .setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, which: Int ->
                            Toasty.info(this, getString(R.string.buy_thread_send), Toast.LENGTH_SHORT).show()
                            threadDetailViewModel.buyThread(tid)
                        }
                builder.show()
            }
        })
        threadDetailViewModel.buyThreadResultMutableLiveData.observe(this, { buyThreadResult: BuyThreadResult? ->
            if (buyThreadResult?.message != null) {
                val key = buyThreadResult.message!!.key
                if (key == "thread_pay_succeed") {
                    Toasty.success(this, getString(R.string.discuz_api_message_template,
                            buyThreadResult.message!!.key,
                            buyThreadResult.message!!.content)).show()
                    reloadThePage()
                } else {
                    Toasty.warning(this, getString(R.string.discuz_api_message_template,
                            buyThreadResult.message!!.key,
                            buyThreadResult.message!!.content)).show()
                }
            }
        })
        threadDetailViewModel.reportResultMutableLiveData.observe(this, { apiMessageActionResult: ApiMessageActionResult? ->
            if (apiMessageActionResult != null) {
                if (apiMessageActionResult.message != null) {
                    if (apiMessageActionResult.message!!.key == "report_succeed") {
                        Toasty.success(this,
                                getString(R.string.discuz_api_message_template, apiMessageActionResult.message!!.key, apiMessageActionResult.message!!.content),
                                Toast.LENGTH_LONG).show()
                    } else {
                        Toasty.error(this,
                                getString(R.string.discuz_api_message_template, apiMessageActionResult.message!!.key, apiMessageActionResult.message!!.content),
                                Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toasty.error(this,
                            getString(R.string.api_message_return_null),
                            Toast.LENGTH_LONG).show()
                }
            }
        })


        smileyViewModel.smileyResultLiveData.observe(this, Observer { it->
            if(it != null){
                val smileyList = it.variables.smileyList
                val smileyCategoryCnt = smileyList.size
                binding.bbsCommentSmileyTabLayout.removeAllTabs()
                for(i in 0 until smileyCategoryCnt){
                    binding.bbsCommentSmileyTabLayout.addTab(
                            binding.bbsCommentSmileyTabLayout.newTab().setText((i + 1).toString())
                    )
                }

                smileyViewPagerAdapter.smileyList = smileyList
                binding.bbsCommentSmileyTabLayout.getTabAt(0)?.icon = ContextCompat.getDrawable(this,R.drawable.ic_baseline_history_24)

            }
        })

    }

    private fun getAndSaveRewriteRule(rewriteRule: Map<String, String>, key: String) {
        if (rewriteRule.containsKey(key)) {
            UserPreferenceUtils.saveRewriteRule(this, discuz, key, rewriteRule[key])
        }
    }

    private fun configureSwipeRefreshLayout() {
        binding.bbsThreadDetailSwipeRefreshLayout.setOnRefreshListener {
            val isLoading = threadDetailViewModel.networkStatus.value == ConstUtils.NETWORK_STATUS_LOADING
            if (!isLoading) {
                reloadThePage()
                threadDetailViewModel.threadStatusMutableLiveData.value?.let { threadDetailViewModel.getThreadDetail(it) }
            }
        }
    }

    private fun configureClient() {
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(this, user)
    }

    private fun configureCommentBtn() {
        // advance post
        val context: Context = this
        binding.advancePostIcon.setOnClickListener {
            val message = binding.bbsThreadDetailCommentEditText.text.toString()
            val intent = Intent(context, PublishActivity::class.java)
            intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, forum)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra(ConstUtils.PASS_POST_TYPE, ConstUtils.TYPE_POST_REPLY)
            intent.putExtra(ConstUtils.PASS_POST_MESSAGE, message)
            intent.putExtra(ConstUtils.PASS_REPLY_POST, selectedThreadComment)
            intent.putExtra("tid", tid)
            intent.putExtra("fid", fid.toString())
            if (forum != null) {
                intent.putExtra("fid_name", forum!!.name)
            }
            context.startActivity(intent)
        }

        // captcha
        binding.bbsPostCaptchaImageview.setOnClickListener { // update it
            threadDetailViewModel.secureInfo
        }
        binding.bbsThreadDetailCommentButton.setOnClickListener(View.OnClickListener {
            val commentMessage = binding.bbsThreadDetailCommentEditText.text.toString()
            val captchaString = binding.bbsPostCaptchaEditText.text.toString()
            if (needCaptcha() && captchaString.length == 0) {
                Toasty.warning(applicationContext, getString(R.string.captcha_required), Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (commentMessage.length < 1) {
                Toasty.info(applicationContext, getString(R.string.bbs_require_comment), Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "SELECTED THREAD COMMENT $selectedThreadComment")
                if (selectedThreadComment == null) {
                    // directly comment thread
                    postCommentToThread(commentMessage)
                } else {
                    val pid = selectedThreadComment!!.pid
                    Log.d(TAG, "Send Reply to $pid")
                    postReplyToSomeoneInThread(pid, commentMessage, selectedThreadComment!!.message)
                }
            }
        })
        binding.bbsThreadDetailEmoijButton.setOnClickListener {
            if (binding.smileyRootLayout.visibility == View.GONE) {
                // smiley picker not visible
                binding.bbsThreadDetailEmoijButton.setImageDrawable(getDrawable(R.drawable.vector_drawable_keyboard_24px))
                binding.bbsThreadDetailCommentEditText.clearFocus()
                // close keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bbsThreadDetailCommentEditText.windowToken, 0)
                binding.smileyRootLayout.visibility = View.VISIBLE

                // tab layout binding...
                smileyViewModel.getSmileyList()
            } else {
                binding.smileyRootLayout.visibility = View.GONE
                binding.bbsThreadDetailEmoijButton.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp))
            }
        }
        binding.bbsThreadDetailCommentEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && binding.smileyRootLayout.visibility == View.VISIBLE) {
                binding.smileyRootLayout.visibility = View.GONE
                binding.bbsThreadDetailEmoijButton.setImageDrawable(getDrawable(R.drawable.ic_edit_emoticon_24dp))
            }
        }
    }



    override fun onSmileyPress(str: String, a: Drawable) {
        // remove \ and /
        val decodeStr = str.replace("/", "")
                .replace("\\", "")
        handler!!.insertSmiley(decodeStr, a)
        Log.d(TAG, "Press string $decodeStr")
    }

    override fun replyToSomeOne(position: Int) {
        val threadCommentInfo = postAdapter!!.threadInfoList[position]
        selectedThreadComment = threadCommentInfo
        binding.bbsThreadDetailReplyChip.text = threadCommentInfo.author
        binding.bbsThreadDetailReplyChip.visibility = View.VISIBLE
        binding.bbsThreadDetailCommentEditText.hint = String.format("@%s", threadCommentInfo.author)
        var decodeString = threadCommentInfo.message
        // filter quote
        val quoteRegexInVer4 = "^<div class=\"reply_wrap\">(.+?)</div><br .>"

        // remove it if possible
        val quotePatternInVer4 = Pattern.compile(quoteRegexInVer4, Pattern.DOTALL)
        val quoteMatcherInVer4 = quotePatternInVer4.matcher(decodeString)
        decodeString = quoteMatcherInVer4.replaceAll("")
        val sp = Html.fromHtml(decodeString)
        binding.bbsThreadDetailReplyContent.setText(sp, TextView.BufferType.SPANNABLE)
        binding.bbsThreadDetailReplyContent.visibility = View.VISIBLE
        binding.bbsThreadDetailReplyChip.setOnCloseIconClickListener {
            binding.bbsThreadDetailReplyChip.visibility = View.GONE
            binding.bbsThreadDetailReplyContent.visibility = View.GONE
            binding.bbsThreadDetailCommentEditText.setHint(R.string.bbs_thread_say_something)
            selectedThreadComment = null
        }
    }

    override fun onPollResultFetched() {
        // reset poll to get realtime result
        Log.d(TAG, "POLL is voted")
        poll = null
        threadDetailViewModel.pollLiveData.value = null
        reloadThePage()
        threadDetailViewModel.threadStatusMutableLiveData.value?.let { threadDetailViewModel.getThreadDetail(it) }
    }

    override fun setAuthorId(authorId: Int) {
        val viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
        viewThreadQueryStatus?.setInitAuthorId(authorId)
        reloadThePage(viewThreadQueryStatus)

        // refresh it
        threadDetailViewModel.threadStatusMutableLiveData.value?.let { threadDetailViewModel.getThreadDetail(it) }
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
                    intent.putExtra("FID", fid)
                    intent.putExtra("TID", redirectTid)
                    intent.putExtra("SUBJECT", url)
                    VibrateUtils.vibrateForClick(this)
                    startActivity(intent)
                    return
                } else {
                    // scroll it
                    val postInfos = postAdapter!!.threadInfoList
                    if (postInfos != null) {
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
                intent.putExtra("FID", fid)
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
            val result = threadDetailViewModel.threadPostResultMutableLiveData.value
            if (result != null && result.threadPostVariables != null) {
                if (result.threadPostVariables.rewriteRule != null) {
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
                                    intent.putExtra("FID", fid)
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
                        parseURLAndOpen(unescapedURL)
                    } else {
                        parseURLAndOpen(unescapedURL)
                    }
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

    override fun onRecommend(recommend: Boolean) {
        threadDetailViewModel.recommendThread(tid, recommend)
    }

    override fun buyThreadPropertyClicked() {

        // buy the thread dialog
        Toasty.info(this, getString(R.string.buy_thread_loading), Toast.LENGTH_SHORT).show()
        threadDetailViewModel.getThreadPriceInfo(tid)
        // prompt dialog first
    }

    override fun reportPost(post: Post) {
        if (user == null) {
            Toasty.warning(this, getString(R.string.report_login_required), Toast.LENGTH_LONG).show()
        } else {
            val fragmentManager = supportFragmentManager
            val reportPostDialogFragment = ReportPostDialogFragment(post)
            reportPostDialogFragment.show(fragmentManager, ReportPostDialogFragment::class.java.simpleName)
        }
    }

    override fun onReportSubmit(pid: Int, reportReason: String, reportForOtherReason: Boolean) {
        threadDetailViewModel.reportPost(pid, reportReason, reportForOtherReason)
    }

    override fun onRefreshBtnClicked() {
        val status = threadDetailViewModel.threadStatusMutableLiveData.value
        status!!.page += 1
        if (status != null) {
            threadDetailViewModel.getThreadDetail(status)
        }
    }


    private fun configureRecyclerview() {


        //binding.postsRecyclerview.setHasFixedSize(true);
        val linearLayoutManager = LinearLayoutManager(this)
        binding.postsRecyclerview.layoutManager = linearLayoutManager
        binding.postsRecyclerview.itemAnimator = getRecyclerviewAnimation(this)
        postAdapter = PostAdapter(this,
                discuz,
                user,
                threadDetailViewModel.threadStatusMutableLiveData.value)
        concatAdapter = ConcatAdapter(postAdapter, networkIndicatorAdapter)
        binding.postsRecyclerview.adapter = getAnimatedAdapter(this, concatAdapter!!)
        binding.postsRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
                    val isLoading = threadDetailViewModel.networkStatus.value == ConstUtils.NETWORK_STATUS_LOADING
                    val hasLoadAll = threadDetailViewModel.networkStatus.value == ConstUtils.NETWORK_STATUS_LOADED_ALL
                    Log.d(TAG, "Recyclerview can scroll vert hasLoadAll $hasLoadAll isloading $isLoading")
                    if (!isLoading && viewThreadQueryStatus != null) {
                        if (hasLoadAll) {
                            // load all posts
                            if (!notifyLoadAll) {
                                // never vibrate before
                                if (UserPreferenceUtils.vibrateWhenLoadingAll(applicationContext)) {
                                    VibrateUtils.vibrateSlightly(applicationContext)
                                }
                                notifyLoadAll = true
                                threadDetailViewModel.notifyLoadAll.postValue(true)
                                Toasty.success(application,
                                        getString(R.string.all_posts_loaded_template, postAdapter.itemCount),
                                        Toast.LENGTH_LONG).show()
                            }
                        } else {
                            viewThreadQueryStatus.page += 1
                            threadDetailViewModel.getThreadDetail(viewThreadQueryStatus)
                        }
                    }
                }
            }
        })
        countAdapter = ThreadCountAdapter()
        if (!UserPreferenceUtils.conciseRecyclerView(applicationContext)) {
            // not to bind this
            binding.bbsThreadInteractiveRecyclerview.setHasFixedSize(true)
            binding.bbsThreadInteractiveRecyclerview.itemAnimator = getRecyclerviewAnimation(this)
            binding.bbsThreadInteractiveRecyclerview.layoutManager = GridLayoutManager(this, 5)
            binding.bbsThreadInteractiveRecyclerview.adapter = getAnimatedAdapter(this, countAdapter)
        }
    }

    private fun needCaptcha(): Boolean {
        return !(threadDetailViewModel.secureInfoResultMutableLiveData.value == null || threadDetailViewModel.secureInfoResultMutableLiveData.value!!.secureVariables == null)
    }

    private fun postCommentToThread(message: String) {
        val timeGetTime = Date()
        val formBodyBuilder = FormBody.Builder()
                .add("subject", "")
                .add("usesig", "1")
                .add("posttime", (timeGetTime.time / 1000 - 1).toString())
                .add("formhash", formHash)
        when (charsetType) {
            CHARSET_GBK -> {
                formBodyBuilder.addEncoded("message", URLEncoder.encode(message, "GBK"))

            }
            CHARSET_BIG5 -> {
                formBodyBuilder.addEncoded("message", URLEncoder.encode(message, "BIG5"))
            }
            else -> {
                formBodyBuilder.add("message", message)
            }
        }
        if (needCaptcha()) {
            val secureInfoResult = threadDetailViewModel.secureInfoResultMutableLiveData.value
            formBodyBuilder.add("seccodehash", secureInfoResult!!.secureVariables.secHash)
                    .add("seccodemodid", "forum::viewthread")
            val captcha = binding.bbsPostCaptchaEditText.text.toString()
            when (charsetType) {
                CHARSET_GBK -> {
                    formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha, "GBK"))
                }
                CHARSET_BIG5 -> {
                    formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha, "BIG5"))
                }
                else -> {
                    formBodyBuilder.add("seccodeverify", captcha)
                }
            }
        }
        val formBody = formBodyBuilder.build()
        Log.d(TAG, "get Form " + message + " hash "
                + formHash + " fid " + fid + " tid " + tid
                + " API ->" + URLUtils.getReplyThreadUrl(fid, tid) + " formhash " + formHash)
        val request = Request.Builder()
                .url(URLUtils.getReplyThreadUrl(fid, tid))
                .post(formBody)
                .addHeader("referer", URLUtils.getViewThreadUrl(tid, "1"))
                .build()
        val mHandler = Handler(Looper.getMainLooper())
        // UI Change
        binding.bbsThreadDetailCommentButton.setText(R.string.bbs_commentting)
        binding.bbsThreadDetailCommentButton.isEnabled = false
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.post {
                    binding.bbsThreadDetailCommentButton.setText(R.string.bbs_thread_comment)
                    binding.bbsThreadDetailCommentButton.isEnabled = true
                    Toasty.error(applicationContext, getString(R.string.bbs_comment_failed), Toast.LENGTH_LONG).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body() != null) {
                    val s = response.body()!!.string()
                    Log.d(TAG, "Recv comment info $s")
                    val returnedMessage = bbsParseUtils.parseReturnMessage(s)
                    if (returnedMessage != null && returnedMessage.value == "post_reply_succeed") {
                        // success!
                        mHandler.post {
                            if (binding.smileyRootLayout.visibility == View.VISIBLE) {
                                binding.bbsThreadDetailEmoijButton.callOnClick()
                            }
                            binding.bbsThreadDetailCommentButton.setText(R.string.bbs_thread_comment)
                            binding.bbsThreadDetailCommentButton.isEnabled = true
                            binding.bbsThreadDetailCommentEditText.setText("")
                            //reloadThePage()
                            threadDetailViewModel.threadStatusMutableLiveData.value?.let { threadDetailViewModel.getThreadDetail(it) }
                            //getThreadComment();
                            Toasty.success(applicationContext, getString(R.string.discuz_api_message_template, returnedMessage.value, returnedMessage.string), Toast.LENGTH_LONG).show()

                        }
                    } else {
                        mHandler.post {
                            binding.bbsThreadDetailCommentButton.setText(R.string.bbs_thread_comment)
                            binding.bbsThreadDetailCommentButton.isEnabled = true
                            if (returnedMessage == null) {
                                Toasty.error(applicationContext, getString(R.string.network_failed), Toast.LENGTH_LONG).show()
                            } else {
                                Toasty.error(applicationContext, getString(R.string.discuz_api_message_template, returnedMessage.value, returnedMessage.string), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun reloadThePage() {
        val viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
        viewThreadQueryStatus?.setInitPage(1)
        Log.d(TAG, "Set status when reload page")
        threadDetailViewModel.threadStatusMutableLiveData.value = viewThreadQueryStatus
        threadDetailViewModel.notifyLoadAll.value = false
        notifyLoadAll = false
    }

    private fun reloadThePage(viewThreadQueryStatus: ViewThreadQueryStatus?) {
        viewThreadQueryStatus?.setInitPage(1)
        Log.d(TAG, "Set status when init data $viewThreadQueryStatus")
        threadDetailViewModel.threadStatusMutableLiveData.value = viewThreadQueryStatus
        threadDetailViewModel.notifyLoadAll.value = false
        notifyLoadAll = false
    }

    private fun postReplyToSomeoneInThread(replyPid: Int, message: String, noticeAuthorMsg: String) {
        // remove noticeAuthorMsg <>
        var noticeAuthorMsg = noticeAuthorMsg
        noticeAuthorMsg = noticeAuthorMsg.replace("<.*>".toRegex(), "")
        val df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL, Locale.getDefault())
        val publishAtString = df.format(selectedThreadComment!!.publishAt)
        val MAX_CHAR_LENGTH = 300
        val trimEnd = Math.min(MAX_CHAR_LENGTH, noticeAuthorMsg.length)
        // not to trim
        //int trimEnd = noticeAuthorMsg.length();
        Log.d(TAG, "Reply msg $noticeAuthorMsg")
        var replyMessage = noticeAuthorMsg.substring(0, trimEnd)
        if (noticeAuthorMsg.length > MAX_CHAR_LENGTH) {
            replyMessage += "..."
        }
        val noticeMsgTrimString = getString(R.string.bbs_reply_notice_author_string,
                URLUtils.getReplyPostURLInLabel(selectedThreadComment!!.pid, selectedThreadComment!!.tid),
                selectedThreadComment!!.author,
                publishAtString,
                replyMessage
        )
        Log.d(TAG, "Get message $noticeAuthorMsg$noticeMsgTrimString")
        val formBodyBuilder = FormBody.Builder()
                .add("formhash", formHash)
                .add("handlekey", "reply")
                .add("usesig", "1")
                .add("reppid", replyPid.toString())
                .add("reppost", replyPid.toString())
        when (charsetType) {
            CHARSET_GBK -> {
                formBodyBuilder.addEncoded("message", URLEncoder.encode(message, "GBK"))
                formBodyBuilder.addEncoded("noticeauthormsg", URLEncoder.encode(noticeAuthorMsg, "GBK"))
                        .addEncoded("noticetrimstr", URLEncoder.encode(noticeMsgTrimString, "GBK"))

            }
            CHARSET_BIG5 -> {
                formBodyBuilder.addEncoded("message", URLEncoder.encode(message, "BIG5"))
                formBodyBuilder.addEncoded("noticeauthormsg", URLEncoder.encode(noticeAuthorMsg, "BIG5"))
                        .addEncoded("noticetrimstr", URLEncoder.encode(noticeMsgTrimString, "BIG5"))
            }
            else -> {
                formBodyBuilder.add("message", message)
                formBodyBuilder.add("noticeauthormsg", noticeAuthorMsg)
                        .add("noticetrimstr", noticeMsgTrimString)
            }
        }
        if (needCaptcha()) {
            val secureInfoResult = threadDetailViewModel.secureInfoResultMutableLiveData.value
            val captcha = binding.bbsPostCaptchaEditText.text.toString()
            formBodyBuilder.add("seccodehash", secureInfoResult!!.secureVariables.secHash)
                    .add("seccodemodid", "forum::viewthread")
            when (charsetType) {
                CHARSET_GBK -> {
                    formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha, "GBK"))

                }
                CHARSET_BIG5 -> {
                    formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha, "BIG5"))
                }
                else -> {
                    formBodyBuilder.add("seccodeverify", captcha)
                }
            }
        }
        val formBody = formBodyBuilder.build()
        val request = Request.Builder()
                .url(URLUtils.getReplyThreadUrl(fid, tid))
                .post(formBody)
                .build()
        binding.bbsThreadDetailCommentButton.setText(R.string.bbs_commentting)
        binding.bbsThreadDetailCommentButton.isEnabled = false
        val mHandler = Handler(Looper.getMainLooper())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.post {
                    binding.bbsThreadDetailCommentButton.setText(R.string.bbs_thread_comment)
                    binding.bbsThreadDetailCommentButton.isEnabled = true
                    Toasty.error(applicationContext, getString(R.string.network_failed), Toast.LENGTH_LONG).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.body() != null) {
                    val s = response.body()!!.string()
                    val returnedMessage = bbsParseUtils.parseReturnMessage(s)
                    Log.d(TAG, "Recv reply comment info $s")
                    if (returnedMessage != null && returnedMessage.value == "post_reply_succeed") {
                        // success!
                        mHandler.post {
                            if (binding.smileyRootLayout.visibility == View.VISIBLE) {
                                binding.bbsThreadDetailEmoijButton.callOnClick()
                            }
                            binding.bbsThreadDetailCommentButton.setText(R.string.bbs_thread_comment)
                            binding.bbsThreadDetailCommentButton.isEnabled = true
                            binding.bbsThreadDetailCommentEditText.setText("")
                            reloadThePage()
                            threadDetailViewModel.threadStatusMutableLiveData.value?.let { threadDetailViewModel.getThreadDetail(it) }
                            //getThreadComment();
                            Toasty.success(applicationContext, getString(R.string.discuz_api_message_template, returnedMessage.value, returnedMessage.string), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        mHandler.post {
                            binding.bbsThreadDetailCommentButton.setText(R.string.bbs_thread_comment)
                            binding.bbsThreadDetailCommentButton.isEnabled = true
                            if (returnedMessage == null) {
                                Toasty.error(applicationContext, getString(R.string.network_failed), Toast.LENGTH_LONG).show()
                            } else {
                                Toasty.error(applicationContext, getString(R.string.discuz_api_message_template, returnedMessage.value, returnedMessage.string), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        //getSupportActionBar().setTitle(subject);
    }

    private fun launchFavoriteThreadDialog(favoriteThread: FavoriteThread) {
        val favoriteDialog = AlertDialog.Builder(this)
        favoriteDialog.setTitle(R.string.favorite_description)
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        favoriteDialog.setView(input)
        favoriteDialog.setPositiveButton(android.R.string.ok) { _, _ ->
            var description: String? = input.text.toString()
            description = if (TextUtils.isEmpty(description)) "" else description
            threadDetailViewModel.favoriteThread(favoriteThread,true,description)
        }
        favoriteDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
        val currentUrl: String
        currentUrl = if (viewThreadQueryStatus == null) {
            URLUtils.getViewThreadUrl(tid, "1")
        } else {
            URLUtils.getViewThreadUrl(tid, viewThreadQueryStatus.page.toString())
        }
        val id = item.itemId
        if (id == android.R.id.home) {
            finishAfterTransition()
            return true
        } else if (id == R.id.bbs_forum_nav_personal_center) {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra("UID", user!!.uid.toString())
            startActivity(intent)
            return true
        } else if (id == R.id.bbs_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.bbs_forum_nav_draft_box) {
            val intent = Intent(this, ThreadDraftActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            startActivity(intent, null)
            return true
        } else if (id == R.id.bbs_forum_nav_show_in_webview) {
            val intent = Intent(this, InternalWebViewActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra(ConstUtils.PASS_URL_KEY, currentUrl)
            Log.d(TAG, "Inputted URL $currentUrl")
            startActivity(intent)
            return true
        } else if (id == R.id.bbs_forum_nav_show_in_external_browser) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
            Log.d(TAG, "Inputted URL $currentUrl")
            startActivity(intent)
            return true
        } else if (id == R.id.bbs_forum_nav_dateline_sort) {
            val context: Context = this
            viewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
            Log.d(TAG, "You press sort btn " + viewThreadQueryStatus!!.datelineAscend)
            // bbsThreadStatus threadStatus = threadDetailViewModel.threadStatusMutableLiveData.getValue();
            viewThreadQueryStatus.datelineAscend = !viewThreadQueryStatus.datelineAscend
            Log.d(TAG, "Changed Ascend mode " + viewThreadQueryStatus.datelineAscend)
            Log.d(TAG, "Apply Ascend mode " + threadDetailViewModel.threadStatusMutableLiveData.value!!.datelineAscend)
            reloadThePage(viewThreadQueryStatus)
            Log.d(TAG, "After reload Ascend mode " + threadDetailViewModel.threadStatusMutableLiveData.value!!.datelineAscend)
            if (viewThreadQueryStatus.datelineAscend) {
                Toasty.success(context, getString(R.string.bbs_thread_status_ascend), Toast.LENGTH_SHORT).show()
            } else {
                Toasty.success(context, getString(R.string.bbs_thread_status_descend), Toast.LENGTH_SHORT).show()
            }
            // reload the parameters
            Log.d(TAG, "dateline ascend " + viewThreadQueryStatus.datelineAscend)
            threadDetailViewModel.getThreadDetail(viewThreadQueryStatus)
            invalidateOptionsMenu()
            return true
        } else if (id == R.id.bbs_share) {
            val result = threadDetailViewModel.threadPostResultMutableLiveData.value
            if (result != null && result.threadPostVariables != null && result.threadPostVariables.detailedThreadInfo != null) {
                val detailedThreadInfo = result.threadPostVariables.detailedThreadInfo
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_template,
                        detailedThreadInfo.subject, currentUrl))
                sendIntent.type = "text/plain"
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                return true
            } else {
                Toasty.info(this, getString(R.string.share_not_prepared), Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (id == R.id.bbs_favorite) {
            val result = threadDetailViewModel.threadPostResultMutableLiveData.value
            if (result != null && result.threadPostVariables != null && result.threadPostVariables.detailedThreadInfo != null) {
                val detailedThreadInfo = result.threadPostVariables.detailedThreadInfo
                val favoriteThread = detailedThreadInfo.toFavoriteThread(discuz.id, if (user != null) user!!.getUid() else 0)
                // save it to the database
                // boolean isFavorite = threadDetailViewModel.isFavoriteThreadMutableLiveData.getValue();
                val favoriteThreadInDB = threadDetailViewModel.favoriteThreadLiveData.value
                val isFavorite = favoriteThreadInDB != null
                if (isFavorite) {
                    Log.d(TAG, "Get Favroite thread$favoriteThreadInDB")
                    if (favoriteThreadInDB != null) {
                        threadDetailViewModel.favoriteThread(favoriteThreadInDB, false,"")
                    }
                } else {
                    Log.d(TAG, "is Favorite $isFavorite")
                    // open up a dialog
                    launchFavoriteThreadDialog(favoriteThread)
                    //new FavoritingThreadAsyncTask(favoriteThread,true).execute();
                }
            } else {
                Toasty.info(this, getString(R.string.favorite_thread_not_prepared), Toast.LENGTH_SHORT).show()
            }
        } else if (id == R.id.bbs_search) {
            val intent = Intent(this, SearchPostsActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            startActivity(intent)
            return true
        } else if (id == R.id.bbs_about_app) {
            val intent = Intent(this, AboutAppActivity::class.java)
            startActivity(intent)
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        // configureIntentData();
        if (user == null) {
            menuInflater.inflate(R.menu.bbs_incognitive_thread_nav_menu, menu)
        } else {
            menuInflater.inflate(R.menu.bbs_thread_nav_menu, menu)
        }
        if (supportActionBar != null) {
            val ViewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
            if (ViewThreadQueryStatus != null) {
                Log.d(TAG, "ON CREATE GET ascend mode in menu " + ViewThreadQueryStatus.datelineAscend)
                if (ViewThreadQueryStatus.datelineAscend) {
                    menu.findItem(R.id.bbs_forum_nav_dateline_sort).icon = ContextCompat.getDrawable(application, R.drawable.ic_baseline_arrow_upward_24)
                } else {
                    menu.findItem(R.id.bbs_forum_nav_dateline_sort).icon = ContextCompat.getDrawable(application, R.drawable.ic_baseline_arrow_downward_24)
                }
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val ViewThreadQueryStatus = threadDetailViewModel.threadStatusMutableLiveData.value
        if (ViewThreadQueryStatus != null) {
            Log.d(TAG, "ON PREPARE GET ascend mode in menu " + ViewThreadQueryStatus.datelineAscend)
            if (ViewThreadQueryStatus.datelineAscend) {
                menu.findItem(R.id.bbs_forum_nav_dateline_sort).icon = ContextCompat.getDrawable(this,R.drawable.ic_baseline_arrow_upward_24)
            } else {
                menu.findItem(R.id.bbs_forum_nav_dateline_sort).icon = ContextCompat.getDrawable(this,R.drawable.ic_baseline_arrow_downward_24)
            }
        }
        //boolean isFavorite = threadDetailViewModel.isFavoriteThreadMutableLiveData.getValue();
        val favoriteThread = threadDetailViewModel.favoriteThreadLiveData.value
        val isFavorite = favoriteThread != null
        Log.d(TAG, "Triggering favorite status $isFavorite")
        if (!isFavorite) {
            menu.findItem(R.id.bbs_favorite).icon = getDrawable(R.drawable.ic_not_favorite_24px)
            menu.findItem(R.id.bbs_favorite).setTitle(R.string.favorite)
        } else {
            menu.findItem(R.id.bbs_favorite).icon = getDrawable(R.drawable.ic_favorite_24px)
            menu.findItem(R.id.bbs_favorite).setTitle(R.string.unfavorite)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun insertViewHistory(viewHistory: ViewHistory){
        val dao = ViewHistoryDatabase.getInstance(applicationContext).dao
        Thread{
            val viewHistories = dao.getViewHistoryByBBSIdAndTid(viewHistory.belongedBBSId, viewHistory.tid)
            if(viewHistories.size == 0){
                dao.insert(viewHistory)
            }
            else{
                for (i in viewHistories.indices) {
                    val updatedViewHistory = viewHistories[i]
                    updatedViewHistory.recordAt = Date()
                }
                dao.insert(viewHistories)
            }
        }.start()
    }

    companion object {
        private val TAG = ThreadActivity::class.java.simpleName
    }

}

