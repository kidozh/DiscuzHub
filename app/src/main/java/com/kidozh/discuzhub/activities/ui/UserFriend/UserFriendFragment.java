package com.kidozh.discuzhub.activities.ui.UserFriend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.NetworkIndicatorAdapter;
import com.kidozh.discuzhub.adapter.bbsUserFriendAdapter;
import com.kidozh.discuzhub.databinding.FragmentUserFriendBinding;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.UserFriendResult;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;

import okhttp3.OkHttpClient;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserFriendFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserFriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFriendFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String UID = "uid";
    private static final String FRIEND_COUNTS = "FRIEND_COUNTS";
    private static final String TAG = UserFriendFragment.class.getSimpleName();


    // TODO: Rename and change types of parameters
    private int uid;
    private int friendCounts = 0;

    private OnFragmentInteractionListener mListener;

    public UserFriendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uid Parameter 1.
     * @return A new instance of fragment userFriendFragment.
     */
    // TODO: Rename and change types and number of parameters

    public static UserFriendFragment newInstance(int uid, int friendCounts) {
        UserFriendFragment fragment = new UserFriendFragment();
        Bundle args = new Bundle();
        args.putInt(UID,uid);
        args.putInt(FRIEND_COUNTS,friendCounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(UID);
            friendCounts = getArguments().getInt(FRIEND_COUNTS);
        }
    }
    
    FragmentUserFriendBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserFriendBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        return view;
    }

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    ForumInfo forum;
    bbsUserFriendAdapter adapter;
    private UserFriendViewModel viewModel;
    ConcatAdapter concatAdapter;
    NetworkIndicatorAdapter networkIndicatorAdapter = new NetworkIndicatorAdapter();


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        bindViewModel();

    }

    private void configureIntentData(){
        Intent intent = getActivity().getIntent();
        forum = intent.getParcelableExtra(ConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY);
        viewModel = new ViewModelProvider(this).get(UserFriendViewModel.class);
        Log.d(TAG,"Set bbs "+bbsInfo+" user "+userBriefInfo);
        viewModel.setInfo(bbsInfo,userBriefInfo, uid, friendCounts);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.userFriendRecyclerview.setLayoutManager(linearLayoutManager);
        binding.userFriendRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        binding.userFriendRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsUserFriendAdapter(bbsInfo,userBriefInfo);
        concatAdapter = new ConcatAdapter(adapter,networkIndicatorAdapter);
        binding.userFriendRecyclerview.setAdapter(concatAdapter);

        binding.userFriendRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!binding.userFriendRecyclerview.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    viewModel.getFriendInfo();

                }
            }
        });
    }

    private void bindViewModel(){
        viewModel.getNewFriendListMutableLiveData().observe(getViewLifecycleOwner(),userFriends -> {
            if(userFriends!=null){
                Log.d(TAG,"Add new friends "+userFriends.size()+" page "+viewModel.getPage());
                adapter.addUserFriendList(userFriends);
                if(viewModel.getPage() == 2){
                    // means successfully
                    binding.userFriendRecyclerview.scrollToPosition(0);
                }
            }
        });
        viewModel.getUserFriendListMutableData().observe(getViewLifecycleOwner(), new Observer<List<UserFriendResult.UserFriend>>() {
            @Override
            public void onChanged(List<UserFriendResult.UserFriend> userFriends) {

                if(userFriends == null || userFriends.size() == 0){
                    // check for privacy
                    if(viewModel.getPrivacyMutableLiveData().getValue()!=null && viewModel.getPrivacyMutableLiveData().getValue() == false){
                        binding.userFriendErrorTextview.setVisibility(View.VISIBLE);
                        binding.userFriendEmptyImageView.setVisibility(View.VISIBLE);
                        binding.userFriendErrorTextview.setText(R.string.bbs_no_friend);
                        binding.userFriendEmptyImageView.setImageResource(R.drawable.ic_empty_friend_64px);
                    }
                    else {

                        binding.userFriendErrorTextview.setVisibility(View.VISIBLE);
                        binding.userFriendEmptyImageView.setVisibility(View.VISIBLE);
                        binding.userFriendEmptyImageView.setImageResource(R.drawable.ic_privacy_24px);
                        binding.userFriendErrorTextview.setText(R.string.bbs_privacy_protect_alert);
                    }

                }
                else {

                    binding.userFriendErrorTextview.setVisibility(View.GONE);
                    binding.userFriendEmptyImageView.setVisibility(View.GONE);
                }
            }
        });
        viewModel.isLoadingMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.userFriendSwipeRefreshLayout.setRefreshing(true);
                    networkIndicatorAdapter.setLoadingStatus();
                }
                else {
                    binding.userFriendSwipeRefreshLayout.setRefreshing(false);
                    networkIndicatorAdapter.setLoadSuccessfulStatus();
                }
            }
        });
        viewModel.isErrorMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.userFriendErrorTextview.setVisibility(View.VISIBLE);
                    binding.userFriendEmptyImageView.setVisibility(View.VISIBLE);
                    binding.userFriendEmptyImageView.setImageResource(R.drawable.ic_error_outline_24px);
                    String errorText = viewModel.getErrorTextMutableLiveData().getValue();

                    if(errorText == null || errorText.length() == 0){
                        binding.userFriendErrorTextview.setText(R.string.network_failed);
                    }
                    else {
                        binding.userFriendErrorTextview.setText(errorText);
                    }

                }
                else {
                    binding.userFriendErrorTextview.setVisibility(View.GONE);
                    binding.userFriendEmptyImageView.setVisibility(View.GONE);
                }
            }
        });
        viewModel.getPrivacyMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.userFriendErrorTextview.setVisibility(View.VISIBLE);
                    binding.userFriendEmptyImageView.setVisibility(View.VISIBLE);
                    binding.userFriendEmptyImageView.setImageResource(R.drawable.ic_privacy_24px);
                    binding.userFriendErrorTextview.setText(R.string.bbs_privacy_protect_alert);
                }
            }
        });
        viewModel.getUserFriendResultMutableLiveData().observe(getViewLifecycleOwner(),userFriendResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(userFriendResult,
                        userFriendResult!=null?userFriendResult.friendVariables:null);
            }
        });
        viewModel.getLoadAllMutableLiveData().observe(getViewLifecycleOwner(),aBoolean -> {
            if(aBoolean){
                networkIndicatorAdapter.setLoadedAllStatus();
            }
        });

    }


    private void configureSwipeRefreshLayout(){
        binding.userFriendSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.setPage(1);
                adapter.clearList();
                viewModel.getLoadAllMutableLiveData().setValue(false);

                viewModel.getFriendInfo();
            }
        });
        viewModel.getFriendInfo();
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
        void onFragmentInteraction(Uri uri);
        void onRenderSuccessfully();
    }
}