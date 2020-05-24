package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserProfileResult;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserProfileViewModel extends AndroidViewModel {

    private static final String TAG = UserProfileViewModel.class.getSimpleName();
    private MutableLiveData<UserProfileResult> userProfileResultMutableLiveData;
    private MutableLiveData<Boolean> isLoading, isError;

    private bbsInformation curBBS;
    private forumUserBriefInfo curUser;
    private int uid;
    private OkHttpClient client;

    public UserProfileViewModel(@NonNull Application application) {
        super(application);
        isLoading = new MutableLiveData<>(false);
        isError = new MutableLiveData<>(false);
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, int uid){
        this.curBBS = bbsInfo;
        this.curUser = userBriefInfo;
        this.uid = uid;
        URLUtils.setBBS(bbsInfo);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }

    public MutableLiveData<UserProfileResult> getUserProfileResultLiveData(){
        if(userProfileResultMutableLiveData == null){
            userProfileResultMutableLiveData = new MutableLiveData<>();
            loadUserProfile();
        }
        return userProfileResultMutableLiveData;
    }

    public void loadUserProfile(){
        isLoading.postValue(true);
        Request request = new Request.Builder()
                .url(URLUtils.getUserProfileUrl(uid))
                .build();
        Log.d(TAG,"Send user profile "+request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading.postValue(false);
                isError.postValue(true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body()!=null){
                    isError.postValue(false);
                    String s = response.body().string();
                    UserProfileResult userProfileResult = bbsParseUtils.parseUserProfileResult(s);
                    userProfileResultMutableLiveData.postValue(userProfileResult);

                }
                else {
                    isError.postValue(true);
                }

                isLoading.postValue(false);
            }
        });
    }
}
