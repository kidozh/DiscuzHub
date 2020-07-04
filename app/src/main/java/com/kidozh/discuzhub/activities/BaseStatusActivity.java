package com.kidozh.discuzhub.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import org.jsoup.Connection;

import okhttp3.OkHttpClient;

public class BaseStatusActivity extends AppCompatActivity {
    private final static String TAG = BaseStatusActivity.class.getSimpleName();
    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    forumUserBriefInfo userBriefInfo;
    OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getIntentInfo();
        configureDarkMode();
    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        if(intent == null){
            return;
        }
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(curBBS == null){
            finishAfterTransition();
        }
        else {
            URLUtils.setBBS(curBBS);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(curBBS.site_name);
        }
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);

    }

    private void configureDarkMode(){

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:{
                this.finishAfterTransition();
                return false;
            }
            case R.id.bbs_forum_nav_personal_center:{
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                intent.putExtra("UID",curUser.uid);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_settings:{
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_forum_nav_draft_box:{
                Intent intent = new Intent(this, bbsShowThreadDraftActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,curUser);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_about_app:{
                Intent intent = new Intent(this,aboutAppActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
