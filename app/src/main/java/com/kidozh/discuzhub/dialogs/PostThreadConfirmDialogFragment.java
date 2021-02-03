package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UploadAttachmentInfoAdapter;
import com.kidozh.discuzhub.databinding.DialogPostThreadConfirmedBinding;
import com.kidozh.discuzhub.entities.ThreadDraft;
import com.kidozh.discuzhub.entities.UploadAttachment;
import com.kidozh.discuzhub.viewModels.PostThreadViewModel;

import java.util.List;



public class PostThreadConfirmDialogFragment extends DialogFragment {
    private static String TAG = PostThreadConfirmDialogFragment.class.getSimpleName();

    
    DialogPostThreadConfirmedBinding binding;

    public interface ConfirmDialogListener {
        public void onPositveBtnClicked();
    }

    ConfirmDialogListener listener;

    PostThreadViewModel postThreadViewModel;



    public PostThreadConfirmDialogFragment(PostThreadViewModel postThreadViewModel){
        this.postThreadViewModel = postThreadViewModel;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        binding = DialogPostThreadConfirmedBinding.inflate(inflater);
        View view = binding.getRoot();


        builder.setView(view)
                // Add action buttons
                .setTitle(getString(R.string.bbs_post_confirm_dialog_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onPositveBtnClicked();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        renderPage();
        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void renderPage(){
        ThreadDraft draft = postThreadViewModel.bbsThreadDraftMutableLiveData.getValue();
        if(draft!=null){
            binding.dialogPostThreadSubjectTextview.setText(draft.subject);
            if(draft.password.length()!=0){
                binding.dialogPostThreadPasswordLayout.setVisibility(View.VISIBLE);
                binding.dialogPostThreadPasswordTextview.setText(draft.password);
            }
            else {
                binding.dialogPostThreadPasswordLayout.setVisibility(View.GONE);
            }
        }
        configureRecyclerview();
    }

    private void configureRecyclerview(){
        binding.dialogPostThreadAttachmentRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        UploadAttachmentInfoAdapter adapter = new UploadAttachmentInfoAdapter();
        binding.dialogPostThreadAttachmentRecyclerview.setHasFixedSize(true);
        binding.dialogPostThreadAttachmentRecyclerview.setAdapter(adapter);
        List<UploadAttachment> uploadAttachments = postThreadViewModel.uploadAttachmentListLiveData.getValue();
        adapter.setAttachmentList(uploadAttachments);
        if(uploadAttachments == null || uploadAttachments.size() == 0){
            binding.dialogPostThreadAttachmentNumberTextview.setVisibility(View.GONE);
        }
        else {
            binding.dialogPostThreadAttachmentNumberTextview.setVisibility(View.VISIBLE);
            binding.dialogPostThreadAttachmentNumberTextview.setText(getString(R.string.upload_attachment_number,uploadAttachments.size()));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (ConfirmDialogListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "must implement ConfirmDialogListener");
        }
    }
}
