package com.kidozh.discuzhub.DataSource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.results.FavoriteThreadResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FavoriteThreadDataSource extends PageKeyedDataSource<Integer, FavoriteThread> {
    final static String TAG = FavoriteThreadDataSource.class.getSimpleName();

    Context context;
    Discuz bbsInfo;
    User userBriefInfo;
    public MutableLiveData<Integer> networkStateLiveData = new MutableLiveData<>(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
    public MutableLiveData<String> errorStatusStringLiveData = new MutableLiveData<>("");


    OkHttpClient client;

    public FavoriteThreadDataSource(@NonNull Context context, @NonNull Discuz bbsInfo, User userBriefInfo) {
        this.context = context;
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(context,userBriefInfo);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, FavoriteThread> callback) {
        networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_LOADING);
        String url = URLUtils.getFavoriteThreadListURL(1,params.requestedLoadSize);
        Log.d(TAG,"get params "+params.requestedLoadSize+" url : "+url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        URLUtils.setBBS(bbsInfo);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                errorStatusStringLiveData.postValue(context.getString(R.string.network_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    FavoriteThreadResult result = bbsParseUtils.getFavoriteThreadResult(s);
                    if(result !=null && result.favoriteThreadVariable !=null){
                        networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
                        callback.onResult(result.favoriteThreadVariable.favoriteThreadList,1,2);
                    }
                    else {
                        networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                        errorStatusStringLiveData.postValue(context.getString(R.string.parse_failed));
                    }

                }
                else {
                    networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                    errorStatusStringLiveData.postValue(context.getString(R.string.network_failed));
                }
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, FavoriteThread> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, FavoriteThread> callback) {
        networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_LOADING);
        String url = URLUtils.getFavoriteThreadListURL(params.key,params.requestedLoadSize);
        Log.d(TAG,"get params "+params.requestedLoadSize+" url : "+url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        URLUtils.setBBS(bbsInfo);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                errorStatusStringLiveData.postValue(context.getString(R.string.network_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    FavoriteThreadResult result = bbsParseUtils.getFavoriteThreadResult(s);
                    if(result !=null && result.favoriteThreadVariable !=null){
                        networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
                        callback.onResult(result.favoriteThreadVariable.favoriteThreadList,params.key+1);
                    }
                    else {
                        networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                        errorStatusStringLiveData.postValue(context.getString(R.string.parse_failed));
                    }

                }
                else {
                    networkStateLiveData.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                    errorStatusStringLiveData.postValue(context.getString(R.string.network_failed));
                }
            }
        });
    }
}
