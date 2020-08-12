package com.kidozh.discuzhub.activities.ui.FavoriteThread;

import androidx.lifecycle.ViewModelProvider;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.FavoriteThreadAdapter;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class FavoriteThreadFragment extends Fragment {

    private FavoriteThreadViewModel mViewModel;

    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";

    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;

    public FavoriteThreadFragment(){

    }

    public static FavoriteThreadFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        FavoriteThreadFragment fragment = new FavoriteThreadFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BBS, bbsInfo);
        args.putSerializable(ARG_USER,userBriefInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (forumUserBriefInfo) getArguments().getSerializable(ARG_USER);
        }
    }

    FavoriteThreadAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorite_thread_fragment, container, false);
    }

    @BindView(R.id.blank_favorite_thread_view)
    View blankFavoriteThreadView;
    @BindView(R.id.favorite_thread_recyclerview)
    RecyclerView favoriteThreadRecyclerview;
    @BindView(R.id.favorite_thread_swipelayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.favorite_thread_sync_progressbar)
    ProgressBar syncFavoriteThreadProgressBar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mViewModel = new ViewModelProvider(this).get(FavoriteThreadViewModel.class);
        mViewModel.setInfo(bbsInfo,userBriefInfo);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
        syncFavoriteThreadFromServer();
    }

    private void configureRecyclerview(){
        favoriteThreadRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteThreadAdapter();
        adapter.setInformation(bbsInfo,userBriefInfo);
        mViewModel.getFavoriteThreadListData().observe(getViewLifecycleOwner(),adapter::submitList);
        favoriteThreadRecyclerview.setAdapter(adapter);
    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mViewModel.startSyncFavoriteThread();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void bindViewModel(){


        mViewModel.getFavoriteThreadListData().observe(getViewLifecycleOwner(),favoriteThreads -> {
            if(favoriteThreads.size() == 0){
                blankFavoriteThreadView.setVisibility(View.VISIBLE);
            }
            else {
                blankFavoriteThreadView.setVisibility(View.GONE);
            }
        });
    }

    public void syncFavoriteThreadFromServer(){
        if(getContext() !=null && UserPreferenceUtils.isSyncBBSInformation(getContext())){
            int page = 1;
            // sync information
            Toasty.info(getContext(),getString(R.string.sync_favorite_thread_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();

            // loop to fetch favorite thread from server
            bindSyncStatus();

            mViewModel.startSyncFavoriteThread();

        }

    }

    private void bindSyncStatus(){
        mViewModel.totalCount.observe(getViewLifecycleOwner(), count ->{
            if(count == -1){
                syncFavoriteThreadProgressBar.setVisibility(View.VISIBLE);
                syncFavoriteThreadProgressBar.setIndeterminate(true);
            }

        });

        mViewModel.favoriteThreadInServer.observe(getViewLifecycleOwner(),favoriteThreads -> {
            if(mViewModel !=null){
                int count = mViewModel.totalCount.getValue();
                if(count == -1){

                }
                else if(count > favoriteThreads.size()){
                    syncFavoriteThreadProgressBar.setVisibility(View.VISIBLE);
                    syncFavoriteThreadProgressBar.setMax(count);

                    syncFavoriteThreadProgressBar.setProgress(favoriteThreads.size());

                }
                else {
                    syncFavoriteThreadProgressBar.setVisibility(View.GONE);
                    Toasty.success(getContext(),getString(R.string.sync_favorite_thread_load_all),Toast.LENGTH_LONG).show();
                }
            }
            else {
                syncFavoriteThreadProgressBar.setVisibility(View.GONE);
            }

        });

        mViewModel.newFavoriteThread.observe(getViewLifecycleOwner(),newFavoriteThreads ->{
            new SaveFavoriteThreadAsyncTask(newFavoriteThreads).execute();
        });
    }

    private class SaveFavoriteThreadAsyncTask extends AsyncTask<Void,Void,Integer>{

        private List<FavoriteThread> favoriteThreadList;

        public SaveFavoriteThreadAsyncTask(List<FavoriteThread> favoriteThreadList) {
            this.favoriteThreadList = favoriteThreadList;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            FavoriteThreadDao dao = FavoriteThreadDatabase.getInstance(getContext()).getDao();
            // query first
            List<Integer> insertTids = new ArrayList<>();
            for(int i=0;i<favoriteThreadList.size();i++){
                insertTids.add(favoriteThreadList.get(i).idKey);
            }

            List<FavoriteThread> queryList = dao.queyFavoriteThreadListByTids(bbsInfo.getId()
                    ,userBriefInfo.getUid(),
                    insertTids
                    );

            for(int i=0;i<queryList.size();i++){
                int tid = queryList.get(i).idKey;
                FavoriteThread queryThread = queryList.get(i);
                for(int j=0;j<favoriteThreadList.size();j++){
                    FavoriteThread favoriteThread = favoriteThreadList.get(j);
                    if(favoriteThread.idKey == tid){
                        favoriteThread.id = queryThread.id;
                        break;
                    }
                }
            }

            dao.insert(favoriteThreadList);
            return favoriteThreadList.size();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }


}