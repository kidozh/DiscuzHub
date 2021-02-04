package com.kidozh.discuzhub.activities.ui.FavoriteForum;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.daos.FavoriteForumDao;
import com.kidozh.discuzhub.database.FavoriteForumDatabase;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.FavoriteForumResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FavoriteForumViewModel extends AndroidViewModel {

    private static final String TAG = FavoriteForumViewModel.class.getSimpleName();
    public MutableLiveData<Integer> networkState = new MutableLiveData<>(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);
    private LiveData<PagedList<FavoriteForum>> FavoriteForumListData;
    @NonNull
    public MutableLiveData<Integer> totalCount = new MutableLiveData<>(-1);
    @NonNull
    public MutableLiveData<List<FavoriteForum>> FavoriteForumInServer = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<FavoriteForum>> newFavoriteForum = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<FavoriteForumResult> resultMutableLiveData = new MutableLiveData<>();
    private OkHttpClient client;
    Discuz bbsInfo;
    forumUserBriefInfo userBriefInfo;
    String idType;

    FavoriteForumDao dao;

    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build();

    public FavoriteForumViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteForumDatabase.getInstance(application).getDao();
    }

    public void setInfo(@NonNull Discuz bbsInfo, forumUserBriefInfo userBriefInfo) {

        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
        FavoriteForumListData = new LivePagedListBuilder<>(dao.getFavoriteItemPageListByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0),myPagingConfig).build();
    }

    public LiveData<PagedList<FavoriteForum>> getFavoriteItemListData() {
        return FavoriteForumListData;
    }

    public void startSyncFavoriteForum(){
        getFavoriteItem(1);
    }



    private void getFavoriteItem(int page){
        if(!NetworkUtils.isOnline(getApplication())){
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()));
            return;
        }
        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING);
        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService apiService = retrofit.create(DiscuzApiService.class);
        Call<FavoriteForumResult> favoriteCall;
        favoriteCall = apiService.getFavoriteForumResult(page);


        Log.d(TAG,"Get favorite result "+favoriteCall.request().url());
        favoriteCall.enqueue(new Callback<FavoriteForumResult>() {
            @Override
            public void onResponse(Call<FavoriteForumResult> call, Response<FavoriteForumResult> response) {
                if(response.isSuccessful() && response.body()!=null){

                    FavoriteForumResult result = response.body();
                    resultMutableLiveData.postValue(result);
                    if(result.isError()){
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                        errorMessageMutableLiveData.postValue(new ErrorMessage(result.getErrorMessage().key,result.getErrorMessage().content));
                    }
                    else if(result.favoriteForumVariable !=null) {
                        totalCount.postValue(result.favoriteForumVariable.count);
                        Log.d(TAG,"Get cnt "+result.favoriteForumVariable.count + " "+result.favoriteForumVariable.FavoriteForumList);
                        newFavoriteForum.postValue(result.favoriteForumVariable.FavoriteForumList);
                        List<FavoriteForum> curFavoriteForumList =
                                FavoriteForumInServer.getValue() == null ? new ArrayList<>()
                                : FavoriteForumInServer.getValue();
                        curFavoriteForumList.addAll(result.favoriteForumVariable.FavoriteForumList);
                        FavoriteForumInServer.postValue(curFavoriteForumList);

                        // recursive
                        if(result.favoriteForumVariable.count > curFavoriteForumList.size()){
                            getFavoriteItem(page+1);
                        }
                    }
                }
                else {
                    Log.d(TAG,"Get favorite response failed"+response.body());
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));
                }
            }

            @Override
            public void onFailure(Call<FavoriteForumResult> call, Throwable t) {
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                t.printStackTrace();
            }
        });
    }
}
