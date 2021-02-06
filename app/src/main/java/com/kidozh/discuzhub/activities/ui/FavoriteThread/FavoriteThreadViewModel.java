package com.kidozh.discuzhub.activities.ui.FavoriteThread;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.results.FavoriteThreadResult;
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

public class FavoriteThreadViewModel extends AndroidViewModel {

    private static final String TAG = FavoriteThreadViewModel.class.getSimpleName();
    public MutableLiveData<Integer> networkState = new MutableLiveData<>(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
    public MutableLiveData<String> errorMsgKey = new MutableLiveData<>(""),
            errorMsgContent = new MutableLiveData<>("");
    private LiveData<PagedList<FavoriteThread>> favoriteThreadListData;
    @NonNull
    public MutableLiveData<Integer> totalCount = new MutableLiveData<>(-1);
    @NonNull
    public MutableLiveData<List<FavoriteThread>> favoriteThreadInServer = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<FavoriteThread>> newFavoriteThread = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<FavoriteThreadResult> resultMutableLiveData = new MutableLiveData<>();
    private OkHttpClient client;
    Discuz bbsInfo;
    User userBriefInfo;
    String idType;

    FavoriteThreadDao dao;

    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build();

    public FavoriteThreadViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteThreadDatabase.getInstance(application).getDao();
    }

    public void setInfo(@NonNull Discuz bbsInfo, User userBriefInfo, String idType) {

        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        this.idType = idType;
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
        favoriteThreadListData = new LivePagedListBuilder<>(dao.getFavoriteItemPageListByBBSId(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0,idType),myPagingConfig).build();
    }

    public LiveData<PagedList<FavoriteThread>> getFavoriteItemListData() {
        return favoriteThreadListData;
    }

    public void startSyncFavoriteThread(){
        getFavoriteItem(1);
    }



    private void getFavoriteItem(int page){

        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING);
        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService apiService = retrofit.create(DiscuzApiService.class);
        Call<FavoriteThreadResult> favoriteCall;
        favoriteCall = apiService.getFavoriteThreadResult(page);


        Log.d(TAG,"Get favorite result "+favoriteCall.request().url());
        favoriteCall.enqueue(new Callback<FavoriteThreadResult>() {
            @Override
            public void onResponse(Call<FavoriteThreadResult> call, Response<FavoriteThreadResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    FavoriteThreadResult result = response.body();
                    resultMutableLiveData.postValue(result);
                    Log.d(TAG,"Get response result "+result.isError()+response.raw().toString());
                    if(result.isError()){
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                        errorMsgKey.postValue(result.getErrorMessage().key);
                        errorMsgContent.postValue(result.getErrorMessage().content);
                    }
                    else if(result.favoriteThreadVariable !=null) {
                        totalCount.postValue(result.favoriteThreadVariable.count);
                        //Log.d(TAG,"Get cnt "+result.favoriteThreadVariable.count + " "+result.favoriteThreadVariable.favoriteThreadList);
                        newFavoriteThread.postValue(result.favoriteThreadVariable.favoriteThreadList);
                        List<FavoriteThread> curFavoriteThreadList =
                                favoriteThreadInServer.getValue() == null ? new ArrayList<>()
                                : favoriteThreadInServer.getValue();
                        curFavoriteThreadList.addAll(result.favoriteThreadVariable.favoriteThreadList);
                        favoriteThreadInServer.postValue(curFavoriteThreadList);

                        // recursive
                        if(result.favoriteThreadVariable.count > curFavoriteThreadList.size()){
                            getFavoriteItem(page+1);
                        }
                    }
                }
                else {
                    Log.d(TAG,"Get favorite response failed"+response.body());
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                    errorMsgContent.postValue(getApplication().getString(R.string.network_failed));
                }
            }

            @Override
            public void onFailure(Call<FavoriteThreadResult> call, Throwable t) {
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                errorMsgContent.postValue(getApplication().getString(R.string.network_failed));
                t.printStackTrace();
            }
        });
    }
}
