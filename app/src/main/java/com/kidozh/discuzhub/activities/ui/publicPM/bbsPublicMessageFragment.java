package com.kidozh.discuzhub.activities.ui.publicPM;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.adapter.PublicMessageAdapter;
import com.kidozh.discuzhub.databinding.ContentEmptyInformationBinding;
import com.kidozh.discuzhub.databinding.FragmentPublicMessageBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class bbsPublicMessageFragment extends Fragment {
    private final static String TAG = bbsPublicMessageFragment.class.getSimpleName();

    FragmentPublicMessageBinding binding;
    ContentEmptyInformationBinding emptyBinding;

    private bbsPrivateMessageFragment.OnNewMessageChangeListener mListener;

    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    private OkHttpClient client = new OkHttpClient();
    PublicMessageAdapter adapter;
    private int globalPage = 1;

    public bbsPublicMessageFragment(){

    }

    private bbsPublicMessageFragment(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo){
        this.bbsInfo= bbsInfo;
        this.userBriefInfo = userBriefInfo;
    }

    private static String ARG_BBS = "ARG_BBS", ARG_USER = "ARG_USER";

    public static bbsPublicMessageFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        bbsPublicMessageFragment fragment = new bbsPublicMessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BBS, bbsInfo);
        args.putSerializable(ARG_USER, userBriefInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(ARG_BBS);
            userBriefInfo = (forumUserBriefInfo) getArguments().getSerializable(ARG_USER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPublicMessageBinding.inflate(inflater,container,false);
        emptyBinding = binding.emptyView;
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        configureEmptyView();
        getPublicMessage(1);

    }

    private void configureEmptyView(){

        emptyBinding.emptyIcon.setImageResource(R.drawable.ic_empty_public_message_64px);
        emptyBinding.emptyContent.setText(R.string.empty_public_message);
    }

    void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        binding.recyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new PublicMessageAdapter();
        binding.recyclerview.setAdapter(adapter);
    }

    void configureSwipeRefreshLayout(){
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                getPublicMessage(globalPage);
            }
        });
    }


    private void configureIntentData(){
//        if(bbsInfo !=null){
//            return;
//        }
//        Intent intent = getActivity().getIntent();
//        forum = intent.getParcelableExtra(bbsConstUtils.PASS_FORUM_THREAD_KEY);
//        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
//        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
    }

    void getPublicMessage(int page){
        binding.swipeRefreshLayout.setRefreshing(true);
        String apiStr = URLUtils.getPublicPMApiUrl(page);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Log.d(TAG,"get public message in page "+apiStr);
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"ERROR in recv api");
                        binding.swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipeRefreshLayout.setRefreshing(false);
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
                                        emptyBinding.emptyView.setVisibility(View.VISIBLE);
                                    }

                                }
                                else {
                                    if(publicMessageList.size()>0){
                                        emptyBinding.emptyView.setVisibility(View.GONE);
                                    }
                                    adapter.addPublicMessageList(publicMessageList);
                                }
                            }
                        });

                        globalPage += 1;
                    }
                    else {

                        emptyBinding.emptyView.setVisibility(View.VISIBLE);

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
