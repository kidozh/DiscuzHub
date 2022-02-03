package com.kidozh.discuzhub.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.WebView
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.ihsanbal.logging.LoggingInterceptor
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.entities.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkUtils {
    private val TAG = NetworkUtils::class.java.simpleName
    const val CONNECT_TIMEOUT = 10
    const val READ_TIMEOUT = 10
    const val WRITE_TIMEOUT = 10
    private const val preferenceName = "use_safe_https_client"
    @JvmStatic
    fun getPreferredClientWithCookieJar(context: Context): OkHttpClient {
        return getSafeOkHttpClientWithCookieJar(context)
    }

    @JvmStatic
    fun getPreferredClient(context: Context): OkHttpClient {
        return getSafeOkHttpClient(context)
    }

    private fun getSafeOkHttpClient(context: Context): OkHttpClient {
        val mBuilder = OkHttpClient.Builder()
        mBuilder.addInterceptor(LoggingInterceptor.Builder().build())
        mBuilder.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        return addUserAgent(context, mBuilder)
    }

    private fun getSafeOkHttpClientWithCookieJar(context: Context): OkHttpClient {
        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        cookieJar.clearSession()
        cookieJar.clear()
        val mBuilder: OkHttpClient.Builder = OkHttpClient.Builder().cookieJar(cookieJar)
        mBuilder.addInterceptor(LoggingInterceptor.Builder().build())
        mBuilder.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        return addUserAgent(context, mBuilder)
    }

    @JvmStatic
    fun getPreferredClientWithCookieJarByUser(context: Context, briefInfo: User?): OkHttpClient {
        return if (briefInfo == null) {
            getPreferredClient(context)
        } else {
            getSafeOkHttpClientWithCookieJarByUser(context, briefInfo)
        }
    }

    @JvmStatic
    fun getPreferredClientWithCookieJarByUserWithDefaultHeader(
        context: Context,
        briefInfo: User?
    ): OkHttpClient {
        return if (briefInfo == null) {
            getPreferredClient(context)
        } else {
            getSafeOkHttpClientWithCookieJarByUserWithDefaultHeader(context, briefInfo)
        }
    }

    fun getSharedPreferenceNameByUser(briefInfo: User): String {
        return "CookiePersistence_U" + briefInfo.id
    }

    private fun addUserAgent(context: Context, mBuilder: OkHttpClient.Builder): OkHttpClient {
        // get preference
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val uaString =
            prefs.getString(context.getString(R.string.preference_key_use_browser_client), "NONE")
        return if ("ANDROID" == uaString) {
            try {
                val useragent = WebView(context).settings.userAgentString
                // Log.d(TAG,"UA "+useragent);
                mBuilder.addInterceptor(Interceptor { chain ->
                    val original: Request = chain.request()
                    val request = original.newBuilder()
                        .header("User-Agent", useragent)
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                })
                mBuilder.build()
            } catch (e: Exception) {
                e.printStackTrace()
                val useragent =
                    "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
                // Log.d(TAG,"UA "+useragent);
                mBuilder.addInterceptor(Interceptor { chain ->
                    val original: Request = chain.request()
                    val request = original.newBuilder()
                        .header("User-Agent", useragent)
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                })
                mBuilder.build()
            }
        } else mBuilder.build()
    }

    private fun addUserAgentWithDefaultAndroidHeader(context: Context, mBuilder: OkHttpClient.Builder): OkHttpClient {
        // get preference
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val uaString =
            prefs.getString(context.getString(R.string.preference_key_use_browser_client), "NONE")
        return when (uaString) {
            "ANDROID" -> {
                val useragent =
                    "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
                // Log.d(TAG,"UA "+useragent);
                mBuilder.addInterceptor(Interceptor { chain ->
                    val original: Request = chain.request()
                    val request = original.newBuilder()
                        .header("User-Agent", useragent)
                        .header("Accept", "application/vnd.yourapi.v1.full+json")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                })
                mBuilder.build()
            }
            else -> {
                mBuilder.build()
            }
        }
    }

    private fun getSafeOkHttpClientWithCookieJarByUser(context: Context, briefInfo: User): OkHttpClient {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(
                context.getSharedPreferences(
                    getSharedPreferenceNameByUser(
                        briefInfo
                    ), Context.MODE_PRIVATE
                )
            )
        )
        val mBuilder: OkHttpClient.Builder = OkHttpClient.Builder().cookieJar(cookieJar)
        mBuilder
            .addInterceptor(LoggingInterceptor.Builder().build())
            .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        return addUserAgent(context, mBuilder)
    }

    // default header
    private fun getSafeOkHttpClientWithCookieJarByUserWithDefaultHeader(
        context: Context,
        briefInfo: User
    ): OkHttpClient {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(
                context.getSharedPreferences(
                    getSharedPreferenceNameByUser(
                        briefInfo
                    ), Context.MODE_PRIVATE
                )
            )
        )
        val mBuilder: OkHttpClient.Builder = OkHttpClient.Builder().cookieJar(cookieJar)
        mBuilder
            .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        return addUserAgentWithDefaultAndroidHeader(context, mBuilder)
    }

    // cleared cookie
    fun clearUserCookieInfo(context: Context, briefInfo: User) {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(
                context.getSharedPreferences(
                    "CookiePersistence_U" + briefInfo.id,
                    Context.MODE_PRIVATE
                )
            )
        )
        context.getSharedPreferences(getSharedPreferenceNameByUser(briefInfo), Context.MODE_PRIVATE)
            .edit().clear().apply()
        cookieJar.clear()
        cookieJar.clearSession()
    }

    const val NETWORK_STATUS_NO_CONNECTION = 0
    const val NETWORK_STATUS_WIFI = 1
    const val NETWORK_STATUS_MOBILE_DATA = 2
    @JvmStatic
    fun isOnline(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @JvmStatic
    fun getOfflineErrorMessage(context: Context): ErrorMessage {
        return ErrorMessage(
            context.getString(R.string.network_state_unavaliable_error_key),
            context.getString(R.string.network_state_unavaliable_error_content),
            R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24
        )
    }

    private fun isWifiConnected(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connMgr.getNetworkCapabilities(
            connMgr.activeNetwork
        ) ?: return false
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
        ) {
            return true
        }
        return false
    }

    @JvmStatic
    fun canDownloadImageOrFile(context: Context): Boolean {
        // for debug
        return if (isWifiConnected(context)) {
            true
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val isDataSaverMode =
                prefs.getBoolean(context.getString(R.string.preference_key_data_save_mode), true)
            !isDataSaverMode
        }
    }

    @JvmStatic
    fun getRetrofitInstance(baseUrl: String, client: OkHttpClient): Retrofit {
        var baseUrl = baseUrl
        if (!baseUrl.endsWith("/")) {
            baseUrl = "$baseUrl/"
        }
        val objectMapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(ParameterNamesModule())
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(client)
            .build()
    }
}