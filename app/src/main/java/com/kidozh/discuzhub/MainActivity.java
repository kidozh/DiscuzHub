package com.kidozh.discuzhub;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kidozh.discuzhub.activities.SettingsActivity;
import com.kidozh.discuzhub.adapter.forumInformationAdapter;
import com.kidozh.discuzhub.callback.forumSwipeToDeleteCallback;
import com.kidozh.discuzhub.database.forumInformationDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private LiveData<List<bbsInformation>> forumInformationListLiveData;
    private forumInformationAdapter adapter;
    static int updatedBBSNum = 0, needUpdatedBBSNum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // render recyclerview
        configureRecyclerview();
        bindForumData();
        configureFab();
        configureSwipeRefreshLayout();
        configureDarkMode();


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

        }
        return;
    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                List<bbsInformation> bbsInformationList = forumInformationListLiveData.getValue();
                if(forumInformationListLiveData.getValue() == null || bbsInformationList == null){
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    // updating ...
                    needUpdatedBBSNum = bbsInformationList.size();
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
                                if(updatedBBSNum >=needUpdatedBBSNum){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefreshLayout.setRefreshing(false);
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
                                Log.d(TAG,"Updated bbs number "+updatedBBSNum+" total "+needUpdatedBBSNum);
                                if(updatedBBSNum >=needUpdatedBBSNum){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.success(getApplicationContext(),
                                                String.format(getString(R.string.updated_bbs_template),bbsInfo.site_name),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

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
            }
        });
    }

    private Dialog createAddNewForumDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_add_new_forum, null);
        EditText urlEditText = view.findViewById(R.id.dialog_new_forum_url_editText);
        Spinner urlPrefixSpinner = view.findViewById(R.id.dialog_new_forum_url_spinner);
        CheckBox useSafeClient = view.findViewById(R.id.dialog_new_forum_safe_client_checkBox);


        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputUrl = s.toString();
                if(inputUrl.startsWith("http://") || inputUrl.startsWith("HTTP://")){
                    urlPrefixSpinner.setSelection(0);
                    urlEditText.setText(inputUrl.replace("http://","")
                            .replace("HTTP://",""));
                    Toasty.info(getApplicationContext(),getApplicationContext().getString(R.string.protocol_http_setting),Toast.LENGTH_SHORT).show();

                }
                else if(inputUrl.startsWith("https://") || inputUrl.startsWith("HTTPS://")){
                    urlPrefixSpinner.setSelection(1);
                    urlEditText.setText(inputUrl.replace("https://","")
                            .replace("HTTPS://",""));
                    Toasty.info(getApplicationContext(),getApplicationContext().getString(R.string.protocol_https_setting),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setView(view)
                // Add action buttons
                .setTitle(R.string.title_add_a_forum_by_url)
                .setIcon(R.drawable.vector_drawable_bbs)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // add the forum
                        String base_url = urlPrefixSpinner.getSelectedItem().toString() + urlEditText.getText();
                        queryForumInfo(base_url,useSafeClient.isChecked());
                        //new queryForumInfoTask(base_url,useSafeClient.isChecked()).execute();

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private Dialog showProgressDialog(Call call){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_data_processing, null);
        builder.setView(view)
                // Add action buttons
                .setTitle(R.string.data_is_processing)
                .setIcon(R.drawable.vector_drawable_info_circle)
                .setPositiveButton(R.string.data_put_in_background, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // add the forum
                        dialog.cancel();

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        call.cancel();
                        Toasty.info(getApplicationContext(),
                                getApplicationContext().getString(R.string.task_is_cancelled),
                                Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private void queryForumInfo(String base_url, Boolean useSafeClient){
        bbsURLUtils.setBaseUrl(base_url);
        String query_url = bbsURLUtils.getBBSForumInformationUrl();
        OkHttpClient client = networkUtils.getPreferredClient(useSafeClient);
        Request request = new Request.Builder().url(query_url).build();
        Call call = client.newCall(request);
        Handler mHandler = new Handler(Looper.getMainLooper());
        // Call a progressbar dialog
        Dialog dialog = showProgressDialog(call);
        dialog.show();
        Log.d(TAG,"Query URL "+query_url);
        call.enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(getApplicationContext(),
                                getApplicationContext().getString(R.string.network_failed),
                                Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s;
                if(response!=null && response.body()!=null){
                    try{
                        s = response.body().string();

                    }
                    catch (Exception e){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.cancel();
                                Toasty.error(getApplicationContext(),
                                        getApplicationContext().getString(R.string.parse_failed),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        e.printStackTrace();
                        return;
                    }
                    Log.d(TAG,"check response " +s);
                    bbsInformation bbsInformation = bbsParseUtils.parseInformationByJson(base_url,s);
                    Log.d(TAG,"Use safe client "+useSafeClient+" final URL"+response.request().url().toString() + useSafeClient + response.isSuccessful());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.cancel();

                            if(bbsInformation !=null){
                                bbsInformation.setUseSafeClient(useSafeClient);
                                if(response!=null&& useSafeClient && response.isSuccessful()){
                                    String finalUrl = response.request().url().toString();
                                    if(finalUrl.startsWith("https://")){
                                        // automatically upgrade to HTTPS
                                        Log.d(TAG,"Upgrade to HTTPS protocol");
                                        bbsInformation.base_url = bbsInformation.base_url.replace("http://","https://");
                                        Toasty.success(getApplicationContext(),
                                                getApplicationContext().getString(R.string.bbs_upgrade_to_https),
                                                Toast.LENGTH_SHORT).show();
                                        bbsInformation.setUseSafeClient(true);
                                    }
                                }
                                new addNewForumInformationTask(bbsInformation,getApplicationContext()).execute();
                                Toasty.success(getApplicationContext(),
                                        getApplicationContext().getString(R.string.add_a_forum_successfully),
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toasty.error(getApplicationContext(),
                                        getApplicationContext().getString(R.string.parse_failed),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

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
        configureRecyclerview();
        bindForumData();
    }

    private void configureFab(){
        FloatingActionButton fab = findViewById(R.id.forum_add_new_fab);
        final Context context = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open a dialog
                Dialog dialogFragment = createAddNewForumDialog();
                dialogFragment.show();
            }
        });
    }



    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mForumInfoRecyclerview.setLayoutManager(linearLayoutManager);
        adapter = new forumInformationAdapter(this,this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mForumInfoRecyclerview.getContext(),
                linearLayoutManager.getOrientation());
        mForumInfoRecyclerview.addItemDecoration(dividerItemDecoration);
        mForumInfoRecyclerview.setAdapter(adapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new forumSwipeToDeleteCallback(adapter));
//        itemTouchHelper.attachToRecyclerView(mForumInfoRecyclerview);
        if(adapter.getBbsInformationList()==null || adapter.getBbsInformationList().size()==0){
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void bindForumData(){
        forumInformationListLiveData = forumInformationDatabase
                .getInstance(this)
                .getForumInformationDao()
                .getAllForumInformations();
        adapter.setBbsInformationList(forumInformationListLiveData.getValue());
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new forumSwipeToDeleteCallback(adapter));
//        itemTouchHelper.attachToRecyclerView(mForumInfoRecyclerview);
        forumInformationListLiveData.observe(this, new Observer<List<bbsInformation>>() {
            @Override
            public void onChanged(List<bbsInformation> bbsInformations) {
                adapter.setBbsInformationList(bbsInformations);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new forumSwipeToDeleteCallback(adapter));
                itemTouchHelper.attachToRecyclerView(mForumInfoRecyclerview);
                if(adapter.getBbsInformationList()==null || adapter.getBbsInformationList().size()==0){
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_user_status) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
