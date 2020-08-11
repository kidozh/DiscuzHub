package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.results.ThreadPostResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostsViewModel extends AndroidViewModel {
    private String TAG = PostsViewModel.class.getSimpleName();



    private bbsInformation bbsInfo;
    private OkHttpClient client;
    private ForumInfo forum;
    private int tid;
    private forumUserBriefInfo userBriefInfo;

    public MutableLiveData<Boolean> isLoading, error, hasLoadAll;
    public MutableLiveData<String> formHash, errorText;
    public MutableLiveData<bbsPollInfo> pollInfoLiveData;
    public MutableLiveData<forumUserBriefInfo> bbsPersonInfoMutableLiveData;
    public MutableLiveData<List<PostInfo>> threadCommentInfoListLiveData;
    public MutableLiveData<URLUtils.ThreadStatus> threadStatusMutableLiveData;
    public MutableLiveData<bbsParseUtils.DetailedThreadInfo> detailedThreadInfoMutableLiveData;
    public MutableLiveData<ThreadPostResult> threadPostResultMutableLiveData;
    private MutableLiveData<SecureInfoResult> secureInfoResultMutableLiveData;
    public LiveData<Boolean> isFavoriteThreadMutableLiveData;
    FavoriteThreadDao dao;

    public PostsViewModel(@NonNull Application application) {
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
        threadPostResultMutableLiveData = new MutableLiveData<>();
        dao = FavoriteThreadDatabase.getInstance(application).getDao();

    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, ForumInfo forum, int tid){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        this.forum = forum;
        this.tid = tid;
        URLUtils.setBBS(bbsInfo);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);


        if(threadStatusMutableLiveData.getValue()==null){
            URLUtils.ThreadStatus threadStatus = new URLUtils.ThreadStatus(tid,1);
            threadStatusMutableLiveData.setValue(threadStatus);
        }
        isFavoriteThreadMutableLiveData = dao.isFavoriteThread(bbsInfo.getId(),tid);



        // bbsPersonInfoMutableLiveData.postValue(userBriefInfo);
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

    public void getThreadDetail(URLUtils.ThreadStatus threadStatus){
        isLoading.postValue(true);
        error.postValue(false);
        hasLoadAll.postValue(false);
        // bbsURLUtils.ThreadStatus threadStatus = threadStatusMutableLiveData.getValue();

        threadStatusMutableLiveData.postValue(threadStatus);
        if(threadStatus.page == 1){
            // clear it first
            threadCommentInfoListLiveData.setValue(new ArrayList<>());
        }
        String apiStr = URLUtils.getThreadCommentUrlByStatus(threadStatus);
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

                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    int totalThreadSize = 0;
                    Log.d(TAG,"Recv thread JSON "+s);
                    ThreadPostResult threadPostResult = bbsParseUtils.parseThreadPostResult(s);
                    bbsParseUtils.DetailedThreadInfo detailedThreadInfo = null;
                    threadPostResultMutableLiveData.postValue(threadPostResult);
                    if(threadPostResult!=null && threadPostResult.threadPostVariables!=null){
                        // update formhash first
                        if(threadPostResult.threadPostVariables.formHash !=null){
                            formHash.postValue(threadPostResult.threadPostVariables.formHash);
                        }
                        // parse message
                        if(threadPostResult.message!=null){
                            errorText.postValue(threadPostResult.message.content);
                        }
                        // update user
                        if(threadPostResult.threadPostVariables!=null){
                            bbsPersonInfoMutableLiveData.postValue(threadPostResult.threadPostVariables.getUserBriefInfo());
                            // parse detailed info
                            detailedThreadInfo = threadPostResult.threadPostVariables.detailedThreadInfo;
                            detailedThreadInfoMutableLiveData.postValue(threadPostResult.threadPostVariables.detailedThreadInfo);

                            bbsPollInfo pollInfo = threadPostResult.threadPostVariables.pollInfo;
                            if(pollInfoLiveData.getValue() == null && pollInfo !=null){
                                Log.d(TAG,"recv poll info "+ pollInfo.votersCount);
                                pollInfoLiveData.postValue(pollInfo);

                            }
                            List<PostInfo> postInfoList = threadPostResult.threadPostVariables.postInfoList;
                            // remove null object
                            Log.d(TAG,"Recv post info size "+postInfoList.size());
                            Iterator<PostInfo> iterator = postInfoList.iterator();
                            while (iterator.hasNext()){
                                PostInfo postInfo = iterator.next();
                                if(postInfo.message == null || postInfo.author == null){
                                    iterator.remove();
                                }
                            }
                            Log.d(TAG,"Recv Non Nullable post info size "+postInfoList.size());
                            if(postInfoList !=null && postInfoList.size()!=0){
                                if(threadStatus.page == 1){
                                    threadCommentInfoListLiveData.postValue(postInfoList);
                                    totalThreadSize = postInfoList.size();
                                }
                                else {
                                    List<PostInfo> currentThreadInfoList = threadCommentInfoListLiveData.getValue();
                                    if(currentThreadInfoList == null){
                                        currentThreadInfoList = new ArrayList<>();
                                    }
                                    currentThreadInfoList.addAll(postInfoList);
                                    threadCommentInfoListLiveData.postValue(currentThreadInfoList);
                                    totalThreadSize = currentThreadInfoList.size();

                                }
                            }
                            else {
                                if(threadStatus.page == 1 && (threadPostResult == null || threadPostResult.message !=null)){
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
                        }

                        // load all?
                        if(detailedThreadInfo !=null){
                            int maxThreadNumber = detailedThreadInfo.replies;
                            List<PostInfo> currentThreadInfoList = threadCommentInfoListLiveData.getValue();
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
                    }
                    else {
                        error.postValue(true);
                        errorText.postValue(getApplication().getString(R.string.parse_post_failed));
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
                isLoading.postValue(false);
            }
        });
    }
}
