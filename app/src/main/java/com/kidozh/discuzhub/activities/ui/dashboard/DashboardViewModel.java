package com.kidozh.discuzhub.activities.ui.dashboard;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.ThreadInfo;
import com.kidozh.discuzhub.results.DisplayThreadsResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardViewModel extends AndroidViewModel {
    private String TAG = DashboardViewModel.class.getSimpleName();
    private MutableLiveData<String> mText;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;
    private OkHttpClient client;

    public MutableLiveData<Integer> pageNum;
    public MutableLiveData<Boolean> isLoading, isError;
    public MutableLiveData<List<ThreadInfo>> threadListLiveData;
    public MutableLiveData<String> errorTextLiveData;
    public String jsonString;



    public DashboardViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
        pageNum = new MutableLiveData<Integer>();
        pageNum.postValue(1);
        isLoading = new MutableLiveData<Boolean>();
        isLoading.postValue(false);
        isError = new MutableLiveData<Boolean>();
        isError.postValue(false);
        errorTextLiveData = new MutableLiveData<>("");

    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.curBBS = bbsInfo;
        this.curUser = userBriefInfo;
        URLUtils.setBBS(bbsInfo);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);
    }

    public LiveData<String> getText() {
        return mText;
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
        isLoading.postValue(true);
        isError.postValue(false);
        Request request = new Request.Builder()
                .url(URLUtils.getHotThreadUrl(page))
                .build();
        Log.d(TAG,"Send request to "+request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading.postValue(false);
                isError.postValue(true);
                errorTextLiveData.postValue(getApplication().getString(R.string.network_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isLoading.postValue(false);

                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    DisplayThreadsResult threadsResult = bbsParseUtils.getThreadListInfo(s);

                    List<ThreadInfo> currentThreadInfo = threadListLiveData.getValue();
                    if(currentThreadInfo == null){
                        currentThreadInfo = new ArrayList<ThreadInfo>();
                    }

                    if(threadsResult!=null && threadsResult.forumVariables !=null){
                        List<ThreadInfo> threadInfos = threadsResult.forumVariables.forumThreadList;
                        if(threadInfos != null){
                            currentThreadInfo.addAll(threadInfos);
                        }
                        isError.postValue(false);
                    }
                    else {
                        isError.postValue(true);
                        if(threadsResult!=null){
                            if(threadsResult.message !=null){
                                errorTextLiveData.postValue(threadsResult.message.content);
                            }
                            else if(threadsResult.error.length()!=0){
                                errorTextLiveData.postValue(threadsResult.error);
                            }
                        }
                        if(page != 1){
                            // not at initial state
                            pageNum.postValue(pageNum.getValue() == null ?1:pageNum.getValue()-1);
                        }
                    }

                    threadListLiveData.postValue(currentThreadInfo);
                }
            }
        });
    }
}