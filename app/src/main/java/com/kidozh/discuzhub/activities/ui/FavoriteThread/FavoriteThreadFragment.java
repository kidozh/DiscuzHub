package com.kidozh.discuzhub.activities.ui.FavoriteThread;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteThreadFragment extends Fragment {

    private FavoriteThreadViewModel mViewModel;

    public static FavoriteThreadFragment newInstance() {
        return new FavoriteThreadFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorite_thread_fragment, container, false);
    }

    @BindView(R.id.blank_favorite_thread_view)
    View blankFavoriteThreadView;
    @BindView(R.id.favorite_thread_recyclerview)
    RecyclerView favoriteThreadRecyclerview;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FavoriteThreadViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
    }
}