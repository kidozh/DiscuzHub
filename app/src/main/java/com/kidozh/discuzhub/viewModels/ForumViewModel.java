package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.FavoriteForumDatabase;
import com.kidozh.discuzhub.database.bbsThreadDraftDatabase;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.FavoriteForum;
import com.kidozh.discuzhub.entities.DisplayForumQueryStatus;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForumViewModel extends AndroidViewModel {
    private String TAG = ForumViewModel.class.getSimpleName();

    public MutableLiveData<DisplayForumQueryStatus> forumStatusMutableLiveData;

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    ForumInfo forum;
    public OkHttpClient client;

    public MutableLiveData<Boolean> isLoading, hasLoadAll;
    public MutableLiveData<List<ThreadInfo>> threadInfoListMutableLiveData;

    public LiveData<Integer> draftNumberLiveData;
    public MutableLiveData<ForumInfo> forumDetailedInfoMutableLiveData;
    public MutableLiveData<ForumResult> displayForumResultMutableLiveData;
    public LiveData<FavoriteForum> favoriteForumLiveData;
    public MutableLiveData<Boolean> ruleTextCollapse = new MutableLiveData<>(true);
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);
    public MutableLiveData<Boolean> loadAllNoticeOnce = new MutableLiveData<>(false);




    public ForumViewModel(@NonNull Application application) {
        super(application);
        forumStatusMutableLiveData = new MutableLiveData<DisplayForumQueryStatus>();
        isLoading = new MutableLiveData<Boolean>(false);

        isLoading.setValue(false);

        hasLoadAll = new MutableLiveData<>(false);
        draftNumberLiveData = bbsThreadDraftDatabase
                .getInstance(getApplication())
                .getbbsThreadDraftDao()
                .getDraftNumber();
        forumDetailedInfoMutableLiveData = new MutableLiveData<>();
        displayForumResultMutableLiveData = new MutableLiveData<>(null);
        ruleTextCollapse = new MutableLiveData<>(UserPreferenceUtils.collapseForumRule(application));
        ruleTextCollapse.postValue(UserPreferenceUtils.collapseForumRule(application));
    }

    public void setBBSInfo(@NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, ForumInfo forum){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        this.forum = forum;
        URLUtils.setBBS(bbsInfo);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
        favoriteForumLiveData = FavoriteForumDatabase.getInstance(getApplication())
                .getDao()
                .getFavoriteItemByfid(bbsInfo.getId(),userBriefInfo!=null? userBriefInfo.getUid():0,forum.fid);
        int uid = userBriefInfo!=null? userBriefInfo.getUid():0;
        Log.d(TAG,"Get favorite form info "+userBriefInfo+" fid "+forum.fid+" uid "+uid);
    }

    public void toggleRuleCollapseStatus(){
        ruleTextCollapse.postValue(!ruleTextCollapse.getValue());
    }

    public LiveData<List<ThreadInfo>> getThreadInfoListLiveData(){
        if(threadInfoListMutableLiveData == null){
            threadInfoListMutableLiveData = new MutableLiveData<>();
            DisplayForumQueryStatus displayForumQueryStatus = new DisplayForumQueryStatus(forum.fid,1);
            setForumStatusAndFetchThread(displayForumQueryStatus);
        }
        return threadInfoListMutableLiveData;
    }

    public void setForumStatusAndFetchThread(DisplayForumQueryStatus displayForumQueryStatus){
        forumStatusMutableLiveData.postValue(displayForumQueryStatus);
        getThreadList(displayForumQueryStatus);
    }

    public void getThreadList(DisplayForumQueryStatus displayForumQueryStatus){
        if(!NetworkUtils.isOnline(getApplication())){
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()));
            isLoading.postValue(false);
            return;
        }
        boolean loading = isLoading.getValue();
        boolean loadAll = hasLoadAll.getValue();
        if(displayForumQueryStatus.page == 1){
            loadAll = false;
            loadAllNoticeOnce.postValue(false);
        }

        if(loading || loadAll){
            return;
        }

        isLoading.postValue(true);
        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<ForumResult> forumResultCall = service.forumDisplayResult(displayForumQueryStatus.generateQueryHashMap());
        forumResultCall.enqueue(new Callback<ForumResult>() {
            @Override
            public void onResponse(Call<ForumResult> call, Response<ForumResult> response) {
                // clear status if page == 1

                if(response.isSuccessful() && response.body()!=null){
                    ForumResult forumResult = response.body();
                    displayForumResultMutableLiveData.postValue(forumResult);
                    Log.d(TAG, "Get forum Result" + forumResult);
                    if(forumResult.message!=null){
                        errorMessageMutableLiveData.postValue(forumResult.message.toErrorMessage());
                    }

                    // for list display
                    List<ThreadInfo> threadInfoList = forumResult.forumVariables.forumThreadList;
                    List<ThreadInfo> currentThreadInfo = threadInfoListMutableLiveData.getValue();
                    if(currentThreadInfo == null || displayForumQueryStatus.page == 1){
                        currentThreadInfo = new ArrayList<>();
                    }

                    if(threadInfoList == null){

                        if(displayForumQueryStatus.page != 1){
                            // rollback
                            displayForumQueryStatus.page -= 1;
                            forumStatusMutableLiveData.postValue(displayForumQueryStatus);
                        }
                        errorMessageMutableLiveData.postValue(new ErrorMessage(
                                getApplication().getString(R.string.empty_result),
                                getApplication().getString(R.string.discuz_network_result_null)
                        ));
                    }
                    else {
                        currentThreadInfo.addAll(threadInfoList);
                        threadInfoListMutableLiveData.postValue(currentThreadInfo);
                        int totalThreadNumber = forumResult.forumVariables.forumInfo.threadCount;
                        if(currentThreadInfo.size() >= totalThreadNumber){
                            hasLoadAll.postValue(true);
                            if(displayForumQueryStatus.page != 1){
                                // rollback
                                displayForumQueryStatus.page -= 1;
                                forumStatusMutableLiveData.postValue(displayForumQueryStatus);
                            }
                        }
                        else {
                            loadAllNoticeOnce.postValue(false);
                        }

                    }
                    forumDetailedInfoMutableLiveData.postValue(forumResult.forumVariables.forumInfo);
                }
                else {
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<ForumResult> call, Throwable t) {
                if(displayForumQueryStatus.page == 1){
                    threadInfoListMutableLiveData.postValue(new ArrayList<>());
                }
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                isLoading.postValue(false);
            }
        });

        Log.d(TAG,"Send request to "+ forumResultCall.request().url().toString());


    }





}
