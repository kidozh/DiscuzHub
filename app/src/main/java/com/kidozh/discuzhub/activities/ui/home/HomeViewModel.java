package com.kidozh.discuzhub.activities.ui.home;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.List;



import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class HomeViewModel extends AndroidViewModel {
    private final static String TAG = HomeViewModel.class.getSimpleName();

    private MutableLiveData<String> mText;
    private MutableLiveData<List<BBSIndexResult.ForumCategory>> forumCategories;
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);
    public MutableLiveData<forumUserBriefInfo> userBriefInfoMutableLiveData;
    public MutableLiveData<Boolean> isLoading;
    public MutableLiveData<BBSIndexResult> bbsIndexResultMutableLiveData;

    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;


    public HomeViewModel(Application application) {
        super(application);
        
        
        isLoading = new MutableLiveData<>(false);
        bbsIndexResultMutableLiveData = new MutableLiveData<>(null);
        userBriefInfoMutableLiveData =  new MutableLiveData<>(null);

    }

    public void setBBSInfo(@NonNull bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        userBriefInfoMutableLiveData = new MutableLiveData<>(userBriefInfo);

    }

    public LiveData<List<BBSIndexResult.ForumCategory>> getForumCategoryInfo(){
        if(forumCategories == null){
            forumCategories = new MutableLiveData<List<BBSIndexResult.ForumCategory>>();
            loadForumCategoryInfo();
        }
        return forumCategories;
    }

    public void loadForumCategoryInfo(){


        OkHttpClient client = networkUtils.getPreferredClientWithCookieJarByUser(this.getApplication(),userBriefInfo);
        Retrofit retrofit = networkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<BBSIndexResult> bbsIndexResultCall = service.indexResult();
        isLoading.postValue(true);

        bbsIndexResultCall.enqueue(new Callback<BBSIndexResult>() {
            @Override
            public void onResponse(Call<BBSIndexResult> call, Response<BBSIndexResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    BBSIndexResult indexResult = response.body();
                    bbsIndexResultMutableLiveData.postValue(indexResult);
                    if(indexResult.forumVariables !=null){
                        forumUserBriefInfo serverReturnedUser = indexResult.forumVariables.getUserBriefInfo();
                        userBriefInfoMutableLiveData.postValue(serverReturnedUser);
                        errorMessageMutableLiveData.postValue(null);
                        // prepare to render index page
                        List<BBSIndexResult.ForumCategory> categoryList = indexResult.forumVariables.forumCategoryList;
                        forumCategories.postValue(categoryList);


                    }
                    else {
                        if(indexResult.message !=null){
                            errorMessageMutableLiveData.postValue(indexResult.message.toErrorMessage());

                        }
                        else if(indexResult.error.length()!=0){
                            errorMessageMutableLiveData.postValue(new ErrorMessage(
                                    getApplication().getString(R.string.discuz_api_error),
                                    indexResult.error
                            ));

                        }
                        else {
                            errorMessageMutableLiveData.postValue(new ErrorMessage(
                                    getApplication().getString(R.string.empty_result),
                                    getApplication().getString(R.string.discuz_network_result_null)
                            ));
                        }
                    }
                }
                else {
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_result_null,response.message())));
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<BBSIndexResult> call, Throwable t) {
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<String> getText() {
        return mText;
    }
}