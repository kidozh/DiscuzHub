package com.kidozh.discuzhub.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;


import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.ViewHistoryAdapter;
import com.kidozh.discuzhub.callback.RecyclerViewItemTouchCallback;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.databinding.ActivityViewHistoryBinding;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.entities.ViewHistory;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.viewModels.ViewHistoryViewModel;

import es.dmoral.toasty.Toasty;

public class ViewHistoryActivity extends BaseStatusActivity implements RecyclerViewItemTouchCallback.onInteraction{
    private static final String TAG = ViewHistoryActivity.class.getSimpleName();

    ViewHistoryViewModel viewModel;
    ViewHistoryAdapter adapter;

    ActivityViewHistoryBinding binding;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewHistoryBinding.inflate(getLayoutInflater());
        
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ViewHistoryViewModel.class);
        configureIntentData();
        configureActionBar();


        configureRecyclerview();
        bindViewModel();
        configureSearchView();
    }

    private void configureIntentData(){
        Intent intent = getIntent();
        bbsInfo = (Discuz) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);
        user = (User) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        if(bbsInfo !=null){
            URLUtils.setBBS(bbsInfo);
            Log.d(TAG,"Recv bbs info "+bbsInfo);
            viewModel.setBBSInfo(bbsInfo);
        }


    }

    private void configureActionBar(){
        setSupportActionBar(binding.toolbar);
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configureRecyclerview(){
        binding.viewHistoryRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.viewHistoryRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(this));
        binding.viewHistoryRecyclerview.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));
        adapter = new ViewHistoryAdapter();
        adapter.setInfo(bbsInfo, user);
        viewModel.getPagedListLiveData().observe(this,adapter::submitList);

        binding.viewHistoryRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(this,adapter));
        // swipe and sort
        RecyclerViewItemTouchCallback callback = new RecyclerViewItemTouchCallback(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.viewHistoryRecyclerview);


    }
    Context context = this;
    private void configureSearchView(){
        binding.viewHistorySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    viewModel.setSearchText(bbsInfo,newText);
                    viewModel.getPagedListLiveData().observe((LifecycleOwner) context,adapter::submitList);
                }
                else {
                    viewModel.setBBSInfo(bbsInfo);
                    viewModel.getPagedListLiveData().observe((LifecycleOwner) context,adapter::submitList);
                }
                return false;
            }
        });
    }

    private void bindViewModel(){
        Context context = this;
        viewModel.getPagedListLiveData().observe(this, new Observer<PagedList<ViewHistory>>() {
            @Override
            public void onChanged(PagedList<ViewHistory> viewHistories) {
                if(viewHistories.size() == 0){
                    binding.infoView.setVisibility(View.VISIBLE);
                    // judge the setting
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean recordHistory = prefs.getBoolean(getString(R.string.preference_key_record_history),false);
                    if(recordHistory){
                        binding.infoContent.setText(R.string.view_history_not_found);
                    }
                    else {
                        binding.infoContent.setText(R.string.view_history_record_not_open);
                    }
                }
                else {
                    binding.infoView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bbs_draft_nav_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }





    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finishAfterTransition();
            return true;
        }
        else if(id == R.id.bbs_draft_nav_menu_swipe_delte){
            showDeleteAllDraftDialog();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteAllDraftDialog(){

        if(adapter.getItemCount() == 0){
            Toasty.info(this,getString(R.string.view_history_not_found), Toast.LENGTH_SHORT).show();
        }
        else {
            AlertDialog alertDialogs = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_all_view_history))
                    .setMessage(getString(R.string.delete_all_view_history_description))
                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new deleteAllViewHistory().execute();
                        }
                    })

                    .create();
            alertDialogs.show();

        }
    }

    @Override
    public void onRecyclerViewSwiped(int position, int direction) {
        PagedList<ViewHistory> viewHistories = viewModel.pagedListLiveData.getValue();
        if(viewHistories != null){
            ViewHistory viewHistory = viewHistories.get(position);
            new RemoveViewHistoryTask(viewHistory).execute();
        }

    }

    @Override
    public void onRecyclerViewMoved(int fromPosition, int toPosition) {

    }

    private class deleteAllViewHistory extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if(bbsInfo == null){
                ViewHistoryDatabase.getInstance(context).getDao().deleteAllViewHistory();
            }
            else {
                ViewHistoryDatabase.getInstance(context).getDao().deleteViewHistoryByBBSId(bbsInfo.getId());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            VibrateUtils.vibrateForNotice(context);
            Toasty.success(context,context.getString(R.string.have_deleted_all_view_history),Toast.LENGTH_LONG).show();

        }
    }

    public class RemoveViewHistoryTask extends AsyncTask<Void, Void, Void> {
        private ViewHistory viewHistory;
        public RemoveViewHistoryTask(ViewHistory viewHistory){
            this.viewHistory = viewHistory;

        }
        @Override
        protected Void doInBackground(Void... voids) {
            ViewHistoryDatabase.getInstance(context).getDao().delete(viewHistory);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Snackbar snackbar = Snackbar.make(binding.viewHistoryCoordinatorLayout,
                    getString(R.string.delete_view_history_item,viewHistory.name),
                    Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.bbs_undo_delete, new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    new InsertViewHistoryTask(viewHistory).execute();
                }
            });
            snackbar.show();
            super.onPostExecute(aVoid);

        }
    }

    public class InsertViewHistoryTask extends AsyncTask<Void, Void, Void> {
        private ViewHistory viewHistory;
        public InsertViewHistoryTask(ViewHistory viewHistory){
            this.viewHistory = viewHistory;

        }
        @Override
        protected Void doInBackground(Void... voids) {
            ViewHistoryDatabase.getInstance(context).getDao().insert(viewHistory);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }
}