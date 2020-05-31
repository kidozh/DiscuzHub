package com.kidozh.discuzhub.activities.ui.notifications;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.BuildConfig;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment;
import com.kidozh.discuzhub.activities.ui.bbsNotificationMessagePortalFragment;
import com.kidozh.discuzhub.activities.ui.privacyProtect.privacyProtectFragment;
import com.kidozh.discuzhub.activities.ui.userThreads.bbsMyThreadFragment;
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsFragment extends Fragment {
    private static final String TAG = NotificationsFragment.class.getSimpleName();
    @BindView(R.id.fragment_notifications_viewpager)
    ViewPager fragmentNotificationViewPager;
    @BindView(R.id.fragment_notifications_tablayout)
    TabLayout fragmentNotificationTabLayout;

    private NotificationsViewModel notificationsViewModel;
    private int privateNewMessageNum = -1;

    public NotificationsFragment(int privateMessage){
        this.privateNewMessageNum = privateMessage;
    }
    private notificationViewPagerAdapter adapter;

    public NotificationsFragment(){
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this,root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViewPager();
    }

    boolean lastSelected = false;

    void configureViewPager(){
        Log.d(TAG, "Configuring notification fragment");
        fragmentNotificationTabLayout.setupWithViewPager(fragmentNotificationViewPager);
        adapter  = new notificationViewPagerAdapter(getChildFragmentManager());
        fragmentNotificationViewPager.setAdapter(adapter);
        setCustomViewToTabLayout();

        fragmentNotificationTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG,"Tab is selected "+tab.getTag());
                String RESELECTED_TAG = "SELECTED";
                if(!lastSelected){
                    int position = tab.getPosition();
                    showPopupWhenTabSelected(position);
                    lastSelected = true;
                    switch (tab.getPosition()){
                        case 1:{
                            adapter.view = "mypost";
                            adapter.type = "post";

                            break;
                        }
                        case 2:{
                            adapter.view = "interactive";
                            adapter.type = "poke";

                            break;
                        }
                    }

                }
                else {
                    lastSelected = false;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG,"tab is unselected "+tab.getPosition());
                lastSelected = false;
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG,"Tab is reselected");
                String RESELECTED_TAG = "SELECTED";
                showPopupWhenTabSelected(tab.getPosition());
                tab.setTag(RESELECTED_TAG);
