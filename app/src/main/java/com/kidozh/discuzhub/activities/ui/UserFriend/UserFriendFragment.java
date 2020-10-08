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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsUserFriendAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.UserFriendResult;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @BindView(R.id.user_friend_recyclerview)
    RecyclerView userFriendRecyclerview;
    @BindView(R.id.user_friend_empty_imageView)
    ImageView userFriendImageView;
    @BindView(R.id.user_friend_error_textview)
    TextView noFriendTextView;
    @BindView(R.id.user_friend_swipe_refreshLayout)
    SwipeRefreshLayout userFriendSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_friend, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    ForumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    bbsUserFriendAdapter adapter;
    private UserFriendViewModel viewModel;


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
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
        viewModel = new ViewModelProvider(this).get(UserFriendViewModel.class);
        Log.d(TAG,"Set bbs "+bbsInfo+" user "+userBriefInfo);
        viewModel.setInfo(bbsInfo,userBriefInfo, uid, friendCounts);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        userFriendRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        userFriendRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsUserFriendAdapter(null,bbsInfo,userBriefInfo);
        userFriendRecyclerview.setAdapter(adapter);

        userFriendRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    viewModel.getFriendInfo();

                }
            }

            public boolean isScrollAtEnd(){

                if (userFriendRecyclerview.computeVerticalScrollExtent() + userFriendRecyclerview.computeVerticalScrollOffset()
                        >= userFriendRecyclerview.computeVerticalScrollRange()){
                    return true;
                }
                else {
                    return false;
                }

            }
        });
    }

    private void bindViewModel(){
        viewModel.getUserFriendListMutableData().observe(getViewLifecycleOwner(), new Observer<List<UserFriendResult.UserFriend>>() {
            @Override
            public void onChanged(List<UserFriendResult.UserFriend> userFriends) {
                adapter.setUserFriendList(userFriends);
                if(userFriends == null || userFriends.size() == 0){
                    // check for privacy
                    if(viewModel.getPrivacyMutableLiveData().getValue()!=null && viewModel.getPrivacyMutableLiveData().getValue() == false){
                        noFriendTextView.setVisibility(View.VISIBLE);
                        userFriendImageView.setVisibility(View.VISIBLE);
                        noFriendTextView.setText(R.string.bbs_no_friend);
                        userFriendImageView.setImageResource(R.drawable.ic_empty_friend_64px);
                    }
                    else {
                        noFriendTextView.setVisibility(View.VISIBLE);
                        userFriendImageView.setVisibility(View.VISIBLE);
                        userFriendImageView.setImageResource(R.drawable.ic_privacy_24px);
                        noFriendTextView.setText(R.string.bbs_privacy_protect_alert);
                    }

                }
                else {
                    noFriendTextView.setVisibility(View.GONE);
                    userFriendImageView.setVisibility(View.GONE);
                }
            }
        });
        viewModel.isLoadingMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    userFriendSwipeRefreshLayout.setRefreshing(true);
                }
                else {
                    userFriendSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        viewModel.isErrorMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    noFriendTextView.setVisibility(View.VISIBLE);
                    userFriendImageView.setVisibility(View.VISIBLE);
                    userFriendImageView.setImageResource(R.drawable.ic_error_outline_24px);
                    String errorText = viewModel.getErrorTextMutableLiveData().getValue();
                    if(errorText == null || errorText.length() == 0){
                        noFriendTextView.setText(R.string.network_failed);

                    }
                    else {
                        noFriendTextView.setText(errorText);
                    }

                }
                else {
//                    noFriendTextView.setVisibility(View.GONE);
//                    userFriendImageView.setVisibility(View.GONE);
                }
            }
        });
        viewModel.getPrivacyMutableLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    noFriendTextView.setVisibility(View.VISIBLE);
                    userFriendImageView.setVisibility(View.VISIBLE);
                    userFriendImageView.setImageResource(R.drawable.ic_privacy_24px);
                    noFriendTextView.setText(R.string.bbs_privacy_protect_alert);
                }
            }
        });
        viewModel.getUserFriendResultMutableLiveData().observe(getViewLifecycleOwner(),userFriendResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(userFriendResult,
                        userFriendResult!=null?userFriendResult.friendVariables:null);
            }
        });

    }


    private void configureSwipeRefreshLayout(){
        userFriendSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.setPage(1);
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