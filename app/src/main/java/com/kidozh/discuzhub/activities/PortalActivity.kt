package com.kidozh.discuzhub.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.kidozh.discuzhub.BuildConfig
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.databinding.ActivityPortalBinding
import com.kidozh.discuzhub.dialogs.DiscuzDetailDialogFragment
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.viewModels.PortalViewModel

class PortalActivity : AppCompatActivity() {
    val TAG = PortalActivity::class.simpleName
    lateinit var binding:ActivityPortalBinding
    lateinit var model: PortalViewModel
    var packageNameConflicted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortalBinding.inflate(layoutInflater)
        model = ViewModelProvider(this).get(PortalViewModel::class.java)
        setContentView(binding.root)
        Log.d(TAG, "Build configuration " + BuildConfig.BUILD_TYPE)

        if(BuildConfig.BUILD_TYPE.contentEquals("debug") || BuildConfig.BUILD_TYPE.contentEquals("release")){
            jumpToDefaultActivity()
            return;
        }

        checkDatabase()


    }

    fun jumpToDefaultActivity(){
        val intent = Intent(this, DrawerActivity::class.java)
        startActivity(intent)
    }

    fun configureManifestData(){
        // get application id
        val applicationId = BuildConfig.APPLICATION_ID
        if(applicationId.contentEquals("com.kidozh.discuzhub")){
            // show warning
            binding.warnAppIdCardview.visibility = View.VISIBLE
            packageNameConflicted = true
        }
        else{
            binding.warnAppIdCardview.visibility = View.GONE
        }
        val metadata = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val baseURL = metadata.getString("discuz_base_url")
        val discuzTitle = metadata.getString("discuz_title")
        binding.discuzTitle.setText(discuzTitle)
        binding.discuzBaseUrl.setText(baseURL)
        val logoURL = URLUtils.getBBSLogoUrl(baseURL)
        Glide.with(this)
                .load(logoURL)
                .error(R.drawable.ic_baseline_public_24)
                .placeholder(R.drawable.ic_baseline_public_24)
                .into(binding.portalLogo)
        binding.checkLoadingText.setText(getString(R.string.loading_bbs_check_info, discuzTitle))
        if(baseURL !=null){
            if(baseURL.toLowerCase().startsWith("http://")){
                binding.warnHttpCardview.visibility = View.VISIBLE
                binding.warnHttpText.setText(getString(R.string.http_warn, discuzTitle))
            }
            else{
                binding.warnHttpCardview.visibility = View.GONE
                binding.warnHttpText.setText(getString(R.string.http_warn, discuzTitle))
            }

        }
        binding.agreeBbsPolicy.setText(getString(R.string.agree_bbs_policy, discuzTitle))


    }

    fun checkDatabase(){
        val metadata = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val baseURL = metadata.getString("discuz_base_url")
        if(baseURL != null){
            val dao = DiscuzDatabase.getInstance(this).forumInformationDao
            model.setBaseURL(baseURL)
            // bind them now
            Thread{
                // need to run instantly
                val bbs = dao.getBBSInformationByBaseURL(baseURL)
                if(bbs!=null){
                    val intent = Intent(this, SingleDiscuzActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                    startActivity(intent)
                }
                else{
                    runOnUiThread {
                        configureManifestData()
                        bindViewModel()
                        configureEnterBtn()
                    }
                }
            }.start()

        }

    }

    fun bindViewModel(){
        val metadata = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val baseURL = metadata.getString("discuz_base_url")
        val discuzTitle = metadata.getString("discuz_title")

        model.discuzInDatabase.observe(this, { bbs->
            if(bbs!=null){
                val intent = Intent(this, SingleDiscuzActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbs)
                startActivity(intent)
            }

        })

        model.errorMessageLiveData.observe(this, { errorMessage ->
            if (errorMessage != null) {
                binding.checkResultIcon.visibility = View.VISIBLE
                binding.checkResultIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_error_outline_24px))
                binding.checkLoadingText.setText(getString(R.string.discuz_api_message_template, errorMessage.key, errorMessage.content))
            } else {
                binding.checkLoadingProgressbar.visibility = View.INVISIBLE
            }

        })

        model.checkResultLiveData.observe(this, { checkResult ->
            if (checkResult != null) {
                binding.checkResultIcon.visibility = View.VISIBLE
                binding.checkLoadingProgressbar.visibility = View.INVISIBLE
                binding.checkResultIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_suggestion_check_circle_outline_24px))
                binding.checkLoadingText.setText(getString(R.string.check_discuz_successfully, checkResult.siteName))
                binding.loadingCardview.setCardBackgroundColor(getColor(R.color.colorGreenseaBackground))
                binding.checkLoadingText.setTextColor(getColor(R.color.colorGreensea))
                binding.agreeBbsPolicy.visibility = View.VISIBLE
                binding.agreeDiscuzhubCheckbox.visibility = View.VISIBLE
                binding.agreePolicyText.visibility = View.VISIBLE
                binding.agreeBbsPolicy.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && binding.agreeDiscuzhubCheckbox.isChecked) {
                        binding.enterButton.visibility = View.VISIBLE
                    } else {
                        binding.enterButton.visibility = View.GONE
                    }
                }

                binding.agreeDiscuzhubCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && binding.agreeBbsPolicy.isChecked) {
                        binding.enterButton.visibility = View.VISIBLE
                    } else {
                        binding.enterButton.visibility = View.GONE
                    }
                }

                // activate click
                binding.loadingCardview.isClickable = true
                binding.loadingCardview.setOnClickListener { v->
                    if(baseURL != null){
                        val bbs = checkResult.toBBSInformation(baseURL)
                        val fragment = DiscuzDetailDialogFragment(bbs)
                        fragment.show(supportFragmentManager,DiscuzDetailDialogFragment::class.simpleName)
                    }

                }

            } else {
                binding.checkLoadingProgressbar.visibility = View.INVISIBLE
            }
        })
        // start testing
        if(baseURL != null && !packageNameConflicted){
            model.verify(baseURL)
            var cnt = 0;
            binding.checkLoadingProgressbar.visibility = View.VISIBLE

        }
        else{
            binding.checkResultIcon.visibility = View.VISIBLE
            binding.checkResultIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_error_outline_24px))
            binding.checkLoadingText.setText(getString(R.string.package_name_conflicted))
        }

    }

    fun configureEnterBtn(){

        binding.enterButton.setOnClickListener { v->
            val metadata = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
            val baseURL = metadata.getString("discuz_base_url")
            val dao = DiscuzDatabase.getInstance(this).forumInformationDao

            val bbsEntity = model.checkResultLiveData.value
            if(bbsEntity!=null && baseURL != null) {
                // add them to database
                Thread {
                    dao.insert(bbsEntity.toBBSInformation(baseURL))
                    Log.d(TAG, "Add to database" + bbsEntity + "  " + baseURL)
                }.start()
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
        finishAfterTransition()
    }
}