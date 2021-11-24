package com.kidozh.discuzhub.activities

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.BlankBBSFragment.BlankBBSFragment
import com.kidozh.discuzhub.activities.ui.DashBoard.DashBoardFragment
import com.kidozh.discuzhub.activities.ui.home.HomeFragment
import com.kidozh.discuzhub.activities.ui.notifications.NotificationsFragment
import com.kidozh.discuzhub.adapter.UserSpinnerAdapter
import com.kidozh.discuzhub.databinding.ActivitySingleDiscuzBinding
import com.kidozh.discuzhub.databinding.NavHeaderMainBinding
import com.kidozh.discuzhub.databinding.SingleDrawerNavigationHeaderBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.viewModels.SingleDiscuzViewModel
import es.dmoral.toasty.Toasty

class SingleDiscuzActivity : BaseStatusActivity() {
    val TAG = SingleDiscuzViewModel::class.simpleName
    lateinit var binding: ActivitySingleDiscuzBinding
    lateinit var headerBinding: SingleDrawerNavigationHeaderBinding
    lateinit var navHeaderBinding: NavHeaderMainBinding
    lateinit var viewModel: SingleDiscuzViewModel
    private var userAdapter: UserSpinnerAdapter = UserSpinnerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleDiscuzBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(SingleDiscuzViewModel::class.java)
        getIntentInfo()
        if(bbsInfo == null){
            // activate single routes
            finishAfterTransition();
            return;
        }
        bindViewModel()
        configureSpinner()
        configureToolbar()
    }

    fun getIntentInfo(){
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?

    }

    fun configureToolbar(){
        setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon = getDrawable(R.drawable.ic_menu_24px)


    }

    private fun configureSpinner(){
        navHeaderBinding = NavHeaderMainBinding.bind(binding.drawerNavigation.getHeaderView(0))
        headerBinding = SingleDrawerNavigationHeaderBinding.inflate(layoutInflater)
        binding.drawerNavigation.addHeaderView(headerBinding.root)
        headerBinding.userSpinner.adapter = userAdapter
        val context = this
        headerBinding.userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.currentUserMutableLiveData.value = userAdapter.userList[position]
                Toasty.info(context, getString(R.string.switch_user, userAdapter.userList[position].username), Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.currentUserMutableLiveData.value = null
                Toasty.info(context, getString(R.string.switch_incognitive), Toast.LENGTH_SHORT).show()
            }
        }
        binding.drawerNavigation.setNavigationItemSelectedListener { menuItem->
            val bbs: Discuz? = viewModel.currentBBSMutableLiveData.value
            when(menuItem.itemId) {
                R.id.add_a_account -> {
                    val intent = Intent(this, LoginActivity::class.java)


                    Log.d(TAG, "ADD A account $bbs")
                    if (bbs != null) {
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, null as User?)
                        val options = ActivityOptions.makeSceneTransitionAnimation(this)
                        val bundle = options.toBundle()
                        startActivity(intent, bundle)
                    }
                    true
                }
                R.id.register_an_account -> {
                    if (bbs != null) {
                        AlertDialog.Builder(this)
                                .setTitle(getString(R.string.bbs_register_an_account, bbs.site_name))
                                .setMessage(R.string.bbs_register_account_notification)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    val uri = Uri.parse(bbs.registerURL)
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    startActivity(intent)
                                }
                                .show()
                    }
                    true
                }
                R.id.manage_users -> {
                    val intent = Intent(this, ManageUserActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                    startActivity(intent)
                    true
                }
                R.id.view_history -> {
                    val user: User? = viewModel.currentUserMutableLiveData.value
                    val intent = Intent(this, ViewHistoryActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                    startActivity(intent)
                    true
                }
                R.id.short_cut ->{

                    val intent = Intent(this, ShortcutActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                    startActivity(intent)
                    true
                }

                R.id.draft_box -> {
                    val userBriefInfo = viewModel.currentUserMutableLiveData.value
                    val intent = Intent(this, ThreadDraftActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                    startActivity(intent)
                    true
                }
                R.id.about_app -> {
                    val intent = Intent(this, AboutAppActivity::class.java)
                    startActivity(intent)
                     true
                }
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.search -> {

                    val userBriefInfo = viewModel.currentUserMutableLiveData.value
                    val intent = Intent(this, SearchPostsActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                    startActivity(intent)
                    true
                }

                else -> {
                    false
                }

            }
        }
    }

    fun bindViewModel(){
        if(bbsInfo !=null){
            viewModel.setBBSInfo(bbsInfo!!)
        }

        viewModel.currentBBSMutableLiveData.observe(this, Observer { bbsInfo ->
            if (bbsInfo != null) {
                binding.toolbarTitle.setText(bbsInfo.site_name)
            }
        })
        viewModel.currentUserMutableLiveData.observe(this, Observer { user ->
            renderViewPagerAndBtnNavigation()
            if (user == null) {
                navHeaderBinding.userAvatar.setImageDrawable(
                        getDrawable(R.drawable.ic_anonymous_user_icon_24px)
                )
                navHeaderBinding.headerTitle.setText(R.string.bbs_anonymous_mode_title)
                if(bbsInfo!=null){
                    navHeaderBinding.headerSubtitle.setText(
                            getString(R.string.bbs_anonymous_mode_description, bbsInfo!!.site_name))
                }

            } else {
                var avatar_num: Int = user.getUid() % 16
                if (avatar_num < 0) {
                    avatar_num = -avatar_num
                }
                val avatarResource: Int = getResources().getIdentifier(String.format("avatar_%s", avatar_num + 1), "drawable", packageName)
                URLUtils.bbsInfo = bbsInfo
                val source: String = URLUtils.getLargeAvatarUrlByUid(user.getUid())
                val glideUrl = GlideUrl(source,
                        LazyHeaders.Builder().addHeader("referer", source).build()
                )
                Glide.with(this)
                        .load(glideUrl)
                        .apply(RequestOptions.placeholderOf(avatarResource).error(avatarResource))
                        .into(navHeaderBinding.userAvatar)
                navHeaderBinding.headerTitle.setText(user.username)
                navHeaderBinding.headerSubtitle.setText(getString(R.string.user_id_description,user.getUid()))
            }
        })
        viewModel.userListLiveData.observe(this, Observer { userList ->
            // add incognitive mode
            userAdapter.setUserList(userList)
            if (userList.size == 0) {
                viewModel.currentUserMutableLiveData.postValue(null)
                headerBinding.userSpinner.visibility = GONE
            } else {
                headerBinding.userSpinner.visibility = VISIBLE
            }
        })


    }

    fun renderViewPagerAndBtnNavigation(){
        val currentBBS = viewModel.currentBBSMutableLiveData.value
        if(currentBBS == null){

            // judge the
            binding.bbsPortalNavViewpager.setAdapter(EmptyViewPagerAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT))
            binding.bbsPortalNavView.getMenu().clear()
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu)
        }
        val user = viewModel.currentUserMutableLiveData.value
        if(user == null){
            binding.bbsPortalNavViewpager.adapter = AnonymousViewPagerAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            binding.bbsPortalNavView.menu.clear()
            binding.bbsPortalNavView.inflateMenu(R.menu.bottom_incognitive_nav_menu)
        }
        else{
            binding.bbsPortalNavViewpager.adapter = UserViewPagerAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
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

    class EmptyViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            return BlankBBSFragment.newInstance()
        }

        override fun getCount(): Int {
            return 2
        }
    }

    inner class AnonymousViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {

            val bbsInfo: Discuz? = this@SingleDiscuzActivity.viewModel.currentBBSMutableLiveData.value
            user = viewModel.currentUserMutableLiveData.value
            when (position) {
                0 -> {
                    val homeFragment: HomeFragment = HomeFragment.newInstance(bbsInfo, user)
                    return homeFragment
                }
                1 -> return DashBoardFragment.newInstance(bbsInfo, user)
            }
            return HomeFragment.newInstance(bbsInfo, user)
        }

        override fun getCount(): Int {
            return 2
        }
    }

    inner class UserViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            val bbsInfo: Discuz? = this@SingleDiscuzActivity.viewModel.currentBBSMutableLiveData.value
            val user: User? = this@SingleDiscuzActivity.viewModel.currentUserMutableLiveData.value
            when (position) {
                0 -> {
                    val homeFragment = HomeFragment.newInstance(bbsInfo, user)
                    return homeFragment
                }
                1 -> return DashBoardFragment.newInstance(bbsInfo, user)
                2 -> {
                    val notificationsFragment = NotificationsFragment(bbsInfo, user)
                    return notificationsFragment
                }
            }
            return HomeFragment.newInstance(bbsInfo, user)
        }

        override fun getCount(): Int {
            return 3
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                if (binding.drawerRoot.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerRoot.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerRoot.openDrawer(GravityCompat.START)
                }

                return true
            }
            R.id.bbs_share -> {
                val bbsInfo: Discuz? = viewModel.currentBBSMutableLiveData.getValue()
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
                return true
            }
            R.id.bbs_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.bbs_about_app -> {
                val intent = Intent(this, AboutAppActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(binding.drawerRoot.isDrawerOpen(GravityCompat.START)){
            binding.drawerRoot.closeDrawer(GravityCompat.START)
        }
        else{
            finishAfterTransition()
        }

    }


}