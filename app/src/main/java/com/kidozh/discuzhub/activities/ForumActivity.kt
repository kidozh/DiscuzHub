package com.kidozh.discuzhub.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ForumActivity
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter.OnRefreshBtnListener
import com.kidozh.discuzhub.adapter.SubForumAdapter
import com.kidozh.discuzhub.adapter.ThreadAdapter
import com.kidozh.discuzhub.database.FavoriteForumDatabase
import com.kidozh.discuzhub.database.ViewHistoryDatabase.Companion.getInstance
import com.kidozh.discuzhub.databinding.ActivityBbsShowForumBinding
import com.kidozh.discuzhub.dialogs.ForumDisplayOptionFragment
import com.kidozh.discuzhub.dialogs.ForumRuleFragment
import com.kidozh.discuzhub.entities.*
import com.kidozh.discuzhub.results.ApiMessageActionResult
import com.kidozh.discuzhub.results.ForumResult
import com.kidozh.discuzhub.results.MessageResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.*
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.syncInformation
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod.OnLinkClickedListener
import com.kidozh.discuzhub.viewModels.ForumViewModel
import es.dmoral.toasty.Toasty
import retrofit2.Call
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

class ForumActivity : BaseStatusActivity(), OnRefreshBtnListener, OnLinkClickedListener {
    lateinit var forum: Forum
    lateinit var adapter: ThreadAdapter
    lateinit var subForumAdapter: SubForumAdapter
    var fid: String? = null

    //MutableLiveData<bbsDisplayForumQueryStatus> forumStatusMutableLiveData;
    lateinit var forumViewModel: ForumViewModel
    private var hasLoadOnce = false
    lateinit var binding: ActivityBbsShowForumBinding
    var networkIndicatorAdapter = NetworkIndicatorAdapter()
    var concatAdapter: ConcatAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBbsShowForumBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        forumViewModel = ViewModelProvider(this)[ForumViewModel::class.java]
        configureIntentData()
        bindViewModel()
        configureActionBar()
        Log.d(TAG, "Get bbs information $bbsInfo")
        initLiveData()

