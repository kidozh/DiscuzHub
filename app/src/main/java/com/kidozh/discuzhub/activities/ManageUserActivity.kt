package com.kidozh.discuzhub.activities

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ManageUserActivity
import com.kidozh.discuzhub.activities.ui.bbsDetailedInformation.bbsShowInformationViewModel
import com.kidozh.discuzhub.adapter.UsersAdapter
import com.kidozh.discuzhub.callback.RecyclerViewItemTouchCallback
import com.kidozh.discuzhub.callback.RecyclerViewItemTouchCallback.onInteraction
import com.kidozh.discuzhub.database.UserDatabase.Companion.getInstance
import com.kidozh.discuzhub.databinding.ActivityManageUserBinding
import com.kidozh.discuzhub.dialogs.ManageAdapterHelpDialogFragment
import com.kidozh.discuzhub.dialogs.ManageUserAdapterHelpDialogFragment
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.VibrateUtils
import java.util.*

class ManageUserActivity : BaseStatusActivity(), onInteraction {
    lateinit var viewModel: bbsShowInformationViewModel
    lateinit var userAdapter: UsersAdapter
    var binding: ActivityManageUserBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUserBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel = ViewModelProvider(this).get(
            bbsShowInformationViewModel::class.java
        )
        configureIntent()
        configureActionBar()
        configureRecyclerView()
        fetchUserList()
        configureAddUserBtn()
        showHelpDialog()
    }

    private fun configureIntent() {
        val intent = intent
        discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
        if (discuz == null) {
            finishAfterTransition()
        } else {
            URLUtils.setBBS(discuz)
        }
    }

    fun configureActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding!!.toolbar.title = discuz!!.site_name
            binding!!.toolbar.subtitle = discuz!!.base_url
        }
    }

    fun configureRecyclerView() {
        binding!!.bbsUserRecyclerview.layoutManager = LinearLayoutManager(this)
        binding!!.bbsUserRecyclerview.itemAnimator = getRecyclerviewAnimation(this)
        userAdapter = UsersAdapter(this, discuz!!)
        binding!!.bbsUserRecyclerview.adapter =
            getAnimatedAdapter(this, userAdapter)
        // swipe to delete
        // swipe to delete support
        val callback = RecyclerViewItemTouchCallback(this)
        //forumSwipeToDeleteUserCallback swipeToDeleteUserCallback = new forumSwipeToDeleteUserCallback(userAdapter);
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding!!.bbsUserRecyclerview)
        binding!!.bbsUserRecyclerview.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun fetchUserList() {
        viewModel.loadUserList(discuz!!.id)
        viewModel.bbsUserInfoLiveDataList?.observe(this, { Users ->
            userAdapter.userList = Users as MutableList<User>
            if (Users.size == 0) {
                binding!!.emptyUserView.visibility = View.VISIBLE
            } else {
                binding!!.emptyUserView.visibility = View.GONE
            }
        })
    }

    private fun configureAddUserBtn() {
        val activity: Activity = this
        binding!!.addAUser.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            Log.d(TAG, "ADD A account $discuz")
            if (discuz != null) {
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, null as User?)
                val options = ActivityOptions.makeSceneTransitionAnimation(activity)
                val bundle = options.toBundle()
                activity.startActivity(intent, bundle)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_manage_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.show_help_info -> {
                showHelpDialog()
                true
            }
            R.id.add_item -> {
                val intent = Intent(this, LoginActivity::class.java)
                if (discuz != null) {
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
                    intent.putExtra(
                        ConstUtils.PASS_BBS_USER_KEY,
                        null as User?
                    )
                    val options = ActivityOptions.makeSceneTransitionAnimation(this)
                    val bundle = options.toBundle()
                    startActivity(intent, bundle)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showHelpDialog() {
        val fragmentManager = supportFragmentManager
        val dialogFragment = ManageUserAdapterHelpDialogFragment()
        dialogFragment.show(fragmentManager, ManageAdapterHelpDialogFragment::class.java.simpleName)
    }

    override fun onRecyclerViewSwiped(position: Int, direction: Int) {
        val userBriefInfos = userAdapter.userList
        val userBriefInfo = userBriefInfos[position]
        Log.d(TAG, "Get direction $direction")
        if (direction == ItemTouchHelper.START) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            startActivity(intent)
            VibrateUtils.vibrateForNotice(this)
            userAdapter.notifyDataSetChanged()
        } else {
            userAdapter.userList.removeAt(position)
            userAdapter.notifyDataSetChanged()
            showUndoSnackbar(userBriefInfo, position)
        }
    }

    override fun onRecyclerViewMoved(fromPosition: Int, toPosition: Int) {
        val userBriefInfos = userAdapter.userList
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(userBriefInfos, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(userBriefInfos, i, i - 1)
            }
        }
        for (i in userBriefInfos.indices) {
            userBriefInfos[i].position = i
        }
        updateDiscuzUser(userBriefInfos)
    }
    
    private fun deleteDiscuzUser(userBriefInfo: User){
        Thread{
            getInstance(this).getforumUserBriefInfoDao().delete(userBriefInfo)
        }.start()
    }
    
    private fun updateDiscuzUser(userBriefInfos: List<User>){
        Thread{
            
            getInstance(this).getforumUserBriefInfoDao().update(userBriefInfos)
        }.start()
    }

    private fun showUndoSnackbar(userBriefInfo: User, position: Int) {
        Log.d(TAG, "SHOW REMOVED POS $position")
        deleteDiscuzUser(userBriefInfo)
        val snackbar = Snackbar.make(
            binding!!.manageUserCoordinatorLayout,
            getString(
                R.string.bbs_delete_user_info_template,
                userBriefInfo.username,
                discuz!!.site_name
            ),
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(R.string.bbs_undo_delete) { undoDelete(userBriefInfo, position) }
        snackbar.show()
    }

    private fun undoDelete(userBriefInfo: User, position: Int) {
        // insert to database
        userAdapter.userList.add(position, userBriefInfo)
        userAdapter.notifyDataSetChanged()
        addUser(userBriefInfo)
        
    }
    
    private fun addUser(userBriefInfo: User){
        Thread{
            getInstance(this).getforumUserBriefInfoDao().insert(userBriefInfo)
        }.start()
    }

    companion object {
        val TAG: String = ManageUserActivity::class.java.simpleName
    }
}