package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ActivityAboutAppBinding;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutAppActivity extends BaseStatusActivity {


    ActivityAboutAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configureActionBar();
        renderVersionText();
        configureCardView();
    }

    void renderVersionText(){
        PackageManager packageManager = getPackageManager();
        try{
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
            String version = packageInfo.versionName;
            binding.aboutAppVersion.setText(getString(R.string.app_version_template,version));
        }
        catch (Exception e){
            binding.aboutAppVersion.setText(R.string.welcome);
        }

    }

    void configureCardView(){
        binding.aboutContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:kidozh@gmail.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_contact_developer));
                startActivity(data);
            }
        });
        binding.aboutHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/"));
                startActivity(intent);
            }
        });
        binding.aboutGithubProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kidozh/DiscuzHub"));
                startActivity(intent);
            }
        });
        binding.aboutPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/privacy_policy/"));
                startActivity(intent);
            }
        });
        binding.aboutTermsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/term_of_use/"));
                startActivity(intent);
            }
        });
        binding.aboutOpenSourceLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discuzhub.kidozh.com/open_source_licence/"));
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
