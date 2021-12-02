package com.kidozh.discuzhub.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.database.UserDatabase
import com.kidozh.discuzhub.databinding.ActivityLoginBbsBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.LoginResult
import com.kidozh.discuzhub.results.SecureInfoResult
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.VibrateUtils
import com.kidozh.discuzhub.viewModels.LoginViewModel
import es.dmoral.toasty.Toasty
import okhttp3.*
import java.io.IOException
import java.io.InputStream

class LoginActivity : BaseStatusActivity() {
    private val TAG = LoginActivity::class.java.simpleName
    var viewModel: LoginViewModel? = null
    lateinit var binding: ActivityLoginBbsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBbsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        configureData()
        configureActionBar()
        setInformation()
        configureEditText()
        configureLoginBtn()
        bindViewModel()
    }

    private fun setInformation() {
        binding.loginBbsTitle.text = bbsInfo!!.site_name
        if (user == null) {
            binding.loginBbsUrl.text = bbsInfo!!.base_url
        } else {
            binding.toolbarTitle.text = getString(R.string.user_relogin, user!!.username)
            binding.loginBbsUrl.text = getString(R.string.user_relogin, user!!.username)
            binding.loginBbsAccountTextInputEditText.setText(user!!.username)
        }
        val factory = OkHttpUrlLoader.Factory(client)
        Glide.get(this).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        Glide.with(this)
            .load(URLUtils.getBBSLogoUrl())
            .error(R.drawable.ic_baseline_public_24)
            .placeholder(R.drawable.ic_baseline_public_24)
            .centerInside()
            .into(binding.loginBbsAvatar)
        binding.loginBbsSecurityQuestionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        binding.loginBbsSecurityAnswerEditText.visibility = View.GONE
                    } else {
                        binding.loginBbsSecurityAnswerEditText.visibility = View.VISIBLE
                        binding.loginBbsSecurityAnswerEditText.hint =
                            binding.loginBbsSecurityQuestionSpinner.selectedItem.toString()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.loginBbsSecurityAnswerEditText.visibility = View.GONE
                }
            }
    }

    private fun configureEditText() {
        // for auto-fill service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.loginBbsAccountTextInputEditText.setAutofillHints(View.AUTOFILL_HINT_USERNAME)
            binding.loginBbsPasswordTextInputEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)
        }


        binding.loginBbsCaptchaImageView.setOnClickListener {
            viewModel!!.loadSecureInfo()
            VibrateUtils.vibrateForClick(application)
        }
        binding.loginBbsAccountTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                binding.loginBbsAccountTextInputLayout.isErrorEnabled = false
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        binding.loginBbsPasswordTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                binding.loginBbsPasswordTextInputLayout.isErrorEnabled = false
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun needCaptcha(): Boolean {
        return !(viewModel == null || viewModel!!.secureInfoResultMutableLiveData.value == null || viewModel!!.secureInfoResultMutableLiveData.value!!.secureVariables == null)
    }

    fun bindViewModel() {
        viewModel!!.errorMessage.observe(this, { errorMessage: ErrorMessage? ->
            if (errorMessage != null) {
                Toasty.error(
                    this,
                    getString(
                        R.string.discuz_api_message_template,
                        errorMessage.key,
                        errorMessage.content
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        viewModel!!.secureInfoResultMutableLiveData.observe(
            this,
            { secureInfoResult: SecureInfoResult? ->
                if (secureInfoResult != null) {
                    if (secureInfoResult.secureVariables == null) {
                        binding.loginBbsCaptchaInputLayout.visibility = View.GONE
                        binding.loginBbsCaptchaImageView.visibility = View.GONE
                    } else {
                        // need further query
                        binding.loginBbsCaptchaInputLayout.visibility = View.VISIBLE
                        binding.loginBbsCaptchaImageView.visibility = View.VISIBLE
                        binding.loginBbsCaptchaImageView.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_captcha_placeholder_24px
                            )
                        )
                        val captchaURL = secureInfoResult.secureVariables.secCodeURL
                        val captchaImageURL =
                            URLUtils.getSecCodeImageURL(secureInfoResult.secureVariables.secHash)
                        val captchaRequest = Request.Builder()
                            .url(captchaURL)
                            .build()
                        client.newCall(captchaRequest).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {}
                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful && response.body() != null) {
                                    // get the session
                                    binding.loginBbsCaptchaImageView.post {
                                        val factory = OkHttpUrlLoader.Factory(
                                            client
                                        )
                                        Glide.get(application).registry.replace(
                                            GlideUrl::class.java, InputStream::class.java, factory
                                        )

                                        // forbid cache captcha
                                        val options = RequestOptions()
                                            .fitCenter()
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .placeholder(R.drawable.ic_captcha_placeholder_24px)
                                            .error(R.drawable.ic_post_status_warned_24px)
                                        val pictureGlideURL = GlideUrl(
                                            captchaImageURL,
                                            LazyHeaders.Builder()
                                                .addHeader("Referer", captchaURL)
                                                .build()
                                        )
                                        Glide.with(application)
                                            .load(pictureGlideURL)
                                            .apply(options)
                                            .into(binding.loginBbsCaptchaImageView)
                                    }
                                }
                            }
                        })
                    }
                }
            })
        viewModel!!.loginResultMutableLiveData.observe(this, { loginResult: LoginResult? ->
            if (loginResult != null) {
                val loginMessage = loginResult.message
                if (loginMessage != null) {
                    val key = loginMessage.key
                    if (key == "login_succeed") {
                        val user = loginResult.variables.userBriefInfo
                        user.belongedBBSID = bbsInfo!!.id
                        user.id = user.id
                        Toasty.success(
                            this,
                            getString(
                                R.string.discuz_api_message_template,
                                loginMessage.key,
                                loginMessage.content
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                        saveUserToDatabase(user, client, bbsInfo!!.base_url)
                    } else {
                        // refresh the captcha
                        viewModel!!.loadSecureInfo()
                        if (key == "login_seccheck2") {
                            // need captcha
                            viewModel!!.loadSecureInfo()
                        }
                        Toasty.error(
                            this,
                            getString(
                                R.string.discuz_api_message_template,
                                loginMessage.key,
                                loginMessage.content
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun configureLoginBtn() {
        binding.loginBbsLoginButton.setOnClickListener {
            if (binding.loginBbsAccountTextInputEditText.text != null && binding.loginBbsPasswordTextInputEditText.text != null && binding.loginBbsCaptchaEditText.text != null) {
                val account = binding.loginBbsAccountTextInputEditText.text.toString()
                val password = binding.loginBbsPasswordTextInputEditText.text.toString()
                val captchaText = binding.loginBbsCaptchaEditText.text.toString()
                var secureHash: String? = null
                val secureInfoResult = viewModel!!.secureInfoResultMutableLiveData.value
                if (secureInfoResult?.secureVariables != null) {
                    secureHash = secureInfoResult.secureVariables.secHash
                }
                if (needCaptcha() && captchaText.isEmpty()) {
                    binding.loginBbsCaptchaInputLayout.error = getString(R.string.field_required)
                    binding.loginBbsCaptchaInputLayout.isErrorEnabled = true
                    return@setOnClickListener
                } else {
                    binding.loginBbsCaptchaInputLayout.isErrorEnabled = false
                }
                if (password.isEmpty() || account.isEmpty()) {
                    if (account.isEmpty()) {
                        binding.loginBbsPasswordTextInputLayout.isErrorEnabled = true
                        binding.loginBbsAccountTextInputLayout.error =
                            getString(R.string.field_required)
                        //binding.loginBbsAccountTextInputEditText.setError();
                    }
                    if (password.length == 0) {
                        binding.loginBbsPasswordTextInputLayout.isErrorEnabled = true
                        binding.loginBbsPasswordTextInputLayout.error =
                            getString(R.string.field_required)
                        //binding.loginBbsPasswordTextInputEditText.setError();
                    }
                    Toasty.warning(
                        applicationContext,
                        getString(R.string.bbs_login_account_password_required),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel!!.login(
                        client,
                        account, password,
                        binding.loginBbsSecurityQuestionSpinner.selectedItemPosition,
                        binding.loginBbsSecurityAnswerEditText.text.toString(),
                        secureHash,
                        binding.loginBbsCaptchaEditText.text.toString()
                    )
                }
            } else {
                Toasty.warning(
                    applicationContext,
                    getString(R.string.bbs_login_account_password_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.loginBbsLoginInWebButton.setOnClickListener {
            val intent = Intent(applicationContext, WebViewLoginActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
            startActivity(intent)
        }
    }

    private fun saveUserToDatabase(userBriefInfo: User, client: OkHttpClient, httpURL: String){
        val httpUrl: HttpUrl = HttpUrl.parse(httpURL) ?: return
        var insertedId: Long = 0
        Thread{
            // may clear all the users first
            val firstMightExistUser = UserDatabase.getInstance(this).getforumUserBriefInfoDao().getFirstUserByDiscuzIdAndUid(bbsInfo!!.id,userBriefInfo.uid)
            Log.d(TAG,"GET all users(${userBriefInfo.uid}) from database $firstMightExistUser")
            if (firstMightExistUser != null){
                // if not null then replace the first one
                userBriefInfo.id = firstMightExistUser.id
                // then delete all the existing users
                UserDatabase.getInstance(this).getforumUserBriefInfoDao().deleteAllUserByDiscuzIdAndUid(bbsInfo!!.id,userBriefInfo.uid)
            }
            // then insert this to database
            insertedId = UserDatabase.getInstance(applicationContext).getforumUserBriefInfoDao().insert(userBriefInfo)
        }.start()
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
                NetworkUtils.getSharedPreferenceNameByUser(
                    userBriefInfo
                ), MODE_PRIVATE
            )
        )
        sharedPrefsCookiePersistor.saveAll(savedClient.cookieJar().loadForRequest(httpUrl))
        Log.d(
            TAG,
            "Http url " + httpUrl.toString() + " saved cookie list size " + savedClient.cookieJar()
                .loadForRequest(httpUrl).size
        )
        finishAfterTransition()
    }

    fun configureActionBar() {
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding.toolbarTitle.text = getString(R.string.login_bbs_title, bbsInfo!!.site_name)
            if (bbsInfo!!.isSecureClient) {
                binding.loginBbsNotice.visibility = View.GONE
            } else {
                binding.loginBbsNotice.visibility = View.VISIBLE
            }
        }
    }

    private fun configureData() {
        val intent = intent
        bbsInfo = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
        client = NetworkUtils.getPreferredClientWithCookieJar(applicationContext)
        viewModel!!.setInfo(bbsInfo!!, user, client)
        if (bbsInfo == null) {
            finishAfterTransition()
        } else {
            Log.d(TAG, "get bbs name " + bbsInfo!!.site_name)
            URLUtils.setBBS(bbsInfo)
            //bbsURLUtils.setBaseUrl(bbsInfo.base_url);
        }
        if (supportActionBar != null) {
            if (user == null) {
                supportActionBar!!.setTitle(R.string.bbs_login)
            } else {
                supportActionBar!!.title = getString(R.string.user_relogin, user!!.username)
            }
            supportActionBar!!.subtitle = bbsInfo!!.site_name
            // clear it first
            getSharedPreferences("CookiePersistence", MODE_PRIVATE).edit().clear().apply()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == android.R.id.home) {
            finishAfterTransition()
            false
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}