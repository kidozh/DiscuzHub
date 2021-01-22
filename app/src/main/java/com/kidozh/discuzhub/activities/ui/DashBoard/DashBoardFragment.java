package com.kidozh.discuzhub.activities.ui.DashBoard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.ExplorePage.ExplorePageFragment;
import com.kidozh.discuzhub.activities.ui.FavoriteForum.FavoriteForumFragment;
import com.kidozh.discuzhub.activities.ui.FavoriteThread.FavoriteThreadFragment;
import com.kidozh.discuzhub.activities.ui.HotForums.HotForumsFragment;
import com.kidozh.discuzhub.activities.ui.HotThreads.HotThreadsFragment;
import com.kidozh.discuzhub.activities.ui.NewThreads.NewThreadsFragment;
import com.kidozh.discuzhub.databinding.FragmentDashboardBinding;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashBoardFragment extends Fragment {
    private static final String TAG = DashBoardFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BBS = "ARG_BBS";
    private static final String ARG_USER = "ARG_USER";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private bbsInformation bbsInfo;
    private forumUserBriefInfo userBriefInfo;

    DashBoardViewModel viewModel;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DashBoardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashBoardFragment newInstance(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo) {
        DashBoardFragment fragment = new DashBoardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BBS, bbsInfo);
        args.putSerializable(ARG_USER,userBriefInfo);
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
        binding = FragmentDashboardBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    FragmentDashboardBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
        bindTabLayoutAndViewPager2();
    }

    private void bindTabLayoutAndViewPager2(){
        viewModel.setFavoriteThreadInfo(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0);
        binding.viewpager2.setAdapter(new DashBoardViewPagerAdapter(getChildFragmentManager(),getLifecycle()));
        binding.viewpager2.setUserInputEnabled(false);

        new TabLayoutMediator(binding.tablayout,binding.viewpager2,
                (tab, position) -> {
            switch (position){
                case 0:{
                    tab.setText(R.string.hot_thread);
                    tab.setIcon(R.drawable.ic_baseline_thread_24);
                    break;
                }
                case 1:{
                    tab.setText(R.string.dashboard_new_threads);
                    tab.setIcon(R.drawable.ic_baseline_quickreply_24);
                    break;
                }
                case 2:{
                    tab.setText(R.string.hot_forum);
                    tab.setIcon(R.drawable.ic_baseline_forum_24);
                    break;
                }
                case 3:{
                    tab.setText(R.string.dashboard_explore_fragment);
                    tab.setIcon(R.drawable.ic_baseline_explore_24);
                    break;
                }
                case 4:{
                    tab.setText(R.string.marked_thread);
                    tab.setIcon(R.drawable.ic_baseline_marked_thread_24);
                    break;
                }
                case 5:{
                    tab.setText(R.string.marked_forum);
                    tab.setIcon(R.drawable.ic_baseline_bookmarks_24);
                    break;
                }

            }
            Log.d(TAG,"Pos "+position);
        }).attach();




    }

    private class DashBoardViewPagerAdapter extends FragmentStateAdapter{

        public DashBoardViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        public DashBoardViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0:{
                    return HotThreadsFragment.newInstance(bbsInfo,userBriefInfo);
                }
                case 1:{
                    return NewThreadsFragment.Companion.newInstance(bbsInfo,userBriefInfo);
                }
                case 2:{
                    return HotForumsFragment.newInstance(bbsInfo,userBriefInfo);
                }
                case 3:{
                    return ExplorePageFragment.Companion.newInstance(bbsInfo,userBriefInfo);
                }
                case 4:{
                    return FavoriteThreadFragment.newInstance(bbsInfo,userBriefInfo,"tid");
                }
                case 5:{
                    return FavoriteForumFragment.newInstance(bbsInfo,userBriefInfo);
                }
            }
            return HotThreadsFragment.newInstance(bbsInfo,userBriefInfo);
        }

        @Override
        public int getItemCount() {
            return 6;
        }


    }
}