package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.R;

public class PreferenceUtils {

    public static boolean isSyncBBSInformation(@NonNull Context context){
        String preferenceName = context.getString(R.string.preference_key_sync_information);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(preferenceName,true);
    }
}
