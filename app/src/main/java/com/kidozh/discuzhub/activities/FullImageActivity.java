package com.kidozh.discuzhub.activities;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ActivityShowImageFullscreenBinding;

import java.io.File;


import es.dmoral.toasty.Toasty;

public class FullImageActivity extends BaseStatusActivity {
    private static final String TAG = FullImageActivity.class.getSimpleName();

    String url = "";
    ActivityShowImageFullscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowImageFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.showImageSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] fileNameSpilt = url.split("/");
                String fileName = fileNameSpilt[fileNameSpilt.length-1];
                if(isExternalStorageWritable()){
                    Bitmap bitmap = ( (BitmapDrawable) binding.showImageFullscreenShownImageview.getDrawable()).getBitmap();
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
                .into(binding.showImageFullscreenShownImageview);
    }

    private void configureActionBar(){

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
