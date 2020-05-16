package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.database.bbsThreadDraftDatabase;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.DisplayForumResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForumThreadViewModel extends AndroidViewModel {
    private String TAG = ForumThreadViewModel.class.getSimpleName();

    public MutableLiveData<bbsURLUtils.ForumStatus> forumStatusMutableLiveData;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    ForumInfo forum;
    private OkHttpClient client;

    public MutableLiveData<Boolean> isLoading, isError, hasLoadAll;
    public MutableLiveData<List<ThreadInfo>> threadInfoListMutableLiveData;
    public MutableLiveData<String> jsonString, forumDescription, forumRule;
    public LiveData<Integer> draftNumberLiveData;
    public MutableLiveData<ForumInfo> forumDetailedInfoMutableLiveData;
    public MutableLiveData<DisplayForumResult> displayForumResultMutableLiveData;


    public ForumThreadViewModel(@NonNull Application application) {
        super(application);
        forumStatusMutableLiveData = new MutableLiveData<bbsURLUtils.ForumStatus>();
        isLoading = new MutableLiveData<Boolean>(false);
        isError = new MutableLiveData<Boolean>(false);
        isError.setValue(false);
        isLoading.setValue(false);
        jsonString = new MutableLiveData<>();
        forumDescription = new MutableLiveData<>("");
        forumRule = new MutableLiveData<>("");
        hasLoadAll = new MutableLiveData<>(false);
        draftNumberLiveData = bbsThreadDraftDatabase
                .getInstance(getApplication())
                .getbbsThreadDraftDao()
                .getDraftNumber();
        forumDetailedInfoMutableLiveData = new MutableLiveData<>();
        displayForumResultMutableLiveData = new MutableLiveData<>(null);
    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, ForumInfo forum){
        this.curBBS = bbsInfo;
        this.curUser = userBriefInfo;
        this.forum = forum;
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }

    public LiveData<List<ThreadInfo>> getThreadInfoListLiveData(){
        if(threadInfoListMutableLiveData == null){
            threadInfoListMutableLiveData = new MutableLiveData<>();
            bbsURLUtils.ForumStatus forumStatus = new bbsURLUtils.ForumStatus(forum.fid,1);
            setForumStatusAndFetchThread(forumStatus);
        }
        return threadInfoListMutableLiveData;
    }

    public void setForumStatusAndFetchThread(bbsURLUtils.ForumStatus forumStatus){
        forumStatusMutableLiveData.postValue(forumStatus);
        getThreadList(forumStatus);
    }

    public void getThreadList(bbsURLUtils.ForumStatus forumStatus){
        boolean loading = isLoading.getValue();
        if(loading){
            return;
        }
        isError.postValue(false);
        isLoading.postValue(true);
        Request request = new Request.Builder()
                .url(bbsURLUtils.getForumUrlByStatus(forumStatus))
                .build();
        Log.d(TAG,"Send request to "+bbsURLUtils.getForumUrlByStatus(forumStatus));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading.postValue(false);
                isError.postValue(true);
                // clear status if page == 1
                if(forumStatus.page == 1){
                    threadInfoListMutableLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // clear status if page == 1
                if(forumStatus.page == 1){
                    threadInfoListMutableLiveData.postValue(new ArrayList<>());
                }

                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    jsonString.postValue(s);
                    Log.d(TAG,"recv forum thread json "+s);
                    DisplayForumResult forumResult = bbsParseUtils.parseForumInfo(s);
                    displayForumResultMutableLiveData.postValue(forumResult);
                    if(forumResult !=null){
                        Log.d(TAG, "Get forum Result" + forumResult);
                        // for list display
                        List<ThreadInfo> threadInfoList = forumResult.forumVariables.forumThreadList;
                        List<ThreadInfo> currentThreadInfo = threadInfoListMutableLiveData.getValue();
                        if(currentThreadInfo == null){
                            currentThreadInfo = new ArrayList<>();
                        }

                        if(threadInfoList == null){
                            isError.postValue(true);
                            if(forumStatus.page != 1){
                                // rollback
                                forumStatus.page -= 1;
                                forumStatusMutableLiveData.postValue(forumStatus);
                            }
                        }
                        else {
                            currentThreadInfo.addAll(threadInfoList);
                            threadInfoListMutableLiveData.postValue(currentThreadInfo);
                            int totalThreadNumber = forumResult.forumVariables.forumInfo.threads;
                            if(currentThreadInfo.size() >= totalThreadNumber){
                                hasLoadAll.postValue(true);
                                if(forumStatus.page != 1){
                                    // rollback
                                    forumStatus.page -= 1;
                                    forumStatusMutableLiveData.postValue(forumStatus);
                                }
                            }

                        }
                        forumDetailedInfoMutableLiveData.postValue(forumResult.forumVariables.forumInfo);

                    }
                    else {
                        isError.postValue(true);
                    }

                }
                else {
                    isError.postValue(true);
                }
                isLoading.postValue(false);
            }
        });

    }



}
