package com.kidozh.discuzhub.activities.ui.home;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeViewModel extends AndroidViewModel {
    private final static String TAG = HomeViewModel.class.getSimpleName();

    private MutableLiveData<String> mText;
    private MutableLiveData<List<BBSIndexResult.ForumCategory>> forumCategories;
    public MutableLiveData<String> errorText;
    public MutableLiveData<forumUserBriefInfo> userBriefInfoMutableLiveData;
    public MutableLiveData<Boolean> isLoading;
    public MutableLiveData<BBSIndexResult> bbsIndexResultMutableLiveData;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;


    public HomeViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.postValue("This is home fragment");
        errorText = new MutableLiveData<String>();
        isLoading = new MutableLiveData<>(false);
        bbsIndexResultMutableLiveData = new MutableLiveData<>(null);
        userBriefInfoMutableLiveData =  new MutableLiveData<>(null);

    }

    public void setBBSInfo(bbsInformation curBBS, forumUserBriefInfo curUser){
        this.curBBS = curBBS;
        this.curUser = curUser;
        userBriefInfoMutableLiveData = new MutableLiveData<>(curUser);

    }

    public LiveData<List<BBSIndexResult.ForumCategory>> getForumCategoryInfo(){
        if(forumCategories == null){
            forumCategories = new MutableLiveData<List<BBSIndexResult.ForumCategory>>();
            loadForumCategoryInfo();
        }
        return forumCategories;
    }

    public void loadForumCategoryInfo(){
        if( curBBS == null){
            return;
        }
        URLUtils.setBBS(curBBS);
        OkHttpClient client = networkUtils.getPreferredClientWithCookieJarByUser(this.getApplication(),curUser);
        Request request = new Request.Builder()
                .url(URLUtils.getBBSForumInfoApi())
                .build();
        Log.d(TAG,"Send request to "+ URLUtils.getBBSForumInfoApi());
        Context context = getApplication();
        errorText.setValue(null);
        forumCategories.setValue(null);
        isLoading.setValue(true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                forumCategories.postValue(null);
                isLoading.postValue(false);
                errorText.postValue(context.getString(R.string.parse_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv Portal JSON "+s);
                    BBSIndexResult indexResult = bbsParseUtils.parseForumIndexResult(s);
                    bbsIndexResultMutableLiveData.postValue(indexResult);
                    if(indexResult !=null && indexResult.forumVariables !=null){
                        forumUserBriefInfo serverReturnedUser = indexResult.forumVariables.getUserBriefInfo();
                        userBriefInfoMutableLiveData.postValue(serverReturnedUser);
                        errorText.postValue(null);
                        // prepare to render index page
                        List<BBSIndexResult.ForumCategory> categoryList = indexResult.forumVariables.forumCategoryList;
                        forumCategories.postValue(categoryList);


                    }
                    else {
                        if(indexResult!=null){
                            if(indexResult.message!=null){
                                errorText.postValue(indexResult.message.content);
                            }
                            else if(indexResult.error.length()!=0){
                                errorText.postValue(indexResult.error);
                            }
                            else {
                                errorText.postValue(context.getString(R.string.parse_failed));
                            }
                        }
                        else {
                            errorText.postValue(context.getString(R.string.parse_failed));
                        }
                    }


                }
                else {
                    String s = response.body().string();
                    errorText.postValue(context.getString(R.string.parse_failed));
                    // errorText.postValue(bbsParseUtils.parseErrorInformation(s));
                    forumCategories.postValue(null);
                }
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<String> getText() {
        return mText;
    }
}