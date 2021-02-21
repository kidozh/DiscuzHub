package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.Discuz;

import java.util.HashSet;
import java.util.Set;

public class UserPreferenceUtils {
    private static final String TAG = UserPreferenceUtils.class.getSimpleName();

    public static String getRewriteRulePreferenceName(@NonNull Context context, @NonNull Discuz bbsInfo, @NonNull String rewriteKey){
        return context.getString(R.string.bbs_rewrite_rule_preference,rewriteKey,bbsInfo.getId());
    }

    public static String REWRITE_FORM_DISPLAY_KEY = "forum_forumdisplay",
            REWRITE_VIEW_THREAD_KEY = "forum_viewthread",
            REWRITE_HOME_SPACE = "home_space";

    public static void saveRewriteRule(@NonNull Context context, @NonNull Discuz bbsInfo, @NonNull String rewriteKey, String rewriteValue){
        String preferenceName = getRewriteRulePreferenceName(context,bbsInfo,rewriteKey);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(preferenceName,rewriteValue);
        editor.apply();
    }

    public static String getRewriteRule(@NonNull Context context, @NonNull Discuz bbsInfo, @NonNull String rewriteKey){
        String preferenceName = getRewriteRulePreferenceName(context,bbsInfo,rewriteKey);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG,"Get rewrite "+rewriteKey+" -> "+prefs.getString(preferenceName,null));
        return prefs.getString(preferenceName,null);
    }

    public static boolean syncFavorite(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_sync_information);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String syncFavoriteName = context.getString(R.string.preference_key_sync_favorite);
        return prefs.getBoolean(preferenceName,true) && prefs.getBoolean(syncFavoriteName,true);
    }

    public static boolean syncInformation(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_sync_information);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }

    public static boolean dontDisturbAtNight(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_dont_distrub_at_night);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }

    public static boolean conciseRecyclerView(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_concise_recyclerview);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,false);
    }

    public static boolean vibrateWhenLoadingAll(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_vibrate_when_load_all);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }

    public static boolean collapseForumRule(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_collapse_forum_rule);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }

    public static boolean autoClearViewHistories(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_clear_history_periodically);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }

    public static boolean dataSaveMode(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_data_save_mode);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }

    public static int getLastSelectedDrawerItemIdentifier(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_last_selected_bbs_identifier);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(preferenceName,-1);
    }

    public static void saveLastSelectedDrawerItemIdentifier(@NonNull Context context, int indentifier){
        String preferenceName = context.getString(R.string.preference_key_last_selected_bbs_identifier);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(preferenceName,indentifier);
        editor.apply();
    }

    public static boolean getEnableRecyclerviewAnimate(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_recyclerview_animation);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,false);
    }

    public static String getRecyclerviewAnimateType(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_recyclerview_animation_list);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(preferenceName,"LandingAnimator");
    }

    public static Set<String> getAdapterAnimateType(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_adapter_animation);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(preferenceName,new HashSet<>());
    }

    public static Integer getThemeIndex(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_theme_index);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(preferenceName,0);
    }

    public static void setThemeIndex(@NonNull Context context, int index){
        String preferenceName = context.getString(R.string.preference_key_theme_index);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(preferenceName,index);
        editor.apply();
    }
}
