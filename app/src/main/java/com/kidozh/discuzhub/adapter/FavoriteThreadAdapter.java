package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.entities.FavoriteThread;

public class FavoriteThreadAdapter extends PagedListAdapter<FavoriteThread, RecyclerView.ViewHolder> {
    Context context;
    protected FavoriteThreadAdapter(@NonNull DiffUtil.ItemCallback<FavoriteThread> diffCallback) {
        super(FavoriteThread.DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        
    }
}
