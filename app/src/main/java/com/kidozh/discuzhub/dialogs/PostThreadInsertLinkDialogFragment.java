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

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostThreadInsertLinkDialogFragment extends DialogFragment {
    private static String TAG = PostThreadInsertLinkDialogFragment.class.getSimpleName();

    @BindView(R.id.insert_link_url_editText)
    EditText linkEditText;

    public interface NoticeDialogListener {
        public void onLinkSubmit(String link);
    }

    NoticeDialogListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_insert_link, null);
        ButterKnife.bind(this,view);

        builder.setView(view)
                // Add action buttons
                .setTitle(getString(R.string.insert_link))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String link = linkEditText.getText().toString();
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
