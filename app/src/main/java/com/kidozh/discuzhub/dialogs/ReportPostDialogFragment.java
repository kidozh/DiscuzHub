package com.kidozh.discuzhub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.PostInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class ReportPostDialogFragment extends DialogFragment {
    private static String TAG = ReportPostDialogFragment.class.getSimpleName();
    @NonNull
    PostInfo postInfo;

    @BindView(R.id.report_title)
    TextView reportTitle;
    @BindView(R.id.report_reason_group)
    RadioGroup reportRadioGroup;
    @BindView(R.id.report_option_others)
    RadioButton reportOptionOthers;
    @BindView(R.id.report_input_reason)
    EditText reportReasonEditText;
    @BindView(R.id.report_submit_btn)
    Button reportSubmitBtn;

    public interface ReportDialogListener {
        public void onReportSubmit(int pid,String reportReason,boolean reportForOtherReason);
    }

    ReportDialogListener listener;


    public ReportPostDialogFragment(@NonNull PostInfo postInfo){
        this.postInfo = postInfo;
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
        View view = inflater.inflate(R.layout.dialog_report, null);
        ButterKnife.bind(this,view);

        builder.setView(view)
                // Add action buttons
                //.setTitle(getString(R.string.report_post_title))
        ;
        bindRadioGroupListener();
        bindSubmitBtn();

        return builder.create();
    }

    private void bindRadioGroupListener(){
        reportRadioGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            Log.d(TAG,"Checked option "+checkedId);
            switch (checkedId){
                case R.id.report_option_others:{
                    reportReasonEditText.setVisibility(View.VISIBLE);
                    reportSubmitBtn.setVisibility(View.VISIBLE);
                    break;
                }
                case -1:{
                    reportReasonEditText.setVisibility(View.GONE);
                    reportSubmitBtn.setVisibility(View.GONE);
                    break;
                }
                default:{
                    reportReasonEditText.setVisibility(View.GONE);
                    reportSubmitBtn.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void bindSubmitBtn(){
        reportSubmitBtn.setOnClickListener(v -> {
            int checkedReasonId = reportRadioGroup.getCheckedRadioButtonId();
            if(checkedReasonId == -1){
                // should not submit
                Toasty.warning(getContext(),getString(R.string.report_reason_required),Toast.LENGTH_SHORT).show();
            }
            else {
                if(checkedReasonId == R.id.report_option_others && TextUtils.isEmpty(reportReasonEditText.getText())){
                    Toasty.warning(getContext(),getString(R.string.report_input_reason_required),Toast.LENGTH_SHORT).show();
                }
                else {
                    String reason = "";
                    boolean isOtherChecked = checkedReasonId == R.id.report_option_others;
                    switch (checkedReasonId){
                        case R.id.report_option_spam:{
                            reason = getString(R.string.report_option_spam);
                            break;
                        }
                        case R.id.report_option_bump:{
                            reason = getString(R.string.report_option_bump);
                            break;
                        }
                        case R.id.report_option_violating_content:{
                            reason = getString(R.string.report_option_violating_content);
                            break;
                        }
                        case R.id.report_option_repeat_post:{
                            reason = getString(R.string.report_option_repeat_post);
                            break;
                        }
                        case R.id.report_option_others:{
                            reason = reportReasonEditText.getText().toString();
                            break;
                        }

                    }
                    listener.onReportSubmit(postInfo.pid,reason,isOtherChecked);
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
