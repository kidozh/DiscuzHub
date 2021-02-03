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
import com.kidozh.discuzhub.entities.Thread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.Forum;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.ConstUtils;
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
    @NonNull
    public MutableLiveData<DisplayForumQueryStatus> forumStatusMutableLiveData;

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    Forum forum;
    public OkHttpClient client;

    public MutableLiveData<Integer> networkState = new MutableLiveData<>(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);
    private MutableLiveData<List<Thread>> threadInfoListMutableLiveData = new MutableLiveData<>(new ArrayList<>()),
            newThreadListMutableLiveData;

    public LiveData<Integer> draftNumberLiveData;
    public MutableLiveData<Forum> forumDetailedInfoMutableLiveData;
    public MutableLiveData<ForumResult> displayForumResultMutableLiveData;
    public LiveData<FavoriteForum> favoriteForumLiveData;
    public MutableLiveData<Boolean> ruleTextCollapse = new MutableLiveData<>(true);
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);
    public MutableLiveData<Boolean> loadAllNoticeOnce = new MutableLiveData<>(false);




    public ForumViewModel(@NonNull Application application) {
        super(application);
        forumStatusMutableLiveData = new MutableLiveData<DisplayForumQueryStatus>();
        draftNumberLiveData = bbsThreadDraftDatabase
                .getInstance(getApplication())
                .getbbsThreadDraftDao()
                .getDraftNumber();
        forumDetailedInfoMutableLiveData = new MutableLiveData<>();
        displayForumResultMutableLiveData = new MutableLiveData<>(null);
        ruleTextCollapse = new MutableLiveData<>(UserPreferenceUtils.collapseForumRule(application));
        ruleTextCollapse.postValue(UserPreferenceUtils.collapseForumRule(application));
    }

    public MutableLiveData<List<Thread>> getNewThreadListMutableLiveData() {
        if(newThreadListMutableLiveData == null){
            newThreadListMutableLiveData = new MutableLiveData<>(new ArrayList<>());
            DisplayForumQueryStatus displayForumQueryStatus = new DisplayForumQueryStatus(forum.fid,1);
            setForumStatusAndFetchThread(displayForumQueryStatus);
        }
        return newThreadListMutableLiveData;
    }

    public void setBBSInfo(@NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, Forum forum){
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



    public void setForumStatusAndFetchThread(DisplayForumQueryStatus displayForumQueryStatus){
        forumStatusMutableLiveData.postValue(displayForumQueryStatus);
        getThreadList(displayForumQueryStatus);
    }

    public void getThreadList(DisplayForumQueryStatus displayForumQueryStatus){
        if(!NetworkUtils.isOnline(getApplication())){
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()));
            networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
            newThreadListMutableLiveData.postValue(new ArrayList<>());
            return;
        }
        boolean loading = networkState.getValue() == ConstUtils.NETWORK_STATUS_LOADING;
        boolean loadAll = networkState.getValue() == ConstUtils.NETWORK_STATUS_LOADED_ALL;
        if(displayForumQueryStatus.page == 1){
            loadAll = false;
            loadAllNoticeOnce.postValue(false);
            threadInfoListMutableLiveData.postValue(new ArrayList<>());
        }

        if(loading || loadAll){
            return;
        }

        networkState.postValue(ConstUtils.NETWORK_STATUS_LOADING);
        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<ForumResult> forumResultCall = service.forumDisplayResult(displayForumQueryStatus.generateQueryHashMap());
        Log.d(TAG,"Browse page "+displayForumQueryStatus.page+" url "+forumResultCall.request().url().toString());
        forumResultCall.enqueue(new Callback<ForumResult>() {
            @Override
            public void onResponse(Call<ForumResult> call, Response<ForumResult> response) {
                // clear status if page == 1

                if(response.isSuccessful() && response.body()!=null){
                    ForumResult forumResult = response.body();
                    displayForumResultMutableLiveData.postValue(forumResult);
                    Log.d(TAG, "Get forum Result" + forumResult);

                    // for list display
                    List<Thread> threadList = forumResult.forumVariables.forumThreadList;
                    List<Thread> totalThreadList = threadInfoListMutableLiveData.getValue();
                    if(totalThreadList == null || displayForumQueryStatus.page == 1){
                        totalThreadList = new ArrayList<>();
                    }

                    if(threadList == null){

                        if(displayForumQueryStatus.page != 1){
                            // rollback
                            displayForumQueryStatus.page -= 1;
                            forumStatusMutableLiveData.postValue(displayForumQueryStatus);
                        }
                        threadInfoListMutableLiveData.postValue(totalThreadList);
                        newThreadListMutableLiveData.postValue(threadList);
                        errorMessageMutableLiveData.postValue(new ErrorMessage(
                                getApplication().getString(R.string.empty_result),
                                getApplication().getString(R.string.discuz_network_result_null)
                        ));
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                    }
                    else {
                        // not null
                        // move to next page
                        displayForumQueryStatus.page += 1;
                        forumStatusMutableLiveData.postValue(displayForumQueryStatus);
                        totalThreadList.addAll(threadList);
                        threadInfoListMutableLiveData.postValue(totalThreadList);
                        newThreadListMutableLiveData.postValue(threadList);
                        int totalThreadNumber = forumResult.forumVariables.forum.threadCount;
                        if(totalThreadList.size() >= totalThreadNumber){
                            networkState.postValue(ConstUtils.NETWORK_STATUS_LOADED_ALL);
                            if(displayForumQueryStatus.page != 1){
                                // rollback
                                displayForumQueryStatus.page -= 1;
                                forumStatusMutableLiveData.postValue(displayForumQueryStatus);
                            }
                        }
                        else {
                            loadAllNoticeOnce.postValue(false);
                            networkState.postValue(ConstUtils.NETWORK_STATUS_SUCCESSFULLY);

                        }
                        // initial page
                        if((displayForumQueryStatus.page == 2||displayForumQueryStatus.page==1) && threadList.size() == 0){
                            errorMessageMutableLiveData.postValue(new ErrorMessage(getApplication().getString(R.string.empty_result),
                                    getApplication().getString(R.string.empty_hot_threads),R.drawable.ic_empty_hot_thread_64px
                            ));
                            networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                        }

                    }

                    if(forumResult.message!=null){
                        errorMessageMutableLiveData.postValue(forumResult.message.toErrorMessage());
                        networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                    }
                    forumDetailedInfoMutableLiveData.postValue(forumResult.forumVariables.forum);
                }
                else {
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));
                    networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
                }
            }

            @Override
            public void onFailure(Call<ForumResult> call, Throwable t) {
                if(displayForumQueryStatus.page == 1){
                    threadInfoListMutableLiveData.postValue(new ArrayList<>());
                    newThreadListMutableLiveData.postValue(new ArrayList<>());
                }
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                networkState.postValue(ConstUtils.NETWORK_STATUS_FAILED);
            }
        });


    }





}
