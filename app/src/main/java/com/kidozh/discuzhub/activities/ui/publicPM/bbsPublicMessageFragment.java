package com.kidozh.discuzhub.activities.ui.publicPM;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.adapter.bbsPublicMessageAdapter;
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

public class bbsPublicMessageFragment extends Fragment {
    private final static String TAG = bbsPublicMessageFragment.class.getSimpleName();

    @BindView(R.id.fragment_private_message_recyclerview)
    RecyclerView publicMessageRecyclerview;
    @BindView(R.id.fragment_private_message_swipeRefreshLayout)
    SwipeRefreshLayout publicMessageSwipeRefreshLayout;
    @BindView(R.id.fragment_private_message_empty_view)
    View publicMessageEmptyView;

    private bbsPrivateMessageFragment.OnNewMessageChangeListener mListener;

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    forumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    bbsPublicMessageAdapter adapter;
    private int globalPage = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_public_message, container, false);
        ButterKnife.bind(this,root);
        Log.d(TAG,"Creating public message page");
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        getPublicMessage(1);

    }

    void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        publicMessageRecyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        publicMessageRecyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsPublicMessageAdapter();
        publicMessageRecyclerview.setAdapter(adapter);
    }

    void configureSwipeRefreshLayout(){
        publicMessageSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                getPublicMessage(globalPage);
            }
        });
    }


    private void configureIntentData(){
        Intent intent = getActivity().getIntent();
        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
    }

    void getPublicMessage(int page){
        publicMessageSwipeRefreshLayout.setRefreshing(true);
        String apiStr = bbsURLUtils.getPublicPMApiUrl(page);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Log.d(TAG,"get public message in page "+page);
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"ERROR in recv api");
                        publicMessageSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        publicMessageSwipeRefreshLayout.setRefreshing(false);
                    }
                });
                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    Log.d(TAG,"Getting PM "+s);
                    bbsParseUtils.noticeNumInfo noticeNumInfo = bbsParseUtils.parseNoticeInfo(s);
                    setNotificationNum(noticeNumInfo);

                    List<bbsParseUtils.publicMessage> publicMessageList = bbsParseUtils.parsePublicMessage(s);
                    if(publicMessageList!=null){
                        Log.d(TAG,"get public message "+publicMessageList.size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(page == 1){
                                    adapter.setPublicMessageList(publicMessageList);
                                    if(publicMessageList.size()==0){
                                        publicMessageEmptyView.setVisibility(View.VISIBLE);
                                    }

                                }
                                else {
                                    if(publicMessageList.size()>0){
                                        publicMessageEmptyView.setVisibility(View.GONE);
                                    }
                                    adapter.addPublicMessageList(publicMessageList);
                                }
                            }
                        });

                        globalPage += 1;
                    }
                    else {

                        publicMessageEmptyView.setVisibility(View.VISIBLE);

                    }

                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void setNotificationNum(bbsParseUtils.noticeNumInfo notificationNum) {
        Log.d(TAG,"set message number "+notificationNum.getAllNoticeInfo());
        if (mListener != null) {
            mListener.setNotificationsNum(notificationNum);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof bbsPrivateMessageFragment.OnNewMessageChangeListener) {
            mListener = (bbsPrivateMessageFragment.OnNewMessageChangeListener) context;
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

    public interface OnNewMessageChangeListener {
        // TODO: Update argument type and name
        void setNotificationsNum(bbsParseUtils.noticeNumInfo notificationsNum);
    }
}
