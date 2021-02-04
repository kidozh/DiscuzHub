package com.kidozh.discuzhub.activities.ui.UserNotification;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserNotificationViewModel extends AndroidViewModel {
    private static final String TAG = UserNotificationViewModel.class.getSimpleName();

    public MutableLiveData<UserNoteListResult> userNoteListResultMutableLiveData;
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false),
            hasLoadedAll = new MutableLiveData<>(false),
            isError= new MutableLiveData<>(false);

    private Discuz curBBS;
    private forumUserBriefInfo curUser;
    private OkHttpClient client;

    public UserNotificationViewModel(@NonNull Application application) {
        super(application);
        userNoteListResultMutableLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        hasLoadedAll = new MutableLiveData<>(false);
        isError = new MutableLiveData<>(false);
    }

    public void setBBSInfo(Discuz curBBS, forumUserBriefInfo curUser){
        this.curBBS = curBBS;
        this.curUser = curUser;
        this.client = NetworkUtils.getPreferredClientWithCookieJarByUser(this.getApplication(),curUser);
    }

    public void getUserNotificationByPage(String view, String type,int page){


        if(isLoading != null && isLoading.getValue() != null && isLoading.getValue()){
            return;
        }
        isLoading.setValue(true);
        String url = URLUtils.getNoteListApiUrl(view,type,page);
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d(TAG,"get notification url "+url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isError.postValue(true);
                isLoading.postValue(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body() !=null){
                    String s = response.body().string();
                    Log.d(TAG, "recv note "+s);
                    UserNoteListResult userNoteListResult = bbsParseUtils.getUserNoteListResult(s);
                    // set it
                    userNoteListResultMutableLiveData.postValue(userNoteListResult);


                }

                // called at final
                isLoading.postValue(false);
            }
        });

    }
}
