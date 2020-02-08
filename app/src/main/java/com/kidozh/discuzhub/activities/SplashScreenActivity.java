package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.kidozh.discuzhub.MainActivity;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.services.updateBBSInformationWork;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    @BindView(R.id.splash_screen_notification)
    TextView splashScreenNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        registerNotification();
        configureDarkMode();

        registerWorkManager();
        enterMainActivity();
    }

    // register notification
    private void registerNotification(){
        splashScreenNotification.setText(R.string.action_register_notification_channel);
        notificationUtils.createUpdateProgressNotificationChannel(this);
        notificationUtils.createUsersUpdateChannel(this);
    }

    private void registerWorkManager(){
        Log.d(TAG,"Register work");
        splashScreenNotification.setText(getString(R.string.action_register_work));
        // Create a Constraints object that defines when the task should run

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();
        // update frequency
        // extracting all user...
        List<forumUserBriefInfo> allUsers = forumUserBriefInfoDatabase
                .getSyncDatabase(this)

                .getforumUserBriefInfoDao()
                .getAllUser();
        Log.d(TAG,"ALL USER "+allUsers.size());
        // sync frequency test
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        String syncFreq = prefs.getString(getString(R.string.preference_key_sync_time),"30min");
        int periodicFreq = 30;
        switch (syncFreq){
            case "15min":
                periodicFreq = 15;
                break;
            case "30min":
                periodicFreq = 30;
                break;
            case "1h":
                periodicFreq = 60;
                break;
            case "2h":
                periodicFreq = 120;
                break;
            case "5h":
                periodicFreq = 500;
                break;
        }

        for(int i=0; i<allUsers.size();i++){

            forumUserBriefInfo userBriefInfo = allUsers.get(i);

            Data userData = new Data.Builder()
                    .putInt(bbsConstUtils.WORK_MANAGER_PASS_USER_ID_KEY, userBriefInfo.getId())
                    .build();
            splashScreenNotification.setText(getString(R.string.action_register_work_template,userBriefInfo.username));
            // start periodic work
            Log.d(TAG,"Register notification "+userBriefInfo.username);
            PeriodicWorkRequest saveRequest =
                    new PeriodicWorkRequest.Builder(updateBBSInformationWork.class, periodicFreq, TimeUnit.MINUTES)
                            .setInputData(userData)
                            .setConstraints(constraints)
                            .addTag(bbsConstUtils.WORK_MANAGER_UPDATE_USERS_TAG)
                            .build();

            WorkManager.getInstance(this)
                    .enqueue(saveRequest);

            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(updateBBSInformationWork.class)
                    .setConstraints(constraints)
                    .setInputData(userData)
                    .build();
            WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);
        }

    }

    private void configureDarkMode(){
        splashScreenNotification.setText(R.string.action_configure_dark_mode);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        String dark_mode_settings = prefs.getString(getString(R.string.preference_key_display_mode),"");
        switch (dark_mode_settings){
            case "MODE_NIGHT_NO":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return;
            case "MODE_NIGHT_YES":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return;
            case "MODE_NIGHT_FOLLOW_SYSTEM":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return;
            case "MODE_NIGHT_AUTO_BATTERY":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                return;

                default:
                    ;

        }
    }



    private void enterMainActivity(){
        splashScreenNotification.setText(R.string.action_ready_to_enter_main);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
