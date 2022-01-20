package com.kidozh.discuzhub.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.results.BaseResult
import com.kidozh.discuzhub.results.VariableResults
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.ThemeUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils
import okhttp3.OkHttpClient

open class BaseStatusActivity : AppCompatActivity(), BaseStatusInteract {
    @JvmField
    var discuz: Discuz? = null
    @JvmField
    public var user: User? = null
    @JvmField
    var client = OkHttpClient()
    var baseVariableResult: BaseResult? = null
    var variableResults: VariableResults? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTheme()
        configureDarkMode()
        // allow adaptive color
        DynamicColors.applyToActivitiesIfAvailable(application)
    }

    var themeIndex : Int = 0

    val styleList = ThemeUtils.styleList

    fun configureTheme(){
        val position = UserPreferenceUtils.getThemeIndex(this)
        themeIndex = position
        if(position < styleList.size){

            setTheme(styleList[position])
            //theme.applyStyle(styleList[position],true)

        }
        // try to implement material you container color
        //setTheme(R.style.AppTheme)
    }

    // follow UTF8 default
    val charsetType: Int
        get() {
            if (baseVariableResult != null) {
                if (baseVariableResult!!.Charset == "GBK") {
                    return CHARSET_GBK
                } else if (baseVariableResult!!.Charset == "BIG5") {
                    return CHARSET_BIG5
                }
            }
            // follow UTF8 default
            return CHARSET_UTF8
        }


    private fun configureDarkMode() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        when (prefs.getString(getString(R.string.preference_key_display_mode), "")) {
            "MODE_NIGHT_NO" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                return
            }
            "MODE_NIGHT_YES" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                return
            }
            "MODE_NIGHT_FOLLOW_SYSTEM" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                return
            }
            "MODE_NIGHT_AUTO_BATTERY" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val position = UserPreferenceUtils.getThemeIndex(this)
        if(position < styleList.size){

            setTheme(styleList[position])
            //theme.applyStyle(styleList[position],true)
            if(themeIndex != position){
                themeIndex = position
                recreate()

            }

        }
    }



    companion object {
        private val TAG = BaseStatusActivity::class.java.simpleName
        const val CHARSET_UTF8 = 1
        const val CHARSET_GBK = 2
        const val CHARSET_BIG5 = 3
    }

    override fun setBaseResult(baseVariableResult: BaseResult, variableResults: VariableResults) {
        if(user!= null && variableResults.member_uid != user!!.uid){
            Log.d(TAG,"Recv variable result ${variableResults.member_uid} , real name ${user!!.uid}")
            // launch a dialog
            val alertDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.user_relogin, user!!.username))
                .setMessage(getString(R.string.user_login_expired, user!!.username))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz!!)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
                    startActivity(intent)
                }
                .create()
            alertDialog.show();
        }
    }
}