package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.UploadAttachment;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.results.PostParameterResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public MutableLiveData<PostParameterResult> threadPostParameterResultMutableLiveData;
    public MutableLiveData<PostParameterResult.AllowPermission> allowPermissionMutableLiveData;
    public MutableLiveData<Boolean> error,isUploadingAttachmentLiveData;
    public MutableLiveData<bbsThreadDraft> bbsThreadDraftMutableLiveData;
    public MutableLiveData<List<UploadAttachment>> uploadAttachmentListLiveData;
    public MutableLiveData<String> selectedAttachmentSuffixLiveData, uploadAttachmentErrorStringLiveData;
    private MutableLiveData<SecureInfoResult> secureInfoResultMutableLiveData;


    public PostThreadViewModel(@NonNull Application application) {
        super(application);
        error = new MutableLiveData<>(false);
        bbsThreadDraftMutableLiveData = new MutableLiveData<>(null);
        allowPermissionMutableLiveData = new MutableLiveData<>(null);
        uploadAttachmentListLiveData = new MutableLiveData<>(new ArrayList<>());
        selectedAttachmentSuffixLiveData = new MutableLiveData<>("");
        uploadAttachmentErrorStringLiveData = new MutableLiveData<>("");
        isUploadingAttachmentLiveData = new MutableLiveData<>(false);
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, String fid){
        this.curBBS = bbsInfo;
        this.curUser = userBriefInfo;
        this.fid = fid;
        URLUtils.setBBS(bbsInfo);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }

    public MutableLiveData<PostParameterResult> getThreadPostParameterResultMutableLiveData() {
        if(threadPostParameterResultMutableLiveData == null){
            threadPostParameterResultMutableLiveData = new MutableLiveData<>(null);
            loadThreadPostParameter();
        }
        return threadPostParameterResultMutableLiveData;
    }

    public void loadThreadPostParameter(){
        Request request = new Request.Builder()
                .url(URLUtils.getCheckPostUrl(fid))
                .build();
        Log.d(TAG,"Post parameters send request "+ URLUtils.getCheckPostUrl(String.valueOf(fid)));
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
                    PostParameterResult postParameterResult = bbsParseUtils.parseThreadPostParameter(s);
                    if(postParameterResult == null){
                        error.postValue(true);
                        Toasty.error(getApplication(),getApplication().getString(R.string.bbs_post_thread_cannot_upload_picture), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        allowPermissionMutableLiveData.postValue(postParameterResult.permissionVariables.allowPerm);
                    }
                    threadPostParameterResultMutableLiveData.postValue(postParameterResult);
                }
                else {
                    error.postValue(false);
                }
            }
        });
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

    public void getSecureInfo(){
        String url = URLUtils.getSecureParameterURL("post");
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
