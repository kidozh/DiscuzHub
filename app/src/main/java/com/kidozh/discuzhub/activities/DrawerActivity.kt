package com.kidozh.discuzhub.activities

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.BlankBBSFragment.BlankBBSFragment
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardFragment
import com.kidozh.discuzhub.activities.ui.HotThreads.HotThreadsFragment
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment
import com.kidozh.discuzhub.activities.ui.home.HomeFragment
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment
import com.kidozh.discuzhub.database.ViewHistoryDatabase
import com.kidozh.discuzhub.database.UserDatabase
import com.kidozh.discuzhub.databinding.ActivityNewMainDrawerBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.*
import com.kidozh.discuzhub.utilities.bbsParseUtils.noticeNumInfo
import com.kidozh.discuzhub.viewModels.MainDrawerViewModel
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader.Companion.init
import com.mikepenz.materialdrawer.util.updateBadge
import com.mikepenz.materialdrawer.util.updateItem
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import es.dmoral.toasty.Toasty
import java.io.InputStream
import java.util.*

class DrawerActivity : BaseStatusActivity(), bbsPrivateMessageFragment.OnNewMessageChangeListener, bbsPublicMessageFragment.OnNewMessageChangeListener, UserNotificationFragment.OnNewMessageChangeListener {
    lateinit var viewModel: MainDrawerViewModel
    val MODE_USER_IGCONGTIVE:Long = -18510478
    val FUNC_ADD_A_BBS:Long = -2
    val FUNC_MANAGE_BBS:Long = -3
    val FUNC_ADD_AN_ACCOUNT:Long = -4
    val FUNC_MANAGE_ACCOUNT:Long = -5
    val FUNC_REGISTER_ACCOUNT:Long = -6
    val FOOTER_SETTINGS:Long = -955415674
    val FOOTER_ABOUT:Long = -964245451
    val FUNC_VIEW_HISTORY:Long = -85642154
    val FUNC_DRFAT_BOX :Long = -85642414
    val FUNC_SEARCH :Long = -85647
    var savedInstanceState: Bundle? = null
    lateinit var headerView: AccountHeaderView
    lateinit var binding: ActivityNewMainDrawerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
        binding = ActivityNewMainDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recoverInstanceState(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainDrawerViewModel::class.java)
        configureToolbar()
        initBBSDrawer()
        bindViewModel()
        initFragments()
        checkTermOfUse()
    }

    private fun checkTermOfUse() {
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            //getSupportActionBar().setDisplayShowTitleEnabled(true);
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
        }
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_menu_24px)
    }

    private fun bindViewModel() {
        viewModel.allBBSInformationMutableLiveData.observe(this, { Discuzs: List<Discuz>? ->
            //binding.materialDrawerSliderView.getItemAdapter().clear();
            headerView.clear()
            //drawerAccountHeader.clear();
            if (Discuzs == null || Discuzs.size == 0) {
                // hide the navIcon
                // show bbs page
                binding.toolbar.navigationIcon = null
                binding.toolbar.title = getString(R.string.app_name)

            } else {
                // bind to headview
                    // show navbar
                binding.toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_menu_24px)
                val accountProfiles: MutableList<IProfile> = ArrayList()
                for (i in Discuzs.indices) {
                    val currentBBSInfo = Discuzs[i]
                    notificationUtils.createUsersUpdateChannel(applicationContext)
                    //URLUtils.setBBS(currentBBSInfo);
                    Log.d(TAG, "Load url " + URLUtils.getBBSLogoUrl(currentBBSInfo.base_url))
                    val bbsProfile = ProfileDrawerItem().apply {
                        identifier = currentBBSInfo.id.toLong()
                        name = StringHolder(currentBBSInfo.site_name)
                        icon = ImageHolder(URLUtils.getBBSLogoUrl(currentBBSInfo.base_url))
                        isNameShown = true
                        description = StringHolder(currentBBSInfo.base_url)

                    }

                    if (currentBBSInfo.apiVersion > 4) {
                        // marked as advanced
                        bbsProfile.badge = StringHolder(
                                getString(R.string.bbs_api_advance)
                        )
                        val badgeStyle = BadgeStyle()
                        badgeStyle.badgeBackground = ContextCompat.getDrawable(this,R.color.colorAPI5BadgeBackgroundColor)
                        val colorHolder = ColorHolder.fromColorRes(R.color.colorPureWhite)
                        badgeStyle.textColor = colorHolder
                        bbsProfile.badgeStyle = badgeStyle
                    }
                    accountProfiles.add(bbsProfile)
                }
                headerView.profiles = accountProfiles
                // drawerAccountHeader.setProfiles(accountProfiles);
                if (Discuzs.size > 0) {
                    val activeIdentifier = UserPreferenceUtils.getLastSelectedDrawerItemIdentifier(this)
                    if (activeIdentifier >= 0) {
                        headerView.setActiveProfile(activeIdentifier.toLong(), true)
                    } else {
                        val currentBBSInfo = Discuzs[0]
                        headerView.setActiveProfile(currentBBSInfo.id.toLong(), true)
                    }
                }
            }
            // add bbs
            val addBBSProfile = ProfileSettingDrawerItem().apply {
                name = StringHolder(getString(R.string.add_a_bbs))
                identifier = FUNC_ADD_A_BBS.toLong()
                description = StringHolder(getString(R.string.title_add_a_forum_by_url))
                isSelectable = false
                icon = ImageHolder(R.drawable.ic_add_24px)
            }

            headerView.addProfiles(addBBSProfile)
            Log.d(TAG, "Add a bbs profile")
            // manage bbs
            if (Discuzs != null && Discuzs.isNotEmpty()) {
                val manageBBSProfile = ProfileSettingDrawerItem().apply {
                    name = StringHolder(getString(R.string.manage_bbs))
                    identifier = FUNC_MANAGE_BBS.toLong()
                    description = StringHolder(getString(R.string.manage_bbs_description))
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_manage_bbs_24px)
                }
                headerView.addProfiles(manageBBSProfile)
            }
        })
        viewModel.forumUserListMutableLiveData.observe(this, { forumUserBriefInfos ->
            // clear it first
            // drawerResult.removeAllItems();
            binding.materialDrawerSliderView.itemAdapter.clear()
            Log.d(TAG, "get forumUsers $forumUserBriefInfos")
            if (forumUserBriefInfos != null) {
                for (i in forumUserBriefInfos.indices) {
                    val userBriefInfo = forumUserBriefInfos[i]
                    Log.d(TAG, "Getting user brief info " + userBriefInfo.username)
                    val uid = userBriefInfo.uid.toInt()
                    var avatar_num = uid % 16
                    if (avatar_num < 0) {
                        avatar_num = -avatar_num
                    }


                    URLUtils.setBBS(viewModel.currentBBSInformationMutableLiveData.value)
                    val userProfile = ProfileDrawerItem().apply {
                        isSelectable = true
                        name = StringHolder(userBriefInfo.username)
                        icon = ImageHolder(URLUtils.getDefaultAvatarUrlByUid(userBriefInfo.uid))
                        isNameShown = true
                        identifier = userBriefInfo.id.toLong()
                        description = StringHolder(getString(R.string.user_id_description, userBriefInfo.uid))
                    }
                    binding.materialDrawerSliderView.itemAdapter.add(userProfile)
                }
                if (forumUserBriefInfos.size > 0) {
                    val userBriefInfo = forumUserBriefInfos[0]
                    // get first
                    binding.materialDrawerSliderView.setSelection(userBriefInfo.id.toLong(), true)
                }
            }
            val bbsInformationList = viewModel.allBBSInformationMutableLiveData.value
            if (bbsInformationList == null || bbsInformationList.size == 0) {
                // add bbs
                val addBBSProfile = PrimaryDrawerItem().apply {
                    name = StringHolder(getString(R.string.add_a_bbs))
                    identifier = FUNC_ADD_A_BBS
                    description = StringHolder(getString(R.string.title_add_a_forum_by_url))
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_add_24px)
                }


                binding.materialDrawerSliderView.itemAdapter.add(addBBSProfile)
            } else {
                val incognito = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.bbs_anonymous)
                    icon = ImageHolder(R.drawable.ic_incognito_user_24px)
                    isSelectable = true
                    identifier = MODE_USER_IGCONGTIVE
                    description = StringHolder(R.string.user_anonymous_description)
                }

                binding.materialDrawerSliderView.itemAdapter.add(incognito)
                // other profiles
                val addAccount = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.add_a_account)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_person_add_24)
                    identifier = FUNC_ADD_AN_ACCOUNT
                    description = StringHolder(R.string.bbs_add_an_account_description)
                }

                val registerAccount = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.register_an_account)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_how_to_reg_24)
                    identifier = FUNC_REGISTER_ACCOUNT
                    description = StringHolder(R.string.register_an_account_description)
                }

                // manage
                val manageAccount = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.bbs_manage_users)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_people_24)
                    identifier = FUNC_MANAGE_ACCOUNT
                    description = StringHolder(R.string.bbs_manage_users_description)
                }

                var badgeStyle = BadgeStyle().apply {
                    textColor = ColorHolder.fromColorRes(R.color.colorPureWhite)
                    badgeBackground = getDrawable(R.color.colorPrimary)
                }

                val draftItem = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.bbs_draft_box)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_drafts_24)
                    identifier = FUNC_DRFAT_BOX
                    description = StringHolder(R.string.draft_box_description)

                }



                // history
                val viewHistory = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.view_history)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_history_24)
                    identifier = FUNC_VIEW_HISTORY
                    description = StringHolder(R.string.preference_summary_on_record_history)
                    badgeStyle = badgeStyle
                }

                // search
                val searchItem = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.search)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_search_24)
                    identifier = FUNC_SEARCH
                    description = StringHolder(R.string.search_description)
                    badgeStyle = badgeStyle
                }

                val settingItem = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.action_settings)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_app_settings_alt_24)
                    identifier = FOOTER_SETTINGS
                    description = StringHolder(R.string.settings_description)

                }

                val aboutItem = PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.app_about)
                    isSelectable = false
                    icon = ImageHolder(R.drawable.ic_baseline_info_24)
                    identifier = FOOTER_ABOUT
                    description = StringHolder(R.string.about_description)

                }


                binding.materialDrawerSliderView.itemAdapter.add(
                        DividerDrawerItem(),
                        addAccount,
                        registerAccount,
                        manageAccount,
                        draftItem,
                        viewHistory,
                        searchItem,
                        DividerDrawerItem(),
                        settingItem,
                        aboutItem
                        
                )
                if (forumUserBriefInfos == null || forumUserBriefInfos.size == 0) {
                    Log.d(TAG, "Trigger igcontive mode")
                    binding.materialDrawerSliderView.setSelection(MODE_USER_IGCONGTIVE.toLong(), true)
                }
            }
        })
        viewModel.currentBBSInformationMutableLiveData.observe(this, { Discuz: Discuz? ->
            bbsInfo = Discuz
            if (Discuz != null) {
                binding.toolbarTitle.text = Discuz.site_name
                if (supportActionBar != null) {
                    supportActionBar!!.title = Discuz.site_name
                }
                val id = Discuz.id
                val allUsersInCurrentBBSLiveData = UserDatabase.getInstance(application)
                        .getforumUserBriefInfoDao()
                        .getAllUserByBBSID(id)
                allUsersInCurrentBBSLiveData.observe(this, { Users: List<User?> ->
                    Log.d(TAG, "Updating " + id + " users information " + Users.size)
                    viewModel.forumUserListMutableLiveData.postValue(Users)
                })
                // updating history number
                Thread{
                    val historyCount = ViewHistoryDatabase.getInstance(application).dao.getViewHistoryCount(id)
                    Log.d(TAG,"GET History count "+historyCount)


                    runOnUiThread {
                        val viewHistory = PrimaryDrawerItem().apply {
                            name = StringHolder(R.string.view_history)
                            isSelectable = false
                            icon = ImageHolder(R.drawable.ic_baseline_history_24)
                            identifier = FUNC_VIEW_HISTORY
                            description = StringHolder(R.string.preference_summary_on_record_history)
                            badgeStyle = badgeStyle
                            badge = StringHolder(historyCount.toString())
                        }

                        binding.materialDrawerSliderView.updateBadge(
                                FUNC_VIEW_HISTORY,
                                StringHolder(historyCount.toString())
                        )
                        binding.materialDrawerSliderView.adapter.notifyAdapterDataSetChanged()
                        binding.materialDrawerSliderView.adapterWrapper?.notifyDataSetChanged()
                        binding.materialDrawerSliderView.itemAdapter.fastAdapter?.notifyDataSetChanged()
                        binding.materialDrawerSliderView.updateItem(viewHistory)
                    }



                }.start()


            } else {
                binding.toolbarTitle.setText(R.string.no_bbs_found_in_db)
            }
        })
        viewModel.currentForumUserBriefInfoMutableLiveData.observe(this, { User: User? ->
            if (User == null) {
                binding.toolbarSubtitle.setText(R.string.bbs_anonymous)
            } else {
                binding.toolbarSubtitle.text = User.username
            }
            user = User
            renderViewPageAndBtmView()
        })
    }

    private fun initBBSDrawer() {
        val activity: Activity = this

        // account header
        headerView = AccountHeaderView(activity)
        headerView.onAccountHeaderListener = label@{ view: View?, iProfile: IProfile, aBoolean: Boolean? ->
            val bbsId = iProfile.identifier
            Log.d(TAG, "profile changed " + bbsId + " name " + iProfile.name)
            if (bbsId > 0) {
                // change view model
                val allBBSList = viewModel.allBBSInformationMutableLiveData.value
                if (allBBSList != null && allBBSList.size > 0) {
                    var i = 0
                    while (i < allBBSList.size) {
                        val curBBS = allBBSList[i]
                        if (curBBS.id.toLong() == bbsId) {
                            viewModel.currentBBSInformationMutableLiveData.value = curBBS
                            UserPreferenceUtils.saveLastSelectedDrawerItemIdentifier(activity, curBBS.id)
                            return@label false
                        }
                        i++
                    }
                }
            } else {
                when (bbsId) {
                    FUNC_ADD_A_BBS -> {
                        val intent = Intent(activity, AddIntroActivity::class.java)
                        startActivity(intent)
                        return@label true
                    }
                    FUNC_MANAGE_BBS -> {
                        val intent = Intent(activity, ManageBBSActivity::class.java)
                        startActivity(intent)
                        return@label true
                    }
                }
            }
            false
        }
        headerView.attachToSliderView(binding.materialDrawerSliderView)
        binding.materialDrawerSliderView.onDrawerItemClickListener = label@{ view: View?, iDrawerItem: IDrawerItem<*>, integer: Int? ->
            val id = iDrawerItem.identifier
            Log.d(TAG, "Drawer id $id")
            if (id > 0) {
                val userBriefInfos = viewModel.forumUserListMutableLiveData.value
                if (userBriefInfos == null) {
                    viewModel.currentForumUserBriefInfoMutableLiveData.postValue(null)
                    return@label false
                }
                var i = 0
                while (i < userBriefInfos.size) {
                    val userBriefInfo = userBriefInfos[i]
                    val userId = userBriefInfo.id
                    if (userId.toLong() == id) {
                        viewModel.currentForumUserBriefInfoMutableLiveData.postValue(userBriefInfo)
                        return@label false
                    }
                    i++
                }
                // not an account
                viewModel.currentForumUserBriefInfoMutableLiveData.postValue(null)
                return@label false
            } else {
                when (id) {
                    MODE_USER_IGCONGTIVE -> {
                        viewModel.currentForumUserBriefInfoMutableLiveData.postValue(null)
                        return@label false
                    }
                    FUNC_ADD_AN_ACCOUNT -> {
                        val intent = Intent(activity, LoginActivity::class.java)
                        val forumInfo = viewModel.currentBBSInformationMutableLiveData.value
                        Log.d(TAG, "ADD A account $forumInfo")
                        if (forumInfo != null) {
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, forumInfo)
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, null as User?)
                            val options = ActivityOptions.makeSceneTransitionAnimation(activity)
                            val bundle = options.toBundle()
                            activity.startActivity(intent, bundle)
                        }
                        return@label true
                    }
                    FUNC_REGISTER_ACCOUNT -> {
                        run {
                            val forumInfo = viewModel.currentBBSInformationMutableLiveData.value
                            if (forumInfo != null) {
                                AlertDialog.Builder(activity)
                                        .setTitle(getString(R.string.bbs_register_an_account, forumInfo.site_name))
                                        .setMessage(R.string.bbs_register_account_notification)
                                        .setPositiveButton(android.R.string.ok) { dialog, which ->
                                            val uri = Uri.parse(forumInfo.registerURL)
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            activity.startActivity(intent)
                                        }
                                        .setNegativeButton(android.R.string.cancel) { dialog, which -> }
                                        .show()
                                return@label true
                            }
                        }
                        run {
                            val intent = Intent(activity, AddIntroActivity::class.java)
                            startActivity(intent)
                            return@label true
                        }
                    }
                    FUNC_ADD_A_BBS -> {
                        val intent = Intent(activity, AddIntroActivity::class.java)
                        startActivity(intent)
                        return@label true
                    }
                    FUNC_SEARCH -> {
                        val forumInfo = viewModel.currentBBSInformationMutableLiveData.value
                        val userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.value
                        val intent = Intent(activity, SearchPostsActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, forumInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        startActivity(intent)
                        return@label true
                    }
                    FUNC_MANAGE_ACCOUNT -> {
                        val forumInfo = viewModel.currentBBSInformationMutableLiveData.value
                        val intent = Intent(activity, ManageUserActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, forumInfo)
                        startActivity(intent)
                        return@label true
                    }
                    FUNC_MANAGE_BBS -> {
                        val intent = Intent(activity, ManageBBSActivity::class.java)
                        startActivity(intent)
                        return@label true
                    }
                    FUNC_VIEW_HISTORY -> {
                        val forumInfo = viewModel.currentBBSInformationMutableLiveData.value
                        val userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.value
                        val intent = Intent(activity, ViewHistoryActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, forumInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        startActivity(intent)
                        return@label true
                    }
                    FUNC_DRFAT_BOX -> {
                        val forumInfo = viewModel.currentBBSInformationMutableLiveData.value
                        val userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.value
                        val intent = Intent(activity, ThreadDraftActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, forumInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        startActivity(intent)
                        return@label true
                    }
                    FOOTER_ABOUT -> {
                        val intent = Intent(activity, AboutAppActivity::class.java)
                        startActivity(intent)
                        return@label true
                    }
                    FOOTER_SETTINGS -> {
                        val intent = Intent(activity, SettingsActivity::class.java)
                        startActivity(intent)
                        return@label true
                    }
                }
            }
            false
        }
        binding.materialDrawerSliderView.setSavedInstance(savedInstanceState)
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerRoot, binding.toolbar, R.string.drawer_open, R.string.drawer_closed)
        binding.drawerRoot.addDrawerListener(actionBarDrawerToggle)
        init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                super.set(imageView, uri, placeholder, tag)
                val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(application))
                Glide.get(application).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
                Glide.with(application)
                        .load(uri)
                        .centerCrop()
                        .into(imageView)
            }
        })
    }

    // fragment adapter
    inner class anonymousViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            val bbsInfo = viewModel.currentBBSInformationMutableLiveData.value
            user = viewModel.currentForumUserBriefInfoMutableLiveData.value
            when (position) {
                0 -> {
                    homeFragment = HomeFragment.newInstance(bbsInfo, user)
                    return (homeFragment as HomeFragment?)!!
                }
                1 -> return DashBoardFragment.newInstance(bbsInfo, user)
            }
            return HomeFragment.newInstance(bbsInfo, user)
        }

        override fun getCount(): Int {
            return 2
        }
    }

    // fragment adapter
    inner class EmptyViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            return BlankBBSFragment.newInstance()
        }

        override fun getCount(): Int {
            return 2
        }
    }

    inner class userViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            val bbsInfo = viewModel.currentBBSInformationMutableLiveData.value
            val userBriefInfo = viewModel.currentForumUserBriefInfoMutableLiveData.value
            when (position) {
                0 -> {
                    homeFragment = HomeFragment.newInstance(bbsInfo, userBriefInfo)
                    return homeFragment!!
                }
                1 -> return DashBoardFragment.newInstance(bbsInfo, userBriefInfo)
                2 -> {
                    notificationsFragment = NotificationsFragment(bbsInfo, userBriefInfo)
                    return notificationsFragment!!
                }
            }
            return HomeFragment.newInstance(bbsInfo, userBriefInfo)
        }

        override fun getCount(): Int {
            return 3
        }
    }

    // BBS Render variables
    var homeFragment: HomeFragment? = null
    var hotThreadsFragment: HotThreadsFragment? = null
    var notificationsFragment: NotificationsFragment? = null
    val HOME_FRAGMENT_KEY = "HOME_FRAGMENT_KEY"
    val DASHBOARD_FRAGMENT_KEY = "DASHBOARD_FRAGMENT_KEY"
    val NOTIFICATION_FRAGMENT_KEY = "NOTIFICATION_FRAGMENT_KEY"
    private fun initFragments() {}
    private fun renderViewPageAndBtmView() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // detecting current bbs
        bbsInfo = viewModel.currentBBSInformationMutableLiveData.value
        if (bbsInfo == null) {
            // judge the
            binding.bbsPortalNavViewpager.adapter = EmptyViewPagerAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            binding.bbsPortalNavView.menu.clear()
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu)
            return
        }
        user = viewModel!!.currentForumUserBriefInfoMutableLiveData.value
        if (user == null) {
            Log.d(TAG, "Current incognitive user $user")
            binding.bbsPortalNavViewpager.adapter = anonymousViewPagerAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            binding.bbsPortalNavView.menu.clear()
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu)
        } else {
            // use fragment transaction instead
            Log.d(TAG, "Current incognitive user " + user!!.username)
            binding.bbsPortalNavViewpager.adapter = userViewPagerAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            binding.bbsPortalNavView.menu.clear()
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_nav_menu)
        }
        binding.bbsPortalNavView.setOnNavigationItemSelectedListener { item ->
            val id = item.itemId
            if (id == R.id.navigation_home) {
                binding.bbsPortalNavViewpager.currentItem = 0
            } else if (id == R.id.navigation_dashboard) {
                binding.bbsPortalNavViewpager.currentItem = 1
            } else if (id == R.id.navigation_notifications) {
                binding.bbsPortalNavViewpager.currentItem = 2
            }
            false
        }
        binding.bbsPortalNavViewpager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.bbsPortalNavView.menu.findItem(R.id.navigation_home).isChecked = true
                    1 -> binding.bbsPortalNavView.menu.findItem(R.id.navigation_dashboard).isChecked = true
                    2 -> binding.bbsPortalNavView.menu.findItem(R.id.navigation_notifications).isChecked = true
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    // fragment lifecyle
    private fun recoverInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }
        homeFragment = supportFragmentManager.getFragment(savedInstanceState, HOME_FRAGMENT_KEY) as HomeFragment?
        notificationsFragment = supportFragmentManager.getFragment(savedInstanceState, NOTIFICATION_FRAGMENT_KEY) as NotificationsFragment?
        hotThreadsFragment = supportFragmentManager.getFragment(savedInstanceState, DASHBOARD_FRAGMENT_KEY) as HotThreadsFragment?
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (homeFragment != null) {
            supportFragmentManager.putFragment(outState, HOME_FRAGMENT_KEY, homeFragment!!)
        }
        if (hotThreadsFragment != null) {
            supportFragmentManager.putFragment(outState, DASHBOARD_FRAGMENT_KEY, hotThreadsFragment!!)
        }
        if (notificationsFragment != null) {
            supportFragmentManager.putFragment(outState, NOTIFICATION_FRAGMENT_KEY, notificationsFragment!!)
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (homeFragment == null && fragment is HomeFragment) {
            homeFragment = fragment
        } else if (hotThreadsFragment == null && fragment is HotThreadsFragment) {
            hotThreadsFragment = fragment
        } else if (notificationsFragment == null && fragment is NotificationsFragment) {
            notificationsFragment = fragment
        }
    }

    // listener
    fun setNewMessageNum(i: Int) {
        if (i == 0) {
            if (binding.bbsPortalNavView.getBadge(R.id.navigation_notifications) != null) {
                binding.bbsPortalNavView.removeBadge(R.id.navigation_notifications)
            }
        } else {
            Log.d(TAG, "set notification num $i")
            val badgeDrawable = binding.bbsPortalNavView.getOrCreateBadge(R.id.navigation_notifications)
            badgeDrawable.number = i
        }
        if (notificationsFragment != null) {
            notificationsFragment!!.setNewMessageNum(i)
        }
    }

    override fun setNotificationsNum(notificationsNum: noticeNumInfo) {
        Log.d(TAG, "Notification fragment $notificationsFragment notification $notificationsNum")
        if (notificationsNum == null) {
            return
        }
        //noticeNumInfo = notificationsNum;
        Log.d(TAG, "notification number " + notificationsNum.allNoticeInfo)
        if (notificationsFragment != null) {
            notificationsFragment!!.renderTabNumber(notificationsNum)
        }
        setNewMessageNum(notificationsNum.allNoticeInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerRoot.isDrawerOpen(binding.materialDrawerSliderView)) {
            binding.drawerRoot.closeDrawer(binding.materialDrawerSliderView)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        Log.d(TAG, "You pressed id $id")
        return if (id == android.R.id.home) {
            finishAfterTransition()
            false
        } else if (id == R.id.bbs_share) {
            val bbsInfo = viewModel!!.currentBBSInformationMutableLiveData.value
            if (bbsInfo != null) {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_template,
                        bbsInfo.site_name, bbsInfo.base_url))
                sendIntent.type = "text/plain"
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            } else {
                Toasty.info(this, getString(R.string.no_bbs_found_in_db), Toast.LENGTH_SHORT).show()
            }
            true
        } else if (id == R.id.bbs_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        } else if (id == R.id.bbs_about_app) {
            val intent = Intent(this, AboutAppActivity::class.java)
            startActivity(intent)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        //        int activeIdentifier = UserPreferenceUtils.getLastSelectedDrawerItemIdentifier(this);
//        if(activeIdentifier >= 0){
//            headerView.setActiveProfile(activeIdentifier,true);
//        }
//        else {
//            headerView.setActiveProfile(0,true);
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.materialDrawerSliderView.saveInstanceState(outState)
        headerView.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val TAG = DrawerActivity::class.java.simpleName
    }
}