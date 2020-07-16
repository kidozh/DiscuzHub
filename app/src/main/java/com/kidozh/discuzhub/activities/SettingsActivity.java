package com.kidozh.discuzhub.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;

public class SettingsActivity extends BaseStatusActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }



    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            bindPreferenceListener();
        }

        void bindPreferenceListener(){
            // night mode preference
            findPreference(getString(R.string.preference_key_display_mode)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    switch (stringValue){
                        case "MODE_NIGHT_NO":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            return true;
                        case "MODE_NIGHT_YES":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            return true;
                        case "MODE_NIGHT_FOLLOW_SYSTEM":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            return true;
                        case "MODE_NIGHT_AUTO_BATTERY":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                            return true;
                    }
                    return false;
                }
            });

            findPreference(getString(R.string.preference_key_record_history)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isRecord = (Boolean) newValue;
                    if(!isRecord && getContext() !=null){
                        AlertDialog alertDialogs = new MaterialAlertDialogBuilder(getContext())
                                .setTitle(getString(R.string.delete_all_view_history))
                                //.setIcon(ContextCompat.getDrawable(getContext(),R.drawable.ic_help_outline_24px))
                                .setMessage(getString(R.string.setting_delete_all_view_history_title))
                                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setPositiveButton(getString(R.string.clear_all_history), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new deleteAllViewHistory(getContext()).execute();
                                    }
                                })

                                .create();
                        alertDialogs.show();
                    }
                    return true;
                }
            });


            ListPreference uaPreference = (ListPreference) findPreference(getString(R.string.preference_key_use_browser_client));
            if(uaPreference!=null){
                uaPreference.setSummaryProvider(new Preference.SummaryProvider() {
                    @Override
                    public CharSequence provideSummary(Preference preference) {
                        String value = uaPreference.getValue();
                        switch (value){
                            case "NONE":{
                                return getString(R.string.preference_summary_use_browser_client_NONE);
                            }
                            case "ANDROID":{
                                String useragent = new WebView(getContext()).getSettings().getUserAgentString();
                                return getString(R.string.preference_summary_use_browser_client_ANDROID)
                                        +"\n"+String.format("\"%s\"",useragent);
                            }
                            default:
                                return "%s";
                        }
                    }
                });
            }

        }


    }

    Context context = this;

    private static class deleteAllViewHistory extends AsyncTask<Void,Void,Void> {
        Context context;

        deleteAllViewHistory(Context context){
            this.context =context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ViewHistoryDatabase.getInstance(context).getDao().deleteAllViewHistory();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            VibrateUtils.vibrateForNotice(context);
            Toasty.success(context,context.getString(R.string.have_deleted_all_view_history), Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finishAfterTransition();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}