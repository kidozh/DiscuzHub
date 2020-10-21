package com.kidozh.discuzhub.activities.ui.HotThreads;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.results.DisplayThreadsResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class HotThreadsViewModel extends AndroidViewModel {
    private String TAG = HotThreadsViewModel.class.getSimpleName();
    private MutableLiveData<String> mText;

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;
    private OkHttpClient client = new OkHttpClient();

    public MutableLiveData<Integer> pageNum;
    public MutableLiveData<Boolean> isLoading;
    public MutableLiveData<List<ThreadInfo>> threadListLiveData;
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);;
    public MutableLiveData<DisplayThreadsResult> resultMutableLiveData = new MutableLiveData<>();




    public HotThreadsViewModel(Application application) {
        super(application);
        pageNum = new MutableLiveData<Integer>();
        pageNum.postValue(1);
        isLoading = new MutableLiveData<Boolean>();
        isLoading.postValue(false);

    }

    public void setBBSInfo(@NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        URLUtils.setBBS(bbsInfo);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }



    public LiveData<List<ThreadInfo>> getThreadListLiveData() {
        if(threadListLiveData == null){
            threadListLiveData = new MutableLiveData<List<ThreadInfo>>();
            getThreadList(pageNum.getValue() == null ? 1 : pageNum.getValue());
        }
        return threadListLiveData;
    }

    public void setPageNumAndFetchThread(int page){
        pageNum.setValue(page);
        getThreadList(page);
    }

    private void getThreadList(int page){
        // init page
        if(!NetworkUtils.isOnline(getApplication())){
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()));
            isLoading.postValue(false);
            return;
        }
        isLoading.postValue(true);
        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<DisplayThreadsResult> displayThreadsResultCall = service.hotThreadResult(page);
        displayThreadsResultCall.enqueue(new Callback<DisplayThreadsResult>() {
            @Override
            public void onResponse(Call<DisplayThreadsResult> call, retrofit2.Response<DisplayThreadsResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    DisplayThreadsResult threadsResult = response.body();
                    resultMutableLiveData.postValue(threadsResult);

                    List<ThreadInfo> currentThreadInfo = threadListLiveData.getValue();
                    if(currentThreadInfo == null){
                        currentThreadInfo = new ArrayList<ThreadInfo>();
                    }

                    if(threadsResult.forumVariables !=null){
                        List<ThreadInfo> threadInfos = threadsResult.forumVariables.forumThreadList;
                        if(threadInfos != null){
                            currentThreadInfo.addAll(threadInfos);
                        }
                        errorMessageMutableLiveData.postValue(null);
                    }
                    else {

                        if(threadsResult.message !=null){
                            errorMessageMutableLiveData.postValue(threadsResult.message.toErrorMessage());

                        }
                        else if(threadsResult.error.length()!=0){
                            errorMessageMutableLiveData.postValue(new ErrorMessage(
                                    getApplication().getString(R.string.discuz_api_error),
                                    threadsResult.error
                            ));

                        }
                        else {
                            errorMessageMutableLiveData.postValue(new ErrorMessage(
                                    getApplication().getString(R.string.empty_result),
                                    getApplication().getString(R.string.discuz_network_result_null)
                            ));
                        }

                        if(page != 1){
                            // not at initial state
                            pageNum.postValue(pageNum.getValue() == null ?1:pageNum.getValue()-1);
                        }
                    }

                    threadListLiveData.postValue(currentThreadInfo);
                }
                else {
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<DisplayThreadsResult> call, Throwable t) {
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                isLoading.postValue(false);
            }
        });


    }
}