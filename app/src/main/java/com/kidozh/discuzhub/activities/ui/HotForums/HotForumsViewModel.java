package com.kidozh.discuzhub.activities.ui.HotForums;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.HotForumsResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;


import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class HotForumsViewModel extends AndroidViewModel {
    private final static String TAG = HotForumsViewModel.class.getSimpleName();

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    public MutableLiveData<Boolean> isLoadingMutableLiveData = new MutableLiveData<>(false),
            isErrorMutableLiveData = new MutableLiveData<>(false);
    public MutableLiveData<String> errorString = new MutableLiveData<>("");
    public MutableLiveData<String> errorValueString = new MutableLiveData<>("");

    private MutableLiveData<HotForumsResult> hotForumsResultMutableLiveData;
    public HotForumsViewModel(@NonNull Application application) {
        super(application);
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        URLUtils.setBBS(bbsInfo);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }

    public MutableLiveData<HotForumsResult> getHotForumsResult() {
        if(hotForumsResultMutableLiveData == null){
            hotForumsResultMutableLiveData = new MutableLiveData<>(null);
            loadHotForums();
        }
        return hotForumsResultMutableLiveData;
    }

    public void loadHotForums(){
        isLoadingMutableLiveData.postValue(true);
        Retrofit retrofit = networkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<HotForumsResult> hotForumsResultCall = service.hotForumResult();

        hotForumsResultCall.enqueue(new retrofit2.Callback<HotForumsResult>() {
            @Override
            public void onResponse(Call<HotForumsResult> call, retrofit2.Response<HotForumsResult> response) {
                isLoadingMutableLiveData.postValue(false);
                if(response.isSuccessful() && response.body() !=null){
                    HotForumsResult result = response.body();
                    hotForumsResultMutableLiveData.postValue(result);
                    if(result.message!=null){
                        isErrorMutableLiveData.postValue(true);
                        errorValueString.postValue(result.message.key);
                        errorString.postValue(result.message.content);
                    }
                    else {
                        if(result.variables.hotForumList == null){
                            isErrorMutableLiveData.postValue(true);
                            errorValueString.postValue(getApplication().getString(R.string.empty_result));
                            errorString.postValue(getApplication().getString(R.string.parse_hot_forum_list_null));
                        }
                        else {
                            isErrorMutableLiveData.postValue(false);
                        }
                    }
                }
                else {
                    isErrorMutableLiveData.postValue(true);
                    errorValueString.postValue(String.valueOf(response.code()));
                    errorString.postValue(getApplication().getString(R.string.discuz_network_result_null,response.message()));
                }
            }

            @Override
            public void onFailure(Call<HotForumsResult> call, Throwable t) {
                isErrorMutableLiveData.postValue(true);
                isLoadingMutableLiveData.postValue(false);
                errorValueString.postValue(t.getLocalizedMessage());
                errorString.postValue(getApplication().
                        getString(R.string.discuz_network_failure_template,t.toString()));
            }
        });
    }
}
