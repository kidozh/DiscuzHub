package com.kidozh.discuzhub.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.daos.bbsThreadDraftDao;
import com.kidozh.discuzhub.database.bbsThreadDraftDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.EmotionInputHandler;
import com.kidozh.discuzhub.utilities.bbsColorPicker;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsSmileyPicker;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class bbsPostThreadActivity extends AppCompatActivity implements View.OnClickListener{
    private static String TAG = bbsPostThreadActivity.class.getSimpleName();

    @BindView(R.id.bbs_post_thread_subject_editText)
    EditText bbsThreadSubjectEditText;
    @BindView(R.id.bbs_post_thread_message)
    EditText bbsThreadMessageEditText;
    @BindView(R.id.bbs_post_thread_cate_spinner)
    Spinner mCategorySpinner;
    private EmotionInputHandler handler;
    private OkHttpClient client;

    LinearLayout editBar;

    @BindView(R.id.bbs_post_thread_editor_bar)
    View bbsPostThreadEditorBarInclude;
    @BindView(R.id.bbs_post_thread_backup_icon)
    ImageView bbsPostThreadBackupIcon;
    @BindView(R.id.bbs_post_thread_backup_info_textview)
    TextView bbsPostThreadBackupInfo;

    private String fid, forumApiString,forumName, uploadHash, formHash;
    private ProgressDialog uploadDialog;
    private forumUserBriefInfo bbsPersonInfo;

    Uri curOutputFileUri;
    String curOutputFilePath;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    forumInfo forum;
    private bbsColorPicker myColorPicker;
    private bbsSmileyPicker smileyPicker;
    private bbsThreadDraft threadDraft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_post_thread);
        ButterKnife.bind(this);
        configureIntentData();
        configureClient();

        editBar = (LinearLayout) bbsPostThreadEditorBarInclude;


        configureToolbar();
        bbsPersonInfo = bbsParseUtils.parseBreifUserInfo(forumApiString);
        // parse api result
        Map<String,String> threadTypeMapper = bbsParseUtils.parseThreadType(forumApiString);
        if (threadTypeMapper == null){
            mCategorySpinner.setVisibility(View.GONE);
        }
        else {
            configureSpinner(threadTypeMapper);
        }
        formHash = bbsParseUtils.parseFormHash(forumApiString);

        configureEditBar();
        configureInputHandler();
        configureEditTools();
        configureSyncDraft();



    }

    private void configureIntentData(){
        Intent intent = getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        // check if it comes from draft box
        threadDraft = (bbsThreadDraft) intent.getSerializableExtra(bbsConstUtils.PASS_THREAD_DRAFT_KEY);
        bbsURLUtils.setBBS(bbsInfo);
        if(threadDraft!=null){
            bbsThreadSubjectEditText.setText(threadDraft.subject);
            bbsThreadMessageEditText.setText(threadDraft.content);
            if(mCategorySpinner.getSelectedItem()!=null){
                mCategorySpinner.setSelection(Integer.parseInt(threadDraft.typeid));
            }
            forumApiString = threadDraft.apiString;

        }

        fid = intent.getStringExtra("fid");
        forumName = intent.getStringExtra("fid_name");
        if(forumApiString==null){
            forumApiString = intent.getStringExtra("api_result");
        }

    }

    private void configureClient(){
        client = networkUtils.getPreferredClientWithCookieJarByUser(this,userBriefInfo);
    }

    private void configureInputHandler(){
        handler = new EmotionInputHandler(bbsThreadMessageEditText, (enable, s) -> {

        });
    }

    private void configureEditBar(){
        for (int i = 0; i < editBar.getChildCount(); i++) {
            View c = editBar.getChildAt(i);
            if (c instanceof ImageView) {
                c.setOnClickListener(this);
            }
        }
        new getUploadHashCodeTask(this).execute();
        myColorPicker = new bbsColorPicker(this);
        myColorPicker.setListener((pos, v, color) -> handleInsert("[color=" + color + "][/color]"));
    }

    private void configureSpinner(Map<String,String> threadTypeMapper){
        List<String> threadTypeNames = new ArrayList<String>();
        Iterator keys = threadTypeMapper.entrySet().iterator();
        while (keys.hasNext()){
            Map.Entry entry = (Map.Entry) keys.next();

            String key = (String) entry.getKey();

            String value = (String)entry.getValue();
            threadTypeNames.add(value);

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,threadTypeNames);
        mCategorySpinner.setAdapter(arrayAdapter);
        //mCategorySpinner.setOnItemClickListener(this);
    }

    private boolean checkPostInput() {
        if (false) {
            Toast.makeText(this, "请选择主题分类", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(bbsThreadSubjectEditText.getText().toString().trim())) {
            Toasty.warning(this, getString(R.string.bbs_post_thread_subject_required), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(bbsThreadMessageEditText.getText().toString().trim())) {
            Toasty.warning(this, getString(R.string.bbs_post_thread_message_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleInsert(String s) {
        int start = bbsThreadMessageEditText.getSelectionStart();
        int end = bbsThreadMessageEditText.getSelectionEnd();
        int p = s.indexOf("[/");//相对于要插入的文本光标所在位置

        Editable edit = bbsThreadMessageEditText.getEditableText();//获取EditText的文字

        if (start < 0 || start >= edit.length()) {
            edit.append(s);
        } else if (start != end && start > 0 && start < end && p > 0) {
            edit.insert(start, s.substring(0, p));//插入bbcode标签开始部分
            end = end + p;
            edit.insert(end, s.substring(p));//插入bbcode标签结束部分
            p = end - start;
        } else {
            edit.insert(start, s);//光标所在位置插入文字
        }

        if (p > 0) {
            bbsThreadMessageEditText.setSelection(start + p);
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
//            case R.id.menu:
//                prePost();
//                break;
            case R.id.action_bold:
                handleInsert("[b][/b]");
                break;
            case R.id.action_italic:
                handleInsert("[i][/i]");
                break;
            case R.id.action_quote:
                handleInsert("[quote][/quote]");
                break;
            case R.id.action_color_text:
                myColorPicker.showAsDropDown(view, 0, 10);
                break;
            case R.id.action_emotion:

                ((ImageView) view).setColorFilter(R.color.colorAccent);
                smileyPicker.showAsDropDown(view, 0, 10);
                smileyPicker.setOnDismissListener(() -> ((ImageView) view).setImageResource(R.drawable.ic_edit_emoticon_24dp));
                break;
            case R.id.action_insert_photo:
                if (TextUtils.isEmpty(uploadHash)) {
                    Toasty.error(bbsPostThreadActivity.this, getString(R.string.bbs_post_thread_cannot_upload_picture), Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(getPickImageChooserIntent(), 200);
                }
                break;
//            case R.id.action_backspace:
//                int start = edContent.getSelectionStart();
//                int end = edContent.getSelectionEnd();
//                if (start == 0) {
//                    return;
//                }
//                if ((start == end) && start > 0) {
//                    start = start - 1;
//                }
//                edContent.getText().delete(start, end);
//                break;
//            case R.id.tv_select_type:
//                typeidSpinner.setData(typeiddatas);
//                typeidSpinner.setWidth(view.getWidth());
//                typeidSpinner.showAsDropDown(view, 0, 15);
//                break;
            default:
                break;
        }

    }

    private void configureSyncDraft(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        Boolean autoPostBackup = prefs.getBoolean(getString(R.string.preference_key_auto_post_backup),true);



        // create an initial backup
        if(threadDraft == null){
            if(mCategorySpinner.getSelectedItem()!=null){

                threadDraft = new bbsThreadDraft(bbsThreadSubjectEditText.getText().toString(),
                        bbsThreadMessageEditText.getText().toString(),
                        new Date(),
                        bbsInfo.getId(),
                        fid,
                        forumName,
                        String.valueOf(mCategorySpinner.getSelectedItemPosition()),
                        mCategorySpinner.getSelectedItem().toString(),
                        forumApiString
                );
            }
            else {
                threadDraft = new bbsThreadDraft(bbsThreadSubjectEditText.getText().toString(),
                        bbsThreadMessageEditText.getText().toString(),
                        new Date(),
                        bbsInfo.getId(),
                        fid,
                        forumName,
                        "0",
                        "",
                        forumApiString
                );
            }
        }


        Activity activity = this;


        if(autoPostBackup){
            bbsPostThreadBackupInfo.setText(R.string.bbs_thread_auto_backup_start);
            bbsThreadMessageEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    threadDraft.subject = bbsThreadSubjectEditText.getText().toString();
                    threadDraft.content = bbsThreadMessageEditText.getText().toString();
                    threadDraft.lastUpdateAt = new Date();

                    if(mCategorySpinner.getSelectedItem()!=null){
                        threadDraft.typeid = String.valueOf(mCategorySpinner.getSelectedItemPosition());
                        threadDraft.typeName = mCategorySpinner.getSelectedItem().toString();
                    }

                    // update or add one
                    if(TextUtils.isEmpty(bbsThreadSubjectEditText.getText().toString().trim())
                            && TextUtils.isEmpty(bbsThreadMessageEditText.getText().toString().trim())){

                    }
                    else {
                        new addThreadDraftTask(activity,threadDraft).execute();
                    }


                }
            });
        }
        else {
            bbsPostThreadBackupInfo.setVisibility(View.GONE);
            bbsPostThreadBackupIcon.setVisibility(View.GONE);
        }
    }


    public class addThreadDraftTask extends AsyncTask<Void, Void, Void> {
        private bbsThreadDraft insertThreadDraft;
        private Context context;
        private Boolean saveThenFinish = false;
        public addThreadDraftTask(Context context,bbsThreadDraft threadDraft ){
            this.insertThreadDraft = threadDraft;
            this.context = context;
        }
        public addThreadDraftTask(Context context,bbsThreadDraft threadDraft,Boolean saveThenFinish){
            this.insertThreadDraft = threadDraft;
            this.context = context;
            this.saveThenFinish = saveThenFinish;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            long inserted = bbsThreadDraftDatabase
                    .getInstance(context)
                    .getbbsThreadDraftDao().insert(insertThreadDraft);
            insertThreadDraft.setId( (int) inserted);
            Log.d(TAG, "add forum into database"+insertThreadDraft.subject+insertThreadDraft.getId());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            threadDraft = insertThreadDraft;
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
            bbsPostThreadBackupInfo.setText(getString(R.string.bbs_thread_auto_backup_updated_time_template,
                    df.format(threadDraft.lastUpdateAt)
            ));
            if(saveThenFinish){
                Toasty.success(context,getString(R.string.bbs_thread_auto_backup_updated_time_template,
                        df.format(threadDraft.lastUpdateAt)),Toast.LENGTH_SHORT).show();
                finishAfterTransition();
            }
        }
    }

    private void configureEditTools(){
        myColorPicker = new bbsColorPicker(this);
        smileyPicker = new bbsSmileyPicker(this);
        smileyPicker.setListener((str,a)->{
            String decodeStr = str.replace("/","")
                    .replace("\\","");
            handler.insertSmiley(decodeStr,a);
        });
        Spinner setSize = findViewById(R.id.action_text_size);
        setSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //[size=7][/size]
                if (bbsThreadMessageEditText == null || (bbsThreadMessageEditText.getText().length() <= 0 && i == 0)) {
                    return;
                }
                handleInsert("[size=" + (i + 1) + "][/size]");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        myColorPicker.setListener((pos, v, color) -> handleInsert("[color=" + color + "][/color]"));

        handler = new EmotionInputHandler(bbsThreadMessageEditText, (enable, s) -> {

        });
    }

    private Uri lastFile;

    private Uri getCaptureImageOutputUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalCacheDir();
        Uri outputFileUri = null;
        if (storageDir != null) {
            File image = null;
            try {
                image = File.createTempFile(imageFileName, ".jpg", storageDir);
                curOutputFilePath = image.getAbsolutePath();
                Log.d(TAG, "create file success " + image.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputFileUri = Uri.fromFile(image);
        }
        lastFile = outputFileUri;
        return outputFileUri;
    }

    public Intent getPickImageChooserIntent() {
        // Determine Uri of camera image to save.

        Uri outputFileUri = getCaptureImageOutputUri();
        curOutputFileUri = outputFileUri;
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            // to get raw picture
            if (outputFileUri != null) {
                curOutputFileUri = outputFileUri;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if ("com.android.documentsui.DocumentsActivity".equals(intent.getComponent().getClassName())) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, getString(R.string.bbs_post_thread_choose_a_picture));

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        Log.v(TAG, "REQUEST_CODE:" + requestCode + "result:" + resultCode);
        if (resultCode == Activity.RESULT_OK && requestCode == 200) {
            if (getPickImageResultUri(data) != null) {
                Uri picUri = getPickImageResultUri(data);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "get intent "+ data);
                if(data!=null && data.hasExtra("data")){

                    // bitmap = (Bitmap) data.getExtras().get("data");
                    bitmap = data.getParcelableExtra("data");
                }
                else {
                    Log.d(TAG, "Bitmap data is null");
                    if(curOutputFilePath !=null){
                        try{
                            FileInputStream mFis = new FileInputStream(curOutputFilePath);
                            //通过输入流得到bitmap
                            bitmap = BitmapFactory.decodeStream(mFis);
                            mFis.close();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }




                }

            }
        }

        if (bitmap != null) {
            uploadImage(bitmap);
        }
    }

    private void uploadImage(Bitmap bitmap) {

        new uploadImageTask(this,bitmap).execute();
    }


    private Bitmap returnBitmap = null;

    private class getUploadHashCodeTask extends AsyncTask<Void,Void,String>{

        Request request;
        Context context;
        ImageView uploadImageView;

        getUploadHashCodeTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String checkPostUrl = bbsURLUtils.getCheckPostUrl();
            request = new Request.Builder()
                    .url(checkPostUrl)
                    .build();
            uploadImageView = (ImageView) findViewById(R.id.action_insert_photo);
            uploadImageView.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                Response resp = client.newCall(request).execute();
                if(resp.isSuccessful() && resp.body()!=null){
                    return resp.body().string();
                }
                else {
                    return null;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //Log.e(TAG,e.printStackTrace());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG,"get post hash"+s);
            if(s == null){
                Toasty.warning(context,context.getString(R.string.bbs_post_thread_cannot_upload_picture),Toast.LENGTH_SHORT).show();
            }
            else {
                uploadHash = bbsParseUtils.parseUploadHashString(s);
                Log.d(TAG, "get upload hash "+uploadHash);
                uploadImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class uploadImageTask extends AsyncTask<Bitmap, String, String> {
        Context context;
        Request request;
        Bitmap bitmap;

        uploadImageTask(Context context, Bitmap bitmap){
            this.context = context;
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadDialog = new ProgressDialog(this.context);
            uploadDialog.setTitle(getString(R.string.bbs_thread_uploading_files));
            uploadDialog.setMessage(getString(R.string.bbs_thread_uploading_picture));
            uploadDialog.setCancelable(false);
            uploadDialog.show();
            Log.d(TAG,"Uploading picture "+bitmap.getWidth()+" "+bitmap.getHeight());

            byte[] bytes = bitmap2Bytes(bitmap);
            returnBitmap = bitmap;
            // generate file
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), bytes);

            String currentTimeString = timeDisplayUtils.getLocalePastTimeString(context, new Date());

            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("Filedata",String.format("NH_upload_%s.jpg",currentTimeString),fileBody)
                    .addFormDataPart("uid",bbsPersonInfo.uid)
                    .addFormDataPart("hash",uploadHash)
                    .build();

            Log.d(TAG,"UID "+bbsPersonInfo.uid+" hash "+uploadHash+" "+bytes.length);


            request = new Request.Builder()
                    .url(bbsURLUtils.getUploadImageUrl())
                    .post(multipartBody)
                    .build();
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            try{
                Response response = client.newCall(request).execute();
                if(response.isSuccessful() && response.body()!=null){
                    String content = response.body().string();
                    return content;
                }
                else {
                    return null;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }


        public int px2dip(int pxValue)
        {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }


        public int dip2px(float dipValue)
        {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dipValue * scale + 0.5f);
        }


        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG,"get information " +s);
            Map<String, String> uploadImageErrors = new HashMap<String, String>() {{
                put("-1", "内部服务器错误");
                put("0", "上传成功");
                put("1", "不支持此类扩展名");
                put("2", "服务器限制无法上传那么大的附件");
                put("3", "用户组限制无法上传那么大的附件");
                put("4", "不支持此类扩展名");
                put("5", "文件类型限制无法上传那么大的附件");
                put("6", "今日您已无法上传更多的附件");
                put("7", "请选择图片文件");
                put("8", "附件文件无法保存");
                put("9", "没有合法的文件被上传");
                put("10", "非法操作");
                put("11", "今日您已无法上传那么大的附件");
            }};
            uploadDialog.dismiss();

            if(s != null && (!TextUtils.isEmpty(s)) && s.contains("|")){
                // parse DISCUZUPLOAD|0|291730|1|0
                String[] spiltInfo = s.split("\\|");
                if(spiltInfo[0].contains("DISCUZUPLOAD") && "0".equals(spiltInfo[1])){

                    String aid = spiltInfo[2];
                    handler.insertImage(aid, new BitmapDrawable(getResources(), returnBitmap),
                            bbsThreadMessageEditText.getWidth() - dip2px( 16));
                    Toasty.success(context,context.getString(R.string.bbs_thread_upload_files_successful),Toast.LENGTH_LONG).show();
                }
                else {
                    String resCode = spiltInfo[1];
                    Toasty.error(context,uploadImageErrors.get(resCode).toString(),Toast.LENGTH_LONG).show();
                    if(resCode.equals("2")||resCode.equals("3")){
                        int splitLength = spiltInfo.length;
                        String maxByteString = spiltInfo[splitLength-1];
                        int maxByte = Integer.parseInt(maxByteString);
                        Toasty.error(context,uploadImageErrors.get(resCode)+String.format("(%s K)",maxByte/1024),Toast.LENGTH_LONG).show();
                    }
                }

            }
            else {
                Toasty.error(context,context.getString(R.string.bbs_thread_upload_files_failed),Toast.LENGTH_SHORT).show();
            }


        }
    }


    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        return baos.toByteArray();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxWidth) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width > maxWidth) {
            float bitmapRatio = (float) width / (float) height;
            width = maxWidth;
            height = (int) (width / bitmapRatio);
            return Bitmap.createScaledBitmap(image, width, height, true);
        }
        return image;
    }

    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? lastFile : data.getData();
    }

    private void configureToolbar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.bbs_post_thread));
        getSupportActionBar().setSubtitle(forumName);


    }

    private FormBody genPostFormBody(){
        FormBody formBody = new FormBody.Builder()
                .add("topicsubmit", "yes")
                .add("subject",bbsThreadSubjectEditText.getText().toString())
                .add("message",bbsThreadMessageEditText.getText().toString())
                .build();
        return formBody;
    }

    private Boolean checkIfThreadCanBePosted(){
        if(uploadHash == null ||TextUtils.isEmpty(bbsThreadSubjectEditText.getText().toString().trim())){
            return false;
        }
        else {
            return true;
        }
    }




    public class publishThreadTask extends AsyncTask<Void,Void,String>{
        String publishThreadApiUrl;
        Request request;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // need typeid
            int selectPos = mCategorySpinner.getSelectedItemPosition();
            List<String> numKeys = bbsParseUtils.getThreadTypeList(forumApiString);


            String subject = bbsThreadSubjectEditText.getText().toString();
            String message = bbsThreadMessageEditText.getText().toString();

            FormBody.Builder formBody = new FormBody.Builder();
            List<String> aids = handler.getImagesAids();
            formBody.add("topicsubmit", "yes")
                    .add("subject",subject)
                    .add("formhash",formHash)
                    .add("message",message);
            if(numKeys!=null && numKeys.size() >0){
                String typeId = numKeys.get(selectPos);
                formBody.add("typeid",typeId);
            }

            for (String aid : aids) {
                formBody.add("attachnew[" + aid + "]", "");
            }
            FormBody form = formBody.build();

            request = new Request.Builder()
                    .url(bbsURLUtils.getPostThreadUrl(fid))
                    .post(form)
                    .build();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                Response response = client.newCall(request).execute();
                if(response.isSuccessful() && response.body()!=null){
                    String content = response.body().string();
                    return content;
                }
                else {
                    return null;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // auto backup ?
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
            Boolean autoPostBackup = prefs.getBoolean(getString(R.string.preference_key_send_post_backup),true);
            if(autoPostBackup){
                new addThreadDraftTask(getApplicationContext(),threadDraft).execute();
            }

            Log.d(TAG,"get post information"+s);
            String message = bbsParseUtils.parsePostThreadInfo(s);
            if(message == null){
                message = getString(R.string.not_known);
            }
            if(bbsParseUtils.isPostThreadSuccessful(s)){
                Toasty.success(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                finishAfterTransition();
            }
            else {
                Toasty.error(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                new addThreadDraftTask(getApplicationContext(),threadDraft).execute();
            }


        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:{
                //返回键的id
                this.finishAfterTransition();
                return false;
            }
            case R.id.bbs_toolbar_send_item:{
                if(checkIfThreadCanBePosted()){
                    new publishThreadTask().execute();
                }
                else {
                    // calling a prompt?
                    Toasty.warning(this,getString(R.string.bbs_post_thread_subject_required),Toast.LENGTH_SHORT).show();

                }
                Log.d(TAG,"You press send item");
            }
            case R.id.bbs_post_thread_toolbar_save_draft:{
                addThreadDraftTask task = new addThreadDraftTask(this,threadDraft,true);
                task.execute();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bbs_post_thread_toolbar, menu);
        return true;
    }
}
