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
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HotForumsViewModel extends AndroidViewModel {
    private final static String TAG = HotForumsViewModel.class.getSimpleName();

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();
    public MutableLiveData<Boolean> isLoadingMutableLiveData = new MutableLiveData<>(false),
            isErrorMutableLiveData = new MutableLiveData<>(false);
    public MutableLiveData<String> errorString = new MutableLiveData<>("");

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
        Request request = new Request.Builder()
                .url(URLUtils.getHotForumURL())
                .build();
        Log.d(TAG,"Send request to "+request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isErrorMutableLiveData.postValue(true);
                isLoadingMutableLiveData.postValue(false);
                errorString.postValue(getApplication().getString(R.string.network_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv hot forum "+s);
                    HotForumsResult result = bbsParseUtils.getHotForumsResult(s);
                    hotForumsResultMutableLiveData.postValue(result);
                    if(result == null){
                        isErrorMutableLiveData.postValue(true);
                        errorString.postValue(getApplication().getString(R.string.parse_failed));
                    }
                    else {
                        if(result.message!=null){
                            isErrorMutableLiveData.postValue(true);
                            errorString.postValue(result.message.content);
                        }
                        else {
                            if(result.variables.hotForumList == null){
                                isErrorMutableLiveData.postValue(true);
                                errorString.postValue(getApplication().getString(R.string.parse_hot_forum_list_null));
                            }
                            else {
                                isErrorMutableLiveData.postValue(false);
                            }
                        }
                    }

                }
                else {
                    errorString.postValue(getApplication().getString(R.string.network_failed));
                }
                isLoadingMutableLiveData.postValue(false);
            }
        });
    }
}
