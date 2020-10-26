package com.kidozh.discuzhub.activities.ui.privateMessages;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.bbsPrivateMessageAdapter;
import com.kidozh.discuzhub.databinding.ContentEmptyInformationBinding;
import com.kidozh.discuzhub.databinding.FragmentBbsPrivateMessageBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link bbsPrivateMessageFragment.OnNewMessageChangeListener} interface
 * to handle interaction events.
 * Use the {@link bbsPrivateMessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bbsPrivateMessageFragment extends Fragment {
    private String TAG = bbsPrivateMessageFragment.class.getSimpleName();
    
    FragmentBbsPrivateMessageBinding binding;
    ContentEmptyInformationBinding emptyInfoBinding;

    private OnNewMessageChangeListener mListener;

    public bbsPrivateMessageFragment() {
        // Required empty public constructor
    }
    

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment bbsPrivateMessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    private static String ARG_BBS = "ARG_BBS", ARG_USER = "ARG_USER";

    public static bbsPrivateMessageFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        bbsPrivateMessageFragment fragment = new bbsPrivateMessageFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBbsPrivateMessageBinding.inflate(inflater,container,false);
        emptyInfoBinding = binding.fragment;
        return binding.getRoot();
    }



    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;
    ForumInfo forum;
    private OkHttpClient client = new OkHttpClient();
    bbsPrivateMessageAdapter adapter;
    private int globalPage = 1;

    private int newMessageNum = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureIntentData();
        configureRecyclerview();
        configureSwipeRefreshLayout();
        configureEmptyView();

    }

    @Override
    public void onResume() {
        super.onResume();
        // need to resume it
        globalPage = 1;
        getPrivateMessage(globalPage);
    }

    private void configureRecyclerview(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerview.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        binding.recyclerview.addItemDecoration(dividerItemDecoration);
        adapter = new bbsPrivateMessageAdapter(bbsInfo,userBriefInfo);
        binding.recyclerview.setAdapter(adapter);

    }

    private void configureIntentData(){
        Log.d(TAG,"Recv user"+userBriefInfo);
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getContext(),userBriefInfo);
    }

    private void configureEmptyView(){
        emptyInfoBinding.emptyIcon.setImageResource(R.drawable.ic_empty_private_messages_64px);
        emptyInfoBinding.emptyContent.setText(R.string.empty_private_message);
    }

    void configureSwipeRefreshLayout(){
        
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                globalPage = 1;
                getPrivateMessage(globalPage);
            }
        });
        getPrivateMessage(globalPage);
    }

    void getPrivateMessage(int page){
        URLUtils.setBBS(bbsInfo);
        binding.swipeRefreshLayout.setRefreshing(true);
        String apiStr = URLUtils.getPrivatePMApiUrl(page);
        Request request = new Request.Builder()
                .url(apiStr)
                .build();
        Log.d(TAG,"get private message in page "+apiStr);
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
                    List<bbsParseUtils.privateMessage> privateMessageList = bbsParseUtils.parsePrivateMessage(s);
                    bbsParseUtils.noticeNumInfo noticeNumInfo = bbsParseUtils.parseNoticeInfo(s);
                    setNotificationNum(noticeNumInfo);

                    if(privateMessageList!=null){
                        newMessageNum = 0;
                        for (int i=0;i<privateMessageList.size();i++){
                            bbsParseUtils.privateMessage privateDetailMessage = privateMessageList.get(i);
                            if(privateDetailMessage.isNew){
                                newMessageNum += 1;
                            }
                        }

                        Log.d(TAG,"get public message "+privateMessageList.size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(page == 1){
                                    adapter.setPrivateMessageList(privateMessageList);
                                    if(privateMessageList.size()==0){

                                        emptyInfoBinding.emptyView.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        emptyInfoBinding.emptyView.setVisibility(View.GONE);
                                    }
                                }
                                else {
                                    adapter.addPrivateMessageList(privateMessageList);
                                }
                            }
                        });

                        globalPage += 1;
                    }
                    else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                emptyInfoBinding.emptyView.setVisibility(View.VISIBLE);
                            }
                        });

                    }

                }
            }
        });
    }




    // TODO: Rename method, update argument and hook method into UI event
    public void setNotificationNum(bbsParseUtils.noticeNumInfo notificationNum) {
        //Log.d(TAG,"set message number "+notificationNum.getAllNoticeInfo());
        if (mListener != null) {
            mListener.setNotificationsNum(notificationNum);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewMessageChangeListener) {
            mListener = (OnNewMessageChangeListener) context;
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
    public interface OnNewMessageChangeListener {
        // TODO: Update argument type and name
        void setNotificationsNum(bbsParseUtils.noticeNumInfo notificationsNum);
    }
}
