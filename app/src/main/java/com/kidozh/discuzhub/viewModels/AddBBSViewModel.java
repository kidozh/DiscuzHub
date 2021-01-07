package com.kidozh.discuzhub.viewModels;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.AddIntroActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.results.AddCheckResult;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddBBSViewModel extends AndroidViewModel {
    private final static String TAG = AddBBSViewModel.class.getSimpleName();

    public MutableLiveData<String> currentURLLiveData, errorTextLiveData;
    public @NonNull MutableLiveData<Boolean>  isLoadingLiveData, autoVerifyURLLiveData;
    public MutableLiveData<bbsInformation> verifiedBBS = new MutableLiveData<>(null);

    public AddBBSViewModel(@NonNull Application application) {
        super(application);
        currentURLLiveData = new MutableLiveData<>("");
        autoVerifyURLLiveData = new MutableLiveData<>(true);
        isLoadingLiveData = new MutableLiveData<>(false);
        errorTextLiveData = new MutableLiveData<>("");
    }

    public void verifyURL(){
        isLoadingLiveData.postValue(true);
        String base_url = currentURLLiveData.getValue();
        URLUtils.setBaseUrl(base_url);
        String query_url = URLUtils.getBBSForumInformationUrl();
        OkHttpClient client = NetworkUtils.getPreferredClient(getApplication());
        Request request;
        try{
            URL url = new URL(query_url);
//            if(url.getAuthority()!=null && url.getHost()!=null){
//                throw new Exception();
//            }
            request = new Request.Builder().url(query_url).build();
        }
        catch (Exception e){
            isLoadingLiveData.postValue(false);
            errorTextLiveData.postValue(getApplication().getString(R.string.bbs_base_url_invalid));
            e.printStackTrace();
            return;
        }

        Call call = client.newCall(request);
        Log.d(TAG,"Query check URL "+query_url);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                isLoadingLiveData.postValue(false);
                e.printStackTrace();
                errorTextLiveData.postValue(getApplication().getString(R.string.network_failed));

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                isLoadingLiveData.postValue(false);
                String s;
                if(response.isSuccessful() && response.body()!=null){
                    s = response.body().string();
                    Log.d(TAG,"check response " +s);
                    AddCheckResult checkResult = bbsParseUtils.parseCheckInfoResult(s);
                    if(checkResult!=null){
                        bbsInformation bbsInfo = checkResult.toBBSInformation(base_url);
                        verifiedBBS.postValue(bbsInfo);
                        //new AddIntroActivity.addNewForumInformationTask(bbsInfo,activity).execute();
                    }
                    else {
                        errorTextLiveData.postValue(getApplication().getString(R.string.parse_failed));
                    }
                }
                else {
                    errorTextLiveData.postValue(getApplication().getString(R.string.parse_failed));
                }
            }
        });
    }
}
