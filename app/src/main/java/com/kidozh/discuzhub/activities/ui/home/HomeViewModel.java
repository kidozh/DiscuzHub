package com.kidozh.discuzhub.activities.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.DiscuzIndexResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;



import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class HomeViewModel extends AndroidViewModel {
    private final static String TAG = HomeViewModel.class.getSimpleName();

    private MutableLiveData<String> mText;
    private MutableLiveData<List<DiscuzIndexResult.ForumCategory>> forumCategories;
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);
    public MutableLiveData<forumUserBriefInfo> userBriefInfoMutableLiveData;
    public MutableLiveData<Boolean> isLoading;
    public MutableLiveData<DiscuzIndexResult> bbsIndexResultMutableLiveData;

    Discuz bbsInfo;
    forumUserBriefInfo userBriefInfo;


    public HomeViewModel(Application application) {
        super(application);
        
        
        isLoading = new MutableLiveData<>(false);
        bbsIndexResultMutableLiveData = new MutableLiveData<>(null);
        userBriefInfoMutableLiveData =  new MutableLiveData<>(null);

    }

    public void setBBSInfo(@NonNull Discuz bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        userBriefInfoMutableLiveData = new MutableLiveData<>(userBriefInfo);

    }

    public LiveData<List<DiscuzIndexResult.ForumCategory>> getForumCategoryInfo(){
        if(forumCategories == null){
            forumCategories = new MutableLiveData<List<DiscuzIndexResult.ForumCategory>>();
            loadForumCategoryInfo();
        }
        return forumCategories;
    }

    public void loadForumCategoryInfo(){
        if(!NetworkUtils.isOnline(getApplication())){
            isLoading.postValue(false);
            errorMessageMutableLiveData.postValue(NetworkUtils.getOfflineErrorMessage(getApplication()));
            return;
        }


        OkHttpClient client = NetworkUtils.getPreferredClientWithCookieJarByUser(this.getApplication(),userBriefInfo);
        Retrofit retrofit = NetworkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<DiscuzIndexResult> bbsIndexResultCall = service.indexResult();
        isLoading.postValue(true);

        bbsIndexResultCall.enqueue(new Callback<DiscuzIndexResult>() {
            @Override
            public void onResponse(Call<DiscuzIndexResult> call, Response<DiscuzIndexResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    DiscuzIndexResult indexResult = response.body();
                    bbsIndexResultMutableLiveData.postValue(indexResult);
                    if(indexResult.forumVariables !=null){
                        forumUserBriefInfo serverReturnedUser = indexResult.forumVariables.getUserBriefInfo();
                        userBriefInfoMutableLiveData.postValue(serverReturnedUser);
                        errorMessageMutableLiveData.postValue(null);
                        // prepare to render index page
                        List<DiscuzIndexResult.ForumCategory> categoryList = indexResult.forumVariables.forumCategoryList;
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
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<DiscuzIndexResult> call, Throwable t) {
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