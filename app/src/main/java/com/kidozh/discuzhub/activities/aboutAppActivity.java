package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class aboutAppActivity extends AppCompatActivity {
    @BindView(R.id.about_app_version)
    TextView appVersionTextview;
    @BindView(R.id.about_contact_us)
    CardView contactUsCardView;
    @BindView(R.id.about_homepage)
    CardView homepageCardView;
    @BindView(R.id.about_github_project)
    CardView githubProjectCardView;
    @BindView(R.id.about_privacy_policy)
    CardView privacyPolicyCardView;
    @BindView(R.id.about_terms_of_use)
    CardView termsOfUseCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        ButterKnife.bind(this);
        configureActionBar();
        renderVersionText();
        configureCardView();
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

    void configureCardView(){
        contactUsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:kidozh@gmail.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_contact_developer));
                startActivity(data);
            }
        });
        homepageCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/"));
                startActivity(intent);
            }
        });
        githubProjectCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kidozh/DiscuzHub"));
                startActivity(intent);
            }
        });
        privacyPolicyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/privacy_policy/"));
                startActivity(intent);
            }
        });
        termsOfUseCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/term_of_use/"));
                startActivity(intent);
            }
        });
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