//                if(tab!=null){
//                    showPopupWhenTabSelected(tab.getPosition());
//                    tab.setTag(RESELECTED_TAG);
//                }
//                else {
//                    tab.setTag(null);
//                }

            }
        });


    }
    void updateTabAndFragment(int position){
        // adapter.notifyDataSetChanged();
        // refresh the fragment
        adapter.notifyDataSetChanged();


        //fragmentNotificationViewPager.setAdapter(adapter);
        //fragmentNotificationTabLayout.selectTab(fragmentNotificationTabLayout.getTabAt(position));
        setCustomViewToTabLayout();
    }

    void setCustomViewToTabLayout(){

        for(int i=0;i<fragmentNotificationTabLayout.getTabCount();i++){

            TabLayout.Tab tab = fragmentNotificationTabLayout.getTabAt(i);
            int index = i;
            if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
                // first is removed!!!
                index += 1;
            }
            if(tab!=null){
                switch (index){
                    case 0:{
                        tab.setIcon(R.drawable.ic_message_24px);
                        switch (adapter.message_state){
                            case "FILTER_PRIVATE_MESSAGE":{
                                tab.setText(R.string.bbs_notification_my_pm);
                                break;
                            }
                            case "FILTER_PUBLIC_MESSAGE":{
                                tab.setText(R.string.bbs_notification_public_pm);
                                break;
                            }
                            default:{
                                tab.setText(R.string.bbs_notification_messages);
                            }

                        }

                        break;
                    }
                    case 1:{
                        tab.setIcon(R.drawable.ic_book_24px);
                        switch (adapter.type){
                            case "post":{
                                tab.setText(R.string.bbs_notification_myThread_thread);
                                break;
                            }
                            case "pcomment":{
                                tab.setText(R.string.bbs_notification_myThread_comment);
                                break;
                            }
                            case "activity":{
                                tab.setText(R.string.bbs_notification_myThread_activity);
                                break;
                            }
                            case "reward":{
                                tab.setText(R.string.bbs_notification_myThread_reward);
                                break;
                            }
                            case "goods":{
                                tab.setText(R.string.bbs_notification_myThread_good);
                                break;
                            }
                            case "at":{
                                tab.setText(R.string.bbs_notification_myThread_mentioned);
                                break;
                            }
                            default:{
                                tab.setText(R.string.bbs_notification_my_thread);
                                break;
                            }

                        }

                        break;
                    }
                    case 2:{
                        tab.setIcon(R.drawable.ic_record_voice_over_24px);
                        switch (adapter.type){
                            case "poke":{
                                tab.setText(R.string.bbs_notification_interact_poke);
                                break;
                            }
                            case "friend":{
                                tab.setText(R.string.bbs_notification_interact_friend);
                                break;
                            }
                            case "wall":{
                                tab.setText(R.string.bbs_notification_interact_wall);
                                break;
                            }
                            case "comment":{
                                tab.setText(R.string.bbs_notification_interact_comment);
                                break;
                            }
                            case "click":{
                                tab.setText(R.string.bbs_notification_interact_agree);
                                break;
                            }
                            case "sharenotice":{
                                tab.setText(R.string.bbs_notification_interact_share);
                                break;
                            }
                            default:{
                                tab.setText(R.string.bbs_notification_interact);

                            }

                        }

                        break;
                    }
                    case 3:{
                        tab.setIcon(R.drawable.ic_security_24px);
                        tab.setText(R.string.bbs_notification_system);
                        break;
                    }
                    case 4:{
                        tab.setIcon(R.drawable.ic_view_list_24px);
                        tab.setText(R.string.bbs_notification_thread_list);
                        break;
                    }
                }
            }


        }
    }

    void showPopupWhenTabSelected(int index){
        TabLayout.Tab messageTab = fragmentNotificationTabLayout.getTabAt(index);
        if(messageTab == null|| index >2){
            return;
        }
        Log.d(TAG,"add popMenu to list "+index);
        PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), messageTab.view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
            // first is removed!!!
            index += 1;
        }
        switch (index){
            case 0:{
                inflater.inflate(R.menu.menu_notification_message,popupMenu.getMenu());
                TabLayout.Tab messageTabInstance = fragmentNotificationTabLayout.getTabAt(0);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menu_notification_messages_pm:{
                                adapter.message_state = bbsNotificationMessagePortalFragment.FILTER_PRIVATE_MESSAGE;
                                messageTabInstance.setText(R.string.bbs_notification_my_pm);

                                break;
                            }
                            case R.id.menu_notification_message_public_message:{
                                adapter.message_state = bbsNotificationMessagePortalFragment.FILTER_PUBLIC_MESSAGE;
                                messageTabInstance.setText(R.string.bbs_notification_public_pm);
                                break;
                            }
                        }
                        Log.d(TAG,"Adapter change message status "+adapter.message_state);
                        updateTabAndFragment(0);
                        return true;
                    }
                });
                break;
            }
            case 1:{
                inflater.inflate(R.menu.menu_notification_my_thread,popupMenu.getMenu());
                TabLayout.Tab tab = fragmentNotificationTabLayout.getTabAt(1);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        adapter.view = "mypost";
                        switch (item.getItemId()){
                            case R.id.menu_notification_myThread_thread:{

                                adapter.type="post";
                                tab.setText(R.string.bbs_notification_myThread_thread);

                                break;
                            }
                            case R.id.menu_notification_myThread_comment:{
                                adapter.type = "pcomment";
                                tab.setText(R.string.bbs_notification_myThread_comment);
                                break;
                            }
                            case R.id.menu_notification_myThread_activity:{
                                adapter.type = "activity";
                                tab.setText(R.string.bbs_notification_myThread_activity);
                                break;
                            }
                            case R.id.menu_notification_myThread_reward:{
                                adapter.type = "reward";
                                tab.setText(R.string.bbs_notification_myThread_reward);
                                break;
                            }
                            case R.id.menu_notification_myThread_good:{
                                adapter.type = "goods";
                                tab.setText(R.string.bbs_notification_myThread_good);
                                break;
                            }
                            case R.id.menu_notification_myThread_mentioned:{
                                adapter.type = "at";
                                tab.setText(R.string.bbs_notification_myThread_mentioned);
                                break;
                            }
                        }
                        updateTabAndFragment(1);
                        return true;
                    }
                });
                break;
            }
            case 2: {
                inflater.inflate(R.menu.menu_notification_interact, popupMenu.getMenu());

                TabLayout.Tab tab = fragmentNotificationTabLayout.getTabAt(2);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        adapter.view = "interactive";
                        switch (item.getItemId()){
                            case R.id.menu_notification_interact_poke:{
                                adapter.type="poke";
                                tab.setText(R.string.bbs_notification_interact_poke);
                                break;
                            }
                            case R.id.menu_notification_interact_friend:{
                                adapter.type="friend";
                                tab.setText(R.string.bbs_notification_interact_friend);
                                break;
                            }
                            case R.id.menu_notification_interact_wall:{
                                adapter.type="wall";
                                tab.setText(R.string.bbs_notification_interact_wall);
                                break;
                            }
                            case R.id.menu_notification_interact_comment:{
                                adapter.type="comment";
                                tab.setText(R.string.bbs_notification_interact_comment);
                                break;
                            }
                            case R.id.menu_notification_interact_agree:{
                                adapter.type="click";
                                tab.setText(R.string.bbs_notification_interact_agree);
                                break;
                            }
                            case R.id.menu_notification_interact_share:{
                                adapter.type="sharenotice";
                                tab.setText(R.string.bbs_notification_interact_share);
                                break;
                            }

                        }
                        updateTabAndFragment(2);
                        return true;
                    }
                });
                break;
            }
        }
        popupMenu.show();
    }




    public class notificationViewPagerAdapter extends FragmentStatePagerAdapter {
        public String message_state = bbsNotificationMessagePortalFragment.FILTER_PRIVATE_MESSAGE;
        public String view="",type="";

        notificationViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }


        public notificationViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public int getItemPosition(Object object) {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
                // first is removed!!!
                position += 1;
            }
            switch (position){
                case 0:{
                    Log.d(TAG,"Position "+position+" message "+message_state);
                    if(message_state.equals(bbsNotificationMessagePortalFragment.FILTER_PRIVATE_MESSAGE)){
                        Log.d(TAG,"GET PRIVATE MESSAGE STATE");
                        return bbsNotificationMessagePortalFragment.newInstance(bbsNotificationMessagePortalFragment.FILTER_PRIVATE_MESSAGE);
                    }
                    else {
                        Log.d(TAG,"GET PUBLIC MESSAGE STATE");
                        return bbsNotificationMessagePortalFragment.newInstance(bbsNotificationMessagePortalFragment.FILTER_PUBLIC_MESSAGE);
                    }

                }


                case 1:{
                    if(view == null || type == null){
                        return UserNotificationFragment.newInstance("mypost","post");
                    }
                    else {
                        return UserNotificationFragment.newInstance(view,type);
                    }

                }


                case 2:{
                    if(view == null || type == null){
                        return UserNotificationFragment.newInstance("interactive","poke");
                    }
                    else {
                        return UserNotificationFragment.newInstance(view,type);
                    }

                }
                case 3:{
                    return UserNotificationFragment.newInstance("system","system");

                }

                case 4:{
                    return new bbsMyThreadFragment();

                }
            }

            Log.d(TAG,"The number is not selected "+position);
            return new privacyProtectFragment();

        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
                // first is removed!!!
                position += 1;
            }
            switch (position){

                case 0:
                    return getString(R.string.bbs_notification_messages);
                case 1:
                    return getString(R.string.bbs_notification_thread);
                case 2:
                    return getString(R.string.bbs_notification_interact);
                case 3:
                    return getString(R.string.bbs_notification_system);
                case 4:
                    return getString(R.string.bbs_notification_app);
                    default:
                        return "";
            }
        }

        @Override
        public int getCount() {
            if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
                // first is removed!!!
                return 4;
            }
            else {
                return 5;
            }

        }
    }



    @SuppressLint("RestrictedApi")
    public void setNewMessageNum(int i) {
        return;
    }


    public interface onPrivateMessageChangeListener{
        public void setPrivateMessageNum(int privateMessageNum);

    }

    public void renderTabNumber(bbsParseUtils.noticeNumInfo noticeNumInfo){
        if(noticeNumInfo == null){
            return;
        }
        try{
            if(noticeNumInfo.pm!=0){
                BadgeDrawable badgeDrawable = fragmentNotificationTabLayout.getTabAt(0).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.pm);
            }
            if(noticeNumInfo.mypost!=0){
                BadgeDrawable badgeDrawable = fragmentNotificationTabLayout.getTabAt(1).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.mypost);
            }
            if(noticeNumInfo.prompt!=0){
                BadgeDrawable badgeDrawable = fragmentNotificationTabLayout.getTabAt(3).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.prompt);
            }
            if(noticeNumInfo.push!=0){
                BadgeDrawable badgeDrawable = fragmentNotificationTabLayout.getTabAt(2).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.push);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}