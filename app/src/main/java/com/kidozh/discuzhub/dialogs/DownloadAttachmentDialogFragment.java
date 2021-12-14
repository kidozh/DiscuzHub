package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.DialogAttachmentInformationBinding;
import com.kidozh.discuzhub.entities.Post;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.TimeDisplayUtils;

import es.dmoral.toasty.Toasty;

public class DownloadAttachmentDialogFragment extends DialogFragment {
    private static String TAG = DownloadAttachmentDialogFragment.class.getSimpleName();
    @NonNull
    Post.Attachment attachment;


    DialogAttachmentInformationBinding binding;


    public DownloadAttachmentDialogFragment(@NonNull Post.Attachment attachment){
        this.attachment = attachment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        binding = DialogAttachmentInformationBinding.inflate(inflater);
        View view = binding.getRoot();
        renderDialog();
        String source = URLUtils.getAttachmentURL(attachment);
        Context mContext = getContext();
        builder.setView(view)
                // Add action buttons
                .setTitle(getString(R.string.download_file))
                .setPositiveButton(android.R.string.ok,(dialog, which) -> {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(source));
                    Log.d(TAG, "Download by URL "+source);
                    request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS,attachment.filename);

                    DownloadManager downloadManager= (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                    request.setTitle(mContext.getString(R.string.bbs_downloading_file_template,attachment.filename));
                    request.setDescription(attachment.filename);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    if(downloadManager!=null){
                        downloadManager.enqueue(request);
                        Toasty.success(mContext,mContext.getString(R.string.bbs_downloading_attachment), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toasty.error(mContext,mContext.getString(R.string.bbs_downloading_attachment_failed), Toast.LENGTH_LONG).show();
                    }
                });


        return builder.create();
    }

    private void renderDialog(){
        binding.filename.setText(attachment.filename);
        binding.fileSize.setText(attachment.attachSize);
        binding.fileUploadTime.setText(TimeDisplayUtils.getLocalePastTimeString(getContext(),attachment.updateAt));
        binding.fileDownloadTimes.setText(getString(R.string.attachment_download_time,attachment.downloads));
        if(attachment.price != 0){
            binding.filePriceTag.setVisibility(View.VISIBLE);
            binding.filePrice.setVisibility(View.VISIBLE);
            if(attachment.payed){
                binding.filePrice.setText(R.string.file_payed);
                binding.filePrice.setTextColor(getContext().getColor(R.color.colorSafeStatus));
            }
            else {
                binding.filePrice.setText(String.valueOf(attachment.price));
                binding.filePrice.setTextColor(getContext().getColor(R.color.MaterialColorDeepOrange));
            }

        }
        else {
            binding.filePriceTag.setVisibility(View.GONE);
            binding.filePrice.setVisibility(View.GONE);
        }
        if(attachment.remote){
            binding.remoteWarn.setVisibility(View.VISIBLE);
        }
        else {
            binding.remoteWarn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }
}
