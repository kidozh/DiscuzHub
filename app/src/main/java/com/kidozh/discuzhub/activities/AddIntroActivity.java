package com.kidozh.discuzhub.activities;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UrlSuggestionAdapter;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.databinding.ActivityBbsAddIntroBinding;
import com.kidozh.discuzhub.entities.SuggestURLInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.results.AddCheckResult;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.viewModels.AddBBSViewModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddIntroActivity extends BaseStatusActivity
        implements UrlSuggestionAdapter.OnClickSuggestionListener {
    private static final String TAG = AddIntroActivity.class.getSimpleName();


    UrlSuggestionAdapter adapter;

    AddBBSViewModel viewModel;
    ActivityBbsAddIntroBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBbsAddIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(AddBBSViewModel.class);
        configureRecyclerview();
        bindViewModel();
        configureUrlEditText();
        configureContinueBtn();
        configureAddGuide();
    }

    private void configureAddGuide(){
        binding.bbsAddGuide.setPaintFlags(binding.bbsAddGuide.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.bbsAddGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("https://discuzhub.kidozh.com/add-a-bbs-guide/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void configureRecyclerview(){
        adapter = new UrlSuggestionAdapter();
        binding.bbsAddIntroRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.bbsAddIntroRecyclerview.setHasFixedSize(true);
        binding.bbsAddIntroRecyclerview.setAdapter(AnimationUtils.INSTANCE.getAnimatedAdapter(this,adapter));
        binding.bbsAddIntroRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(this));
        // add examples
        List<SuggestURLInfo> suggestURLInfoList = new ArrayList<>();
        suggestURLInfoList.add(new SuggestURLInfo("https://bbs.nwpu.edu.cn",getString(R.string.bbs_url_example_npubbs),true));
        suggestURLInfoList.add(new SuggestURLInfo("https://bbs.comsenz-service.com",getString(R.string.bbs_url_example_discuz_support),true));
        suggestURLInfoList.add(new SuggestURLInfo("https://www.mcbbs.net",getString(R.string.bbs_url_example_mcbbs),true));
        suggestURLInfoList.add(new SuggestURLInfo("https://keylol.com",getString(R.string.bbs_url_example_keylol),true));
        suggestURLInfoList.add(new SuggestURLInfo("https://bbs.qzzn.com",getString(R.string.bbs_url_example_qzzn),true));
        suggestURLInfoList.add(new SuggestURLInfo("https://www.right.com.cn/forum",getString(R.string.bbs_url_example_right_com),true));
        adapter.setSuggestURLInfoList(suggestURLInfoList);
    }

    private void bindViewModel(){
        viewModel.currentURLLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                // need to analyze the URL
                List<SuggestURLInfo> suggestURLInfos = getSuggestedURLList(s);
                adapter.setSuggestURLInfoList(suggestURLInfos);
            }
        });
        viewModel.useSafeClientLiveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                adapter.setUseSafeClient(aBoolean);
            }
        });
        viewModel.isLoadingLiveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.bbsAddIntroProgressBar.setVisibility(View.VISIBLE);
                }
                else {
                    binding.bbsAddIntroProgressBar.setVisibility(View.GONE);
                }
            }
        });
        viewModel.errorTextLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()!=0){
                    Toasty.warning(getApplication(),s, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<SuggestURLInfo> getSuggestedURLList(String urlString){
        List<SuggestURLInfo> suggestURLInfoList = new ArrayList<>();
        if(urlString.equals("")){
            suggestURLInfoList.add(new SuggestURLInfo("https://bbs.nwpu.edu.cn",getString(R.string.bbs_url_example_npubbs),true));
            suggestURLInfoList.add(new SuggestURLInfo("https://bbs.comsenz-service.com",getString(R.string.bbs_url_example_discuz_support),true));
            suggestURLInfoList.add(new SuggestURLInfo("https://www.mcbbs.net",getString(R.string.bbs_url_example_mcbbs),true));
            suggestURLInfoList.add(new SuggestURLInfo("https://keylol.com",getString(R.string.bbs_url_example_keylol),true));
            suggestURLInfoList.add(new SuggestURLInfo("https://www.1point3acres.com/bbs",getString(R.string.bbs_url_example_1point3acres),true));
            suggestURLInfoList.add(new SuggestURLInfo("https://www.right.com.cn/forum",getString(R.string.bbs_url_example_right_com),true));
        }
        else {
            // add url
            try{
                URL url = new URL(urlString);
                String[] splitString = urlString.split("/");
                if(splitString.length >=3){
                    String pathPrefix = splitString[0]+"/"+splitString[1]+"/"+splitString[2];

                    Log.d(TAG,"Path prefix "+pathPrefix);
                    suggestURLInfoList.add(new SuggestURLInfo(pathPrefix,getString(R.string.bbs_url_suggestion_host),false));
                    for(int i=3;i<splitString.length;i++){
                        pathPrefix += "/" + splitString[i];
                        suggestURLInfoList.add(new SuggestURLInfo(pathPrefix,getString(R.string.bbs_url_suggestion_level, i-2),false));
                    }
                }
            }
            catch (Exception e){
                suggestURLInfoList.add(new SuggestURLInfo("https://bbs.nwpu.edu.cn",getString(R.string.bbs_url_example_npubbs),true));
                suggestURLInfoList.add(new SuggestURLInfo("https://bbs.comsenz-service.com",getString(R.string.bbs_url_example_discuz_support),true));
                suggestURLInfoList.add(new SuggestURLInfo("https://www.mcbbs.net",getString(R.string.bbs_url_example_mcbbs),true));
                suggestURLInfoList.add(new SuggestURLInfo("https://keylol.com",getString(R.string.bbs_url_example_keylol),true));
                suggestURLInfoList.add(new SuggestURLInfo("https://www.1point3acres.com/bbs",getString(R.string.bbs_url_example_1point3acres),true));
                suggestURLInfoList.add(new SuggestURLInfo("https://www.right.com.cn/forum",getString(R.string.bbs_url_example_right_com),true));
            }


        }
        return suggestURLInfoList;
    }

    private void configureUrlEditText(){
        binding.bbsAddIntroUrlEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // need to post data
                viewModel.currentURLLiveData.setValue(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.bbsAddIntroHttpsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.useSafeClientLiveData.setValue(isChecked);
            }
        });
    }

    private void configureContinueBtn(){
        binding.bbsAddIntroContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlString = binding.bbsAddIntroUrlEdittext.getText().toString();
                try{
                    URL url = new URL(urlString);
                    queryBBSInfo(urlString);
                }
                catch (MalformedURLException e){
                    Toasty.warning(getApplication(),getString(R.string.add_bbs_url_failed, urlString),Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    private void queryBBSInfo(String base_url){
        boolean useSafeClient = binding.bbsAddIntroHttpsCheckbox.isChecked();
        viewModel.isLoadingLiveData.postValue(true);

        URLUtils.setBaseUrl(base_url);
        String query_url = URLUtils.getBBSForumInformationUrl();
        OkHttpClient client = NetworkUtils.getPreferredClient(this,useSafeClient);
        Request request;
        try{
            URL url = new URL(query_url);
//            if(url.getAuthority()!=null && url.getHost()!=null){
//                throw new Exception();
//            }
            request = new Request.Builder().url(query_url).build();
        }
        catch (Exception e){
            viewModel.isLoadingLiveData.postValue(false);
            viewModel.errorTextLiveData.postValue(getString(R.string.bbs_base_url_invalid));
            e.printStackTrace();
            return;
        }

        Call call = client.newCall(request);
        Log.d(TAG,"Query check URL "+query_url);
        Activity activity = this;
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                viewModel.isLoadingLiveData.postValue(false);
                e.printStackTrace();
                viewModel.errorTextLiveData.postValue(getString(R.string.network_failed));

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                viewModel.isLoadingLiveData.postValue(false);
                String s;
                if(response.isSuccessful() && response.body()!=null){
                    s = response.body().string();
                    Log.d(TAG,"check response " +s);
                    AddCheckResult checkResult = bbsParseUtils.parseCheckInfoResult(s);
                    if(checkResult!=null){
                        bbsInformation bbsInfo = checkResult.toBBSInformation(base_url);
                        new addNewForumInformationTask(bbsInfo,activity).execute();
                        finishAfterTransition();
                    }
                    else {
                        viewModel.errorTextLiveData.postValue(getString(R.string.parse_failed));
                    }
                }
                else {
                    viewModel.errorTextLiveData.postValue(getString(R.string.parse_failed));
                }
            }
        });
    }

    public static class addNewForumInformationTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation forumInfo;
        private Activity activity;
        public addNewForumInformationTask(bbsInformation bbsInformation, Activity activity){
            this.forumInfo = bbsInformation;
            this.activity = activity;
            forumInfo.position = 9999999;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            BBSInformationDatabase
                    .getInstance(activity)
                    .getForumInformationDao().insert(forumInfo);


            Log.d(TAG, "add forum into database"+forumInfo.site_name);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toasty.success(activity,
                    activity.getString(R.string.add_a_bbs_successfully,forumInfo.site_name),
                    Toast.LENGTH_SHORT).show();
            activity.finishAfterTransition();
        }
    }

    @Override
    public void onClickSuggestion(SuggestURLInfo suggestURLInfo) {
        VibrateUtils.vibrateForClick(this);
        // lazy load
        if(!binding.bbsAddIntroUrlEdittext.getText().toString().equals(suggestURLInfo.url)){
            binding.bbsAddIntroUrlEdittext.setText(suggestURLInfo.url);
        }

    }
}
