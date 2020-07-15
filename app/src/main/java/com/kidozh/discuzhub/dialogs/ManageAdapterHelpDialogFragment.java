package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

public class ManageAdapterHelpDialogFragment extends DialogFragment {
    private static String TAG = ManageAdapterHelpDialogFragment.class.getSimpleName();




    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_help_manage_adapter, null);
        ButterKnife.bind(this,view);


        builder.setView(view)
                // Add action buttons
                .setTitle(getString(R.string.manage_help_text))
                //.setIcon(R.drawable.ic_help_outline_24px)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
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
