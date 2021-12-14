package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.DialogInsertLinkBinding;



public class PostThreadInsertLinkDialogFragment extends DialogFragment {
    private static String TAG = PostThreadInsertLinkDialogFragment.class.getSimpleName();

    public interface NoticeDialogListener {
        public void onLinkSubmit(String link);
    }

    NoticeDialogListener listener;
    
    DialogInsertLinkBinding binding;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        binding = DialogInsertLinkBinding.inflate(inflater);
        View view = binding.getRoot();
        

        builder.setView(view)
                // Add action buttons
                .setTitle(getString(R.string.insert_link))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String link = binding.insertLinkUrlEditText.getText().toString();
                        Log.d(TAG,"set link "+link);
                        listener.onLinkSubmit(link);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (NoticeDialogListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "must implement NoticeDialogListener");
        }
    }
}
