package com.kidozh.discuzhub.activities;

import androidx.annotation.NonNull;
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

    @NonNull AddBBSViewModel viewModel;
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
        viewModel.verifiedBBS.observe(this,bbsInformation -> {
            if(bbsInformation !=null){
                new Thread(()->{
                    BBSInformationDatabase
                            .getInstance(this)
                            .getForumInformationDao().insert(bbsInformation);
                }).start();
                Toasty.success(this,
                        getString(R.string.add_a_bbs_successfully,bbsInformation.site_name),
                        Toast.LENGTH_SHORT).show();
                finishAfterTransition();
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
            suggestURLInfoList.add(new SuggestURLInfo("https://bbs.qzzn.com",getString(R.string.bbs_url_example_qzzn),true));
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
        binding.autoAddBbs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.autoVerifyURLLiveData.postValue(isChecked);
        });

    }

    private void configureContinueBtn(){
        binding.bbsAddIntroContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlString = binding.bbsAddIntroUrlEdittext.getText().toString();
                try{
                    URL url = new URL(urlString);
                    viewModel.verifyURL();
                }
                catch (MalformedURLException e){
                    Toasty.warning(getApplication(),getString(R.string.add_bbs_url_failed, urlString),Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onClickSuggestion(SuggestURLInfo suggestURLInfo) {
        VibrateUtils.vibrateForClick(this);
        // lazy load
        if(!binding.bbsAddIntroUrlEdittext.getText().toString().equals(suggestURLInfo.url)){
            binding.bbsAddIntroUrlEdittext.setText(suggestURLInfo.url);
        }

    }

    boolean hasSubmitAutoVerify = false;

    @Override
    public void onURLVerified(String base_url) {

        boolean autoCheck = viewModel.autoVerifyURLLiveData.getValue();
        if(!hasSubmitAutoVerify && autoCheck){
            hasSubmitAutoVerify = true;
            // only implement when auto check is on
            // not triggering url
            viewModel.currentURLLiveData.postValue(base_url);
            viewModel.verifyURL();
        }
    }
}
