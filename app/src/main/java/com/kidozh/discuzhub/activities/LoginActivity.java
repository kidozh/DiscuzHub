package com.kidozh.discuzhub.activities;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.databinding.ActivityLoginBbsBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.LoginResult;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.viewModels.LoginViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
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
        }

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        if(userBriefInfo == null){

        }
        Glide.with(this)
                .load(URLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
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
                viewModel.getSecureInfo();
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
        viewModel.errorString.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s!=null && s.length()!=0){
                    Toasty.error(getApplication(),s,Toast.LENGTH_LONG).show();
                    VibrateUtils.vibrateForError(getApplication());
                }
            }
        });
        viewModel.getSecureInfoResultMutableLiveData().observe(this, new Observer<SecureInfoResult>() {
            @Override
            public void onChanged(SecureInfoResult secureInfoResult) {
                if(secureInfoResult != null){

                    if(secureInfoResult.secureVariables == null){
                        binding.loginBbsCaptchaInputLayout.setVisibility(View.GONE);
                        binding.loginBbsCaptchaImageView.setVisibility(View.GONE);
                    }
                    else {
                        binding.loginBbsCaptchaInputLayout.setVisibility(View.VISIBLE);
                        binding.loginBbsCaptchaImageView.setVisibility(View.VISIBLE);
                        binding.loginBbsCaptchaImageView.setImageDrawable(getDrawable(R.drawable.ic_captcha_placeholder_24px));
                        // need a captcha
                        String captchaURL = secureInfoResult.secureVariables.secCodeURL;
                        String captchaImageURL = URLUtils.getSecCodeImageURL(secureInfoResult.secureVariables.secHash);
                        // load it
                        Request captchaRequest = new Request.Builder()
                                .url(captchaURL)
                                .build();
                        // get first
                        client.newCall(captchaRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
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
                else {
                    binding.loginBbsCaptchaInputLayout.setVisibility(View.GONE);
                    binding.loginBbsCaptchaImageView.setVisibility(View.GONE);
                }
            }
        });
    }

    void configureLoginBtn(){
        binding.loginBbsLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.loginBbsAccountTextInputEditText.getText()!=null && binding.loginBbsPasswordTextInputEditText.getText()!=null && binding.loginBbsCaptchaEditText.getText()!=null){
                    String account = binding.loginBbsAccountTextInputEditText.getText().toString();
                    String password = binding.loginBbsPasswordTextInputEditText.getText().toString();
                    String captchaText = binding.loginBbsCaptchaEditText.getText().toString();
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
                        sendLoginRequest();
                    }
                }
                else {

                    Toasty.warning(getApplicationContext(),getString(R.string.bbs_login_account_password_required),Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.loginBbsLoginInWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewLoginActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                startActivity(intent);
            }
        });
    }

    void sendLoginRequest(){
        viewModel.error.postValue(false);
        if(viewModel.getSecureInfoResultMutableLiveData().getValue() == null){
            return;
        }
        if(binding.loginBbsAccountTextInputEditText.getText() == null
                || binding.loginBbsPasswordTextInputEditText.getText() == null){
            return;
        }
        String account = binding.loginBbsAccountTextInputEditText.getText().toString();
        String password = binding.loginBbsPasswordTextInputEditText.getText().toString();
        String captcha = binding.loginBbsCaptchaEditText.getText().toString();
        forumUserBriefInfo savedUserBriefInfo = new forumUserBriefInfo("","","","","",50,"");
        //Log.d(TAG,"Send user id "+userBriefInfo.getId());
        if(userBriefInfo !=null){
            savedUserBriefInfo.setId(userBriefInfo.getId());
        }
        NetworkUtils.clearUserCookieInfo(getApplicationContext(),savedUserBriefInfo);


        // exact login url
        // need formhash
        SecureInfoResult secureInfoResult = viewModel.getSecureInfoResultMutableLiveData().getValue();

        String loginUrl = URLUtils.getLoginUrl();


        FormBody.Builder formBodyBuilder = new FormBody.Builder()
                .add("loginfield", "username")
                .add("cookietime", "2592000")

                .add("questionid",String.valueOf(binding.loginBbsSecurityQuestionSpinner.getSelectedItemPosition()))

                .add("quickforward", "yes")
                .add("handlekey", "1s")

                .add("referer",bbsInfo.base_url);
        String answer = binding.loginBbsSecurityAnswerEditText.getText().toString();
        switch (getCharsetType()){
            case CHARSET_GBK:{
                try {
                    formBodyBuilder.addEncoded("answer", URLEncoder.encode(answer,"GBK"))
                            .add("username", URLEncoder.encode(account,"GBK"))
                            .add("password", URLEncoder.encode(password,"GBK"))
                    ;
                    break;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            case CHARSET_BIG5:{
                try {
                    formBodyBuilder.addEncoded("answer", URLEncoder.encode(answer,"BIG5"))
                            .add("username", URLEncoder.encode(account,"BIG5"))
                            .add("password", URLEncoder.encode(password,"BIG5"))
                    ;
                    break;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            default:{
                formBodyBuilder.add("answer",answer)
                        .add("username", account)
                        .add("password", password);
            }
        }

        if(needCaptcha()){
            Log.d(TAG,"Formhash "+secureInfoResult.secureVariables.formHash);
            formBodyBuilder
                    .add("seccodehash",secureInfoResult.secureVariables.secHash)
                    .add("seccodemodid", "member::logging")
                    //.add("formhash",secureInfoResult.secureVariables.formHash)
                    ;
            switch (getCharsetType()){
                case CHARSET_GBK:{
                    try {
                        formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha,"GBK"))

                        ;
                        break;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                case CHARSET_BIG5:{
                    try {
                        formBodyBuilder.addEncoded("seccodeverify", URLEncoder.encode(captcha,"BIG5"))

                        ;
                        break;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                default:{
                    formBodyBuilder.add("seccodeverify", captcha);
                }
            }
        }

        FormBody formBody = formBodyBuilder
                .build();

        Log.d(TAG,"send login url "+loginUrl+" form post "
                +account + " "+password+" "
                +" verify "+captcha);
        Request request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build();

        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // showing error information
                        Toasty.error(getApplicationContext(),getString(R.string.network_failed),Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body()!=null){

                    String res = response.body().string();
                    // fetch the api URL
                    Log.d(TAG,"get result json "+res);
                    ObjectMapper mapper = new ObjectMapper();
                    LoginResult loginResult = null;
                    try{
                        loginResult = mapper.readValue(res, LoginResult.class);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    if(loginResult !=null && loginResult.variables !=null){
                        forumUserBriefInfo parsedUserInfo = loginResult.variables.getUserBriefInfo();
                        if(parsedUserInfo !=null
                                && loginResult.message!=null
                                && loginResult.message.key.equals("login_succeed")){
                            // successful
                            parsedUserInfo.belongedBBSID = bbsInfo.getId();
                            if(userBriefInfo !=null){
                                // relogin user
                                parsedUserInfo.setId(userBriefInfo.getId());
                            }
                            new saveUserToDatabaseAsyncTask(parsedUserInfo,client).execute();
                        }
                        else {
                            if(loginResult.message!=null){
                                if(loginResult.message.key.equals("login_seccheck2")){
                                    Log.d(TAG,"Need seccode to login ");
                                    viewModel.getSecureInfo();
                                }
                                viewModel.error.postValue(true);
                                viewModel.errorString.postValue(loginResult.message.content);

                            }
                            else {
                                viewModel.error.postValue(true);
                                viewModel.errorString.postValue(getString(R.string.parse_failed));
                            }
                        }
                    }
                    else {
                        if(loginResult !=null && loginResult.message!=null){
                            viewModel.error.postValue(true);
                            viewModel.errorString.postValue(loginResult.message.content);
                        }
                        else {
                            viewModel.error.postValue(true);
                            viewModel.errorString.postValue(getString(R.string.parse_failed));
                        }
                    }

                }
            }
        });
    }

    private class saveUserToDatabaseAsyncTask extends AsyncTask<Void,Void,Void>{
        forumUserBriefInfo userBriefInfo;
        OkHttpClient client;
        long insertedId;
        saveUserToDatabaseAsyncTask(forumUserBriefInfo userBriefInfo, OkHttpClient client){
            this.userBriefInfo = userBriefInfo;
            this.client = client;
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
            Toasty.success(context,
                    String.format(context.getString(R.string.save_user_to_bbs_successfully_template),userBriefInfo.username,bbsInfo.site_name),
                    Toast.LENGTH_SHORT
            ).show();
            Log.d(TAG,"save user to database id: "+userBriefInfo.getId()+"  "+insertedId);
            userBriefInfo.setId((int) insertedId);
            // transiting data
            NetworkUtils.copySharedPrefence(
                    context.getSharedPreferences("CookiePersistence",Context.MODE_PRIVATE),
                    context.getSharedPreferences(NetworkUtils.getSharedPreferenceNameByUser(userBriefInfo),Context.MODE_PRIVATE)
            );

            Log.d(TAG, "Transiting data to preference "+ NetworkUtils.getSharedPreferenceNameByUser(userBriefInfo));

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
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        viewModel.setBBSInfo(bbsInfo,userBriefInfo);
        client = NetworkUtils.getPreferredClientWithCookieJar(getApplicationContext());
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
