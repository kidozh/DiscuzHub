package com.kidozh.discuzhub.activities;

import android.content.DialogInterface;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.errorprone.annotations.Var;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.BaseResult;
import com.kidozh.discuzhub.results.VariableResults;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import org.jsoup.Connection;

import okhttp3.OkHttpClient;

public class BaseStatusActivity extends AppCompatActivity
    implements BaseStatusInteract {
    private final static String TAG = BaseStatusActivity.class.getSimpleName();
    public bbsInformation bbsInfo;
    public forumUserBriefInfo userBriefInfo;
    OkHttpClient client = new OkHttpClient();
    BaseResult baseVariableResult;
    VariableResults variableResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getIntentInfo();
        configureDarkMode();
    }

    public void setBaseResult(BaseResult baseVariableResult, VariableResults variableResults){
        Log.d(TAG,"Recv "+userBriefInfo+" "+variableResults+" UID "+String.valueOf(variableResults!=null?variableResults.member_uid:-8512));
        if(userBriefInfo!=null && this.variableResults!=null && variableResults!=null && variableResults.member_uid == 0){
            // open up a dialog
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            //MaterialAlertDialogBuilder builder =  new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.user_login_expired,userBriefInfo!=null?userBriefInfo.username:""))
                    .setPositiveButton(getString(R.string.user_relogin, userBriefInfo!=null?userBriefInfo.username:""), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                            intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.show();
        }
        this.baseVariableResult = baseVariableResult;
        this.variableResults = variableResults;
    }

    public static final int CHARSET_UTF8 = 1;
    public static final int CHARSET_GBK = 2;
    public static final int CHARSET_BIG5 = 3;

    public int getCharsetType(){
        if(baseVariableResult !=null && baseVariableResult.Charset!=null){
            if(baseVariableResult.Charset.equals("GBK")){
                return CHARSET_GBK;
            }
            else if(baseVariableResult.Charset.equals("BIG5")){
                return CHARSET_BIG5;
            }
        }
        // follow UTF8 default
        return CHARSET_UTF8;

    }

    public boolean isGBKCharset(){
        return getCharsetType() == CHARSET_GBK;
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
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra("UID",userBriefInfo.uid);
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
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
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
