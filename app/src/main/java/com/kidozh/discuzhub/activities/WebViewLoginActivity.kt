package com.kidozh.discuzhub.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.WebViewLoginActivity
import com.kidozh.discuzhub.database.DiscuzDatabase
import com.kidozh.discuzhub.database.UserDatabase
import com.kidozh.discuzhub.database.UserDatabase.Companion.getInstance
import com.kidozh.discuzhub.databinding.ActivityLoginByWebViewBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.LoginResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import es.dmoral.toasty.Toasty
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

class WebViewLoginActivity : BaseStatusActivity() {
    var cookieWebViewClientInstance: cookieWebViewClient? = null
    var binding: ActivityLoginByWebViewBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginByWebViewBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        configureIntentData()
        configureActionBar()
        configureAlertDialog()
    }

    fun configureAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.bbs_login_in_webpage_alert)
            .setMessage(R.string.bbs_login_in_webpage_content)
            .setPositiveButton(android.R.string.ok) { dialog, which -> }
            .show()
    }

    fun configureIntentData() {
        val intent = intent
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
        if (bbsInfo != null) {
            URLUtils.setBBS(bbsInfo)
            configureWebView()
        } else {
            // judge whether from QQ
            if (Intent.ACTION_VIEW == intent.action && intent.data != null) {
                val url = intent.data.toString()
                Log.d(TAG, "Get QQ Login URL $url")
                configureQQLoginWebview(url)
            }
        }
    }

    fun configureActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(true)
            supportActionBar!!.setTitle(R.string.bbs_login_by_browser)
            if (bbsInfo != null) {
                supportActionBar!!.subtitle = bbsInfo!!.site_name
            }
        }
    }

    fun configureWebView() {
        cookieWebViewClientInstance = cookieWebViewClient()
        Log.d(
            TAG, "login web url " + URLUtils.getLoginWebURL(
                bbsInfo!!
            )
        )
        binding!!.loginByWebWebview.loadUrl(URLUtils.getLoginWebURL(bbsInfo!!))
        binding!!.loginByWebWebview.clearCache(true)
        val webSettings = binding!!.loginByWebWebview.settings
        if (webSettings != null) {

            // to allow authentication to use JS
            webSettings.javaScriptEnabled = true
            webSettings.useWideViewPort = true
            webSettings.loadWithOverviewMode = true

            //缩放操作
            webSettings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
            webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
            webSettings.displayZoomControls = false //隐藏原生的缩放控件

            // other detailed information
            webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //关闭webview中缓存
            webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            webSettings.loadsImagesAutomatically = true //支持自动加载图片
            binding!!.loginByWebWebview.webViewClient = cookieWebViewClientInstance!!
        }
    }

    fun configureQQLoginWebview(url: String) {
        cookieWebViewClientInstance = cookieWebViewClient()
        Log.d(TAG, "login qq url $url")
        binding!!.loginByWebWebview.loadUrl(url)


        // binding.loginByWebWebview.clearCache(true);
        val webSettings = binding!!.loginByWebWebview.settings
        if (webSettings != null) {

            // to allow authentication to use JS
            webSettings.javaScriptEnabled = true
            webSettings.useWideViewPort = true
            webSettings.loadWithOverviewMode = true

            //缩放操作
            webSettings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
            webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
            webSettings.displayZoomControls = false //隐藏原生的缩放控件

            // other detailed information
            webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //关闭webview中缓存
            webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            webSettings.loadsImagesAutomatically = true //支持自动加载图片
            binding!!.loginByWebWebview.webViewClient = cookieWebViewClientInstance!!
        }
    }

    // hook for qq
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (Intent.ACTION_VIEW == intent.action && intent.data != null) {
            val url = intent.data.toString()
            Log.d(TAG, "Get QQ Login URL $url")
            binding!!.loginByWebWebview.loadUrl(url)
        }
    }

    private fun triggerQQLoginNoticeDialog(url: String) {
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.qq_login_title))
            .setMessage(getString(R.string.qq_login_message))
            .setPositiveButton(
                android.R.string.ok
            ) { dialog: DialogInterface?, which: Int ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivityForResult(intent, 0)
            }
        builder.show()
    }

    inner class cookieWebViewClient internal constructor() : WebViewClient() {
        var cookieString: CookieManager
        override fun shouldOverrideUrlLoading(webview: WebView, url: String): Boolean {
            return if (url.startsWith("wtloginmqq://ptlogin/qlogin")) {
                // to new
                Log.d(TAG, "GET redirect URL $url")
                // trigger the dialog
                triggerQQLoginNoticeDialog(url)
                true
            } else {
                webview.loadUrl(url)
                true
            }
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding!!.loginByWebProgressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            //cookieManager = CookieManager.getInstance();
            val CookieStr = cookieString.getCookie(url)
            Log.i(TAG, "URL $url Cookies = $CookieStr")
            super.onPageFinished(view, url)
            binding!!.loginByWebProgressBar.visibility = View.GONE
        }

        init {
            cookieString = CookieManager.getInstance()
        }
    }

    fun authUserIntergrity() {
        // get cookie from webview first
        val userBriefInfo = User("", "", 0, "", "", 50, 0)
        Log.d(TAG, "Send user id " + userBriefInfo.id)
        NetworkUtils.clearUserCookieInfo(applicationContext, userBriefInfo)
        val client = NetworkUtils.getPreferredClientWithCookieJar(applicationContext)
        // networkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);
        val currentUrl = binding!!.loginByWebWebview.url ?: return
        val cookieString = cookieWebViewClientInstance!!.cookieString.getCookie(currentUrl)
        val cookieStringArray = cookieString.split(";").toTypedArray()
        val cookieList: MutableList<Cookie?> = ArrayList()
        val httpUrl = HttpUrl.parse(currentUrl) ?: return
        for (eachCookieString in cookieStringArray) {
            Log.d(TAG, "http url " + httpUrl.toString() + " cookie " + eachCookieString)
            val cookie = Cookie.parse(httpUrl, eachCookieString)
            cookieList.add(cookie)
        }
        NetworkUtils.clearUserCookieInfo(applicationContext, userBriefInfo)
        client.cookieJar().saveFromResponse(httpUrl, cookieList)
        // exact login url
        val retrofit: Retrofit
        retrofit = if (bbsInfo != null) {
            NetworkUtils.getRetrofitInstance(bbsInfo!!.base_url, client)
        } else {
            val currentURL = binding!!.loginByWebWebview.url
            // parse base url
            val urlSegements = currentURL!!.split("/").toTypedArray()
            val baseURLBuilder = StringBuilder()
            // match it
            if (urlSegements.size > 1) {
                for (i in 0 until urlSegements.size - 1) {
                    baseURLBuilder.append(urlSegements[i]).append("/")
                }
            }
            NetworkUtils.getRetrofitInstance(baseURLBuilder.toString(), client)
        }
        val mHandler = Handler(Looper.getMainLooper())
        val service = retrofit.create(DiscuzApiService::class.java)
        val loginResultCall = service.loginResult
        loginResultCall.enqueue(object : Callback<LoginResult?> {
            override fun onResponse(call: Call<LoginResult?>, response: Response<LoginResult?>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()
                    result!!.variables.userBriefInfo
                    val parsedUserInfo = result.variables.userBriefInfo
                    Log.d(TAG, "Parse user info " + parsedUserInfo.uid + " " + parsedUserInfo.id)
                    // save it to database
                    if (bbsInfo != null) {
                        parsedUserInfo.belongedBBSID = bbsInfo!!.id
                    }

                    //client.cookieJar().saveFromResponse(httpUrl,cookieList);
                    val cookie = response.headers()["Set-Cookie"]
                    Log.d(
                        TAG,
                        "SAVE Cookie to " + httpUrl.toString() + " cookie list " + cookieList.size + " SET COOKIE" + cookie
                    )
                    if (bbsInfo != null) {
                        saveUserToDatabase(parsedUserInfo,client,httpUrl,null)
                        //saveUserToDatabaseAsyncTask(parsedUserInfo, client, httpUrl).execute()
                    } else {
                        var baseURL = retrofit.baseUrl().toString()
                        if (baseURL.endsWith("/")) {
                            baseURL = baseURL.substring(0, baseURL.length - 1)
                        }
                        Log.d(TAG, "Parsed QQ base url $baseURL")
                        saveUserToDatabase(parsedUserInfo,client,httpUrl,baseURL)
//                        saveUserToDatabaseAsyncTask(
//                            parsedUserInfo,
//                            client,
//                            httpUrl,
//                            baseURL
//                        ).execute()
                    }
                } else {
                    mHandler.post {
                        Toasty.error(
                            applicationContext,
                            getString(
                                R.string.discuz_api_message_template,
                                response.code().toString(),
                                response.message()
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResult?>, t: Throwable) {
                mHandler.post {
                    if (t.localizedMessage != null) {
                        Toasty.error(
                            applicationContext,
                            t.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toasty.error(
                            applicationContext,
                            t.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cookieWebViewClientInstance!!.cookieString.removeAllCookies { }
    }

    fun saveUserToDatabase(userBriefInfo: User, client: OkHttpClient, httpUrl: HttpUrl, redirectURL: String?){
        var insertUserIdList: MutableList<Long> = ArrayList()
        var discuzList: MutableList<Discuz> = ArrayList()
        if (bbsInfo != null) {
            discuzList.add(bbsInfo!!)
        }
        Thread{
            if (bbsInfo == null) {
                // search it
                discuzList = DiscuzDatabase.getInstance(applicationContext)
                    .forumInformationDao
                    .getBBSInformationsByBaseURL(redirectURL)
                // insert them by bbs
                for (i in discuzList.indices) {
                    val bbsInfo = discuzList[i]
                    userBriefInfo.belongedBBSID = bbsInfo.id
                    val firstMightExistUser = UserDatabase.getInstance(this).getforumUserBriefInfoDao().getFirstUserByDiscuzIdAndUid(bbsInfo.id,userBriefInfo.uid)
                    if (firstMightExistUser != null){
                        // if not null then replace the first one
                        userBriefInfo.id = firstMightExistUser.id
                        // then delete all the existing users
                        getInstance(this).getforumUserBriefInfoDao().deleteAllUserByDiscuzIdAndUid(bbsInfo.id,userBriefInfo.uid)
                    }

                    val insertedId = getInstance(applicationContext)
                        .getforumUserBriefInfoDao().insert(userBriefInfo)
                    insertUserIdList.add(insertedId)
                }
            } else {
                // need to search it
                val firstMightExistUser = getInstance(this).getforumUserBriefInfoDao().getFirstUserByDiscuzIdAndUid(bbsInfo!!.id,userBriefInfo.uid)
                if (firstMightExistUser != null){
                    // if not null then replace the first one
                    userBriefInfo.id = firstMightExistUser.id
                    // then delete all the existing users
                    getInstance(this).getforumUserBriefInfoDao().deleteAllUserByDiscuzIdAndUid(bbsInfo!!.id,userBriefInfo.uid)
                }

                val insertedId = getInstance(applicationContext)
                    .getforumUserBriefInfoDao().insert(userBriefInfo)
                insertUserIdList.add(insertedId)
            }

        }.start()

        if (bbsInfo != null) {
            Toasty.success(
                this,
                String.format(
                    getString(R.string.save_user_to_bbs_successfully_template),
                    userBriefInfo.username,
                    bbsInfo!!.site_name
                ),
                Toast.LENGTH_SHORT
            ).show()
        } else if (discuzList.size > 1) {
            Toasty.success(
                this,
                String.format(
                    getString(R.string.bulk_save_user_to_bbs_successfully_template),
                    discuzList.size
                ),
                Toast.LENGTH_SHORT
            ).show()
        } else if (discuzList.size == 1) {
            Toasty.success(
                this,
                String.format(
                    getString(R.string.save_user_to_bbs_successfully_template),
                    userBriefInfo.username,
                    discuzList[0].site_name
                ),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.d(TAG, "No bbs found")
            Toasty.error(
                this,
                String.format(
                    getString(R.string.save_user_bbs_not_found),
                    userBriefInfo.username,
                    redirectURL
                ),
                Toast.LENGTH_LONG
            ).show()
        }
        for (insertedId in insertUserIdList) {
            Log.d(TAG, "save user to database id: " + userBriefInfo.id + "  " + insertedId)
            userBriefInfo.id = insertedId.toInt()
            val savedClient = NetworkUtils.getPreferredClientWithCookieJarByUser(
                applicationContext, userBriefInfo
            )
            val cookies = client.cookieJar().loadForRequest(httpUrl)
            Log.d(TAG, "Http url " + httpUrl.toString() + " cookie list size " + cookies.size)
            savedClient.cookieJar().saveFromResponse(httpUrl, cookies)
            // manually set the cookie to shared preference
            val sharedPrefsCookiePersistor = SharedPrefsCookiePersistor(
                getSharedPreferences(
                    NetworkUtils.getSharedPreferenceNameByUser(userBriefInfo), MODE_PRIVATE
                )
            )
            sharedPrefsCookiePersistor.saveAll(savedClient.cookieJar().loadForRequest(httpUrl))
            Log.d(
                TAG,
                "Http url " + httpUrl.toString() + " saved cookie list size " + savedClient.cookieJar()
                    .loadForRequest(httpUrl).size
            )
        }

        finishAfterTransition()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_login_in_browser, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == android.R.id.home) {
            finishAfterTransition()
            return true
        } else if (id == R.id.action_login_in_web_finished) {
            authUserIntergrity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val TAG = WebViewLoginActivity::class.java.simpleName
    }
}