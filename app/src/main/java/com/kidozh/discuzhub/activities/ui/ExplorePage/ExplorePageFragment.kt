package com.kidozh.discuzhub.activities.ui.ExplorePage

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.kidozh.discuzhub.activities.ForumActivity
import com.kidozh.discuzhub.activities.ThreadActivity
import com.kidozh.discuzhub.activities.UserProfileActivity
import com.kidozh.discuzhub.databinding.FragmentExplorePageBinding
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.bbsInformation
import com.kidozh.discuzhub.entities.forumUserBriefInfo
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils
import com.kidozh.discuzhub.utilities.VibrateUtils
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.regex.Pattern


/**
 * A simple [Fragment] subclass.
 * Use the [ExplorePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExplorePageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var exploreURL = ""

    private lateinit var cookieClient: CookieWebViewClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            bbsInfo = it.getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY) as bbsInformation
            userBriefInfo = it.getSerializable(ConstUtils.PASS_BBS_USER_KEY) as forumUserBriefInfo?
            client = NetworkUtils.getPreferredClientWithCookieJarByUser(context, userBriefInfo)
            exploreURL = bbsInfo.base_url+"/forum.php?mod=guide&view=new"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        runActivity = requireActivity() as Activity
        binding = FragmentExplorePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureWebview()
//        triggerWarnDialog()
    }

    fun configureWebview() {
        val webSettings: WebSettings = binding.explorePageWebview.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.loadsImagesAutomatically = true

        // configure service worker
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val swController =  ServiceWorkerController.getInstance()
            swController.setServiceWorkerClient(object : ServiceWorkerClient(){
                override fun shouldInterceptRequest(request: WebResourceRequest?): WebResourceResponse? {
                    if (request != null) {
                        Log.d(TAG, "load service worker url "+request.url)
                        runActivity.runOnUiThread {
                            parseURLAndOpen(runActivity, bbsInfo, userBriefInfo, request.url.toString())
                        }
                    }
                    return super.shouldInterceptRequest(request)
                }
            })

        }


        cookieClient = CookieWebViewClient()
        cookieClient.cookieManager.setAcceptThirdPartyCookies(binding.explorePageWebview, true)
        // set cookie
        val currentHttpUrl = HttpUrl.parse(exploreURL)
        if (currentHttpUrl != null) {
            val cookies = client.cookieJar().loadForRequest(currentHttpUrl)
            for (i in cookies.indices) {
                val cookie = cookies[i]
                val value = cookie.name() + "=" + cookie.value()
                cookieClient.cookieManager.setCookie(cookie.domain(), value)
            }
        }
        binding.explorePageWebview.setWebViewClient(cookieClient)
        Log.d(TAG, "load url $exploreURL")
        binding.explorePageWebview.loadUrl(exploreURL)
    }

    class CookieWebViewClient internal constructor() : WebViewClient() {
        var cookieManager: CookieManager = CookieManager.getInstance()


        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

            binding.explorePageProgressbar.visibility = View.VISIBLE
            Log.d(TAG,"GET new URL "+url)
            parseURLAndOpen(runActivity, bbsInfo, userBriefInfo, url)
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.explorePageProgressbar.visibility = View.GONE
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            Log.d(TAG,"request url "+request.url)
            return parseURLAndOpen(runActivity, bbsInfo, userBriefInfo, request.url.toString())

        }

    }

//    fun triggerWarnDialog() {
//        /* Some web page use PWA and disable automatic url navigation
//        *  trigger this dialog to warn user to use PC versioned page in priority
//        *  */
//        val dialog = AlertDialog.Builder(requireContext())
//                .setTitle(getString(R.string.explore_in_page_warn_title))
//                .setMessage(getString(R.string.explore_in_page_warn_description))
//                .setPositiveButton(android.R.string.ok) { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .create()
//        dialog.show()
//    }




    companion object {
        private lateinit var bbsInfo: bbsInformation
        private var userBriefInfo: forumUserBriefInfo? = null
        private lateinit var client: OkHttpClient
        private lateinit var binding: FragmentExplorePageBinding
        private lateinit var runActivity: Activity
        private val TAG = ExplorePageFragment::class.simpleName
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExplorePageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(bbsInfo: bbsInformation, userBriefInfo: forumUserBriefInfo?) =
                ExplorePageFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        putSerializable(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)

                    }
                }

        fun parseURLAndOpen(context: Context,
                            bbsInfo: bbsInformation,
                            userBriefInfo: forumUserBriefInfo?,
                            url: String): Boolean {
            // simple unescape
            var url = url
            url = url
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&nbsp;", " ")
            val finalURL = url
            Log.d(TAG,"Parse URL "+url)
            val uri = Uri.parse(url)
            val baseUri = Uri.parse(bbsInfo.base_url)
            val clickedUri = Uri.parse(url)
            var clickedURLPath = clickedUri.path
            val basedURLPath = baseUri.path
            if (clickedURLPath != null && basedURLPath != null) {
                if (clickedURLPath.matches(Regex("^$basedURLPath.*"))) {
                    clickedURLPath = clickedURLPath.substring(basedURLPath.length)
                }
            }
            return if (clickedUri.host == null || clickedUri.host == baseUri.host) {
                // check static first
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && clickedURLPath != null) {
                    if (!TextUtils.isEmpty(
                                    UserPreferenceUtils.getRewriteRule(
                                            context,
                                            bbsInfo,
                                            UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY))) {
                        var rewriteRule = UserPreferenceUtils.getRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY)
                        UserPreferenceUtils.saveRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY, rewriteRule)

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
                                val intent = Intent(context, ForumActivity::class.java)
                                val clickedForum = Forum()
                                clickedForum.fid = fid
                                intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)

                                VibrateUtils.vibrateForClick(context)
                                context.startActivity(intent)
                                return true
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(UserPreferenceUtils.getRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY))) {
                        // match template such as t{tid}-{page}-{prevpage}
                        var rewriteRule = UserPreferenceUtils.getRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY)
                        UserPreferenceUtils.saveRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY, rewriteRule)

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
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                                intent.putExtra("FID", "0")
                                intent.putExtra("TID", tid)
                                intent.putExtra("SUBJECT", url)
                                VibrateUtils.vibrateForClick(context)
                                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                val bundle = options.toBundle()
                                context.startActivity(intent, bundle)
                                return true
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(UserPreferenceUtils.getRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_HOME_SPACE))) {
                        // match template such as s{user}-{name}
                        var rewriteRule = UserPreferenceUtils.getRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_HOME_SPACE)
                        //UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_HOME_SPACE,rewriteRule);


                        // match template such as f{fid}-{page}
                        // crate reverse copy
                        rewriteRule = rewriteRule.replace("{user}", "(?<user>\\w+)")
                        rewriteRule = rewriteRule.replace("{value}", "(?<value>\\d+)")
                        val pattern = Pattern.compile(rewriteRule)
                        val matcher = pattern.matcher(clickedURLPath)
                        if (matcher.find()) {
                            val userString = matcher.group("user")
                            val uidString = matcher.group("value")
                            // handle it
                            if (uidString != null) {
                                var uid = 0
                                try {
                                    uid = uidString.toInt()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                val intent = Intent(context, UserProfileActivity::class.java)
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                intent.putExtra("UID", uid)
                                VibrateUtils.vibrateForClick(context)
                                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                val bundle = options.toBundle()
                                context.startActivity(intent, bundle)
                                return true
                            }
                        }
                    }
                }
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
                        val putThreadInfo = Thread()
                        putThreadInfo.tid = redirectTid
                        val intent = Intent(context, ThreadActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                        intent.putExtra("FID", 0)
                        intent.putExtra("TID", redirectTid)
                        intent.putExtra("SUBJECT", url)
                        VibrateUtils.vibrateForClick(context)
                        context.startActivity(intent)
                        return true
                    } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "viewthread" && uri.getQueryParameter("tid") != null) {
                        val tidString = uri.getQueryParameter("tid")
                        var redirectTid = 0
                        redirectTid = try {
                            tidString!!.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        val putThreadInfo = Thread()
                        putThreadInfo.tid = redirectTid
                        val intent = Intent(context, ThreadActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                        intent.putExtra("FID", 0)
                        intent.putExtra("TID", redirectTid)
                        intent.putExtra("SUBJECT", url)
                        VibrateUtils.vibrateForClick(context)
                        context.startActivity(intent)
                        return true
                    } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "forumdisplay" && uri.getQueryParameter("fid") != null) {
                        val fidString = uri.getQueryParameter("fid")
                        var fid = 0
                        fid = try {
                            fidString!!.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        val intent = Intent(context, ForumActivity::class.java)
                        val clickedForum = Forum()
                        clickedForum.fid = fid
                        intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        VibrateUtils.vibrateForClick(context)
                        context.startActivity(intent)
                        return true
                    } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "space" && uri.getQueryParameter("uid") != null) {
                        val uidStr = uri.getQueryParameter("uid")
                        var uid = 0
                        uid = try {
                            uidStr!!.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        val intent = Intent(context, UserProfileActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        intent.putExtra("UID", uid)
                        context.startActivity(intent)
                        return true
                    }
                    false
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
}