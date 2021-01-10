package com.kidozh.discuzhub.activities;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.databinding.ActivityLoginBbsBinding;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.LoginResult;
import com.kidozh.discuzhub.results.MessageResult;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.viewModels.LoginViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends BaseStatusActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    LoginViewModel viewModel;

    private OkHttpClient client;
    ActivityLoginBbsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBbsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        configureData();
        configureActionBar();
        setInformation();
        configureEditText();
        configureLoginBtn();
        bindViewModel();

    }

    void setInformation(){
        binding.loginBbsTitle.setText(bbsInfo.site_name);
        if(userBriefInfo == null){
            binding.loginBbsUrl.setText(bbsInfo.base_url);
        }
        else {
            binding.loginBbsUrl.setText(getString(R.string.user_relogin,userBriefInfo.username));
            binding.loginBbsAccountTextInputEditText.setText(userBriefInfo.username);
        }

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(this)
                .load(URLUtils.getBBSLogoUrl())
                .error(R.drawable.ic_baseline_public_24)
                .placeholder(R.drawable.ic_baseline_public_24)
                .centerInside()
                .into(binding.loginBbsAvatar);
        binding.loginBbsSecurityQuestionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    binding.loginBbsSecurityAnswerEditText.setVisibility(View.GONE);
                }
                else {
                    binding.loginBbsSecurityAnswerEditText.setVisibility(View.VISIBLE);
                    binding.loginBbsSecurityAnswerEditText.setHint(binding.loginBbsSecurityQuestionSpinner.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.loginBbsSecurityAnswerEditText.setVisibility(View.GONE);
            }
        });
        

    }

    void configureEditText(){
        binding.loginBbsCaptchaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.loadSecureInfo();
                VibrateUtils.vibrateForClick(getApplication());
            }
        });

        binding.loginBbsAccountTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.loginBbsAccountTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.loginBbsPasswordTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.loginBbsPasswordTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean needCaptcha(){
        if(viewModel == null
                || viewModel.getSecureInfoResultMutableLiveData().getValue()==null
                || viewModel.getSecureInfoResultMutableLiveData().getValue().secureVariables==null){
            return false;
        }
        else {
            return true;
        }
    }

    void bindViewModel(){

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if(errorMessage != null){
                Toasty.error(this,
                        getString(R.string.discuz_api_message_template,errorMessage.key,errorMessage.content),
                        Toast.LENGTH_LONG).show();
            }

        });

        viewModel.getSecureInfoResultMutableLiveData().observe(this,secureInfoResult -> {
            if(secureInfoResult != null){
                if(secureInfoResult.secureVariables == null){
                    binding.loginBbsCaptchaInputLayout.setVisibility(View.GONE);
                    binding.loginBbsCaptchaImageView.setVisibility(View.GONE);
                }
                else{
                    // need further query
                    binding.loginBbsCaptchaInputLayout.setVisibility(View.VISIBLE);
                    binding.loginBbsCaptchaImageView.setVisibility(View.VISIBLE);
                    binding.loginBbsCaptchaImageView.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_captcha_placeholder_24px));
                    String captchaURL = secureInfoResult.secureVariables.secCodeURL;
                    String captchaImageURL = URLUtils.getSecCodeImageURL(secureInfoResult.secureVariables.secHash);
                    Request captchaRequest = new Request.Builder()
                            .url(captchaURL)
                            .build();
                    client.newCall(captchaRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                // get the session
                                binding.loginBbsCaptchaImageView.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
                                        Glide.get(getApplication()).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

                                        // forbid cache captcha
                                        RequestOptions options = new RequestOptions()
                                                .fitCenter()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .placeholder(R.drawable.ic_captcha_placeholder_24px)
                                                .error(R.drawable.ic_post_status_warned_24px);
                                        GlideUrl pictureGlideURL = new GlideUrl(captchaImageURL,
                                                new LazyHeaders.Builder()
                                                        .addHeader("Referer",captchaURL)
                                                        .build()
                                        );

                                        Glide.with(getApplication())
                                                .load(pictureGlideURL)
                                                .apply(options)
                                                .into(binding.loginBbsCaptchaImageView);
                                    }
                                });
                            }
                        }
                    });

                }
            }
            else{
                // not get login parameter

            }
        });

        viewModel.getLoginResultMutableLiveData().observe(this, loginResult -> {
            if(loginResult!=null){
                MessageResult loginMessage = loginResult.message;
                if(loginMessage !=null){
                    String key = loginMessage.key;
                    if(key.equals("login_succeed")){
                        forumUserBriefInfo user = loginResult.variables.getUserBriefInfo();
                        user.belongedBBSID = bbsInfo.getId();
                        if(userBriefInfo !=null){
                            // relogin user
                            user.setId(userBriefInfo.getId());
                        }
                        Toasty.success(this,
                                getString(R.string.discuz_api_message_template,loginMessage.key,loginMessage.content),
                                Toast.LENGTH_LONG).show();
                        new saveUserToDatabaseAsyncTask(user,client,bbsInfo.base_url).execute();

                    }
                    else{
                        if(key.equals("login_seccheck2")){
                            // need captcha
                            viewModel.loadSecureInfo();
                        }
                        Toasty.error(this,
                                getString(R.string.discuz_api_message_template,loginMessage.key,loginMessage.content),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    void configureLoginBtn(){
        binding.loginBbsLoginButton.setOnClickListener(v -> {
            if(binding.loginBbsAccountTextInputEditText.getText()!=null && binding.loginBbsPasswordTextInputEditText.getText()!=null && binding.loginBbsCaptchaEditText.getText()!=null){
                String account = binding.loginBbsAccountTextInputEditText.getText().toString();
                String password = binding.loginBbsPasswordTextInputEditText.getText().toString();
                String captchaText = binding.loginBbsCaptchaEditText.getText().toString();



                String secureHash = null;

                SecureInfoResult secureInfoResult = viewModel.getSecureInfoResultMutableLiveData().getValue();
                if(secureInfoResult!=null && secureInfoResult.secureVariables!=null){
                    secureHash = secureInfoResult.secureVariables.secHash;
                }
                if(needCaptcha() && captchaText.length() == 0){
                    binding.loginBbsCaptchaInputLayout.setError(getString(R.string.field_required));
                    binding.loginBbsCaptchaInputLayout.setErrorEnabled(true);
                    return;
                }
                else {
                    binding.loginBbsCaptchaInputLayout.setErrorEnabled(false);
                }


                if(password.length()==0 || account.length() == 0){
                    if(account.length() == 0){
                        binding.loginBbsPasswordTextInputLayout.setErrorEnabled(true);
                        binding.loginBbsAccountTextInputLayout.setError(getString(R.string.field_required));
                        //binding.loginBbsAccountTextInputEditText.setError();
                    }
                    if(password.length() == 0){
                        binding.loginBbsPasswordTextInputLayout.setErrorEnabled(true);
                        binding.loginBbsPasswordTextInputLayout.setError(getString(R.string.field_required));
                        //binding.loginBbsPasswordTextInputEditText.setError();
                    }
                    Toasty.warning(getApplicationContext(),getString(R.string.bbs_login_account_password_required),Toast.LENGTH_SHORT).show();
                }
                else {
                    viewModel.login(
                            client,
                            account,password,
                            binding.loginBbsSecurityQuestionSpinner.getSelectedItemPosition(),
                            binding.loginBbsSecurityAnswerEditText.getText().toString(),
                            secureHash,
                            binding.loginBbsCaptchaEditText.getText().toString()

                    );
                }
            }
            else {

                Toasty.warning(getApplicationContext(),getString(R.string.bbs_login_account_password_required),Toast.LENGTH_SHORT).show();
            }
        });


        binding.loginBbsLoginInWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewLoginActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                startActivity(intent);
            }
        });
    }



    private class saveUserToDatabaseAsyncTask extends AsyncTask<Void,Void,Void>{
        forumUserBriefInfo userBriefInfo;
        OkHttpClient client;
        long insertedId;
        HttpUrl httpUrl;
        saveUserToDatabaseAsyncTask(forumUserBriefInfo userBriefInfo, OkHttpClient client, String httpURL){
            this.userBriefInfo = userBriefInfo;
            this.client = client;
            this.httpUrl = HttpUrl.parse(httpURL);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            insertedId = forumUserBriefInfoDatabase.getInstance(getApplicationContext())
                    .getforumUserBriefInfoDao().insert(userBriefInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context context = getApplicationContext();
            Log.d(TAG,"save user to database id: "+userBriefInfo.getId()+"  "+insertedId);
            userBriefInfo.setId((int) insertedId);

            OkHttpClient savedClient = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplicationContext(),userBriefInfo);
            List<Cookie> cookies = client.cookieJar().loadForRequest(httpUrl);
            Log.d(TAG,"Http url "+httpUrl.toString()+" cookie list size "+cookies.size());
            savedClient.cookieJar().saveFromResponse(httpUrl,cookies);
            // manually set the cookie to shared preference
            SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(context.getSharedPreferences(NetworkUtils.getSharedPreferenceNameByUser(userBriefInfo),Context.MODE_PRIVATE));
            sharedPrefsCookiePersistor.saveAll(savedClient.cookieJar().loadForRequest(httpUrl));
            Log.d(TAG,"Http url "+httpUrl.toString()+" saved cookie list size "+savedClient.cookieJar().loadForRequest(httpUrl).size());

            finishAfterTransition();
        }
    }

    void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            if(bbsInfo.isSecureClient()){
                binding.loginBbsNotice.setVisibility(View.GONE);
            }
            else {
                binding.loginBbsNotice.setVisibility(View.VISIBLE);
            }
        }
    }

    void configureData(){
        Intent intent = getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJar(getApplicationContext());
        viewModel.setInfo(bbsInfo,userBriefInfo,client);


        if(bbsInfo == null){
            finishAfterTransition();
        }
        else {
            Log.d(TAG,"get bbs name "+bbsInfo.site_name);
            URLUtils.setBBS(bbsInfo);
            //bbsURLUtils.setBaseUrl(bbsInfo.base_url);
        }
        if(getSupportActionBar()!=null){
            if(userBriefInfo == null){
                getSupportActionBar().setTitle(R.string.bbs_login);
            }
            else {
                getSupportActionBar().setTitle(getString(R.string.user_relogin,userBriefInfo.username));
            }

            getSupportActionBar().setSubtitle(bbsInfo.site_name);
            // clear it first
            getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE).edit().clear().apply();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finishAfterTransition();
            return false;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
