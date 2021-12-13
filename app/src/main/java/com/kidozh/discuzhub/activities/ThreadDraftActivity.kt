package com.kidozh.discuzhub.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.ThreadDraftAdapter
import com.kidozh.discuzhub.callback.recyclerViewSwipeToDeleteCallback
import com.kidozh.discuzhub.callback.recyclerViewSwipeToDeleteCallback.onRecyclerviewSwiped
import com.kidozh.discuzhub.database.ThreadDraftDatabase
import com.kidozh.discuzhub.databinding.ActivityViewThreadDraftBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ThreadDraft
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils
import es.dmoral.toasty.Toasty

class ThreadDraftActivity : BaseStatusActivity(), onRecyclerviewSwiped {
    private val TAG = ThreadDraftActivity::class.java.simpleName
    private var threadDraftAdapter: ThreadDraftAdapter? = null
    private lateinit var listLiveData: LiveData<List<ThreadDraft>>
    var binding: ActivityViewThreadDraftBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewThreadDraftBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        configureIntentData()
        configureActionBar()
        configureRecyclerview()
    }

    private fun configureIntentData() {
        val intent = intent
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
    }

    private fun configureActionBar() {
        setSupportActionBar(binding!!.toolbar)
        binding!!.toolbar.title = getString(R.string.bbs_draft_box)
        binding!!.toolbar.subtitle = bbsInfo!!.site_name
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun configureRecyclerview() {
        binding!!.bbsShowThreadDraftRecyclerview.layoutManager = LinearLayoutManager(this)
        threadDraftAdapter = ThreadDraftAdapter(bbsInfo, user)
        binding!!.bbsShowThreadDraftRecyclerview.itemAnimator = getRecyclerviewAnimation(this)
        binding!!.bbsShowThreadDraftRecyclerview.adapter =
            getAnimatedAdapter(this, threadDraftAdapter!!)
        listLiveData = ThreadDraftDatabase.getInstance(this)
            .getbbsThreadDraftDao()
            .getAllThreadDraftByBBSId(bbsInfo!!.id)
        listLiveData.observe(this, { ThreadDrafts ->
            if (ThreadDrafts != null && ThreadDrafts.isNotEmpty()) {
                threadDraftAdapter!!.threadDraftList = ThreadDrafts
                binding!!.bbsShowThreadDraftNoItemFound.visibility = View.GONE
            } else {
                binding!!.bbsShowThreadDraftNoItemFound.visibility = View.VISIBLE
                threadDraftAdapter!!.threadDraftList = ThreadDrafts!!
                threadDraftAdapter!!.notifyDataSetChanged()
            }
        })
        // swipe to delete support
        val swipeToDeleteUserCallback = recyclerViewSwipeToDeleteCallback(this, threadDraftAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteUserCallback)
        itemTouchHelper.attachToRecyclerView(binding!!.bbsShowThreadDraftRecyclerview)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                false
            }
            R.id.bbs_draft_nav_menu_sort -> {
                false
            }
            R.id.bbs_draft_nav_menu_swipe_delte -> {
                showDeleteAllDraftDialog()
                false
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bbs_draft_nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun showDeleteAllDraftDialog() {
        if (threadDraftAdapter!!.threadDraftList == null || threadDraftAdapter!!.threadDraftList.size == 0) {
            Toasty.info(this, getString(R.string.bbs_thread_draft_empty), Toast.LENGTH_SHORT).show()
        } else {
            val alertDialogs = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.bbs_delete_all_draft))
                .setMessage(getString(R.string.bbs_delete_all_drafts_alert, bbsInfo!!.site_name))
                .setNegativeButton(getString(android.R.string.cancel)) { _, _ -> }
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    deleteAllThreadDraft()
                }
                .create()
            alertDialogs.show()
        }
    }

    override fun onSwiped(position: Int, direction: Int) {
        Log.d(TAG, "On swiped $position$direction")
        val threadDraftList = threadDraftAdapter!!.threadDraftList
        val deleteThreadDraft = threadDraftList[position]
        deleteThreadDraft(deleteThreadDraft)
    }

    private fun addThreadDraft(insertThreadDraft: ThreadDraft){
        Thread{
            val inserted = ThreadDraftDatabase
                .getInstance(this)
                .getbbsThreadDraftDao().insert(insertThreadDraft)
            insertThreadDraft.id = inserted.toInt()
        }.start()
    }

    private fun deleteAllThreadDraft(){
        Thread{
            ThreadDraftDatabase
                .getInstance(this)
                .getbbsThreadDraftDao().deleteAllForumInformation(bbsInfo!!.id)
        }.start()
    }

    private fun deleteThreadDraft(threadDraft: ThreadDraft){
        Thread{
            ThreadDraftDatabase
                .getInstance(this)
                .getbbsThreadDraftDao().delete(threadDraft)
        }.start()

        showUndoSnackbar(threadDraft)
    }

    private fun showUndoSnackbar(threadDraft: ThreadDraft) {
        val view = findViewById<View>(R.id.bbs_show_thread_draft_coordinatorlayout)
        val snackbar = Snackbar.make(
            view, getString(R.string.bbs_delete_draft, threadDraft.subject, bbsInfo!!.site_name),
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(R.string.bbs_undo_delete) { undoDeleteDraft(threadDraft) }
        snackbar.show()
    }

    private fun undoDeleteDraft(threadDraft: ThreadDraft) {
        addThreadDraft(threadDraft)
    }
}