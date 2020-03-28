package com.kidozh.discuzhub.activities.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link bbsNotificationMessagePortalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bbsNotificationMessagePortalFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_FILTER = "filter";
    public static String TAG = bbsNotificationMessagePortalFragment.class.getSimpleName();
    public static String FILTER_PUBLIC_MESSAGE = "FILTER_PUBLIC_MESSAGE", FILTER_PRIVATE_MESSAGE = "FILTER_PRIVATE_MESSAGE";

    // TODO: Rename and change types of parameters
    private String filter;

    public bbsNotificationMessagePortalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment bbsNotificationMessagePortalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static bbsNotificationMessagePortalFragment newInstance(String filterName) {
        bbsNotificationMessagePortalFragment fragment = new bbsNotificationMessagePortalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filterName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filter = getArguments().getString(ARG_FILTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bbs_notification_message_portal, container, false);
    }





    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private void preLoadMessagesFragment(){

        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        Log.d(TAG,"Pre-load message fragment "+filter);
        preLoadMessagesFragment();
        if(filter.equals("FILTER_PRIVATE_MESSAGE")){
            fragmentTransaction.replace(R.id.fragment_notification_message_fragment, new bbsPrivateMessageFragment());
        }
        else {
            fragmentTransaction.replace(R.id.fragment_notification_message_fragment, new bbsPublicMessageFragment());
        }
        fragmentTransaction.commit();
    }

    public void notifyMessageStatusChanged(){
        switch (filter){
            case "FILTER_PRIVATE_MESSAGE":{
                fragmentTransaction.replace(R.id.fragment_notification_message_fragment, new bbsPrivateMessageFragment());
                break;
            }
            case "FILTER_PUBLIC_MESSAGE":{
                fragmentTransaction.replace(R.id.fragment_notification_message_fragment, new bbsPublicMessageFragment());
                break;
            }
        }
        fragmentTransaction.commit();
    }
}
