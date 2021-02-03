package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.DialogReportBinding;
import com.kidozh.discuzhub.entities.Post;


import es.dmoral.toasty.Toasty;

public class ReportPostDialogFragment extends DialogFragment {
    private static String TAG = ReportPostDialogFragment.class.getSimpleName();
    @NonNull
    Post post;

    public interface ReportDialogListener {
        public void onReportSubmit(int pid,String reportReason,boolean reportForOtherReason);
    }

    ReportDialogListener listener;
    DialogReportBinding binding;


    public ReportPostDialogFragment(@NonNull Post post){
        this.post = post;
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
        binding = DialogReportBinding.inflate(inflater);
        View view = binding.getRoot();

        builder.setView(view)
                // Add action buttons
                //.setTitle(getString(R.string.report_post_title))
        ;
        bindRadioGroupListener();
        bindSubmitBtn();

        return builder.create();
    }

    private void bindRadioGroupListener(){
        binding.reportReasonGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            Log.d(TAG,"Checked option "+checkedId);
            if(checkedId == R.id.report_option_others){
                binding.reportInputReason.setVisibility(View.VISIBLE);
                binding.reportSubmitBtn.setVisibility(View.VISIBLE);
            }
            else if(checkedId == -1){
                binding.reportInputReason.setVisibility(View.GONE);
                binding.reportSubmitBtn.setVisibility(View.GONE);
            }
            else {
                binding.reportInputReason.setVisibility(View.GONE);
                binding.reportSubmitBtn.setVisibility(View.VISIBLE);
            }

        }));
    }

    private void bindSubmitBtn(){
        binding.reportSubmitBtn.setOnClickListener(v -> {
            int checkedReasonId = binding.reportReasonGroup.getCheckedRadioButtonId();
            if(checkedReasonId == -1){
                // should not submit
                Toasty.warning(getContext(),getString(R.string.report_reason_required),Toast.LENGTH_SHORT).show();
            }
            else {
                if(checkedReasonId == R.id.report_option_others && TextUtils.isEmpty(binding.reportInputReason.getText())){
                    Toasty.warning(getContext(),getString(R.string.report_input_reason_required),Toast.LENGTH_SHORT).show();
                }
                else {
                    String reason = "";
                    boolean isOtherChecked = checkedReasonId == R.id.report_option_others;
                    if(checkedReasonId == R.id.report_option_spam){
                        reason = getString(R.string.report_option_spam);
                    }
                    else if(checkedReasonId == R.id.report_option_bump){
                        reason = getString(R.string.report_option_bump);
                    }
                    else if(checkedReasonId == R.id.report_option_violating_content){
                        reason = getString(R.string.report_option_violating_content);
                    }
                    else if(checkedReasonId == R.id.report_option_repeat_post){
                        reason = getString(R.string.report_option_repeat_post);
                    }
                    else if(checkedReasonId == R.id.report_option_others){
                        reason = binding.reportInputReason.getText().toString();
                    }

                    listener.onReportSubmit(post.pid,reason,isOtherChecked);
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (ReportDialogListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "must implement NoticeDialogListener");
        }
    }
}