        configureFab()
        configureRecyclerview()
        configureSwipeRefreshLayout()
        configurePostThreadBtn()
        //  start to get the first page info
        forumViewModel.getNextThreadList()
    }

    private fun configureIntentData() {
        val intent = intent
        forum = intent.getSerializableExtra(ConstUtils.PASS_FORUM_THREAD_KEY) as Forum
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        URLUtils.setBBS(bbsInfo)
        fid = forum.fid.toString()
        forumViewModel.setBBSInfo(bbsInfo!!, user, forum)
        // hasLoadOnce = intent.getBooleanExtra(bbsConstUtils.PASS_IS_VIEW_HISTORY,false);
        forumRuleFragment = ForumRuleFragment(bbsInfo!!,user,forum)
    }

    private fun bindViewModel() {
        forumViewModel.totalThreadListMutableLiveData.observe(this, {
            var threadTypeMap: HashMap<String, String> = HashMap()
            if (forumViewModel.displayForumResultMutableLiveData.value != null &&
                forumViewModel.displayForumResultMutableLiveData.value!!.forumVariables.threadTypeInfo != null
            ) {
                threadTypeMap =
                    forumViewModel.displayForumResultMutableLiveData.value!!.forumVariables.threadTypeInfo!!.idNameMap
            }
            adapter.updateListAndType(it, threadTypeMap)
            if (forumViewModel.forumStatusMutableLiveData.value != null) {
                val page = forumViewModel.forumStatusMutableLiveData.value!!.page
                // point to the next page
                if (page == 2) {
                    binding.bbsForumThreadRecyclerview.smoothScrollToPosition(0)
                }
            }
        })
        forumViewModel.networkState.observe(this, { integer: Int ->
            Log.d(TAG, "Network state changed $integer")
            when (integer) {
                ConstUtils.NETWORK_STATUS_LOADING -> {
                    binding.bbsForumInfoSwipeRefreshLayout.isRefreshing = true
                    networkIndicatorAdapter.setLoadingStatus()
                }
                ConstUtils.NETWORK_STATUS_LOADED_ALL -> {
                    binding.bbsForumInfoSwipeRefreshLayout.isRefreshing = false
                    //Log.d(TAG,"Network changed "+integer);
                    networkIndicatorAdapter.setLoadedAllStatus()
                    val prefs = PreferenceManager.getDefaultSharedPreferences(
                        application
                    )
                    val needVibrate = prefs.getBoolean(
                        getString(R.string.preference_key_vibrate_when_load_all),
                        true
                    )
                    Toasty.success(
                        application,
                        getString(R.string.thread_has_load_all),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (needVibrate) {
                        VibrateUtils.vibrateSlightly(this)
                    }
                }
                ConstUtils.NETWORK_STATUS_SUCCESSFULLY -> {
                    binding.bbsForumInfoSwipeRefreshLayout.isRefreshing = false
                    networkIndicatorAdapter.setLoadSuccessfulStatus()
                }
                else -> {
                    binding.bbsForumInfoSwipeRefreshLayout.isRefreshing = false
                }
            }
        })
        forumViewModel.errorMessageMutableLiveData.observe(
            this,
            { errorMessage: ErrorMessage? ->
                Log.d(TAG, "recv error message $errorMessage")
                if (errorMessage != null) {
                    Toasty.error(
                        applicationContext,
                        getString(
                            R.string.discuz_api_message_template,
                            errorMessage.key,
                            errorMessage.content
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    networkIndicatorAdapter.setErrorStatus(errorMessage)
                    VibrateUtils.vibrateForError(application)
                }
            })
        forumViewModel.displayForumResultMutableLiveData.observe(
            this,
            { forumResult -> // deal with sublist
                Log.d(TAG, "GET result $forumResult")
                if (forumResult != null) {
                    Log.d(TAG, "GET sublist size " + forumResult.forumVariables.subForumLists.size)
                    subForumAdapter.setSubForumInfoList(forumResult.forumVariables.subForumLists)
                    val forum = forumResult.forumVariables.forum
                    this@ForumActivity.forum = forum
                    binding.toolbar.title = forum.name
                    binding.toolbar.subtitle = forum.fid.toString()

                }
            })
        forumViewModel.favoriteForumLiveData!!.observe(this, { favoriteForum: FavoriteForum? ->
            Log.d(TAG, "Detecting change favorite forum $favoriteForum")
            if (favoriteForum != null) {
                Log.d(TAG, "favorite forum id " + favoriteForum.id)
            }
            invalidateOptionsMenu()
        })
        forumViewModel.displayForumResultMutableLiveData.observe(
            this,
            { result: ForumResult? ->
                if (result != null) {
                    val forum = result.forumVariables.forum
                    if (!hasLoadOnce) {
                        recordViewHistory(forum)
                        hasLoadOnce = true
                    }
                }
            })

    }

    private fun initLiveData() {
        val forumStatus = DisplayForumQueryStatus(forum.fid, 1)
        forumViewModel.forumStatusMutableLiveData.value = forumStatus
    }

    private var forumDisplayOptionFragment = ForumDisplayOptionFragment()
    private lateinit var forumRuleFragment: ForumRuleFragment
    private fun recordViewHistory(forum: Forum) {

        val recordHistory = UserPreferenceUtils.viewHistoryEnabled(this)
        Log.i(TAG,"Record view history ? ${recordHistory}")

        if (recordHistory) {
            // check if it exsit before

            insertViewHistory(
                ViewHistory(
                    forum.iconUrl,
                    forum.name,
                    bbsInfo!!.id,
                    forum.description,
                    ViewHistory.VIEW_TYPE_FORUM,
                    forum.fid,
                    0,
                    Date()
                )
            )
        }
    }

    private fun reConfigureAndRefreshPage(status: DisplayForumQueryStatus?) {
        status!!.hasLoadAll = false
        status.page = 1
        forumViewModel.forumStatusMutableLiveData.postValue(status)
        forumViewModel.setForumStatusAndFetchThread(forumViewModel.forumStatusMutableLiveData.value!!)
    }

    private fun configureSwipeRefreshLayout() {
        binding.bbsForumInfoSwipeRefreshLayout.setOnRefreshListener {
            val status = forumViewModel.forumStatusMutableLiveData.value
            reConfigureAndRefreshPage(status)
        }
    }

    private fun configurePostThreadBtn() {
        val context: Context = this
        if (user == null) {
            binding.bbsForumFab.visibility = View.GONE
        }
        binding.bbsForumFab.setOnClickListener {
            if (forumViewModel.displayForumResultMutableLiveData.value != null
            ) {
                val userInResponse =
                    forumViewModel.displayForumResultMutableLiveData.value!!.forumVariables.userBriefInfo
                if (userInResponse.isValid) {
                    val intent = Intent(context, PublishActivity::class.java)
                    intent.putExtra("fid", fid)
                    intent.putExtra("fid_name", forum.name)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                    intent.putExtra(ConstUtils.PASS_POST_TYPE, ConstUtils.TYPE_POST_THREAD)
                    if (forumViewModel.displayForumResultMutableLiveData
                            .value!!.forumVariables.threadTypeInfo != null
                    ) {
                        intent.putExtra(
                            ConstUtils.PASS_THREAD_CATEGORY_KEY,
                            forumViewModel.displayForumResultMutableLiveData
                                .value!!.forumVariables.threadTypeInfo!!.idNameMap as Serializable
                        )
                    }
                    Log.d(TAG, "You pass fid name" + forum.name)
                    startActivity(intent)
                } else {
                    Toasty.info(
                        context,
                        context.getString(R.string.bbs_require_login_to_comment),
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                    startActivity(intent)
                }
            } else {
                binding.bbsForumFab.visibility = View.GONE
            }
        }
    }

    private fun configureActionBar() {
        binding.toolbar.title = bbsInfo!!.site_name
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        binding.toolbar.title = bbsInfo!!.site_name

        if (forum.name != null) {
            binding.toolbar.title = forum.name
            //getSupportActionBar().setSubtitle(forum.name);
        }
    }



    private fun configureRecyclerview() {
        binding.bbsForumSublist.setHasFixedSize(true)
        binding.bbsForumSublist.itemAnimator = getRecyclerviewAnimation(this)
        binding.bbsForumSublist.layoutManager = GridLayoutManager(this, 4)
        subForumAdapter = SubForumAdapter(bbsInfo, user)
        binding.bbsForumSublist.adapter = getAnimatedAdapter(this, subForumAdapter)
        binding.bbsForumThreadRecyclerview.setHasFixedSize(true)
        binding.bbsForumThreadRecyclerview.itemAnimator = getRecyclerviewAnimation(this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.bbsForumThreadRecyclerview.layoutManager = linearLayoutManager
        adapter = ThreadAdapter(null, bbsInfo!!, user)
        concatAdapter = ConcatAdapter(adapter, networkIndicatorAdapter)
        binding.bbsForumThreadRecyclerview.adapter = getAnimatedAdapter(this, concatAdapter!!)
        binding.bbsForumThreadRecyclerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val hasLoadAll =
                        forumViewModel.networkState.value == ConstUtils.NETWORK_STATUS_LOADED_ALL
                    val loading =
                        forumViewModel.networkState.value == ConstUtils.NETWORK_STATUS_LOADING
                    val loadAllOnce = forumViewModel.loadAllNoticeOnce.value!!
                    Log.d(
                        TAG,
                        "load all " + hasLoadAll + " page " + forumViewModel.forumStatusMutableLiveData.value!!.page
                    )
                    if (hasLoadAll) {
                        if (!loadAllOnce) {
                            Toasty.success(
                                application,
                                getString(
                                    R.string.has_load_all_threads_in_forum,
                                    adapter.itemCount
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                            VibrateUtils.vibrateSlightly(application)
                            forumViewModel.loadAllNoticeOnce.postValue(true)
                        }
                    } else {
                        if (!loading) {
                            val status = forumViewModel.forumStatusMutableLiveData.value
                            if (status != null) {
                                forumViewModel.setForumStatusAndFetchThread(status)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun configureFab() {}
    override fun onLinkClicked(url: String): Boolean {
        return bbsLinkMovementMethod.onLinkClicked(this, bbsInfo!!, user, url)
    }

    override fun onRefreshBtnClicked() {
        val status = forumViewModel.forumStatusMutableLiveData.value
        if (status != null) {
            forumViewModel.setForumStatusAndFetchThread(status)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentUrl: String
        val forumStatus = forumViewModel.forumStatusMutableLiveData.value
        currentUrl = if (forumStatus == null || forumStatus.page == 1) {
            URLUtils.getForumDisplayUrl(fid, "1")
        } else {
            URLUtils.getForumDisplayUrl(fid, (forumStatus.page - 1).toString())
        }
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                false
            }
            R.id.forum_filter -> {
                forumDisplayOptionFragment.show(
                    supportFragmentManager,
                    ForumDisplayOptionFragment::class.java.simpleName
                )
                true
            }
            R.id.forum_info ->{
                forumRuleFragment.show(
                    supportFragmentManager,
                    ForumDisplayOptionFragment::class.java.simpleName
                )
                true
            }

            R.id.bbs_forum_nav_personal_center -> {
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                intent.putExtra("UID", user!!.uid.toString())
                startActivity(intent)
                true
            }
            R.id.bbs_forum_nav_draft_box -> {
                val intent = Intent(this, ThreadDraftActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                startActivity(intent, null)
                true
            }
            R.id.bbs_forum_nav_show_in_webview -> {
                val intent = Intent(this, InternalWebViewActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                intent.putExtra(ConstUtils.PASS_URL_KEY, currentUrl)
                Log.d(TAG, "Inputted URL $currentUrl")
                startActivity(intent)
                true
            }
            R.id.bbs_search -> {
                val intent = Intent(this, SearchPostsActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                startActivity(intent)
                true
            }
            R.id.bbs_forum_nav_show_in_external_browser -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                Log.d(TAG, "Inputted URL $currentUrl")
                startActivity(intent)
                true
            }
            R.id.bbs_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.bbs_about_app -> {
                val intent = Intent(this, AboutAppActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.bbs_share -> {
                val result = forumViewModel.displayForumResultMutableLiveData.value
                if (result != null) {
                    val forum = result.forumVariables.forum
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT, getString(
                            R.string.share_template,
                            forum.name, URLUtils.getForumDisplayUrl(this.forum.fid.toString(), "1")
                        )
                    )
                    sendIntent.type = "text/plain"
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                } else {
                    Toasty.info(this, getString(R.string.share_not_prepared), Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.bbs_favorite -> {
                val result = forumViewModel.displayForumResultMutableLiveData.value
                if (result != null) {
                    val forum = result.forumVariables.forum
                    val favoriteForum = forum.toFavoriteForm(
                        bbsInfo!!.id,
                        if (user != null) user!!.uid else 0
                    )
                    // save it to the database
                    // boolean isFavorite = threadDetailViewModel.isFavoriteThreadMutableLiveData.getValue();
                    val favoriteForumInDB = forumViewModel.favoriteForumLiveData!!.value
                    Log.d(TAG, "Get db favorite formD $favoriteForumInDB")
                    val isFavorite = favoriteForumInDB != null
                    if (isFavorite) {
                        if (favoriteForumInDB != null) {
                            favoriteForum(favoriteForumInDB, false,"")
                        }
                    } else {
                        // open up a dialog
                        launchFavoriteForumDialog(favoriteForum)
                        //new FavoritingThreadAsyncTask(favoriteThread,true).execute();
                    }
                } else {
                    Toasty.info(
                        this,
                        getString(R.string.favorite_thread_not_prepared),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


    private fun insertViewHistory(viewHistory: ViewHistory){
        Thread{
            val dao = getInstance(applicationContext).dao
            val viewHistories = dao.getViewHistoryByBBSIdAndFid(viewHistory.belongedBBSId, viewHistory.fid)
            Log.d(TAG,"Recv history ${viewHistories} ${viewHistories.size}")
            if (viewHistories == null || viewHistories.size == 0) {
                runOnUiThread{
                    forumRuleFragment.show(
                        supportFragmentManager,
                        ForumDisplayOptionFragment::class.java.simpleName
                    )
                }


                dao.insert(viewHistory)
            } else {
                for (i in viewHistories.indices) {
                    val updatedViewHistory = viewHistories[i]
                    updatedViewHistory.recordAt = Date()
                }
                dao.insert(viewHistories)
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        // configureIntentData();
        if (user == null) {
            menuInflater.inflate(R.menu.menu_incognitive_forum_nav_menu, menu)
        } else {
            menuInflater.inflate(R.menu.bbs_forum_nav_menu, menu)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val favoriteForum = forumViewModel.favoriteForumLiveData!!.value
        val isFavorite = favoriteForum != null
        Log.d(TAG, "Triggering favorite status $isFavorite $favoriteForum")
        if (!isFavorite) {
            menu.findItem(R.id.bbs_favorite).icon =
                ContextCompat.getDrawable(this, R.drawable.ic_not_favorite_24px)
            menu.findItem(R.id.bbs_favorite).setTitle(R.string.favorite)
        } else {
            menu.findItem(R.id.bbs_favorite).icon =
                ContextCompat.getDrawable(this, R.drawable.ic_favorite_24px)
            menu.findItem(R.id.bbs_favorite).setTitle(R.string.unfavorite)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun launchFavoriteForumDialog(favoriteForum: FavoriteForum) {
        val favoriteDialog = AlertDialog.Builder(this)
        favoriteDialog.setTitle(R.string.favorite_description)
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        favoriteDialog.setView(input)
        favoriteDialog.setPositiveButton(android.R.string.ok) { _, _ ->
            var description: String? = input.text.toString()
            description = if (TextUtils.isEmpty(description)) "" else description
            favoriteForum(favoriteForum, true, description)
        }
        favoriteDialog.show()
    }

    fun favoriteForum(favoriteForum: FavoriteForum, favorite: Boolean, description: String?){
        if(description != null){
            favoriteForum.description = description
        }

        var favoriteForumActionResultCall: Call<ApiMessageActionResult?>? = null
        var messageResult: MessageResult? = null

        val retrofit = NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, forumViewModel.client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val result = forumViewModel.displayForumResultMutableLiveData.value
        val dao = FavoriteForumDatabase.getInstance(applicationContext).dao
        if (result != null && favoriteForum.userId != 0 && syncInformation(
                application
            )
        ) {
            Log.d(TAG, "Favorite formhash " + result.forumVariables.formHash)
            if (favorite) {
                favoriteForumActionResultCall = service.favoriteForumActionResult(
                    result.forumVariables.formHash, favoriteForum.idKey, description
                )
            } else {
                Log.d(TAG, "Favorite id " + favoriteForum.favid)
                if (favoriteForum.favid == 0) {
                    // just remove it from database
                } else {
                    favoriteForumActionResultCall = service.unfavoriteForumActionResult(
                        result.forumVariables.formHash,
                        "true",
                        "a_delete_" + favoriteForum.favid,
                        favoriteForum.favid
                    )
                }
            }
        }
        var favoriteResult = true
        var containError = true
        Thread{
            if (favoriteForumActionResultCall != null) {
                try {
                    Log.d(
                        TAG,
                        "request favorite url ${favoriteForumActionResultCall.request()}"
                    )
                    val response = favoriteForumActionResultCall.execute()
                    //Log.d(TAG,"get response "+response.raw().body().string());
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()
                        messageResult = result!!.message
                        val key = result.message!!.key
                        if (favorite && key == "favorite_do_success") {
                            dao!!.delete(
                                bbsInfo!!.id,
                                if (user != null) user!!.uid else 0,
                                favoriteForum.idKey
                            )
                            dao.insert(favoriteForum)
                        }
                        if (favorite && key == "favorite_repeat") {
                            dao!!.delete(
                                bbsInfo!!.id,
                                if (user != null) user!!.uid else 0,
                                favoriteForum.idKey
                            )
                            dao.insert(favoriteForum)
                        } else if (!favorite && key == "do_success") {
                            dao.delete(favoriteForum)
                            dao.delete(bbsInfo!!.id, user!!.uid, favoriteForum.idKey)
                        } else {
                            containError = true
                        }
                    } else {
                        messageResult = MessageResult()
                        messageResult!!.content = getString(R.string.network_failed)
                        messageResult!!.key = response.code().toString()
                        if (favorite) {
                            dao!!.delete(
                                bbsInfo!!.id,
                                if (user != null) user!!.uid else 0,
                                favoriteForum.idKey
                            )
                            dao.insert(favoriteForum)
                            favoriteResult =true
                        } else {

                            // clear potential
                            dao!!.delete(
                                bbsInfo!!.id,
                                if (user != null) user!!.uid else 0,
                                favoriteForum.idKey
                            )
                            //dao.delete(favoriteThread);
                            Log.d(TAG, "Just remove it from database " + favoriteForum.idKey)
                            favoriteResult =false
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    containError = true
                    messageResult = MessageResult()
                    messageResult!!.content = e.message
                    messageResult!!.key = e.toString()
                    // insert as local database
                    if (favorite) {
                        dao!!.delete(
                            bbsInfo!!.id,
                            if (user != null) user!!.uid else 0,
                            favoriteForum.idKey
                        )
                        dao.insert(favoriteForum)
                        favoriteResult = true
                    } else {
                        // clear potential
                        dao!!.delete(
                            bbsInfo!!.id,
                            if (user != null) user!!.uid else 0,
                            favoriteForum.idKey
                        )
                        //dao.delete(favoriteThread);
                        Log.d(TAG, "Just remove it from database " + favoriteForum.idKey)
                        favoriteResult = false
                    }
                }
            } else {
                if (favorite) {
                    dao!!.delete(
                        bbsInfo!!.id,
                        if (user != null) user!!.uid else 0,
                        favoriteForum.idKey
                    )
                    dao.insert(favoriteForum)
                    favoriteResult = true
                } else {
                    // clear potential
                    dao!!.delete(
                        bbsInfo!!.id,
                        if (user != null) user!!.uid else 0,
                        favoriteForum.idKey
                    )
                    //dao.delete(favoriteThread);
                    Log.d(TAG, "Just remove it from database " + favoriteForum.idKey)
                    favoriteResult = false
                }
            }
        }.start()

        if (messageResult != null) {
            val key = messageResult!!.key
            if (favoriteResult && key == "favorite_do_success") {
                Toasty.success(
                    application,
                    getString(
                        R.string.discuz_api_message_template,
                        messageResult!!.key,
                        messageResult!!.content
                    ),
                    Toast.LENGTH_LONG
                ).show()
            } else if (!favoriteResult && key == "do_success") {
                Toasty.success(
                    application,
                    getString(
                        R.string.discuz_api_message_template,
                        messageResult!!.key,
                        messageResult!!.content
                    ),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toasty.warning(
                    application,
                    getString(
                        R.string.discuz_api_message_template,
                        messageResult!!.key,
                        messageResult!!.content
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            if (favoriteResult) {
                Toasty.success(application, getString(R.string.favorite), Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toasty.success(application, getString(R.string.unfavorite), Toast.LENGTH_SHORT)
                    .show()
            }
            VibrateUtils.vibrateSlightly(application)
        }
    }



    companion object {
        private val TAG = ForumActivity::class.java.simpleName
    }
}