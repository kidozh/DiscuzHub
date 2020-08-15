package com.kidozh.discuzhub.activities.ui.uploadAttachment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UploadAttachmentInfoAdapter;
import com.kidozh.discuzhub.entities.UploadAttachment;
import com.kidozh.discuzhub.results.PostParameterResult;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.viewModels.PostThreadViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class UploadAttachmentDialogFragment extends BottomSheetDialogFragment {
    private final String TAG = UploadAttachmentDialogFragment.class.getSimpleName();

    @BindView(R.id.upload_attachment_chipGroup)
    ChipGroup uploadAttachmentTypeChipGroup;
    @BindView(R.id.upload_attachment_notice)
    TextView uploadAttachmentNotice;
    @BindView(R.id.upload_attachment_file_recyclerview)
    RecyclerView uploadAttachmentRecyclerview;
    @BindView(R.id.upload_attachment_btn)
    Button uploadAttachmentBtn;
    @BindView(R.id.upload_attachment_progressBar)
    ProgressBar uploadAttachmentProgressbar;

    PostThreadViewModel viewModel;
    UploadAttachmentInfoAdapter adapter;

    private BottomSheetBehavior mBehavior;

    public static UploadAttachmentDialogFragment newInstance(PostThreadViewModel viewModel){
        UploadAttachmentDialogFragment uploadAttachmentDialogFragment = new UploadAttachmentDialogFragment();
        uploadAttachmentDialogFragment.viewModel = viewModel;
        return uploadAttachmentDialogFragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_upload_attachment, null);
        ButterKnife.bind(this,view);
        dialog.setContentView(view);

        // configure viewmodel
        //viewModel =  new ViewModelProvider(this).get(PostThreadViewModel.class);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());

        configureAttachmentRecyclerview();
        configureChipGroup();
        configureBtn();
        bindViewModel();

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG," view is created");


    }

    public void configureAttachmentRecyclerview(){
        uploadAttachmentRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UploadAttachmentInfoAdapter();

        uploadAttachmentRecyclerview.setHasFixedSize(true);
        uploadAttachmentRecyclerview.setAdapter(adapter);

    }

    public void configureChipGroup(){
        uploadAttachmentTypeChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {


                Log.d(TAG, "get position "+checkedId);
                PostParameterResult postParameterResult = viewModel.getThreadPostParameterResultMutableLiveData().getValue();

                if(postParameterResult !=null){
                    PostParameterResult.UploadSize uploadSize = postParameterResult.permissionVariables.allowPerm.uploadSize;
                    List<String> allowableType = uploadSize.getAllowableFileSuffix();
                    if(checkedId >0){
                        for(int i=0;i< group.getChildCount(); i++){
                            Chip selectedChip = (Chip) group.getChildAt(i);
                            if(selectedChip.isChecked()){
                                String selectedType = allowableType.get(i);
                                uploadAttachmentBtn.setText(getString(R.string.bbs_upload,selectedType));

                                break;
                            }
                        }
                    }
                    else if(checkedId == -1) {
                        // clear state
                        uploadAttachmentBtn.setText(R.string.bbs_upload_an_attachment);
                    }
                }

            }
        });
        uploadAttachmentBtn.setText(R.string.bbs_upload_an_attachment);


    }

    private void configureBtn(){
        uploadAttachmentBtn.setOnClickListener(view->{
            Intent intent = getChooseFileIntent();
            if(getActivity()!=null){
                getActivity().startActivityForResult(intent, bbsConstUtils.REQUEST_CODE_UPLOAD_ATTACHMENT);
            }

        });
    }

    private void bindViewModel(){
        viewModel.uploadAttachmentListLiveData.observe(this, new Observer<List<UploadAttachment>>() {
            @Override
            public void onChanged(List<UploadAttachment> uploadAttachments) {
                adapter.setAttachmentList(uploadAttachments);
            }
        });
        Context context = getContext();
        Log.d(TAG,"bind view model");
        viewModel.getThreadPostParameterResultMutableLiveData().observe(this, new Observer<PostParameterResult>() {
            @Override
            public void onChanged(PostParameterResult postParameterResult) {
                PostParameterResult.UploadSize uploadSize = postParameterResult.permissionVariables.allowPerm.uploadSize;
                List<String> allowableType = uploadSize.getAllowableFileSuffix();
                Log.d(TAG,"get all allowable list "+allowableType.size());
                // add to chip group
                for(String type : allowableType){
                    Chip typeChip = new Chip(context);
                    typeChip.setText(type);
                    typeChip.setCheckable(true);
                    typeChip.setClickable(true);
                    uploadAttachmentTypeChipGroup.addView(typeChip);
                }
            }
        });
        viewModel.isUploadingAttachmentLiveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    uploadAttachmentProgressbar.setVisibility(View.VISIBLE);
                }
                else {
                    uploadAttachmentProgressbar.setVisibility(View.GONE);
                }
            }
        });
        viewModel.uploadAttachmentListLiveData.observe(this, new Observer<List<UploadAttachment>>() {
            @Override
            public void onChanged(List<UploadAttachment> uploadAttachments) {
                adapter.setAttachmentList(uploadAttachments);
            }
        });
    }

    private String getSelectedFileType(){
        Boolean findType = false;
        PostParameterResult postParameterResult = viewModel.getThreadPostParameterResultMutableLiveData().getValue();
        if(postParameterResult == null){
            return "";
        }
        PostParameterResult.UploadSize uploadSize = postParameterResult.permissionVariables.allowPerm.uploadSize;
        List<String> allowableType = uploadSize.getAllowableFileSuffix();
        for(int i=0;i<uploadAttachmentTypeChipGroup.getChildCount();i++){
            Chip curChip = (Chip) uploadAttachmentTypeChipGroup.getChildAt(i);
            if(curChip.isChecked() && i<allowableType.size()){
                return allowableType.get(i);
            }
        }
        return "";
    }

    private Intent getChooseFileIntent(){
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getActivity().getPackageManager();
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        String fileType = getSelectedFileType();
        switch (fileType){
            case "":
                galleryIntent.setType("*/*");
                break;
            case "gif":{
                galleryIntent.setType("image/gif");
                break;
            }
            case "jpg":{
                galleryIntent.setType("image/jpeg");
                break;
            }
            case "jpeg":{
                galleryIntent.setType("image/jpeg");
                break;
            }
            case "png":{
                galleryIntent.setType("image/png");
                break;
            }
            case "rar":{
                galleryIntent.setType("application/x-rar-compressed");
                break;
            }
            case "zip":{
                galleryIntent.setType("application/zip");
                break;
            }
            case "mp3":{
                galleryIntent.setType("audio/mpeg");
                break;
            }
            case "txt":{
                galleryIntent.setType("text/plain");
                break;
            }
            case "pdf":{
                galleryIntent.setType("application/pdf");
                break;
            }
            default:{
                galleryIntent.setType("*/*");
            }
        }
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        if(allIntents.size()!=0){
            Intent mainIntent = allIntents.get(allIntents.size() - 1);
            for (Intent intent : allIntents) {
                if ("com.android.documentsui.DocumentsActivity".equals(intent.getComponent().getClassName())) {
                    mainIntent = intent;
                    break;
                }
            }
            allIntents.remove(mainIntent);
            Intent chooserIntent = Intent.createChooser(mainIntent, getString(R.string.bbs_upload_an_attachment));

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

            return chooserIntent;
        }
        else {
            Toasty.warning(getContext(),getString(R.string.can_not_find_the_file_resolver),Toast.LENGTH_SHORT).show();
            return galleryIntent;
        }


    }




}
