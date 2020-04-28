package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ThreadDetailViewModel extends AndroidViewModel {
    private String TAG = ThreadDetailViewModel.class.getSimpleName();



    private bbsInformation bbsInfo;
    private OkHttpClient client;
    private forumInfo forum;
    private String tid;
    private forumUserBriefInfo userBriefInfo;

    public MutableLiveData<Boolean> isLoading, error, hasLoadAll;
    public MutableLiveData<String> formHash, errorText;
    public MutableLiveData<bbsPollInfo> pollInfoLiveData;
    public MutableLiveData<forumUserBriefInfo> bbsPersonInfoMutableLiveData;
    public MutableLiveData<List<threadCommentInfo>> threadCommentInfoListLiveData;
    public MutableLiveData<bbsURLUtils.ThreadStatus> threadStatusMutableLiveData;
    public MutableLiveData<bbsParseUtils.DetailedThreadInfo> detailedThreadInfoMutableLiveData;


    public ThreadDetailViewModel(@NonNull Application application) {
        super(application);
        isLoading = new MutableLiveData<>(false);
        error = new MutableLiveData<>(false);
        formHash = new MutableLiveData<>("");
        bbsPersonInfoMutableLiveData = new MutableLiveData<>();
        threadCommentInfoListLiveData = new MutableLiveData<>();
        hasLoadAll = new MutableLiveData<>(false);
        pollInfoLiveData = new MutableLiveData<>(null);
        threadStatusMutableLiveData = new MutableLiveData<>();
        errorText = new MutableLiveData<>("");
        detailedThreadInfoMutableLiveData = new MutableLiveData<>();
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, forumInfo forum, String tid){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        this.forum = forum;
        this.tid = tid;
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);


        if(threadStatusMutableLiveData.getValue()==null){
            bbsURLUtils.ThreadStatus threadStatus = new bbsURLUtils.ThreadStatus(Integer.parseInt(tid),1);
            threadStatusMutableLiveData.setValue(threadStatus);
        }



        // bbsPersonInfoMutableLiveData.postValue(userBriefInfo);
    }

    public void getThreadDetail(bbsURLUtils.ThreadStatus threadStatus){
        isLoading.postValue(true);
        error.postValue(false);
        hasLoadAll.postValue(false);
        // bbsURLUtils.ThreadStatus threadStatus = threadStatusMutableLiveData.getValue();

        threadStatusMutableLiveData.postValue(threadStatus);
        if(threadStatus.page == 1){
            // clear it first
            threadCommentInfoListLiveData.setValue(new ArrayList<>());
        }
        String apiStr = bbsURLUtils.getThreadCommentUrlByStatus(threadStatus);
        Log.d(TAG,"Send request to "+apiStr);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error.postValue(true);
                isLoading.postValue(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isLoading.postValue(false);
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv thread JSON "+s);
                    String curFormHash = bbsParseUtils.parseFormHash(s);
                    formHash.postValue(curFormHash);
                    forumUserBriefInfo curPersonInfo = bbsParseUtils.parseBreifUserInfo(s);
                    // Log.d(TAG,"Recv USER info "+curPersonInfo);
                    bbsPersonInfoMutableLiveData.postValue(curPersonInfo);

                    bbsParseUtils.returnMessage message = bbsParseUtils.parseReturnMessage(s);
                    if(message !=null){
                        Log.d(TAG,"parse message "+message.string);
                        errorText.postValue(message.string);
                    }
                    bbsParseUtils.DetailedThreadInfo detailedThreadInfo = bbsParseUtils.parseDetailedThreadInfo(s);
                    detailedThreadInfoMutableLiveData.postValue(detailedThreadInfo);
                    bbsPollInfo pollInfo = bbsParseUtils.parsePollInfo(s);

                    if(pollInfoLiveData.getValue() == null && pollInfo !=null){
                        Log.d(TAG,"recv poll info "+ pollInfo.votersCount);
                        pollInfoLiveData.postValue(pollInfo);

                    }
                    List<threadCommentInfo> threadInfoList = bbsParseUtils.parseThreadCommentInfo(s);
                    int totalThreadSize = 0;
                    if(threadInfoList !=null && threadInfoList.size()!=0){

                        if(threadStatus.page == 1){
                            threadCommentInfoListLiveData.postValue(threadInfoList);
                            totalThreadSize = threadInfoList.size();
                        }
                        else {
                            List<threadCommentInfo> currentThreadInfoList = threadCommentInfoListLiveData.getValue();
                            if(currentThreadInfoList == null){
                                currentThreadInfoList = new ArrayList<>();
                            }
                            currentThreadInfoList.addAll(threadInfoList);
                            threadCommentInfoListLiveData.postValue(currentThreadInfoList);
                            totalThreadSize = currentThreadInfoList.size();

                        }
                        // special condition?

                    }
                    else {
                        if(threadStatus.page == 1 && message == null){
                            errorText.postValue(getApplication().getString(R.string.parse_failed));
                        }
                        hasLoadAll.postValue(true);
                        // rollback
                        if(threadStatus.page != 1){
                            threadStatus.page -=1;
                            Log.d(TAG,"Roll back page when page to "+threadStatus.page);
                            threadStatusMutableLiveData.postValue(threadStatus);
                        }
                    }
                    // load all?
                    if(detailedThreadInfo !=null){
                        int maxThreadNumber = detailedThreadInfo.replies;
                        List<threadCommentInfo> currentThreadInfoList = threadCommentInfoListLiveData.getValue();
                        int totalThreadCommentsNumber = 0;

                        if(currentThreadInfoList !=null){
                            totalThreadCommentsNumber = currentThreadInfoList.size();
                            Log.d(TAG, "current size "+totalThreadCommentsNumber);
                        }
                        else {
                            Log.d(TAG, "no thread is found ");
                        }

                        Log.d(TAG,"PAGE "+threadStatus.page+" MAX POSITION "+maxThreadNumber +" CUR "+totalThreadCommentsNumber+ " "+totalThreadSize);
                        if(totalThreadSize >= maxThreadNumber +1){
                            hasLoadAll.postValue(true);
                        }
                        else {
                            hasLoadAll.postValue(false);
                        }
                    }
                    else {
                        if(threadInfoList.size() < threadStatus.perPage){
                            hasLoadAll.postValue(true);
                        }
                        else {
                            hasLoadAll.postValue(false);
                        }
                    }
                }
                else {
                    if(threadStatus.page == 1){
                        errorText.postValue(getApplication().getString(R.string.network_failed));
                    }
                    error.postValue(true);
                    if(threadStatus.page != 1){
                        threadStatus.page -=1;
                        Log.d(TAG,"Roll back page when page to "+threadStatus.page);
                        threadStatusMutableLiveData.postValue(threadStatus);
                    }
                }
            }
        });
    }
}
