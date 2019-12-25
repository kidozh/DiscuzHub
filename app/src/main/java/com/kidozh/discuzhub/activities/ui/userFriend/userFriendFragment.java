package com.kidozh.discuzhub.activities.ui.userFriend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsUserFriendAdapter;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link userFriendFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link userFriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class userFriendFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String UID = "uid";
    private static final String TAG = userFriendFragment.class.getSimpleName();


    // TODO: Rename and change types of parameters
    private String uid;

    private OnFragmentInteractionListener mListener;

    public userFriendFragment() {
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
    public static userFriendFragment newInstance(String uid) {
        userFriendFragment fragment = new userFriendFragment();
        Bundle args = new Bundle();
        args.putString(UID,uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString(UID);
        }
    }

    @BindView(R.id.user_friend_recyclerview)
    RecyclerView userFriendRecyclerview;
    @BindView(R.id.user_friend_empty_imageView)
    ImageView userFriendImageView;
    @BindView(R.id.user_friend_no_item_textview)
    TextView noFriendTextView;

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
    forumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    bbsUserFriendAdapter adapter;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureIntentData();
        configureRecyclerview();
        getFriendInfo();

    }

    private void configureIntentData(){
        Intent intent = getActivity().getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        userFriendRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        userFriendRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsUserFriendAdapter(null,bbsInfo,userBriefInfo);
        userFriendRecyclerview.setAdapter(adapter);


    }

    private void getFriendInfo(){
        String apiStr = bbsURLUtils.getFriendApiUrlByUid(Integer.valueOf(uid));
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Log.d(TAG,"Get user friend by uid "+uid);
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        noFriendTextView.setVisibility(View.VISIBLE);
                        userFriendImageView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&&response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"get friend string :"+s);
                    List<bbsParseUtils.userFriend> userFriendList = bbsParseUtils.parseUserFriendInfo(s);
                    if(userFriendList!=null){
                        Log.d(TAG,"get user friend list size "+userFriendList.size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setUserFriendList(userFriendList);
                                if(userFriendList.size()==0){
                                    noFriendTextView.setVisibility(View.VISIBLE);
                                    userFriendImageView.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                    }
                    else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                noFriendTextView.setVisibility(View.VISIBLE);
                                userFriendImageView.setVisibility(View.VISIBLE);
                            }
                        });
                    }


                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            noFriendTextView.setVisibility(View.VISIBLE);
                            userFriendImageView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        void onFragmentInteraction(Uri uri);
    }
}
