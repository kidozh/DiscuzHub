package com.kidozh.discuzhub;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kidozh.discuzhub.activities.SettingsActivity;
import com.kidozh.discuzhub.activities.SplashScreenActivity;
import com.kidozh.discuzhub.activities.aboutAppActivity;
import com.kidozh.discuzhub.activities.bbsAddIntroActivity;
import com.kidozh.discuzhub.adapter.forumInformationAdapter;
import com.kidozh.discuzhub.callback.forumSwipeToDeleteCallback;
import com.kidozh.discuzhub.database.forumInformationDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.services.updateBBSInformationWork;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;
import com.kidozh.discuzhub.viewModels.LocalBBSViewModel;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.forum_information_recyclerview)
    RecyclerView mForumInfoRecyclerview;
    @BindView(R.id.forum_information_emptyview)
    View emptyView;
    @BindView(R.id.forum_information_swipe_refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private forumInformationAdapter adapter;
    static int updatedBBSNum = 0, needUpdatedBBSNum = 0;
    LocalBBSViewModel localBBSViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        localBBSViewModel = new ViewModelProvider(this).get(LocalBBSViewModel.class);

        // render recyclerview
        configureRecyclerview();
        bindForumData();
        configureFab();
        configureSwipeRefreshLayout();
        checkTermOfUse();


    }

    private void checkTermOfUse(){
        Intent intent = new Intent(this, SplashScreenActivity.class);
        startActivity(intent);
    }



    private void configureSwipeRefreshLayout(){
        Context context = this;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                List<bbsInformation> bbsInformationList = localBBSViewModel.getBBSInformation().getValue();
                if(localBBSViewModel.getBBSInformation().getValue() == null || bbsInformationList == null){
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    // updating ...
                    needUpdatedBBSNum = bbsInformationList.size();
                    // open notification
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationUtils.updateProgressNotificationId);
                    builder.setContentTitle(getString(R.string.bbs_updating_information))
                            .setContentText(getString(R.string.bbs_updating_bbs_information_description))
                            .setSmallIcon(R.drawable.vector_drawable_update_24px)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            // Set the intent that will fire when the user taps the notification
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);
                    // Issue the initial notification with zero progress
                    int PROGRESS_MAX = needUpdatedBBSNum;
                    int PROGRESS_CURRENT = 0;
                    int notificationId = -15900000;
                    builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                    notificationManager.notify(notificationId, builder.build());

                    for(int i=0;i<bbsInformationList.size();i++){
                        bbsInformation bbsInfo = bbsInformationList.get(i);
                        OkHttpClient client = networkUtils.getPreferredClient(getApplicationContext());
                        bbsURLUtils.setBaseUrl(bbsInfo.base_url);
                        String query_url = bbsURLUtils.getBBSForumInformationUrl();
                        Request request = new Request.Builder().url(query_url).build();
                        Call call = client.newCall(request);
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                // not enroll it
                                updatedBBSNum +=1;
                                builder.setProgress(PROGRESS_MAX, updatedBBSNum, false);
                                notificationManager.notify(notificationId, builder.build());
                                if(updatedBBSNum >=needUpdatedBBSNum){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefreshLayout.setRefreshing(false);
                                            builder.setContentText(getString(R.string.bbs_updating_complete))
                                                    .setProgress(0,0,false);
                                            notificationManager.notify(notificationId, builder.build());
                                        }
                                    });
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(getApplicationContext(),
                                                String.format(getString(R.string.failed_to_update_bbs_template),bbsInfo.site_name),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String s = response.body().string();
                                bbsInformation bbsInformation = bbsParseUtils.parseInformationByJson(bbsInfo.base_url,s);
                                updatedBBSNum += 1;
                                builder.setProgress(PROGRESS_MAX, updatedBBSNum, false);
                                notificationManager.notify(notificationId, builder.build());
                                Log.d(TAG,"Updated bbs number "+updatedBBSNum+" total "+needUpdatedBBSNum);
                                if(updatedBBSNum >=needUpdatedBBSNum){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefreshLayout.setRefreshing(false);
                                            // finished
                                            builder.setContentText(getString(R.string.bbs_updating_complete))
                                                    .setProgress(0,0,false);
                                            notificationManager.notify(notificationId, builder.build());
                                        }
                                    });
                                }

                                if(bbsInformation!=null){
                                    bbsInformation.setId(bbsInfo.getId());
                                    // update it
                                    Log.d(TAG,"Updating "+bbsInformation.site_name);
                                    new updateForumInformationTask(bbsInformation,getBaseContext()).execute();
                                }
                                else {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.error(getApplicationContext(),
                                                    String.format(getString(R.string.failed_to_update_bbs_template),bbsInfo.site_name),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }


                            }
                        });
                    }
                    if(needUpdatedBBSNum != 0){
                        Toasty.info(getApplicationContext(),
                                getApplicationContext().getString(R.string.data_is_processing),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                // refresh users
                updateUserInfo();
            }
        });
    }
    private void updateUserInfo(){
        Log.d(TAG,"Register work");
        // update frequency
        // extracting all user...
        LiveData<List<forumUserBriefInfo>> allUsersLiveData = forumUserBriefInfoDatabase
                .getInstance(this)
                .getforumUserBriefInfoDao()
                .getAllUserLiveData();
        Context context = this;
        allUsersLiveData.observe(this, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> allUsers) {

                Log.d(TAG,"ALL USER "+allUsers.size());
                for(int i=0; i<allUsers.size();i++){

                    forumUserBriefInfo userBriefInfo = allUsers.get(i);

                    Data userData = new Data.Builder()
                            .putInt(bbsConstUtils.WORK_MANAGER_PASS_USER_ID_KEY, userBriefInfo.getId())
                            .build();
                    // start periodic work
                    Log.d(TAG,"Register notification "+userBriefInfo.username);

                    OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(updateBBSInformationWork.class)
                            .setInputData(userData)
                            .build();
                    WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
                }
            }
        });


    }


    public static class addNewForumInformationTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation forumInfo;
        private Context context;
        public addNewForumInformationTask(bbsInformation bbsInformation, Context context){
            this.forumInfo = bbsInformation;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumInformationDatabase
                    .getInstance(context)
                    .getForumInformationDao().insert(forumInfo);
            Log.d(TAG, "add forum into database"+forumInfo.site_name);
            return null;
        }

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

    public static class removeNewForumInformationTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation forumInfo;
        private Context context;
        public removeNewForumInformationTask(bbsInformation bbsInformation, Context context){
            this.forumInfo = bbsInformation;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            forumInformationDatabase
                    .getInstance(context)
                    .getForumInformationDao().delete(forumInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG,"Remove forum "+this.forumInfo.site_name);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void configureFab(){
        FloatingActionButton fab = findViewById(R.id.forum_add_new_fab);
        final Context context = this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open a dialog
                Intent intent = new Intent(context, bbsAddIntroActivity.class);
                startActivity(intent);
            }
        });
    }



    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mForumInfoRecyclerview.setLayoutManager(linearLayoutManager);
        adapter = new forumInformationAdapter(this,this);
        mForumInfoRecyclerview.setAdapter(adapter);
        if(adapter.getBbsInformationList()==null || adapter.getBbsInformationList().size()==0){
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void bindForumData(){
        localBBSViewModel.getBBSInformation().observe(this, new Observer<List<bbsInformation>>() {
            @Override
            public void onChanged(List<bbsInformation> bbsInformations) {
                Log.d(TAG,"bbs information changed! " +bbsInformations+" "+ bbsInformations.size());
                adapter.setBbsInformationList(bbsInformations);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new forumSwipeToDeleteCallback(adapter));
                itemTouchHelper.attachToRecyclerView(mForumInfoRecyclerview);
                if(bbsInformations ==null || bbsInformations.size()==0){
                    emptyView.setVisibility(View.VISIBLE);
                }
                else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_user_status:{
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.bbs_about_app:{
                Intent intent = new Intent(this, aboutAppActivity.class);
                startActivity(intent);
                return true;
            }
            case android.R.id.home:{
                finishAfterTransition();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
