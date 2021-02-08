package com.kidozh.discuzhub.activities

import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation

import com.kidozh.discuzhub.activities.BaseStatusActivity
import com.kidozh.discuzhub.adapter.UrlSuggestionAdapter.OnClickSuggestionListener
import com.kidozh.discuzhub.adapter.UrlSuggestionAdapter
import com.kidozh.discuzhub.viewModels.AddBBSViewModel
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidozh.discuzhub.entities.SuggestURLInfo
import com.kidozh.discuzhub.R
import es.dmoral.toasty.Toasty
import android.widget.Toast
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.activities.AddIntroActivity
import android.text.TextWatcher
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import com.kidozh.discuzhub.databinding.ActivityBbsAddIntroBinding
import com.kidozh.discuzhub.utilities.VibrateUtils
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

class AddIntroActivity : BaseStatusActivity(), OnClickSuggestionListener {
    var adapter: UrlSuggestionAdapter = UrlSuggestionAdapter()
    lateinit var viewModel: AddBBSViewModel
    var binding: ActivityBbsAddIntroBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBbsAddIntroBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel = ViewModelProvider(this).get(AddBBSViewModel::class.java)
        configureRecyclerview()
        bindViewModel()
        configureUrlEditText()
        configureContinueBtn()
        configureAddGuide()
    }

    private fun configureAddGuide() {
        binding!!.bbsAddGuide.paintFlags = binding!!.bbsAddGuide.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding!!.bbsAddGuide.setOnClickListener {
            val uri = Uri.parse("https://discuzhub.kidozh.com/add-a-bbs-guide/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun configureRecyclerview() {

        binding!!.bbsAddIntroRecyclerview.layoutManager = LinearLayoutManager(this)
        binding!!.bbsAddIntroRecyclerview.setHasFixedSize(true)
        binding!!.bbsAddIntroRecyclerview.adapter = getAnimatedAdapter(this, adapter)
        binding!!.bbsAddIntroRecyclerview.itemAnimator = getRecyclerviewAnimation(this)
        // add examples
        val suggestURLInfoList: MutableList<SuggestURLInfo> = ArrayList()
        suggestURLInfoList.add(SuggestURLInfo("https://bbs.nwpu.edu.cn", getString(R.string.bbs_url_example_npubbs), true))
        suggestURLInfoList.add(SuggestURLInfo("https://bbs.comsenz-service.com", getString(R.string.bbs_url_example_discuz_support), true))
        suggestURLInfoList.add(SuggestURLInfo("https://www.mcbbs.net", getString(R.string.bbs_url_example_mcbbs), true))
        suggestURLInfoList.add(SuggestURLInfo("https://keylol.com", getString(R.string.bbs_url_example_keylol), true))
        suggestURLInfoList.add(SuggestURLInfo("https://bbs.qzzn.com", getString(R.string.bbs_url_example_qzzn), true))
        suggestURLInfoList.add(SuggestURLInfo("https://www.right.com.cn/forum", getString(R.string.bbs_url_example_right_com), true))
        adapter.setSuggestURLInfoList(suggestURLInfoList)
    }

    private fun bindViewModel() {
        viewModel.currentURLLiveData.observe(this, { s -> // need to analyze the URL
            val suggestURLInfos = getSuggestedURLList(s)
            adapter.setSuggestURLInfoList(suggestURLInfos)
        })
        viewModel.isLoadingLiveData.observe(this, { aBoolean ->
            if (aBoolean) {
                binding!!.bbsAddIntroProgressBar.visibility = View.VISIBLE
            } else {
                binding!!.bbsAddIntroProgressBar.visibility = View.GONE
            }
        })
        viewModel.errorTextLiveData.observe(this, { s ->
            if (s.length != 0) {
                Toasty.warning(application, s, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.verifiedBBS.observe(this, Observer { bbsInformation: Discuz? ->
            if (bbsInformation != null) {
                Thread {
                    DiscuzDatabase
                            .getInstance(this)
                            .forumInformationDao.insert(bbsInformation)
                }.start()
                Toasty.success(this,
                        getString(R.string.add_a_bbs_successfully, bbsInformation.site_name),
                        Toast.LENGTH_SHORT).show()
                finishAfterTransition()
            }
        })
        viewModel.errorMessageMutableLiveData.observe(this, { it ->
            if(it != null){
                Toasty.error(this,getString(R.string.discuz_api_message_template,it.key,it.content),Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getSuggestedURLList(urlString: String): List<SuggestURLInfo> {
        val suggestURLInfoList: MutableList<SuggestURLInfo> = ArrayList()
        if (urlString == "") {
            suggestURLInfoList.add(SuggestURLInfo("https://bbs.nwpu.edu.cn", getString(R.string.bbs_url_example_npubbs), true))
            suggestURLInfoList.add(SuggestURLInfo("https://bbs.comsenz-service.com", getString(R.string.bbs_url_example_discuz_support), true))
            suggestURLInfoList.add(SuggestURLInfo("https://www.mcbbs.net", getString(R.string.bbs_url_example_mcbbs), true))
            suggestURLInfoList.add(SuggestURLInfo("https://keylol.com", getString(R.string.bbs_url_example_keylol), true))
            suggestURLInfoList.add(SuggestURLInfo("https://bbs.qzzn.com", getString(R.string.bbs_url_example_qzzn), true))
            suggestURLInfoList.add(SuggestURLInfo("https://www.right.com.cn/forum", getString(R.string.bbs_url_example_right_com), true))
        } else {
            // add url
            try {
                val url = URL(urlString)
                val splitString = urlString.split("/").toTypedArray()
                if (splitString.size >= 3) {
                    var pathPrefix = splitString[0] + "/" + splitString[1] + "/" + splitString[2]
                    Log.d(TAG, "Path prefix $pathPrefix")
                    suggestURLInfoList.add(SuggestURLInfo(pathPrefix, getString(R.string.bbs_url_suggestion_host), false))
                    for (i in 3 until splitString.size) {
                        pathPrefix += "/" + splitString[i]
                        suggestURLInfoList.add(SuggestURLInfo(pathPrefix, getString(R.string.bbs_url_suggestion_level, i - 2), false))
                    }
                }
            } catch (e: Exception) {
                suggestURLInfoList.add(SuggestURLInfo("https://bbs.nwpu.edu.cn", getString(R.string.bbs_url_example_npubbs), true))
                suggestURLInfoList.add(SuggestURLInfo("https://bbs.comsenz-service.com", getString(R.string.bbs_url_example_discuz_support), true))
                suggestURLInfoList.add(SuggestURLInfo("https://www.mcbbs.net", getString(R.string.bbs_url_example_mcbbs), true))
                suggestURLInfoList.add(SuggestURLInfo("https://keylol.com", getString(R.string.bbs_url_example_keylol), true))
                suggestURLInfoList.add(SuggestURLInfo("https://www.1point3acres.com/bbs", getString(R.string.bbs_url_example_1point3acres), true))
                suggestURLInfoList.add(SuggestURLInfo("https://www.right.com.cn/forum", getString(R.string.bbs_url_example_right_com), true))
            }
        }
        return suggestURLInfoList
    }

    private fun configureUrlEditText() {
        binding!!.bbsAddIntroUrlEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // need to post data
                viewModel.currentURLLiveData.value = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding!!.autoAddBbs.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean -> viewModel.autoVerifyURLLiveData.postValue(isChecked) }
    }

    private fun configureContinueBtn() {
        binding!!.bbsAddIntroContinueButton.setOnClickListener {
            val urlString = binding!!.bbsAddIntroUrlEdittext.text.toString()
            try {
                val url = URL(urlString)
                viewModel.verifyURL()
            } catch (e: MalformedURLException) {
                Toasty.warning(application, getString(R.string.add_bbs_url_failed, urlString), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onClickSuggestion(suggestURLInfo: SuggestURLInfo) {
        VibrateUtils.vibrateForClick(this)
        // lazy load
        if (binding!!.bbsAddIntroUrlEdittext.text.toString() != suggestURLInfo.url) {
            binding!!.bbsAddIntroUrlEdittext.setText(suggestURLInfo.url)
        }
    }

    var hasSubmitAutoVerify = false
    override fun onURLVerified(base_url: String) {
        val autoCheck = viewModel.autoVerifyURLLiveData.value!!
        if (!hasSubmitAutoVerify && autoCheck) {
            hasSubmitAutoVerify = true
            // only implement when auto check is on
            // not triggering url
            viewModel.currentURLLiveData.postValue(base_url)
            viewModel.verifyURL()
        }
    }

    companion object {
        private val TAG = AddIntroActivity::class.java.simpleName
    }
}