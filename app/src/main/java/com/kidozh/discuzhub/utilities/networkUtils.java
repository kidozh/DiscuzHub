package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class networkUtils {
    private static String TAG = networkUtils.class.getSimpleName();
    public final static int CONNECT_TIMEOUT =5;
    public final static int READ_TIMEOUT=5;
    public final static int WRITE_TIMEOUT=5;



    private static String preferenceName = "use_safe_https_client";

    public static OkHttpClient getPreferredClientWithCookieJar(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
        Boolean useSafeHttpClient = prefs.getBoolean(preferenceName,true);
        if(useSafeHttpClient){
            return getSafeOkHttpClientWithCookieJar(context);
        }
        else {
            return getUnsafeOkHttpClientWithCookieJar(context);
        }
    }

    public static OkHttpClient getPreferredClient(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
        Boolean useSafeHttpClient = prefs.getBoolean(preferenceName,true);
        if(useSafeHttpClient){
            return getSafeOkHttpClient(context);
        }
        else {
            return getUnsafeOkHttpClient(context);
        }
    }

    public static OkHttpClient getPreferredClient(Context context,Boolean useSafeHttpClient){
        if(useSafeHttpClient){
            return getSafeOkHttpClient(context);
        }
        else {
            return getUnsafeOkHttpClient(context);
        }
    }

    public static OkHttpClient getSafeOkHttpClient(Context context){
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);
        return addUserAgent(context,mBuilder);
    }

    public static OkHttpClient getSafeOkHttpClientWithCookieJar(Context context){

        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        cookieJar.clearSession();
        cookieJar.clear();
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        mBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);


        return addUserAgent(context,mBuilder);
    }

    public static OkHttpClient getPreferredClientWithCookieJarByUser(Context context, forumUserBriefInfo briefInfo){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
        Boolean useSafeHttpClient = prefs.getBoolean(preferenceName,true);
        if(briefInfo==null){
            return getPreferredClient(context);
        }
        else {
            if(useSafeHttpClient){
                return getSafeOkHttpClientWithCookieJarByUser(context,briefInfo);
            }
            else {
                return getUnSafeOkHttpClientWithCookieJarByUser(context,briefInfo);
            }
        }


    }

    public static OkHttpClient getPreferredClientWithCookieJarByUserWithDefaultHeader(Context context, forumUserBriefInfo briefInfo){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
        Boolean useSafeHttpClient = prefs.getBoolean(preferenceName,true);
        if(briefInfo==null){
            return getPreferredClient(context);
        }
        else {
            if(useSafeHttpClient){
                return getSafeOkHttpClientWithCookieJarByUserWithDefaultHeader(context,briefInfo);
            }
            else {
                return getUnSafeOkHttpClientWithCookieJarByUserWithDefaultHeader(context,briefInfo);
            }
        }


    }

    public static void copySharedPrefence(SharedPreferences fromSp, SharedPreferences toSp){
        SharedPreferences sp1 = toSp, sp = fromSp;
        SharedPreferences.Editor ed = sp1.edit();
        // SharedPreferences sp = Sp2; //The shared preferences to copy from
        ed.clear(); // This clears the one we are copying to, but you don't necessarily need to do that.
        //Cycle through all the entries in the sp
        for(Map.Entry<String,?> entry : sp.getAll().entrySet()){
            Object v = entry.getValue();
            String key = entry.getKey();
            Log.d(TAG,"Transition "+key+" val :"+v.toString());
            //Now we just figure out what type it is, so we can copy it.
            // Note that i am using Boolean and Integer instead of boolean and int.
            // That's because the Entry class can only hold objects and int and boolean are primatives.
            if(v instanceof Boolean)
                // Also note that i have to cast the object to a Boolean
                // and then use .booleanValue to get the boolean
                ed.putBoolean(key, (Boolean) v);
            else if(v instanceof Float)
                ed.putFloat(key, (Float) v);
            else if(v instanceof Integer)
                ed.putInt(key, (Integer) v);
            else if(v instanceof Long)
                ed.putLong(key, (Long) v);
            else if(v instanceof String)
                ed.putString(key, ((String)v));
        }
        ed.apply(); //save it.
    }

    public static String getSharedPreferenceNameByUser(@NonNull forumUserBriefInfo briefInfo){
        return "CookiePersistence_U"+briefInfo.getId();
    }

    public static OkHttpClient addUserAgent(Context context,OkHttpClient.Builder mBuilder){
        // get preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
        String uaString = prefs.getString(context.getString(R.string.preference_key_use_browser_client),"NONE");
        switch (uaString){
            case "NONE":{
                return mBuilder.build();
            }
            case "ANDROID":{

                try{
                    String useragent = new WebView(context).getSettings().getUserAgentString();
                    // Log.d(TAG,"UA "+useragent);
                    mBuilder.addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();

                            Request request = original.newBuilder()
                                    .header("User-Agent", useragent)
                                    .method(original.method(), original.body())
                                    .build();

                            return chain.proceed(request);
                        }
                    });

                    return mBuilder.build();
                }
                catch (Exception e){
                    e.printStackTrace();
                    String useragent = "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
                    // Log.d(TAG,"UA "+useragent);
                    mBuilder.addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();

                            Request request = original.newBuilder()
                                    .header("User-Agent", useragent)
                                    .method(original.method(), original.body())
                                    .build();

                            return chain.proceed(request);
                        }
                    });

                    return mBuilder.build();
                }



            }
            default:{
                return mBuilder.build();
            }
        }


    }

    public static OkHttpClient addUserAgentWithDefaultAndroidHeader(Context context,OkHttpClient.Builder mBuilder){
        // get preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
        String uaString = prefs.getString(context.getString(R.string.preference_key_use_browser_client),"NONE");
        switch (uaString){
            case "NONE":{
                return mBuilder.build();
            }
            case "ANDROID":{
                String useragent = "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
                // Log.d(TAG,"UA "+useragent);
                mBuilder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .header("User-Agent", useragent)
                                .header("Accept", "application/vnd.yourapi.v1.full+json")
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(request);
                    }
                });

                return mBuilder.build();
            }
            default:{
                return mBuilder.build();
            }
        }


    }

    public static OkHttpClient getSafeOkHttpClientWithCookieJarByUser(Context context, forumUserBriefInfo briefInfo){
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(),
                        new SharedPrefsCookiePersistor(context.getSharedPreferences(getSharedPreferenceNameByUser(briefInfo),Context.MODE_PRIVATE))
                );
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        mBuilder

                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);

        return addUserAgent(context,mBuilder);
    }

    public static OkHttpClient getUnSafeOkHttpClientWithCookieJarByUser(Context context, forumUserBriefInfo briefInfo){


        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(),
                        new SharedPrefsCookiePersistor(context.getSharedPreferences(getSharedPreferenceNameByUser(briefInfo),Context.MODE_PRIVATE))
                );

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        final X509TrustManager trustManager = new TrustAllCerts();
        mBuilder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), trustManager);
        mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
        mBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);


        return addUserAgent(context,mBuilder);
    }

    // default header
    public static OkHttpClient getSafeOkHttpClientWithCookieJarByUserWithDefaultHeader(Context context, forumUserBriefInfo briefInfo){
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(),
                        new SharedPrefsCookiePersistor(context.getSharedPreferences(getSharedPreferenceNameByUser(briefInfo),Context.MODE_PRIVATE))
                );
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        mBuilder

                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);

        return addUserAgentWithDefaultAndroidHeader(context,mBuilder);
    }

    public static OkHttpClient getUnSafeOkHttpClientWithCookieJarByUserWithDefaultHeader(Context context, forumUserBriefInfo briefInfo){


        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(),
                        new SharedPrefsCookiePersistor(context.getSharedPreferences(getSharedPreferenceNameByUser(briefInfo),Context.MODE_PRIVATE))
                );

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        final X509TrustManager trustManager = new TrustAllCerts();
        mBuilder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), trustManager);
        mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
        mBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);


        return addUserAgentWithDefaultAndroidHeader(context,mBuilder);
    }

    // cleared cookie
    public static void clearUserCookieInfo(Context context, forumUserBriefInfo briefInfo){
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(),
                        new SharedPrefsCookiePersistor(context.getSharedPreferences("CookiePersistence_U"+briefInfo.getId(),Context.MODE_PRIVATE))
                );
        context.getSharedPreferences(getSharedPreferenceNameByUser(briefInfo),Context.MODE_PRIVATE).edit().clear().apply();
        cookieJar.clear();
        cookieJar.clearSession();

    }

    public static void clearUserCookieInfo(Context context){
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(),
                        new SharedPrefsCookiePersistor(context.getSharedPreferences("CookiePersistence",Context.MODE_PRIVATE))
                );
        context.getSharedPreferences("CookiePersistence",Context.MODE_PRIVATE).edit().clear().apply();
        cookieJar.clear();
        cookieJar.clearSession();

    }
    // end cleared cookie


    public static OkHttpClient getUnsafeOkHttpClient(Context context){
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        //mBuilder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory());
        final X509TrustManager trustManager = new TrustAllCerts();
        mBuilder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), trustManager);
        mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
        mBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);
        return addUserAgent(context,mBuilder);
    }

    public static OkHttpClient getUnsafeOkHttpClientWithCookieJar(Context context){
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        cookieJar.clearSession();
        cookieJar.clear();
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        //mBuilder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory());
        final X509TrustManager trustManager = new TrustAllCerts();
        mBuilder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), trustManager);
        mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
        mBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);


        return addUserAgent(context,mBuilder);
    }



    public final static int NETWORK_STATUS_NO_CONNECTION = 0;
    public final static int NETWORK_STATUS_WIFI = 1;
    public final static int NETWORK_STATUS_MOBILE_DATA = 2;


    public static int getConnectedType(Context context) {
        int netType = 0;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NETWORK_STATUS_NO_CONNECTION;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = NETWORK_STATUS_WIFI;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            netType = NETWORK_STATUS_MOBILE_DATA;
        }
        return netType;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean canDownloadImageOrFile(Context context){
        // for debug
        if(getConnectedType(context) == NETWORK_STATUS_WIFI){
            return true;
        }
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
            boolean isDataSaverMode = prefs.getBoolean(context.getString(R.string.preference_key_data_save_mode),true);
            if(isDataSaverMode){
                return false;
            }
            else {
                return true;
            }
        }


    }


}
