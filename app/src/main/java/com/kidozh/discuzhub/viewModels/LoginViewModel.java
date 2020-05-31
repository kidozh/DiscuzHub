package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginViewModel extends AndroidViewModel {
    private final static String TAG = LoginViewModel.class.getSimpleName();
    private MutableLiveData<SecureInfoResult> secureInfoResultMutableLiveData;
    public MutableLiveData<String> errorString;
    public MutableLiveData<Boolean> error;

    private bbsInformation bbsInfo;
    private OkHttpClient client;
    private ForumInfo forum;
    private int tid;
    private forumUserBriefInfo userBriefInfo;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        error = new MutableLiveData<>(false);
        errorString = new MutableLiveData<>("");
    }

    public MutableLiveData<SecureInfoResult> getSecureInfoResultMutableLiveData(){
        if(secureInfoResultMutableLiveData == null){
            // load the secure info result
            secureInfoResultMutableLiveData = new MutableLiveData<>(null);
            // load the information
            getSecureInfo();
        }
        return secureInfoResultMutableLiveData;
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        URLUtils.setBBS(bbsInfo);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
        // bbsPersonInfoMutableLiveData.postValue(userBriefInfo);
    }

    public void getSecureInfo(){
        String url = URLUtils.getSecureParameterURL("login");
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d(TAG,"Send secure code to "+url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                secureInfoResultMutableLiveData.postValue(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s= response.body().string();
                    Log.d(TAG,"Recv secure code "+s);
                    secureInfoResultMutableLiveData.postValue(bbsParseUtils.parseSecureInfoResult(s));
                }
                else {
                    secureInfoResultMutableLiveData.postValue(null);
                }
            }
        });
    }
}
