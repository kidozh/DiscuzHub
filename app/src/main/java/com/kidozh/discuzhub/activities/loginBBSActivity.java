package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;

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
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class loginBBSActivity extends AppCompatActivity {

    private String TAG = loginBBSActivity.class.getSimpleName();

    bbsInformation curBBS;
    forumUserBriefInfo curUser;

    @BindView(R.id.login_bbs_avatar)
    ImageView bbsAvatar;
    @BindView(R.id.login_bbs_title)
    TextView bbsTitle;
    @BindView(R.id.login_bbs_url)
    TextView bbsBaseUrl;
    @BindView(R.id.login_bbs_security_question_spinner)
    Spinner bbsSecurityQuestionSpinner;
    @BindView(R.id.login_bbs_security_answer_editText)
    EditText bbsSecurityAnswerEditText;
    @BindView(R.id.login_bbs_login_button)
    Button bbsLoginBtn;
    @BindView(R.id.login_bbs_login_in_web_button)
    Button bbsLoginInWebBtn;
    @BindView(R.id.login_bbs_notice)
    TextView bbsUnsecureNoticeTextview;
    @BindView(R.id.login_bbs_account_textInputEditText)
    TextInputEditText bbsAccountInputEditText;
    @BindView(R.id.login_bbs_password_textInputEditText)
    TextInputEditText bbsPasswordInputEditText;
    @BindView(R.id.login_bbs_account_textInputLayout)
    TextInputLayout bbsAccountTextInputLayout;
    @BindView(R.id.login_bbs_password_textInputLayout)
    TextInputLayout bbsPasswordTextInputLayout;
    private String formhash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_bbs);
        ButterKnife.bind(this);

        configureData();
        configureActionBar();
        setInformation();
        configureEditText();
        configureLoginBtn();

    }

    void setInformation(){
        bbsTitle.setText(curBBS.site_name);
        bbsBaseUrl.setText(curBBS.base_url);
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(this,curBBS.useSafeClient));
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(this)
                .load(bbsURLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(bbsAvatar);
        bbsSecurityQuestionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    bbsSecurityAnswerEditText.setVisibility(View.GONE);
                }
                else {
                    bbsSecurityAnswerEditText.setVisibility(View.VISIBLE);
                    bbsSecurityAnswerEditText.setHint(bbsSecurityQuestionSpinner.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bbsSecurityAnswerEditText.setVisibility(View.GONE);
            }
        });
        

    }

    void configureEditText(){
        bbsAccountInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bbsAccountTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bbsPasswordInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bbsPasswordTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void configureLoginBtn(){
        bbsLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bbsAccountInputEditText.getText()!=null && bbsPasswordInputEditText.getText()!=null){
                    String account = bbsAccountInputEditText.getText().toString();
                    String password = bbsPasswordInputEditText.getText().toString();
                    if(password.length()==0 || account.length() == 0){
                        if(account.length() == 0){
                            bbsPasswordTextInputLayout.setErrorEnabled(true);
                            bbsAccountTextInputLayout.setError(getString(R.string.field_required));
                            //bbsAccountInputEditText.setError();
                        }
                        if(password.length() == 0){
                            bbsPasswordTextInputLayout.setErrorEnabled(true);
                            bbsPasswordTextInputLayout.setError(getString(R.string.field_required));
                            //bbsPasswordInputEditText.setError();
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

        bbsLoginInWebBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),loginByWebViewActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,curBBS);
                startActivity(intent);
            }
        });
    }

    void sendLoginRequest(){


        String account = bbsAccountInputEditText.getText().toString();
        String password = bbsPasswordInputEditText.getText().toString();
        forumUserBriefInfo userBriefInfo = new forumUserBriefInfo("","","","","",50,"");
        Log.d(TAG,"Send user id "+userBriefInfo.getId());
        networkUtils.clearUserCookieInfo(getApplicationContext(),userBriefInfo);
        //OkHttpClient client = networkUtils.getPreferredClientWithCookieJarByUser(getApplicationContext(),userBriefInfo);
        OkHttpClient client = networkUtils.getPreferredClientWithCookieJar(getApplicationContext());
        // exact login url
        String loginUrl = bbsURLUtils.getLoginUrl();


        FormBody formBody = new FormBody.Builder()
                .add("fastloginfield", "username")
                .add("cookietime", "2592000")
                .add("username", account)
                .add("password", password)
                .add("questionid",String.valueOf(bbsSecurityQuestionSpinner.getSelectedItemPosition()))
                .add("answer",bbsSecurityAnswerEditText.getText().toString())
                .add("quickforward", "yes")
                .add("handlekey", "1s")
                .build();

        Log.d(TAG,"send login url "+loginUrl);
        Request request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // showing error information
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body()!=null){
                    String res = response.body().string();
                    // fetch the api URL
                    Log.d(TAG,"get result json "+res);
                    String loginApiUrl = bbsURLUtils.getLoginApiUrl();
                    Request request = new Request.Builder()
                            .url(loginApiUrl)
                            .build();
                    // secondary verification
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(getApplicationContext(),getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                                    // showing error information
                                }
                            });

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.body()!=null){
                                String jsonString = response.body().string();
                                // parse it
                                Log.d(TAG,"get login API json "+jsonString);
                                forumUserBriefInfo parsedUserInfo = bbsParseUtils.parseLoginBreifUserInfo(jsonString);

                                if(parsedUserInfo!=null){
                                    Log.d(TAG,"Parse user info "+parsedUserInfo.uid+ " "+parsedUserInfo.getId());
                                    // save it to database
                                    parsedUserInfo.belongedBBSID = curBBS.getId();
                                    new saveUserToDatabaseAsyncTask(parsedUserInfo,client).execute();


                                }
                                else {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.error(getApplicationContext(),getString(R.string.bbs_login_user_null),Toast.LENGTH_SHORT).show();
                                            // showing error information
                                        }
                                    });
                                }
                            }

                        }
                    });


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
            insertedId = forumUserBriefInfoDatabase.getDatabase(getApplicationContext())
                    .getforumUserBriefInfoDao().insert(userBriefInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context context = getApplicationContext();
            Toasty.success(context,
                    String.format(context.getString(R.string.save_user_to_bbs_successfully_template),userBriefInfo.username,curBBS.site_name),
                    Toast.LENGTH_SHORT
            ).show();
            Log.d(TAG,"save user to database id: "+userBriefInfo.getId()+"  "+insertedId);
            userBriefInfo.setId((int) insertedId);
            // transiting data
            networkUtils.copySharedPrefence(
                    context.getSharedPreferences("CookiePersistence",Context.MODE_PRIVATE),
                    context.getSharedPreferences(networkUtils.getSharedPreferenceNameByUser(userBriefInfo),Context.MODE_PRIVATE)
            );

            finish();
        }
    }

    void configureActionBar(){
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            if(curBBS.isSecureClient()){
                bbsUnsecureNoticeTextview.setVisibility(View.GONE);
            }
            else {
                bbsUnsecureNoticeTextview.setVisibility(View.VISIBLE);
            }
        }
    }

    void configureData(){
        Intent intent = getIntent();
        curBBS = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        curUser = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(curBBS == null){
            finish();
        }
        else {
            Log.d(TAG,"get bbs name "+curBBS.site_name);
            bbsURLUtils.setBBS(curBBS);
            //bbsURLUtils.setBaseUrl(curBBS.base_url);
        }
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(R.string.bbs_login);
            getSupportActionBar().setSubtitle(curBBS.site_name);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finish();
            return false;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
