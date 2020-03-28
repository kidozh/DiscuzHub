package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class aboutAppActivity extends AppCompatActivity {
    @BindView(R.id.about_app_version)
    TextView appVersionTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        ButterKnife.bind(this);
        configureActionBar();
        renderVersionText();
    }

    void renderVersionText(){
        PackageManager packageManager = getPackageManager();
        try{
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
            String version = packageInfo.versionName;
            appVersionTextview.setText(getString(R.string.app_version_template,version));
        }
        catch (Exception e){
            appVersionTextview.setText(R.string.welcome);
        }

    }

    private void configureActionBar(){

        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:{
                this.finishAfterTransition();
                return false;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
