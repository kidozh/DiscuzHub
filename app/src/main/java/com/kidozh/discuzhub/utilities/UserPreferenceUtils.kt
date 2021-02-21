package com.kidozh.discuzhub.utilities

import android.content.Context
import androidx.preference.PreferenceManager
import android.util.Log
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import java.util.*
import kotlin.collections.HashSet

object UserPreferenceUtils {
    private val TAG = UserPreferenceUtils::class.java.simpleName
    fun getRewriteRulePreferenceName(context: Context, bbsInfo: Discuz, rewriteKey: String): String {
        return context.getString(R.string.bbs_rewrite_rule_preference, rewriteKey, bbsInfo.id)
    }

    @JvmField
    var REWRITE_FORM_DISPLAY_KEY = "forum_forumdisplay"
    @JvmField
    var REWRITE_VIEW_THREAD_KEY = "forum_viewthread"
    @JvmField
    var REWRITE_HOME_SPACE = "home_space"
    @JvmStatic
    fun saveRewriteRule(context: Context, bbsInfo: Discuz, rewriteKey: String, rewriteValue: String?) {
        val preferenceName = getRewriteRulePreferenceName(context, bbsInfo, rewriteKey)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putString(preferenceName, rewriteValue)
        editor.apply()
    }

    @JvmStatic
    fun getRewriteRule(context: Context, bbsInfo: Discuz, rewriteKey: String): String? {
        val preferenceName = getRewriteRulePreferenceName(context, bbsInfo, rewriteKey)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        Log.d(TAG, "Get rewrite " + rewriteKey + " -> " + prefs.getString(preferenceName, null))
        return prefs.getString(preferenceName, null)
    }

    @JvmStatic
    fun syncFavorite(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_sync_information)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val syncFavoriteName = context.getString(R.string.preference_key_sync_favorite)
        return prefs.getBoolean(preferenceName, true) && prefs.getBoolean(syncFavoriteName, true)
    }

    @JvmStatic
    fun syncInformation(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_sync_information)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, true)
    }

    @JvmStatic
    fun dontDisturbAtNight(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_dont_distrub_at_night)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, true)
    }

    @JvmStatic
    fun conciseRecyclerView(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_concise_recyclerview)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, false)
    }

    fun vibrateWhenLoadingAll(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_vibrate_when_load_all)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, true)
    }

    @JvmStatic
    fun collapseForumRule(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_collapse_forum_rule)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, true)
    }

    @JvmStatic
    fun autoClearViewHistories(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_clear_history_periodically)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, true)
    }

    fun dataSaveMode(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_data_save_mode)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, true)
    }

    fun getLastSelectedDrawerItemIdentifier(context: Context): Int {
        val preferenceName = context.getString(R.string.preference_key_last_selected_bbs_identifier)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(preferenceName, -1)
    }

    fun saveLastSelectedDrawerItemIdentifier(context: Context, indentifier: Int) {
        val preferenceName = context.getString(R.string.preference_key_last_selected_bbs_identifier)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putInt(preferenceName, indentifier)
        editor.apply()
    }

    fun getEnableRecyclerviewAnimate(context: Context): Boolean {
        val preferenceName = context.getString(R.string.preference_key_recyclerview_animation)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(preferenceName, false)
    }

    fun getRecyclerviewAnimateType(context: Context): String? {
        val preferenceName = context.getString(R.string.preference_key_recyclerview_animation_list)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(preferenceName, "LandingAnimator")
    }

    fun getAdapterAnimateType(context: Context): Set<String> {
        val preferenceName = context.getString(R.string.preference_key_adapter_animation)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if(prefs.getStringSet(preferenceName, HashSet()) == null){
            return HashSet()
        }
        else{
            return prefs.getStringSet(preferenceName, HashSet())  as Set<String>
        }

    }

    fun getThemeIndex(context: Context): Int {
        val preferenceName = context.getString(R.string.preference_key_theme_index)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(preferenceName, 0)
    }

    fun setThemeIndex(context: Context, index: Int) {
        val preferenceName = context.getString(R.string.preference_key_theme_index)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putInt(preferenceName, index)
        editor.apply()
    }
}