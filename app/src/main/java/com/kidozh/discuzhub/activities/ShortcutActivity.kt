package com.kidozh.discuzhub.activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.databinding.ActivityShortcutBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.VibrateUtils

class ShortcutActivity : BaseStatusActivity() {
    lateinit var binding: ActivityShortcutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortcutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureIntentData()
        configureFid()
        configureTid()
        configureUid()
    }

    fun configureIntentData(){
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = getString(R.string.short_cut_activity_title)
    }

    fun configureFid(){
        binding.fidEditText.doAfterTextChanged {
            if(it.isNullOrBlank()){
                binding.fidSubmitButton.visibility = View.GONE
            }
            else{
                binding.fidSubmitButton.visibility = View.VISIBLE
            }
        }
        binding.fidEditText.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                goForFidActivity()
                return true
            }

        })

        binding.fidSubmitButton.setOnClickListener {
            goForFidActivity()
        }
    }

    fun goForFidActivity(){
        val fidText = binding.fidEditText.text
        var fid = 0
        try {
            fid = fidText.toString().toInt()
        }
        catch (e: Exception){

        }
        var forum = Forum()
        forum.fid = fid

        val intent = Intent(this, ForumActivity::class.java)
        intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, forum)
        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
        VibrateUtils.vibrateForClick(this)
        startActivity(intent)
    }

    fun configureTid(){
        binding.tidEditText.doAfterTextChanged {
            if(it.isNullOrBlank()){
                binding.tidSubmitButton.visibility = View.GONE
            }
            else{
                binding.tidSubmitButton.visibility = View.VISIBLE
            }
        }
        binding.tidEditText.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                goForTidActivity()
                return true
            }

        })

        binding.tidSubmitButton.setOnClickListener {
            goForTidActivity()
        }
    }

    fun goForTidActivity(){
        val tidText = binding.tidEditText.text
        var tid = 0
        try {
            tid = tidText.toString().toInt()
        }
        catch (e: Exception){

        }
        val thread = Thread()
        thread.tid = tid

        val intent = Intent(this, ThreadActivity::class.java)
        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
        intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread)
        intent.putExtra("FID", thread.fid)
        intent.putExtra("TID", thread.tid)
        intent.putExtra("SUBJECT", thread.subject)
        VibrateUtils.vibrateForClick(this)
        startActivity(intent)
    }

    fun configureUid(){
        binding.uidEditText.doAfterTextChanged {
            if(it.isNullOrBlank()){
                binding.uidSubmitButton.visibility = View.GONE
            }
            else{
                binding.uidSubmitButton.visibility = View.VISIBLE
            }
        }
        binding.uidEditText.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                goForUidActivity()
                return true
            }

        })

        binding.uidSubmitButton.setOnClickListener {
            goForUidActivity()
        }
    }

    fun goForUidActivity(){
        val uidText = binding.tidEditText.text
        var uid = 0
        try {
            uid = uidText.toString().toInt()
        }
        catch (e: Exception){

        }
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
        intent.putExtra("UID", uid)

        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finishAfterTransition();
        }

        return super.onOptionsItemSelected(item)
    }
}