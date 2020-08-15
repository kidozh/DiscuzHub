package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.R;

public class UserPreferenceUtils {

    public static boolean isSyncBBSInformation(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_sync_information);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String syncFavoriteName = context.getString(R.string.preference_key_sync_favorite);
        return prefs.getBoolean(preferenceName,true) && prefs.getBoolean(syncFavoriteName,true);
    }
}
