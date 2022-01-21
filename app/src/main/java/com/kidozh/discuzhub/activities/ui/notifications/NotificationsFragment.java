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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.BuildConfig;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.UserNotification.UserNotificationFragment;
import com.kidozh.discuzhub.activities.ui.bbsNotificationMessagePortalFragment;
import com.kidozh.discuzhub.activities.ui.privacyProtect.privacyProtectFragment;
import com.kidozh.discuzhub.activities.ui.userThreads.UserThreadFragment;
import com.kidozh.discuzhub.databinding.FragmentNotificationsBinding;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.utilities.bbsParseUtils;


public class NotificationsFragment extends Fragment {
    private static final String TAG = NotificationsFragment.class.getSimpleName();

    private NotificationsViewModel notificationsViewModel;
    
    FragmentNotificationsBinding binding;
    
    private notificationViewPagerAdapter adapter;

    Discuz Discuz;
    User userBriefInfo;

    PopupMenu popupMenu;

    public NotificationsFragment(){

    }

    public NotificationsFragment(Discuz Discuz, User userBriefInfo){
        this.Discuz = Discuz;
        this.userBriefInfo = userBriefInfo;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        binding = FragmentNotificationsBinding.inflate(inflater,container,false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViewPager();
    }

    boolean lastSelected = false;

    void configureViewPager(){
        Log.d(TAG, "Configuring notification fragment");
        binding.notificationsTablayout.setupWithViewPager(binding.notificationsViewpager);
        adapter  = new notificationViewPagerAdapter(getChildFragmentManager());
        binding.notificationsViewpager.setAdapter(adapter);
        setCustomViewToTabLayout();

        binding.notificationsTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        setCustomViewToTabLayout();
    }

    void setCustomViewToTabLayout(){

        for(int i=0;i<binding.notificationsTablayout.getTabCount();i++){

            TabLayout.Tab tab = binding.notificationsTablayout.getTabAt(i);
            if(tab!=null){
                switch (i){
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
                        tab.setText(R.string.bbs_notification_my_thread);
                        break;
                    }
                    case 2:{
                        tab.setIcon(R.drawable.ic_security_24px);
                        tab.setText(R.string.bbs_notification_system);
                        break;
                    }
                    case 3:{
                        tab.setIcon(R.drawable.ic_view_list_24px);
                        tab.setText(R.string.bbs_notification_thread_list);
                        break;
                    }
                }
            }


        }
    }

    void showPopupWhenTabSelected(int index){
        TabLayout.Tab messageTab = binding.notificationsTablayout.getTabAt(index);
        if(messageTab == null|| index >2){
            return;
        }
        Log.d(TAG,"add popMenu to list "+index);
        popupMenu = new PopupMenu(getActivity().getApplicationContext(), messageTab.view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
            // first is removed!!!
            index += 1;
        }
        if (index == 0) {
            inflater.inflate(R.menu.menu_notification_message, popupMenu.getMenu());
            TabLayout.Tab messageTabInstance = binding.notificationsTablayout.getTabAt(0);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_notification_messages_pm: {
                            adapter.message_state = bbsNotificationMessagePortalFragment.FILTER_PRIVATE_MESSAGE;
                            messageTabInstance.setText(R.string.bbs_notification_my_pm);

                            break;
                        }
                        case R.id.menu_notification_message_public_message: {
                            adapter.message_state = bbsNotificationMessagePortalFragment.FILTER_PUBLIC_MESSAGE;
                            messageTabInstance.setText(R.string.bbs_notification_public_pm);
                            break;
                        }
                    }
                    Log.d(TAG, "Adapter change message status " + adapter.message_state);
                    updateTabAndFragment(0);
                    return true;
                }
            });
            popupMenu.show();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(popupMenu !=null){
            popupMenu.dismiss();
        }

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
                        return bbsNotificationMessagePortalFragment.newInstance(bbsNotificationMessagePortalFragment.FILTER_PRIVATE_MESSAGE, Discuz,userBriefInfo);
                    }
                    else {
                        Log.d(TAG,"GET PUBLIC MESSAGE STATE");
                        return bbsNotificationMessagePortalFragment.newInstance(bbsNotificationMessagePortalFragment.FILTER_PUBLIC_MESSAGE, Discuz,userBriefInfo);
                    }

                }

                case 1:{
                    return UserNotificationFragment.newInstance("mypost","", Discuz,userBriefInfo);

                }
                case 2:{
                    return UserNotificationFragment.newInstance("system","system", Discuz,userBriefInfo);

                }

                case 3:{
                    return UserThreadFragment.newInstance(Discuz,userBriefInfo);

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
                    return getString(R.string.bbs_notification_system);
                case 3:
                    return getString(R.string.bbs_notification_app);
                    default:
                        return "";
            }
        }

        @Override
        public int getCount() {
            if(BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")){
                // first is removed!!!
                return 3;
            }
            else {
                return 4;
            }

        }
    }



    @SuppressLint("RestrictedApi")
    public void setNewMessageNum(int i) {
    }



    public void renderTabNumber(bbsParseUtils.noticeNumInfo noticeNumInfo){
        if(noticeNumInfo == null){
            return;
        }
        try{
            if(noticeNumInfo.pm!=0){
                BadgeDrawable badgeDrawable = binding.notificationsTablayout.getTabAt(0).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.pm);
            }
            else{
                binding.notificationsTablayout.getTabAt(0).removeBadge();
            }
            if(noticeNumInfo.mypost!=0){
                BadgeDrawable badgeDrawable = binding.notificationsTablayout.getTabAt(1).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.mypost);
            }
            else{
                binding.notificationsTablayout.getTabAt(1).removeBadge();
            }
            if(noticeNumInfo.prompt!=0){
                BadgeDrawable badgeDrawable = binding.notificationsTablayout.getTabAt(3).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.prompt);
            }
            else{
                binding.notificationsTablayout.getTabAt(3).removeBadge();
            }
            if(noticeNumInfo.push!=0){
                BadgeDrawable badgeDrawable = binding.notificationsTablayout.getTabAt(2).getOrCreateBadge();
                badgeDrawable.setNumber(noticeNumInfo.push);
            }
            else{
                binding.notificationsTablayout.getTabAt(2).removeBadge();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}