package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsDetailInformationAdapter;
import com.kidozh.discuzhub.database.forumInformationDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsShowInformationActivity extends AppCompatActivity {
    private final static String TAG = bbsShowInformationActivity.class.getSimpleName();
    @BindView(R.id.show_bbs_information_name)
    TextView bbsInfoName;
    @BindView(R.id.show_bbs_information_avatar)
    ImageView bbsInfoAvatar;
    @BindView(R.id.show_bbs_information_post_number)
    TextView bbsInfoPost;
    @BindView(R.id.show_bbs_information_siteid)
    TextView bbsInfoSiteId;
    @BindView(R.id.show_bbs_information_member_number)
    TextView bbsInfoMember;
    @BindView(R.id.show_bbs_information_recyclerview)
    RecyclerView bbsInfoRecyclerview;
    @BindView(R.id.show_bbs_information_use_safe_client_switch)
    Switch bbsInfoUseSafeClientCheckBox;
    @BindView(R.id.show_bbs_information_sync_switch)
    Switch bbsInfoSyncSwitch;


    bbsInformation bbsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_information);
        ButterKnife.bind(this);
        configureIntent();
        setInformation();
        configureActionBar();
        configureRecyclerview();
        configureUseSafeClientCheckbox();
    }

    private void configureIntent(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        bbsURLUtils.setBBS(bbsInfo);
    }

    private void setInformation(){
        bbsInfoName.setText(bbsInfo.site_name);
        bbsInfoPost.setText(bbsInfo.total_posts);
        bbsInfoMember.setText(bbsInfo.total_members);
        bbsInfoSiteId.setText(bbsInfo.mysite_id);
        bbsInfoUseSafeClientCheckBox.setChecked(bbsInfo.useSafeClient);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(this));
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(this)
                .load(bbsURLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(bbsInfoAvatar);
    }

    private void configureRecyclerview(){
        // discuz version
        List<bbsDetailInformationAdapter.bbsKV> bbsKVList = new ArrayList<>();
        bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_electronics,
                getString(R.string.bbs_discuz_api),
                bbsInfo.discuz_version));
        bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_history ,
                getString(R.string.bbs_api_version),
                bbsInfo.version));
        bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_machinery,
                getString(R.string.bbs_discuz_plugin_version),
                bbsInfo.plugin_version));
        bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_link,
                getString(R.string.bbs_ucenter_url),
                bbsInfo.ucenter_url));
        bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_code,
                getString(R.string.bbs_charset),
                bbsInfo.charset));
        if(bbsInfo.hideRegister){
            bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_close,
                    getString(R.string.bbs_hide_register),
                    null));
        }
        else {
            bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_add_2,
                    getString(R.string.bbs_allow_register),
                    null));
        }
        if(bbsInfo.qqConnect){
            bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_qq_status,
                    getString(R.string.bbs_qq_connect_ok),
                    null));
        }
        bbsKVList.add(new bbsDetailInformationAdapter.bbsKV(R.drawable.vector_drawable_clock,
                getString(R.string.bbs_update_time),
                timeDisplayUtils.getLocalePastTimeString(this,bbsInfo.addedTime)));
        bbsDetailInformationAdapter adapter = new bbsDetailInformationAdapter(bbsKVList);
        bbsInfoRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        bbsInfoRecyclerview.setLayoutManager(linearLayoutManager);
        bbsInfoRecyclerview.setAdapter(adapter);

    }

    void configureUseSafeClientCheckbox(){
        bbsInfoUseSafeClientCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bbsInfo.useSafeClient = isChecked;
                new updateForumInformationTask(bbsInfo,getApplicationContext()).execute();
            }
        });
        bbsInfoSyncSwitch.setChecked(bbsInfo.isSync);
        bbsInfoSyncSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bbsInfo.isSync = isChecked;
                new updateForumInformationTask(bbsInfo,getApplicationContext()).execute();
            }
        });
    }

    public static class updateForumInformationTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation forumInfo;
        private Context context;
        public updateForumInformationTask(bbsInformation bbsInformation, Context context){
            this.forumInfo = bbsInformation;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumInformationDatabase
                    .getInstance(context)
                    .getForumInformationDao().update(forumInfo);
            Log.d(TAG, "add forum into database"+forumInfo.site_name);
            return null;
        }

    }



    void configureActionBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(bbsInfo.site_name);
        getSupportActionBar().setSubtitle(bbsInfo.base_url);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
