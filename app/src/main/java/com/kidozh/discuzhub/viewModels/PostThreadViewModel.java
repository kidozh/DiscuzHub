package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.ThreadPostParameterResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostThreadViewModel extends AndroidViewModel {
    private String TAG = PostThreadViewModel.class.getSimpleName();

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    String fid;
    OkHttpClient client;

    public MutableLiveData<ThreadPostParameterResult> threadPostParameterResultMutableLiveData;
    public MutableLiveData<ThreadPostParameterResult.AllowPermission> allowPermissionMutableLiveData;
    public MutableLiveData<Boolean> error;
    public MutableLiveData<bbsThreadDraft> bbsThreadDraftMutableLiveData;



    public PostThreadViewModel(@NonNull Application application) {
        super(application);
        error = new MutableLiveData<>(false);
        bbsThreadDraftMutableLiveData = new MutableLiveData<>(null);
        allowPermissionMutableLiveData = new MutableLiveData<>(null);
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, String fid){
        this.curBBS = bbsInfo;
        this.curUser = userBriefInfo;
        this.fid = fid;
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }

    public MutableLiveData<ThreadPostParameterResult> getThreadPostParameterResultMutableLiveData() {
        if(threadPostParameterResultMutableLiveData == null){
            threadPostParameterResultMutableLiveData = new MutableLiveData<>(null);
            loadThreadPostParameter();
        }
        return threadPostParameterResultMutableLiveData;
    }

    public void loadThreadPostParameter(){
        Request request = new Request.Builder()
                .url(bbsURLUtils.getCheckPostUrl(fid))
                .build();
        Log.d(TAG,"Post parameters send request "+bbsURLUtils.getCheckPostUrl(String.valueOf(fid)));
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error.postValue(true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv post parameters "+s);
                    ThreadPostParameterResult threadPostParameterResult = bbsParseUtils.parseThreadPostParameter(s);
                    if(threadPostParameterResult == null){
                        error.postValue(true);
                        Toasty.error(getApplication(),getApplication().getString(R.string.bbs_post_thread_cannot_upload_picture), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        allowPermissionMutableLiveData.postValue(threadPostParameterResult.permissionVariables.allowPerm);
                    }
                    threadPostParameterResultMutableLiveData.postValue(threadPostParameterResult);
                }
                else {
                    error.postValue(false);
                }
            }
        });
    }
}
