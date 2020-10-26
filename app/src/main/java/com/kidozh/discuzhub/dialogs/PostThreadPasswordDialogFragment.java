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

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.DialogSetThreadPasswordBinding;



public class PostThreadPasswordDialogFragment extends DialogFragment {
    private static String TAG = PostThreadPasswordDialogFragment.class.getSimpleName();

    EditText threadPasswordEditText;

    public interface NoticeDialogListener {
        public void onPasswordSubmit(String password);
    }

    NoticeDialogListener listener;
    String password;

    public PostThreadPasswordDialogFragment(String password){
        this.password = password;
    }

    DialogSetThreadPasswordBinding binding;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        binding = DialogSetThreadPasswordBinding.inflate(inflater);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = binding.getRoot();

        threadPasswordEditText.setText(password);
        builder.setView(view)
                // Add action buttons
                .setTitle(getString(R.string.bbs_thread_encrypt))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String password = threadPasswordEditText.getText().toString();
                        Log.d(TAG,"set password "+password);
                        listener.onPasswordSubmit(password);
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
