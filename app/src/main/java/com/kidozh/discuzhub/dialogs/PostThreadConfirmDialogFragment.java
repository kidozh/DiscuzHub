package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.UploadAttachmentInfoAdapter;
import com.kidozh.discuzhub.entities.UploadAttachment;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.viewModels.PostThreadViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostThreadConfirmDialogFragment extends DialogFragment {
    private static String TAG = PostThreadConfirmDialogFragment.class.getSimpleName();

    @BindView(R.id.dialog_post_thread_subject_textview)
    TextView subjectTextview;
    @BindView(R.id.dialog_post_thread_password_layout)
    View passwordLayout;
    @BindView(R.id.dialog_post_thread_password_textview)
    TextView passwordTextview;
    @BindView(R.id.dialog_post_thread_attachment_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.dialog_post_thread_attachment_number_textview)
    TextView attachmentNumberTextview;

    public interface ConfirmDialogListener {
        public void onPositveBtnClicked();
    }

    ConfirmDialogListener listener;
    String password;

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
        View view = inflater.inflate(R.layout.dialog_post_thread_confirmed, null);
        ButterKnife.bind(this,view);


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
        bbsThreadDraft draft = postThreadViewModel.bbsThreadDraftMutableLiveData.getValue();
        if(draft!=null){
            subjectTextview.setText(draft.subject);
            if(draft.password.length()!=0){
                passwordLayout.setVisibility(View.VISIBLE);
                passwordTextview.setText(draft.password);
            }
            else {
                passwordLayout.setVisibility(View.GONE);
            }
        }
        configureRecyclerview();
    }

    private void configureRecyclerview(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        UploadAttachmentInfoAdapter adapter = new UploadAttachmentInfoAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        List<UploadAttachment> uploadAttachments = postThreadViewModel.uploadAttachmentListLiveData.getValue();
        adapter.setAttachmentList(uploadAttachments);
        if(uploadAttachments == null || uploadAttachments.size() == 0){
            attachmentNumberTextview.setVisibility(View.GONE);
        }
        else {
            attachmentNumberTextview.setVisibility(View.VISIBLE);
            attachmentNumberTextview.setText(getString(R.string.upload_attachment_number,uploadAttachments.size()));
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
