package com.kidozh.discuzhub.activities.ui.bbsNotification;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsForumThreadAdapter;
import com.kidozh.discuzhub.adapter.bbsNotificationAdapter;
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
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link bbsNotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link bbsNotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bbsNotificationFragment extends Fragment {
    private static final String TAG = bbsNotificationFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;

    public bbsNotificationFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.fragment_bbs_notification_recyclerview)
    RecyclerView bbsNotificationRecyclerview;
    @BindView(R.id.fragment_bbs_notification_swipeRefreshLayout)
    SwipeRefreshLayout bbsNotificationSwipeRefreshLayout;
    @BindView(R.id.fragment_bbs_notification_empty_view)
    View bbsNotificationEmptyView;

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    forumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    bbsNotificationAdapter adapter;
    private int globalPage = 1;
    private Boolean hasLoadAll = false;
    private String noticeType;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment bbsNotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static bbsNotificationFragment newInstance(String noticeType) {
        bbsNotificationFragment fragment = new bbsNotificationFragment();
        Bundle args = new Bundle();
        args.putString("NOTICE_TYPE",noticeType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noticeType = getArguments().getString("NOTICE_TYPE");
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bbs_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
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
        bbsNotificationRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        bbsNotificationRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsNotificationAdapter(bbsInfo,userBriefInfo);
        bbsNotificationRecyclerview.setAdapter(adapter);

        bbsNotificationRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(isScrollAtEnd()){
                    getBBSNotificationByPage(globalPage);

                }
            }

            public boolean isScrollAtEnd(){

                if (bbsNotificationRecyclerview.computeVerticalScrollExtent() + bbsNotificationRecyclerview.computeVerticalScrollOffset()
                        >= bbsNotificationRecyclerview.computeVerticalScrollRange()){
                    return true;
                }
                else {
                    return false;
                }

            }
        });

    }

    void configureSwipeRefreshLayout(){
        bbsNotificationSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                hasLoadAll = false;
                getBBSNotificationByPage(globalPage);
            }
        });
        getBBSNotificationByPage(globalPage);
    }

    void getBBSNotificationByPage(int page){

        if(hasLoadAll){
            return;
        }
        bbsNotificationSwipeRefreshLayout.setRefreshing(true);
        String apiString = "";
        if(noticeType.equals("SYSTEM")){
            apiString = bbsURLUtils.getPromptNotificationListApiUrl(page);
        }
        else if(noticeType.equals("NOTICE")){
            apiString = bbsURLUtils.getNotificationListApiUrl(page);
        }
        else {
            apiString = bbsURLUtils.getNotificationListApiUrl(page);
        }

        Log.d(TAG,"get notice type "+noticeType);

        Request request = new Request.Builder()
                .url(apiString)
                .build();
        Log.d(TAG,"get notification url "+request.url().toString());
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bbsNotificationEmptyView.setVisibility(View.VISIBLE);
                        bbsNotificationSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body()!=null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            bbsNotificationSwipeRefreshLayout.setRefreshing(false);
                        }
                    });

                    String s = response.body().string();
                    List<bbsParseUtils.notificationDetailInfo> notificationDetailInfoList = bbsParseUtils.parseNotificationDetailInfo(s);
                    if(notificationDetailInfoList!=null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                bbsNotificationEmptyView.setVisibility(View.GONE);
                                if(page == 1){
                                    adapter.setNotificationDetailInfoList(notificationDetailInfoList);
                                }
                                else {
                                    adapter.addNotificationDetailInfoList(notificationDetailInfoList);
                                }
                                // load all?
                                int perpage = bbsParseUtils.parsePrivateDetailMessagePerPage(s);
                                if(perpage > notificationDetailInfoList.size()){
                                    hasLoadAll = true;
                                }
                                if(notificationDetailInfoList.size()==0 && page == 1){
                                    bbsNotificationEmptyView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        globalPage += 1;
                    }
                    else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(getContext(),getString(R.string.network_failed), Toast.LENGTH_SHORT).show();
                                if(page == 1){
                                    bbsNotificationEmptyView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
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
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
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
