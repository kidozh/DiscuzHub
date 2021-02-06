package com.kidozh.discuzhub.activities.ui.FavoriteForum;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.FavoriteForumAdapter;
import com.kidozh.discuzhub.daos.FavoriteForumDao;
import com.kidozh.discuzhub.database.FavoriteForumDatabase;
import com.kidozh.discuzhub.databinding.FragmentFavoriteThreadBinding;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class FavoriteForumFragment extends Fragment {
    private static final String TAG = FavoriteForumFragment.class.getSimpleName();

    private FavoriteForumViewModel mViewModel;

    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";
    private static final String ARG_IDTYPE = "ARG_IDTYPE";

    private Discuz bbsInfo;
    private User userBriefInfo;

    public FavoriteForumFragment(){

    }

    public static FavoriteForumFragment newInstance(Discuz bbsInfo, User userBriefInfo) {
        FavoriteForumFragment fragment = new FavoriteForumFragment();
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
            bbsInfo = (Discuz) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    FavoriteForumAdapter adapter;
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

        mViewModel = new ViewModelProvider(this).get(FavoriteForumViewModel.class);
        mViewModel.setInfo(bbsInfo,userBriefInfo);
        configureRecyclerview();
        bindViewModel();
        configureSwipeRefreshLayout();
        syncFavoriteThreadFromServer();
    }

    private void configureRecyclerview(){
        binding.favoriteThreadRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
        binding.favoriteThreadRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteForumAdapter();
        adapter.setInformation(bbsInfo,userBriefInfo);
        mViewModel.getFavoriteItemListData().observe(getViewLifecycleOwner(),adapter::submitList);
        binding.favoriteThreadRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(getContext(),adapter));
        binding.favoriteThreadRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
    }

    private void configureSwipeRefreshLayout(){
        if(userBriefInfo!=null){
            binding.favoriteThreadSwipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    binding.favoriteThreadSyncProgressbar.setVisibility(View.GONE);
                    Toasty.info(getContext(),getString(R.string.sync_favorite_forum_start,bbsInfo.site_name), Toast.LENGTH_SHORT).show();
                    mViewModel.startSyncFavoriteForum();
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
                binding.blankFavoriteThreadNotice.setText(R.string.favorite_forum_not_found);
            }
            else {
                binding.blankFavoriteThreadView.setVisibility(View.GONE);
            }
        });

        mViewModel.errorMessageMutableLiveData.observe(getViewLifecycleOwner(),errorMessage -> {
            if(errorMessage!=null){
                Toasty.error(getContext(),getString(R.string.discuz_api_message_template,
                        errorMessage.key,errorMessage.content),Toast.LENGTH_SHORT).show();
            }
        });
        mViewModel.resultMutableLiveData.observe(getViewLifecycleOwner(),favoriteForumResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(favoriteForumResult,favoriteForumResult!=null?favoriteForumResult.favoriteForumVariable:null);
            }
        });
    }

    public void syncFavoriteThreadFromServer(){
        if(getContext() !=null && UserPreferenceUtils.syncFavorite(getContext()) && userBriefInfo!=null){

            bindSyncStatus();

            mViewModel.startSyncFavoriteForum();

        }

    }

    private void bindSyncStatus(){
        mViewModel.totalCount.observe(getViewLifecycleOwner(), count ->{
            if(count == -1){
                binding.favoriteThreadSyncProgressbar.setVisibility(View.VISIBLE);
                binding.favoriteThreadSyncProgressbar.setIndeterminate(true);
            }
            else {
                binding.favoriteThreadSyncProgressbar.setVisibility(View.GONE);
            }

        });

        mViewModel.FavoriteForumInServer.observe(getViewLifecycleOwner(),favoriteForums -> {
            if(mViewModel !=null){
                int count = mViewModel.totalCount.getValue();
                if(count == -1){

                }
                else if(count > favoriteForums.size()){
                    binding.favoriteThreadSyncProgressbar.setVisibility(View.VISIBLE);
                    binding.favoriteThreadSyncProgressbar.setMax(count);

                    binding.favoriteThreadSyncProgressbar.setProgress(favoriteForums.size());

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

        mViewModel.newFavoriteForum.observe(getViewLifecycleOwner(),newFavoriteForums ->{
            new SaveFavoriteItemAsyncTask(newFavoriteForums).execute();
        });
    }

    private class SaveFavoriteItemAsyncTask extends AsyncTask<Void,Void,Integer>{

        private List<FavoriteForum> favoriteForumList;

        public SaveFavoriteItemAsyncTask(List<FavoriteForum> favoriteForumList) {
            this.favoriteForumList = favoriteForumList;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            FavoriteForumDao dao = FavoriteForumDatabase.getInstance(getContext()).getDao();
            // query first
            List<Integer> insertTids = new ArrayList<>();
            for(int i = 0; i< favoriteForumList.size(); i++){
                insertTids.add(favoriteForumList.get(i).idKey);
                favoriteForumList.get(i).belongedBBSId = bbsInfo.getId();
                favoriteForumList.get(i).userId = userBriefInfo!=null?userBriefInfo.getUid():0;
                //Log.d(TAG,"fav id "+favoriteForumList.get(i).favid);
            }



            List<FavoriteForum> queryList = dao.queryFavoriteItemListByfids(bbsInfo.getId()
                    ,userBriefInfo!=null?userBriefInfo.getUid():0,
                    insertTids
                    );

            for(int i=0;i<queryList.size();i++){
                int tid = queryList.get(i).idKey;
                FavoriteForum queryForum = queryList.get(i);
                for(int j = 0; j< favoriteForumList.size(); j++){
                    FavoriteForum favoriteForum = favoriteForumList.get(j);
                    if(favoriteForum.idKey == tid){
                        favoriteForum.id = queryForum.id;
                        break;
                    }
                }
            }
            // remove all synced information

            //dao.clearSyncedFavoriteItemByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0);

            dao.insert(favoriteForumList);
            return favoriteForumList.size();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }


}