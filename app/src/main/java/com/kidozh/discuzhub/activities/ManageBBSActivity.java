package com.kidozh.discuzhub.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.ManageBBSAdapter;
import com.kidozh.discuzhub.callback.RecyclerViewItemTouchCallback;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.databinding.ActivityManageBbsBinding;
import com.kidozh.discuzhub.dialogs.ManageAdapterHelpDialogFragment;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.viewModels.ManageBBSViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;


public class ManageBBSActivity extends BaseStatusActivity
        implements RecyclerViewItemTouchCallback.onInteraction{
    final static String TAG = ManageBBSActivity.class.getSimpleName();


    ManageBBSViewModel viewModel;
    // bbsInformation bbsInfo;

    ManageBBSAdapter adapter;
    
    ActivityManageBbsBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageBbsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ManageBBSViewModel.class);
        configureActionBar();
        configureRecyclerView();
        bindViewModel();
        showHelpDialog();
    }


    void configureActionBar(){
        setSupportActionBar(binding.toolbar);
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.manage_bbs_title);
        }

    }

    void configureRecyclerView(){
        binding.recyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(this));
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageBBSAdapter();
        viewModel.getPagedListLiveData().observe(this, adapter::submitList);
        binding.recyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(this,adapter));
        // swipe to delete
        // swipe to delete support
        RecyclerViewItemTouchCallback callback = new RecyclerViewItemTouchCallback(this);

        //forumSwipeToDeleteUserCallback swipeToDeleteUserCallback = new forumSwipeToDeleteUserCallback(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerview);
        //binding.recyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    void bindViewModel(){

        viewModel.getPagedListLiveData().observe(this, new Observer<PagedList<bbsInformation>>() {
            @Override
            public void onChanged(PagedList<bbsInformation> bbsInformations) {
                Log.d(TAG,"Recv list length "+bbsInformations.size()+" adapter "+adapter.getItemCount());
                if(bbsInformations.size() == 0){
                    binding.emptyUserView.setVisibility(View.VISIBLE);
                }
                else {
                    binding.emptyUserView.setVisibility(View.GONE);
                }
            }
        });
    }






    @Override
    public void onRecyclerViewSwiped(int position, int direction) {
        // delete bbs
        List<bbsInformation> bbsInformations= viewModel.getPagedListLiveData().getValue();
        if(bbsInformations!=null && bbsInformations.size()>position){
            showUndoSnackbar(bbsInformations.get(position),position);
        }

    }

    @Override
    public void onRecyclerViewMoved(int fromPosition, int toPosition) {
        List<bbsInformation> bbsInformations= viewModel.getPagedListLiveData().getValue();
        if(bbsInformations !=null){
            bbsInformations = new ArrayList<>(bbsInformations);
            // swap the data directly
            Log.d(TAG,"list "+bbsInformations+" from "+fromPosition+" to "+toPosition);
            Collections.swap(bbsInformations,fromPosition, toPosition);
            for(int i=0;i<bbsInformations.size();i++){
                bbsInformations.get(i).position = i;
            }

            new UpdateBBSTask(bbsInformations).execute();
        }


    }

    public class removeBBSTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation bbsInfo;
        public removeBBSTask(bbsInformation bbsInfo){
            this.bbsInfo = bbsInfo;

        }
        @Override
        protected Void doInBackground(Void... voids) {
            BBSInformationDatabase.getInstance(getApplication()).getForumInformationDao().delete(bbsInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public class AddBBSTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation bbsInfo;
        public AddBBSTask(bbsInformation bbsInfo){
            this.bbsInfo = bbsInfo;

        }
        @Override
        protected Void doInBackground(Void... voids) {
            BBSInformationDatabase.getInstance(getApplication()).getForumInformationDao().insert(bbsInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public class UpdateBBSTask extends AsyncTask<Void, Void, Void> {
        private List<bbsInformation> bbsInfos;
        public UpdateBBSTask(List<bbsInformation> bbsInfos){
            this.bbsInfos = bbsInfos;

        }
        @Override
        protected Void doInBackground(Void... voids) {
            BBSInformationDatabase.getInstance(getApplication()).getForumInformationDao().update(bbsInfos);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public void showUndoSnackbar(final bbsInformation bbsInfo, final int position) {
        Log.d(TAG,"SHOW REMOVED POS "+position);
        new removeBBSTask(bbsInfo).execute();
        Snackbar snackbar = Snackbar.make(binding.manageBbsCoordinatorLayout, getString(R.string.delete_bbs_template,bbsInfo.site_name),
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.bbs_undo_delete, new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                undoDelete(bbsInfo,position);
            }
        });
        snackbar.show();
    }

    public void undoDelete(bbsInformation bbsInfo, int position) {
        // insert to database
        new AddBBSTask(bbsInfo).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_info, menu);
        return true;
    }

    public void showHelpDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        ManageAdapterHelpDialogFragment dialogFragment = new ManageAdapterHelpDialogFragment();
        dialogFragment.show(fragmentManager,ManageAdapterHelpDialogFragment.class.getSimpleName());
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finishAfterTransition();
            return true;
        }
        else if(id == R.id.show_help_info){
            showHelpDialog();
            return true;
        }
        else if(id == R.id.add_item){
            Intent intent = new Intent(this, AddIntroActivity.class);
            startActivity(intent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }


}