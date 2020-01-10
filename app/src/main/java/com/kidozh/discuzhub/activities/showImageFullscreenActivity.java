package com.kidozh.discuzhub.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.widgets.PinchImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class showImageFullscreenActivity extends AppCompatActivity {
    private static final String TAG = showImageFullscreenActivity.class.getSimpleName();
    @BindView(R.id.show_image_fullscreen_shown_imageview)
    PinchImageView pinchImageView;
    @BindView(R.id.show_image_save_btn)
    Button mSaveBtn;
    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_fullscreen);
        ButterKnife.bind(this);

        configureActionBar();
        loadImage();
        configureSaveBtn();



    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void configureSaveBtn(){
        Context mContext = this;
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] fileNameSpilt = url.split("/");
                String fileName = fileNameSpilt[fileNameSpilt.length-1];
                if(isExternalStorageWritable()){
                    Bitmap bitmap = ( (BitmapDrawable) pinchImageView.getDrawable()).getBitmap();
                    String savedUri = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, fileName);
                    if(savedUri!=null){
                        Toasty.success(getApplicationContext(),String.format(getString(R.string.save_file_successfully_template),fileName), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Log.d(TAG,"get saved file uri "+url +" " +savedUri);
                        Uri uri = Uri.fromFile(new File(savedUri));
                        intent.setData(uri);
                        sendBroadcast(intent);
                    }
                    else {
                        Toasty.error(mContext,mContext.getString(R.string.failed_to_save_file),Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toasty.error(mContext,mContext.getString(R.string.failed_to_save_file),Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void loadImage(){
        Intent intent = getIntent();
        url = intent.getStringExtra("URL");
        Glide.with(this)
                .load(url)
                .error(R.drawable.vector_drawable_image_failed)
                .centerInside()
                .placeholder(R.drawable.vector_drawable_loading_image)
                .into(pinchImageView);
    }

    private void configureActionBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.show_picture);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finishAfterTransition();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
