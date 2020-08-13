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
import com.kidozh.discuzhub.daos.FavoriteItemDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteItem;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.FavoriteThreadResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FavoriteItemViewModel extends AndroidViewModel {

    private static final String TAG = FavoriteItemViewModel.class.getSimpleName();
    public MutableLiveData<Integer> networkState = new MutableLiveData<>(bbsConstUtils.NETWORK_STATUS_SUCCESSFULLY);
    public MutableLiveData<String> errorMsgKey = new MutableLiveData<>(""),
            errorMsgContent = new MutableLiveData<>("");
    private LiveData<PagedList<FavoriteItem>> favoriteThreadListData;
    @NonNull
    public MutableLiveData<Integer> totalCount = new MutableLiveData<>(-1);
    @NonNull
    public MutableLiveData<List<FavoriteItem>> favoriteThreadInServer = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<FavoriteItem>> newFavoriteThread = new MutableLiveData<>(new ArrayList<>());
    private OkHttpClient client = new OkHttpClient();
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    String idType;

    FavoriteItemDao dao;

    PagedList.Config myPagingConfig = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build();

    public FavoriteItemViewModel(@NonNull Application application) {
        super(application);
        dao = FavoriteThreadDatabase.getInstance(application).getDao();
    }

    public void setInfo( @NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, String idType) {

        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        this.idType = idType;
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
        favoriteThreadListData = new LivePagedListBuilder<>(dao.getFavoriteItemPageListByBBSId(bbsInfo.getId(),userBriefInfo.getUid(),idType),myPagingConfig).build();
    }

    public LiveData<PagedList<FavoriteItem>> getFavoriteThreadListData() {
        return favoriteThreadListData;
    }

    public void startSyncFavoriteThread(){
        getFavoriteThread(1);
    }



    private void getFavoriteThread(int page){
        networkState.postValue(bbsConstUtils.NETWORK_STATUS_LOADING);
        Retrofit retrofit = networkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService apiService = retrofit.create(DiscuzApiService.class);
        Call<FavoriteThreadResult> favoriteCall = apiService.getFavoriteThreadResult(page);
        Log.d(TAG,"Get favorite result "+favoriteCall.request().url());
        favoriteCall.enqueue(new Callback<FavoriteThreadResult>() {
            @Override
            public void onResponse(Call<FavoriteThreadResult> call, Response<FavoriteThreadResult> response) {
                if(response.isSuccessful() && response.body()!=null){


                    FavoriteThreadResult result = response.body();
                    if(result.isError()){
                        networkState.postValue(bbsConstUtils.NETWORK_STATUS_FAILED);
                        errorMsgKey.postValue(result.message.key);
                        errorMsgContent.postValue(getApplication().getString(R.string.discuz_error,result.message.key,result.message.content));
                    }
                    else {
                        totalCount.postValue(result.favoriteThreadVariable.count);
                        Log.d(TAG,"Get cnt "+result.favoriteThreadVariable.count + " "+result.favoriteThreadVariable.favoriteItemList);
                        newFavoriteThread.postValue(result.favoriteThreadVariable.favoriteItemList);
                        List<FavoriteItem> curFavoriteItemList =
                                favoriteThreadInServer.getValue() == null ? new ArrayList<>()
                                : favoriteThreadInServer.getValue();
                        curFavoriteItemList.addAll(result.favoriteThreadVariable.favoriteItemList);
                        favoriteThreadInServer.postValue(curFavoriteItemList);

                        // recursive
                        if(result.favoriteThreadVariable.count > curFavoriteItemList.size()){
                            getFavoriteThread(page+1);
                        }
                    }
                }
                else {
                    Log.d(TAG,"Get favorite response failed"+response.body());
                    networkState.postValue(bbsConstUtils.NETWORK_STATUS_FAILED);
                    errorMsgContent.postValue(getApplication().getString(R.string.network_failed));
                }
            }

            @Override
            public void onFailure(Call<FavoriteThreadResult> call, Throwable t) {
                networkState.postValue(bbsConstUtils.NETWORK_STATUS_FAILED);
                errorMsgContent.postValue(getApplication().getString(R.string.network_failed));
                t.printStackTrace();
            }
        });
    }
}
