package com.kidozh.discuzhub.activities.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumCategorySection;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
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
    private MutableLiveData<List<forumCategorySection>> forumCategories;
    public MutableLiveData<String> errorText, jsonString;
    public MutableLiveData<forumUserBriefInfo> userBriefInfoMutableLiveData;

    bbsInformation curBBS;
    forumUserBriefInfo curUser;


    public HomeViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.postValue("This is home fragment");
        errorText = new MutableLiveData<String>();
        jsonString = new MutableLiveData<String>();

    }

    public void setBBSInfo(bbsInformation curBBS, forumUserBriefInfo curUser){
        this.curBBS = curBBS;
        this.curUser = curUser;
        userBriefInfoMutableLiveData = new MutableLiveData<>(curUser);
    }

    public LiveData<List<forumCategorySection>> getForumCategoryInfo(){
        if(forumCategories == null){
            forumCategories = new MutableLiveData<List<forumCategorySection>>();
            loadForumCategoryInfo();
        }
        return forumCategories;
    }

    private void loadForumCategoryInfo(){
        if( curBBS == null){
            return;
        }
        bbsURLUtils.setBBS(curBBS);
        OkHttpClient client = networkUtils.getPreferredClientWithCookieJarByUser(this.getApplication(),curUser);
        Request request = new Request.Builder()
                .url(bbsURLUtils.getBBSForumInfoApi())
                .build();
        Log.d(TAG,"Send request to "+bbsURLUtils.getBBSForumInfoApi());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                forumCategories.postValue(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Recv Portal JSON "+s);
                    jsonString.postValue(s);
                    List<forumCategorySection> categorySectionFidList = bbsParseUtils.parseCategoryFids(s);
                    forumCategories.postValue(categorySectionFidList);
                    if(categorySectionFidList == null){
                        errorText.postValue(bbsParseUtils.parseErrorInformation(s));
                    }
                    // parse person info
                    forumUserBriefInfo severReturnedUser = bbsParseUtils.parseBreifUserInfo(s);
                    Log.d(TAG,"Current User is "+severReturnedUser);
                    userBriefInfoMutableLiveData.postValue(severReturnedUser);

                }
                else {
                    String s = response.body().string();
                    errorText.postValue(bbsParseUtils.parseErrorInformation(s));
                    forumCategories.postValue(null);
                }
            }
        });
    }

    public LiveData<String> getText() {
        return mText;
    }
}