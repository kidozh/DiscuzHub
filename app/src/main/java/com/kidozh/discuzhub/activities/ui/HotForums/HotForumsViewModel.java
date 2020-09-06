package com.kidozh.discuzhub.activities.ui.HotForums;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.HotForumsResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;

public class HotForumsViewModel extends AndroidViewModel {
    private final static String TAG = HotForumsViewModel.class.getSimpleName();

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    public MutableLiveData<Boolean> isLoadingMutableLiveData = new MutableLiveData<>(false);
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);


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
            public void onResponse(@NotNull Call<HotForumsResult> call, @NotNull retrofit2.Response<HotForumsResult> response) {

                if(response.isSuccessful() && response.body() !=null){
                    HotForumsResult result = response.body();
                    hotForumsResultMutableLiveData.postValue(result);
                    if(result.message!=null){
                        errorMessageMutableLiveData.postValue(result.message.toErrorMessage());

                    }
                    else {
                        if(result.variables.hotForumList == null){
                            errorMessageMutableLiveData.postValue(new ErrorMessage(getApplication().getString(R.string.empty_result),
                                    getApplication().getString(R.string.parse_hot_forum_list_null)));

                        }
                        else {
                            errorMessageMutableLiveData.postValue(null);
                        }
                    }
                }
                else {
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));

                }
                isLoadingMutableLiveData.postValue(false);
            }

            @Override
            public void onFailure(Call<HotForumsResult> call, Throwable t) {
                Log.d(TAG,"Network fail "+t.getLocalizedMessage());
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                isLoadingMutableLiveData.postValue(false);
            }
        });
    }
}
