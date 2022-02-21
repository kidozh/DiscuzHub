package com.kidozh.discuzhub.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.kidozh.discuzhub.BuildConfig
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.TokenAdapter
import com.kidozh.discuzhub.databinding.ActivityPushTokenBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.viewModels.PushTokenViewModel


class PushTokenActivity : BaseStatusActivity() {
    final val TAG = PushTokenActivity::class.simpleName
    lateinit var binding: ActivityPushTokenBinding
    lateinit var model: PushTokenViewModel
    val adapter: TokenAdapter = TokenAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushTokenBinding.inflate(layoutInflater)
        model = ViewModelProvider(this)[PushTokenViewModel::class.java]
        setContentView(binding.root)
        loadIntentData()
        initToolbar()
        bindViewModel()
        bindSwipeToRefresh()
        bindRecyclerView()
    }

    private fun loadIntentData(){

        discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?

        if(user == null){
            model.errorMessage.value = ErrorMessage("not_logined",getString(R.string.user_login_required,))
        }
        else{
            model.loadDiscuzInfo(discuz!!, user!!)
        }
    }

    private fun initToolbar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
    }

    private fun bindRecyclerView(){
        binding.tokenRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.tokenRecyclerview.adapter = adapter
    }

    private fun bindViewModel(){
        model.errorMessage.observe(this){
            if(it!=null){
                binding.errorMessageTitle.text = it.content
                binding.errorMessageTitle.visibility = View.VISIBLE
            }
            else{
                binding.errorMessageTitle.visibility = View.GONE
            }
        }

        model.postTokenResult.observe(this){
            if(it!=null){
                model.formHash = it.formhash
            }
        }

        model.tokenResult.observe(this){
            if(it!= null){
                binding.successText.visibility = View.VISIBLE
                binding.successText.text = getString(R.string.dhpush_max_token_description, it.maxToken.toString())
                // refresh it
                adapter.tokenList = it.list
                Log.d(TAG,"Obtain all token ${it.list}")
                // send it to the server
                for (obj in it.list){
                    if(obj.token == model.token.value){
                        // don't send duplicated token
                        return@observe
                    }
                }




                model.formHash = it.formhash
                if(!model.token.value.isNullOrBlank()){
                    val appId = BuildConfig.APPLICATION_ID
                    model.pushChannel.value?.let { channel ->
                        model.sendTokenToServer(model.token.value!!, getDeviceName(),appId, channel)
                        Log.d(TAG,"Send token to server ${it.formhash} ${getDeviceName()} ${channel}")
                    }

                }

            }
            else{
                binding.successText.visibility = View.GONE
            }
        }

        model.loading.observe(this){
            binding.swipeRefreshLayout.isRefreshing = it
        }

        model.token.observe(this){
            adapter.currentDeviceToken = it
        }

        model.pushChannel.observe(this){
            if(user!= null){
                if(it.isNotBlank()){
                    // shall send the server for a result
                    when(it.uppercase()){
                        "FCM" ->{
                            // should register FCM
                            registerFCMToken()
                        }
                    }

                }
            }

        }
    }

    private fun bindSwipeToRefresh(){
        binding.swipeRefreshLayout.setOnRefreshListener {
            model.loadTokenListFromServer()
        }
    }

    private fun registerFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            if(!it.isSuccessful){
                model.errorMessage.postValue(ErrorMessage("fcm_token_registration_failed",getString(R.string.fcm_token_registration_failed)))
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = it.result
            Log.d(TAG,"Get token from server ${token}")
            model.token.value = token
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        onCheckGooglePlayServices()
    }

    override fun onResume() {
        super.onResume()
        onCheckGooglePlayServices()
    }

    private fun getDeviceName(): String {
        return "${android.os.Build.BRAND}/${android.os.Build.MODEL}"
    }

    private fun onCheckGooglePlayServices() {
        // 验证是否已在此设备上安装并启用Google Play服务，以及此设备上安装的旧版本是否为此客户端所需的版本
        val code: Int = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        Log.d(TAG,"Check with Google service ${android.os.Build.PRODUCT} ${android.os.Build.BRAND} ${android.os.Build.DEVICE} ${android.os.Build.MODEL}")
        if (code == ConnectionResult.SUCCESS) {
            // 支持Google服务
            model.pushChannel.postValue("FCM")
        } else {
            Log.d(TAG,"No Google service found in your device")
            model.errorMessage.postValue(ErrorMessage("no_google_service",getString(R.string.device_not_support_push_service,getDeviceName())))
            /**
             * 依靠 Play 服务 SDK 运行的应用在访问 Google Play 服务功能之前，应始终检查设备是否拥有兼容的 Google Play 服务 APK。
             * 我们建议您在以下两个位置进行检查：主 Activity 的 onCreate() 方法中，及其 onResume() 方法中。
             * onCreate() 中的检查可确保该应用在检查成功之前无法使用。
             * onResume() 中的检查可确保当用户通过一些其他方式返回正在运行的应用（比如通过返回按钮）时，检查仍将继续进行。
             * 如果设备没有兼容的 Google Play 服务版本，您的应用可以调用以下方法，以便让用户从 Play 商店下载 Google Play 服务。
             * 它将尝试在此设备上提供Google Play服务。如果Play服务已经可用，则Task可以立即完成返回。
             */
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)

            // 或者使用以下代码
            /**
             * 通过isUserResolvableError来确定是否可以通过用户操作解决错误
             */
            if (GoogleApiAvailability.getInstance().isUserResolvableError(code)) {
                /**
                 * 返回一个对话框，用于解决提供的errorCode。
                 * @param activity  用于创建对话框的父活动
                 * @param code      通过调用返回的错误代码
                 * @param activity  调用startActivityForResult时给出的requestCode
                 */
                //GoogleApiAvailability.getInstance().getErrorDialog(this, code, 200)?.show()
            }

        }
    }


}