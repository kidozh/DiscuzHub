package com.kidozh.discuzhub.activities.ui.bbsDetailedInformation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.BaseStatusActivity;
import com.kidozh.discuzhub.activities.LoginActivity;
import com.kidozh.discuzhub.adapter.bbsDetailInformationAdapter;
import com.kidozh.discuzhub.adapter.forumUsersAdapter;
import com.kidozh.discuzhub.callback.forumSwipeToDeleteUserCallback;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.databinding.ActivityBbsShowInformationBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class bbsShowInformationActivity extends BaseStatusActivity implements forumSwipeToDeleteUserCallback.onSwipedInteraction{
    private final static String TAG = bbsShowInformationActivity.class.getSimpleName();

    forumUsersAdapter userAdapter;


    private bbsShowInformationViewModel viewModel;
    private Observer bbsUserObserver;
    private LiveData<List<forumUserBriefInfo>> bbsUserInfoLiveDatas;

    ActivityBbsShowInformationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_show_information);
        binding = ActivityBbsShowInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configureIntent();
        configureViewModel();
        setInformation();
        configureActionBar();
        configureRecyclerview();
        configureUseSafeClientCheckbox();
    }

    private void configureIntent(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);

        URLUtils.setBBS(bbsInfo);
    }

    private void configureViewModel(){
        viewModel = ViewModelProviders.of(this).get(bbsShowInformationViewModel.class);
    }

    private void setInformation(){
        binding.showBbsInformationName.setText(bbsInfo.site_name);
        binding.showBbsInformationPostNumber.setText(bbsInfo.total_posts);
        binding.showBbsInformationMemberNumber.setText(bbsInfo.total_members);
        binding.showBbsInformationSiteid.setText(bbsInfo.mysite_id);
        binding.showBbsInformationUseSafeClientSwitch.setChecked(bbsInfo.useSafeClient);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(this));
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(this)
                .load(URLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(binding.showBbsInformationAvatar);
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
        binding.showBbsInformationRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.showBbsInformationRecyclerview.setLayoutManager(linearLayoutManager);
        binding.showBbsInformationRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(this,adapter));

        binding.showBbsInformationUserListRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new forumUsersAdapter(this, bbsInfo);
        binding.showBbsInformationUserListRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(this,userAdapter));

        // render forum user info

        bbsUserInfoLiveDatas = forumUserBriefInfoDatabase
                .getInstance(this)
                .getforumUserBriefInfoDao()
                .getAllUserByBBSID(bbsInfo.getId());
        viewModel.setBbsUserInfoLiveDataList(bbsUserInfoLiveDatas);
        bbsUserObserver = new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                Log.d(TAG, "Updating bbs registered user " + forumUserBriefInfos + " id " + bbsInfo.getId());
                if (forumUserBriefInfos != null && forumUserBriefInfos.size() != 0) {

                    binding.showBbsInformationEmptyView.setVisibility(View.GONE);
                } else {
                    binding.showBbsInformationEmptyView.setVisibility(View.VISIBLE);
                }
                userAdapter.setUserList(forumUserBriefInfos);
            }
        };

        viewModel.getBbsUserInfoLiveDataList().observe(this, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                Log.d(TAG, "ViewModel  Updating bbs registered user " + forumUserBriefInfos + " id " + bbsInfo.getId());
                if (forumUserBriefInfos != null && forumUserBriefInfos.size() != 0) {

                    binding.showBbsInformationEmptyView.setVisibility(View.GONE);
                } else {
                    binding.showBbsInformationEmptyView.setVisibility(View.VISIBLE);
                }
                userAdapter.setUserList(forumUserBriefInfos);
            }
        });
        bbsUserInfoLiveDatas.observe(this, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                Log.d(TAG, "LiveData Updating bbs registered user " + forumUserBriefInfos + " id " + bbsInfo.getId());
                if (forumUserBriefInfos != null && forumUserBriefInfos.size() != 0) {

                    binding.showBbsInformationEmptyView.setVisibility(View.GONE);
                } else {
                    binding.showBbsInformationEmptyView.setVisibility(View.VISIBLE);
                }
                userAdapter.setUserList(forumUserBriefInfos);
            }
        });


        // swipe to delete support
        forumSwipeToDeleteUserCallback swipeToDeleteUserCallback = new forumSwipeToDeleteUserCallback(userAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteUserCallback);
        itemTouchHelper.attachToRecyclerView(binding.showBbsInformationUserListRecyclerview);
        Activity activity = this;

        binding.showBbsInformationAddAUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create(binding.showBbsInformationName, "bbs_info_name"),
                        Pair.create(binding.showBbsInformationAvatar, "bbs_info_avatar")


                );

                Bundle bundle = options.toBundle();
                startActivity(intent,bundle);
            }
        });
    }



    void configureUseSafeClientCheckbox(){
        binding.showBbsInformationUseSafeClientSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bbsInfo.useSafeClient = isChecked;
                new updateForumInformationTask(bbsInfo,getApplicationContext()).execute();
            }
        });
        binding.showBbsInformationSyncSwitch.setChecked(bbsInfo.isSync);
        binding.showBbsInformationSyncSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            BBSInformationDatabase
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
                this.finishAfterTransition();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRecyclerViewSwiped(int position) {
        Log.d(TAG,"Recyclerview swiped "+position);
        List<forumUserBriefInfo> userBriefInfos = userAdapter.getUserList();
        if(userBriefInfos!=null){
            forumUserBriefInfo userBriefInfo = userBriefInfos.get(position);
            userAdapter.getUserList().remove(position);
            userAdapter.notifyDataSetChanged();
            showUndoSnackbar(userBriefInfo,position);

        }

    }

    public class removeBBSUserTask extends AsyncTask<Void, Void, Void> {
        private forumUserBriefInfo userBriefInfo;
        private Context context;
        public removeBBSUserTask(forumUserBriefInfo userBriefInfo, Context context){
            this.userBriefInfo = userBriefInfo;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumUserBriefInfoDatabase.getInstance(context).getforumUserBriefInfoDao().delete(userBriefInfo);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public void showUndoSnackbar(final forumUserBriefInfo userBriefInfo, final int position) {
        Log.d(TAG,"SHOW REMOVED POS "+position);
        new removeBBSUserTask(userBriefInfo,this).execute();
        View view = findViewById(R.id.show_bbs_information_coordinatorLayout);
        Snackbar snackbar = Snackbar.make(view, getString(R.string.bbs_delete_user_info_template,userBriefInfo.username,bbsInfo.site_name),
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.bbs_undo_delete, new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                undoDelete(userBriefInfo,position);
            }
        });
        snackbar.show();
    }

    public void undoDelete(forumUserBriefInfo userBriefInfo, int position) {
        // insert to database
        userAdapter.getUserList().add(position,userBriefInfo);
        userAdapter.notifyDataSetChanged();
        new addBBSUserTask(userBriefInfo, this).execute();

    }

    public class addBBSUserTask extends AsyncTask<Void, Void, Void> {
        private forumUserBriefInfo userBriefInfo;
        private Context context;
        public addBBSUserTask(forumUserBriefInfo userBriefInfo, Context context){
            this.userBriefInfo = userBriefInfo;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumUserBriefInfoDatabase.getInstance(context).getforumUserBriefInfoDao().insert(userBriefInfo);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

}
