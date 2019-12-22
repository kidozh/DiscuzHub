package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.utilities.ListItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by free2 on 16-5-1.
 * 表情adapter
 */
public class bbsSmileyAdapter extends RecyclerView.Adapter<bbsSmileyAdapter.SmileyViewHolder> {

    private List<Pair<String, String>> smileys = new ArrayList<>();
    private ListItemClickListener itemListener;
    private Context context;

    public bbsSmileyAdapter(Context context,ListItemClickListener itemListener, List<Pair<String, String>> smileys) {
        this.smileys = smileys;
        this.itemListener = itemListener;
        this.context = context;
    }


    @Override
    public SmileyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SmileyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bbs_smiley, parent, false));
    }

    @Override
    public void onBindViewHolder(SmileyViewHolder holder, int position) {
        holder.setSmiley(position);
    }


    @Override
    public int getItemCount() {
        return smileys.size();
    }

    class SmileyViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        SmileyViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.smiley);
            image.setOnClickListener(view -> itemListener.onListItemClick(image, getAdapterPosition()));
        }


        private void setSmiley(int position) {
            Glide.with(context).load(smileys.get(position).first).into(image);
            //Picasso.get().load(smileys.get(position).first).into(image);
        }


    }


}
