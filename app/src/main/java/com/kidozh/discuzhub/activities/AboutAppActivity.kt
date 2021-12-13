package com.kidozh.discuzhub.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.kidozh.discuzhub.BuildConfig
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.databinding.ActivityAboutAppBinding
import com.kidozh.discuzhub.dialogs.DiscuzDetailDialogFragment
import com.kidozh.discuzhub.utilities.URLUtils

class AboutAppActivity : BaseStatusActivity() {
    lateinit var binding: ActivityAboutAppBinding
    val TAG = AboutAppActivity::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureManifestData()
        configureActionBar()
        renderVersionText()
        configureCardView()
    }

    fun configureManifestData(){
        // get application id
        val buildType = BuildConfig.BUILD_TYPE
        Log.d(TAG,"Build type "+buildType)

        if(buildType.contentEquals("release") || buildType.contentEquals("debug")){
            return
        }

        // hide all cardview
        binding.aboutContactUs.visibility = View.GONE
        binding.aboutHomepage.visibility = View.GONE
        binding.aboutGithubProject.visibility = View.GONE
        binding.aboutPrivacyPolicy.visibility = View.GONE
        binding.aboutTermsOfUse.visibility = View.GONE
        binding.aboutOpenSourceLib.visibility = View.GONE
        binding.discuzInfoCardview.visibility = View.VISIBLE

        val dao = DiscuzDatabase.getMainUIDatabase(this).forumInformationDao
        // looking for thread
        val metadata = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val baseURL = metadata.getString("discuz_base_url")
        val discuzTitle = metadata.getString("discuz_title")
        val bbs = dao.getBBSInformationByBaseURL(baseURL)

        binding.aboutAppTitle.setText(discuzTitle)
        val logoURL = URLUtils.getBBSLogoUrl(baseURL)
        Glide.with(this)
                .load(logoURL)
                .error(R.drawable.ic_baseline_public_24)
                .placeholder(R.drawable.ic_baseline_public_24)
                .into(binding.aboutAppLogo)
        binding.aboutFootNote.setText(R.string.discuz_single_copyright)
        binding.checkLoadingText.setText(getString(R.string.check_discuz_successfully, bbs.site_name))
        binding.discuzInfoCardview.setOnClickListener {
            val fragment = DiscuzDetailDialogFragment(bbs)
            fragment.show(supportFragmentManager, DiscuzDetailDialogFragment::class.simpleName)
        }

    }

    fun renderVersionText() {
        val packageManager = packageManager
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val version = packageInfo.versionName
            binding.aboutAppVersion.text = getString(R.string.app_version_template, version)
        } catch (e: Exception) {
            binding.aboutAppVersion.setText(R.string.welcome)
        }
    }

    fun configureCardView() {
        binding.aboutContactUs.setOnClickListener {
            val data = Intent(Intent.ACTION_SENDTO)
            data.data = Uri.parse("mailto:kidozh@gmail.com")
            data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_contact_developer))
            startActivity(data)
        }
        binding.aboutHomepage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/"))
            startActivity(intent)
        }
        binding.aboutGithubProject.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kidozh/DiscuzHub"))
            startActivity(intent)
        }
        binding.aboutPrivacyPolicy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/privacy_policy/"))
            startActivity(intent)
        }
        binding.aboutTermsOfUse.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/term_of_use/"))
            startActivity(intent)
        }
        binding.aboutOpenSourceLib.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/open_source_licence/"))
            startActivity(intent)
        }
    }

    private fun configureActionBar() {
        binding.toolbar.title = getString(R.string.app_name)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(true)
            val buildType = BuildConfig.BUILD_TYPE
            if(buildType.contentEquals("release")){
                return
            }
            val metadata = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
            val discuzTitle = metadata.getString("discuz_title")
            binding.toolbar.title = discuzTitle
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            android.R.id.home -> {
                finishAfterTransition()
                false
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}