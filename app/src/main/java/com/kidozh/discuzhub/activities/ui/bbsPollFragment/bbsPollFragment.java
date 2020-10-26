package com.kidozh.discuzhub.activities.ui.bbsPollFragment;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.PollOptionAdapter;
import com.kidozh.discuzhub.databinding.FragmentBbsPollBinding;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.RecyclerItemClickListener;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import java.io.IOException;
import java.util.List;


import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link bbsPollFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link bbsPollFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bbsPollFragment extends Fragment {
    private static final String TAG = bbsPollFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    // TODO: Rename and change types of parameters
    private bbsPollInfo pollInfo;
    private int tid;
    private forumUserBriefInfo userBriefInfo;
    private String formhash;
    private OnFragmentInteractionListener mListener;

    public bbsPollFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment bbsPollFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static bbsPollFragment newInstance(bbsPollInfo pollInfo,forumUserBriefInfo userBriefInfo, int tid, String formhash) {
        bbsPollFragment fragment = new bbsPollFragment();
        Bundle args = new Bundle();
        args.putSerializable(bbsConstUtils.PASS_POLL_KEY,pollInfo);
        args.putInt(bbsConstUtils.PASS_TID_KEY,tid);
        args.putSerializable(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
        args.putSerializable(bbsConstUtils.PASS_FORMHASH_KEY,formhash);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pollInfo = (bbsPollInfo) getArguments().getSerializable(bbsConstUtils.PASS_POLL_KEY);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(bbsConstUtils.PASS_BBS_USER_KEY);
            tid = getArguments().getInt(bbsConstUtils.PASS_TID_KEY);
            client = NetworkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
            formhash = getArguments().getString(bbsConstUtils.PASS_FORMHASH_KEY);
        }
    }

    FragmentBbsPollBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBbsPollBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }



    PollOptionAdapter adapter;
    OkHttpClient client;

    Context context;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"Poll " + pollInfo);
        configurePollInformation();

    }

    void configurePollInformation(){
        context = getActivity();

        binding.bbsPollExpireTime.setText(
                getString(R.string.poll_expire_at,
                            timeDisplayUtils.getLocalePastTimeString(context,pollInfo.expirations)
                ));
        Resources res = getResources();
        String votersNumberString = res.getQuantityString(R.plurals.poll_voter_number, pollInfo.votersCount, pollInfo.votersCount);
        binding.bbsPollVoterNumber.setText(votersNumberString);
        // add attributes
        Chip chip = new Chip(context);
        chip.setChipBackgroundColor(context.getColorStateList(R.color.chip_background_select_state));
        chip.setTextColor(context.getColor(R.color.colorPrimary));
        if(!pollInfo.multiple){
            chip.setText(R.string.poll_single_choice);
            chip.setChipIcon(context.getDrawable(R.drawable.vector_drawable_radio_button_checked_24px));
        }
        else {
            chip.setText(R.string.poll_multiple_choices);
            chip.setChipIcon(context.getDrawable(R.drawable.vector_drawable_format_list_bulleted_24px));
        }
        binding.bbsPollChipGroup.addView(chip);
        chip = new Chip(context);


        if(pollInfo.allowVote){
            chip.setText(R.string.poll_can_vote);
            chip.setChipIcon(context.getDrawable(R.drawable.vector_drawable_check_24px));
            chip.setChipBackgroundColorResource(R.color.colorSafeStatus);
        }
        else {
            chip.setText(R.string.poll_cannot_vote);
            chip.setChipIcon(context.getDrawable(R.drawable.vector_drawable_block_24px));
            chip.setChipBackgroundColorResource(R.color.colorUnSafeStatus);
        }
        chip.setTextColor(context.getColor(R.color.colorPureWhite));
        binding.bbsPollChipGroup.addView(chip);
        chip = new Chip(context);
        chip.setChipBackgroundColor(context.getColorStateList(R.color.chip_background_select_state));
        chip.setTextColor(context.getColor(R.color.colorPrimary));
        if(pollInfo.resultVisible){
            chip.setText(R.string.poll_visible_after_vote);
            chip.setChipIcon(context.getDrawable(R.drawable.vector_drawable_how_to_vote_24px));
            binding.bbsPollChipGroup.addView(chip);
        }
        configurePollVoteBtn();
        configureRecyclerview();


    }

    void configurePollVoteBtn(){
        if(!pollInfo.allowVote){
            binding.bbsPollVoteBtn.setVisibility(View.GONE);
        }
        else {
            binding.bbsPollVoteBtn.setVisibility(View.VISIBLE);
        }
        binding.bbsPollVoteBtn.setEnabled(false);
        binding.bbsPollVoteBtn.setText(getString(R.string.poll_vote_progress,0,pollInfo.maxChoices));
        binding.bbsPollVoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<bbsPollInfo.option> options = adapter.getPollOptions();
                int checkedNumber = pollInfo.getCheckedOptionNumber();

                if(pollInfo.allowVote && checkedNumber > 0 && checkedNumber<=pollInfo.maxChoices && formhash!=null){
                    Log.d(TAG,"VOTING "+formhash);
                    binding.bbsPollVoteBtn.setEnabled(false);
                    FormBody.Builder formBodyBuilder = new FormBody.Builder()
                            .add("formhash",formhash);
                    // append pollanswers[]: id accordingly
                    for(int i=0;i<options.size();i++){
                        bbsPollInfo.option option = options.get(i);
                        if(option.checked){
                            Log.d(TAG,"Option id "+option.id);
                            formBodyBuilder.add("pollanswers[]",option.id);
                        }
                    }
                    Request request = new Request.Builder()
                            .url(URLUtils.getVotePollApiUrl(tid))
                            .post(formBodyBuilder.build())
                            .build();
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.warning(context,context.getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()&& response.body()!=null){
                                String s = response.body().string();
                                Log.d(TAG,"recv poll "+s);

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.bbsPollVoteBtn.setEnabled(true);
                                        // need to notify the activity if success
                                        bbsParseUtils.returnMessage message = bbsParseUtils.parseReturnMessage(s);
                                        if(message!=null){
                                            if(message.value.equals("thread_poll_succeed")){
                                                // toast using
                                                Toasty.success(context,message.string, Toast.LENGTH_SHORT).show();
                                                binding.bbsPollVoteBtn.setEnabled(false);
                                                binding.bbsPollVoteBtn.setText(message.string);
                                            }
                                            else {
                                                Toasty.error(context,message.string,Toast.LENGTH_SHORT).show();
                                            }
                                            mListener.onPollResultFetched();
                                        }
                                        else {
                                            Toasty.warning(context,context.getString(R.string.parse_failed),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });
    }

    void configureRecyclerview(){
        //binding.bbsPollOptionRecyclerview.setLayoutManager(new GridLayoutManager(getActivity(),2));
        binding.bbsPollOptionRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new PollOptionAdapter();
        binding.bbsPollOptionRecyclerview.setAdapter(adapter);
        List<bbsPollInfo.option> options = pollInfo.options;
        if(options!= null && options.size() > 0){
            adapter.setPollOptions(options);
        }
        // recyclerview check
        binding.bbsPollOptionRecyclerview.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), binding.bbsPollOptionRecyclerview, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // if check
                if(pollInfo.allowVote){
                    List<bbsPollInfo.option> options = adapter.getPollOptions();
                    // trigger it
                    options.get(position).checked = !options.get(position).checked;
                    adapter.setPollOptions(options);
                    int checkedNumber = pollInfo.getCheckedOptionNumber();

                    if(checkedNumber <= pollInfo.maxChoices && checkedNumber >0){
                        binding.bbsPollVoteBtn.setEnabled(true);
                        binding.bbsPollVoteBtn.setText(getString(R.string.poll_vote_progress,checkedNumber,pollInfo.maxChoices));
                    }
                    else {
                        binding.bbsPollVoteBtn.setEnabled(false);
                        binding.bbsPollVoteBtn.setText(getString(R.string.poll_vote_progress,checkedNumber,pollInfo.maxChoices));
                    }
                }
                else {
                    binding.bbsPollVoteBtn.setVisibility(View.GONE);
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onPollResultFetched();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onPollResultFetched();
    }
}
