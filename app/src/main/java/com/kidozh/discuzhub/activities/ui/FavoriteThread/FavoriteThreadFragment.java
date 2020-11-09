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
import com.kidozh.discuzhub.adapter.FavoriteThreadAdapter;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.databinding.FragmentFavoriteThreadBinding;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class FavoriteThreadFragment extends Fragment {
    private static final String TAG = FavoriteThreadFragment.class.getSimpleName();

    private FavoriteThreadViewModel mViewModel;

    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";
    private static final String ARG_IDTYPE = "ARG_IDTYPE";

    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;
    private String idType;

    public FavoriteThreadFragment(){

    }

    public static FavoriteThreadFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, String idType) {
        FavoriteThreadFragment fragment = new FavoriteThreadFragment();
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

    FavoriteThreadAdapter adapter;
    FragmentFavoriteThreadBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteThreadBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FavoriteThreadViewModel.class);
        mViewModel.setInfo(bbsInfo,userBriefInfo,idType);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
        syncFavoriteThreadFromServer();
    }

    private void configureRecyclerview(){
        binding.favoriteThreadRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
        binding.favoriteThreadRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteThreadAdapter();
        adapter.setInformation(bbsInfo,userBriefInfo);
        mViewModel.getFavoriteItemListData().observe(getViewLifecycleOwner(),adapter::submitList);
        binding.favoriteThreadRecyclerview.setAdapter(adapter);
        binding.favoriteThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
    }

    private void configureSwipeRefreshLayout(){
        if(userBriefInfo !=null){
            binding.favoriteThreadSwipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    binding.favoriteThreadSyncProgressbar.setVisibility(View.GONE);
                    Toasty.info(getContext(),getString(R.string.sync_favorite_thread_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();
                    mViewModel.startSyncFavoriteThread();
                    binding.favoriteThreadSwipelayout.setRefreshing(false);
                }
            });
        }
        else {
            binding.favoriteThreadSwipelayout.setEnabled(false);
        }

    }

    private void bindViewModel(){


        mViewModel.getFavoriteItemListData().observe(getViewLifecycleOwner(),favoriteThreads -> {
            if(favoriteThreads.size() == 0){
                binding.blankFavoriteThreadView.setVisibility(View.VISIBLE);
            }
            else {
                binding.blankFavoriteThreadView.setVisibility(View.GONE);
            }
        });
        mViewModel.errorMsgContent.observe(getViewLifecycleOwner(),error->{
            if(!TextUtils.isEmpty(error)){
                Toasty.error(getContext(),error,Toast.LENGTH_SHORT).show();
            }

        });
        mViewModel.resultMutableLiveData.observe(getViewLifecycleOwner(), favoriteThreadResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(favoriteThreadResult,
                        favoriteThreadResult!=null?favoriteThreadResult.favoriteThreadVariable:null);
            }
        });
    }

    public void syncFavoriteThreadFromServer(){
        if(getContext() !=null && UserPreferenceUtils.syncFavorite(getContext()) && userBriefInfo!=null){
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
                binding.favoriteThreadSyncProgressbar.setVisibility(View.VISIBLE);
                binding.favoriteThreadSyncProgressbar.setIndeterminate(true);
            }

        });

        mViewModel.favoriteThreadInServer.observe(getViewLifecycleOwner(),favoriteThreads -> {
            if(mViewModel !=null){
                int count = mViewModel.totalCount.getValue();
                if(count == -1){

                }
                else if(count > favoriteThreads.size()){
                    binding.favoriteThreadSyncProgressbar.setVisibility(View.VISIBLE);
                    binding.favoriteThreadSyncProgressbar.setMax(count);

                    binding.favoriteThreadSyncProgressbar.setProgress(favoriteThreads.size());

                }
                else {
                    binding.favoriteThreadSyncProgressbar.setVisibility(View.GONE);
                    //Toasty.success(getContext(),getString(R.string.sync_favorite_thread_load_all),Toast.LENGTH_LONG).show();
                }
            }
            else {
                binding.favoriteThreadSyncProgressbar.setVisibility(View.GONE);
            }

        });

        mViewModel.newFavoriteThread.observe(getViewLifecycleOwner(),newFavoriteThreads ->{
            new SaveFavoriteItemAsyncTask(newFavoriteThreads).execute();
        });
    }

    private class SaveFavoriteItemAsyncTask extends AsyncTask<Void,Void,Integer>{

        private List<FavoriteThread> favoriteThreadList;

        public SaveFavoriteItemAsyncTask(List<FavoriteThread> favoriteThreadList) {
            this.favoriteThreadList = favoriteThreadList;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            FavoriteThreadDao dao = FavoriteThreadDatabase.getInstance(getContext()).getDao();
            // query first
            List<Integer> insertTids = new ArrayList<>();
            for(int i = 0; i< favoriteThreadList.size(); i++){
                insertTids.add(favoriteThreadList.get(i).idKey);
                favoriteThreadList.get(i).belongedBBSId = bbsInfo.getId();
                favoriteThreadList.get(i).userId = userBriefInfo!=null?userBriefInfo.getUid():0;
                //Log.d(TAG,"fav id "+favoriteThreadList.get(i).favid);
            }



            List<FavoriteThread> queryList = dao.queryFavoriteItemListByTids(bbsInfo.getId()
                    ,userBriefInfo!=null?userBriefInfo.getUid():0,
                    insertTids
                    ,idType
                    );

            for(int i=0;i<queryList.size();i++){
                int tid = queryList.get(i).idKey;
                FavoriteThread queryThread = queryList.get(i);
                for(int j = 0; j< favoriteThreadList.size(); j++){
                    FavoriteThread favoriteThread = favoriteThreadList.get(j);
                    if(favoriteThread.idKey == tid){
                        favoriteThread.id = queryThread.id;
                        break;
                    }
                }
            }
            // remove all synced information
            //dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0,idType);

            dao.insert(favoriteThreadList);
            return favoriteThreadList.size();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }


}