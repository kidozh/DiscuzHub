package com.kidozh.discuzhub.activities.ui.FavoriteThread;

import androidx.lifecycle.ViewModelProvider;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.FavoriteItemAdapter;
import com.kidozh.discuzhub.daos.FavoriteItemDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteItem;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class FavoriteItemFragment extends Fragment {
    private static final String TAG = FavoriteItemFragment.class.getSimpleName();

    private FavoriteItemViewModel mViewModel;

    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";
    private static final String ARG_IDTYPE = "ARG_IDTYPE";

    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;
    private String idType;

    public FavoriteItemFragment(){

    }

    public static FavoriteItemFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, String idType) {
        FavoriteItemFragment fragment = new FavoriteItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BBS, bbsInfo);
        args.putSerializable(ARG_USER,userBriefInfo);
        args.putString(ARG_IDTYPE,idType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (forumUserBriefInfo) getArguments().getSerializable(ARG_USER);
            idType = (String) getArguments().getString(ARG_IDTYPE,"tid");
        }
    }

    FavoriteItemAdapter adapter;

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
        mViewModel = new ViewModelProvider(this).get(FavoriteItemViewModel.class);
        mViewModel.setInfo(bbsInfo,userBriefInfo,idType);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
        syncFavoriteThreadFromServer();
    }

    private void configureRecyclerview(){
        favoriteThreadRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteItemAdapter();
        adapter.setInformation(bbsInfo,userBriefInfo);
        mViewModel.getFavoriteThreadListData().observe(getViewLifecycleOwner(),adapter::submitList);
        favoriteThreadRecyclerview.setAdapter(adapter);
        favoriteThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncFavoriteThreadProgressBar.setVisibility(View.GONE);
                Toasty.info(getContext(),getString(R.string.sync_favorite_thread_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();
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
        mViewModel.errorMsgContent.observe(getViewLifecycleOwner(),error->{
            if(!TextUtils.isEmpty(error)){
                Toasty.error(getContext(),error,Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void syncFavoriteThreadFromServer(){
        if(getContext() !=null && UserPreferenceUtils.isSyncBBSInformation(getContext())){
            int page = 1;
            // sync information
            // Toasty.info(getContext(),getString(R.string.sync_favorite_thread_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();

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
                    //Toasty.success(getContext(),getString(R.string.sync_favorite_thread_load_all),Toast.LENGTH_LONG).show();
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

        private List<FavoriteItem> favoriteItemList;

        public SaveFavoriteThreadAsyncTask(List<FavoriteItem> favoriteItemList) {
            this.favoriteItemList = favoriteItemList;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            FavoriteItemDao dao = FavoriteThreadDatabase.getInstance(getContext()).getDao();
            // query first
            List<Integer> insertTids = new ArrayList<>();
            for(int i = 0; i< favoriteItemList.size(); i++){
                insertTids.add(favoriteItemList.get(i).idKey);
                favoriteItemList.get(i).belongedBBSId = bbsInfo.getId();
                favoriteItemList.get(i).userId = userBriefInfo.getUid();
                //Log.d(TAG,"fav id "+favoriteThreadList.get(i).favid);
            }



            List<FavoriteItem> queryList = dao.queryFavoriteItemListByTids(bbsInfo.getId()
                    ,userBriefInfo.getUid(),
                    insertTids
                    ,idType
                    );

            for(int i=0;i<queryList.size();i++){
                int tid = queryList.get(i).idKey;
                FavoriteItem queryThread = queryList.get(i);
                for(int j = 0; j< favoriteItemList.size(); j++){
                    FavoriteItem favoriteItem = favoriteItemList.get(j);
                    if(favoriteItem.idKey == tid){
                        favoriteItem.id = queryThread.id;
                        break;
                    }
                }
            }
            // remove all synced information
            dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo.getUid(),idType);

            dao.insert(favoriteItemList);
            return favoriteItemList.size();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }


}