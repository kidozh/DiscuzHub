package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.ThreadDraftAdapter;
import com.kidozh.discuzhub.callback.recyclerViewSwipeToDeleteCallback;
import com.kidozh.discuzhub.database.bbsThreadDraftDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class bbsShowThreadDraftActivity extends BaseStatusActivity implements recyclerViewSwipeToDeleteCallback.onRecyclerviewSwiped{
    private final String TAG = bbsShowThreadDraftActivity.class.getSimpleName();

    @BindView(R.id.bbs_show_thread_draft_recyclerview)
    RecyclerView threadDraftRecyclerview;
    @BindView(R.id.bbs_show_thread_draft_no_item_found)
    ConstraintLayout threadDraftNoItemFoundView;

    ThreadDraftAdapter threadDraftAdapter;
    LiveData<List<bbsThreadDraft>> listLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_thread_draft);
        ButterKnife.bind(this);
        configureIntentData();
        configureActionBar();
        configureRecyclerview();


    }

    private void configureIntentData(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
    }

    private void configureActionBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.bbs_draft_box));
        getSupportActionBar().setSubtitle(bbsInfo.site_name);
    }

    private void configureRecyclerview(){
        threadDraftRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        threadDraftAdapter = new ThreadDraftAdapter(bbsInfo,userBriefInfo);
        threadDraftRecyclerview.setAdapter(threadDraftAdapter);
        listLiveData = bbsThreadDraftDatabase.getInstance(this)
                .getbbsThreadDraftDao()
                .getAllThreadDraftByBBSId(bbsInfo.getId());
        listLiveData.observe(this, new Observer<List<bbsThreadDraft>>() {
            @Override
            public void onChanged(List<bbsThreadDraft> bbsThreadDrafts) {
                if(bbsThreadDrafts!=null && bbsThreadDrafts.size()!=0){
                    threadDraftAdapter.setBbsThreadDraftList(bbsThreadDrafts);
                    threadDraftNoItemFoundView.setVisibility(View.GONE);
                }
                else {
                    threadDraftNoItemFoundView.setVisibility(View.VISIBLE);
                    threadDraftAdapter.setBbsThreadDraftList(bbsThreadDrafts);
                    threadDraftAdapter.notifyDataSetChanged();
                }

            }
        });
        // swipe to delete support
        recyclerViewSwipeToDeleteCallback swipeToDeleteUserCallback = new recyclerViewSwipeToDeleteCallback(this,threadDraftAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteUserCallback);
        itemTouchHelper.attachToRecyclerView(threadDraftRecyclerview);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finishAfterTransition();
                return false;
            case R.id.bbs_draft_nav_menu_sort:{
                return false;
            }
            case R.id.bbs_draft_nav_menu_swipe_delte:{
                // transaction all
                showDeleteAllDraftDialog();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bbs_draft_nav_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showDeleteAllDraftDialog(){
        if(threadDraftAdapter.getBbsThreadDraftList() == null || threadDraftAdapter.getBbsThreadDraftList().size() == 0){
            Toasty.info(this,getString(R.string.bbs_thread_draft_empty),Toast.LENGTH_SHORT).show();
        }
        else {
            AlertDialog alertDialogs = new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.bbs_delete_all_draft))
                    .setIcon(getDrawable(R.drawable.vector_drawable_warning_24px))
                    .setMessage(getString(R.string.bbs_delete_all_drafts_alert,bbsInfo.site_name))
                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new deleteAllThreadDraftTask(getApplicationContext()).execute();
                        }
                    })

                    .create();
            alertDialogs.show();

        }
    }

    @Override
    public void onSwiped(int position, int direction) {
        Log.d(TAG,"On swiped "+position + direction);
        List<bbsThreadDraft> bbsThreadDraftList = threadDraftAdapter.getBbsThreadDraftList();
        bbsThreadDraft deleteThreadDraft = bbsThreadDraftList.get(position);
        new deleteThreadDraftTask(this,deleteThreadDraft).execute();
    }

    public class addThreadDraftTask extends AsyncTask<Void, Void, Void> {
        private bbsThreadDraft insertThreadDraft;
        private Context context;
        private Boolean saveThenFinish = false;
        public addThreadDraftTask(Context context,bbsThreadDraft threadDraft ){
            this.insertThreadDraft = threadDraft;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            long inserted = bbsThreadDraftDatabase
                    .getInstance(context)
                    .getbbsThreadDraftDao().insert(insertThreadDraft);
            insertThreadDraft.setId( (int) inserted);
            Log.d(TAG, "add forum into database"+insertThreadDraft.subject+insertThreadDraft.getId());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class deleteAllThreadDraftTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        public deleteAllThreadDraftTask(Context context){
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            bbsThreadDraftDatabase
                    .getInstance(context)
                    .getbbsThreadDraftDao().deleteAllForumInformation(bbsInfo.getId());

            Log.d(TAG, "delete all forum from database");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class deleteThreadDraftTask extends AsyncTask<Void, Void, Void> {
        private bbsThreadDraft threadDraft;
        private Context context;
        public deleteThreadDraftTask(Context context,bbsThreadDraft threadDraft ){
            this.threadDraft = threadDraft;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            bbsThreadDraftDatabase
                    .getInstance(context)
                    .getbbsThreadDraftDao().delete(threadDraft);
            Log.d(TAG, "delete forum into database"+threadDraft.subject+threadDraft.getId());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // show undo dialog
            showUndoSnackbar(threadDraft);
        }
    }

    public void showUndoSnackbar(final bbsThreadDraft threadDraft) {
        View view = findViewById(R.id.bbs_show_thread_draft_coordinatorlayout);
        Snackbar snackbar = Snackbar.make(view, getString(R.string.bbs_delete_draft,threadDraft.subject,bbsInfo.site_name),
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.bbs_undo_delete, new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                undoDeleteDraft(threadDraft);
            }
        });
        snackbar.show();
    }

    private void undoDeleteDraft(final bbsThreadDraft threadDraft){
        new addThreadDraftTask(this,threadDraft).execute();

    }


}
