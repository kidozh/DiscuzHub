package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.utilities.ListItemClickListener;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by free2 on 16-5-1.
 * 表情adapter
 */
public class SmileyAdapter extends RecyclerView.Adapter<SmileyAdapter.SmileyViewHolder> {

    private ListItemClickListener itemListener;
    private Context context;

    private List<bbsParseUtils.smileyInfo> smileyInfos;

    public SmileyAdapter(Context context, ListItemClickListener itemListener) {
        this.itemListener = itemListener;
        this.context = context;
    }

    public void setSmileyInfos(List<bbsParseUtils.smileyInfo> smileyInfos) {
        this.smileyInfos = smileyInfos;
        notifyDataSetChanged();
    }

    public List<bbsParseUtils.smileyInfo> getSmileyInfos() {
        return smileyInfos;
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
        if(smileyInfos != null){
            return smileyInfos.size();
        }
        else {
            return 0;
        }
    }

    class SmileyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_bbs_smiley_imageview)
        ImageView image;

        SmileyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            image.setOnClickListener(view -> itemListener.onListItemClick(image, getAdapterPosition()));
        }


        private void setSmiley(int position) {
            bbsParseUtils.smileyInfo smileyInfo = smileyInfos.get(position);
            Glide.with(context).
                    load(URLUtils.getSmileyImageUrl(smileyInfo.imageRelativePath))
                    .into(image);
            //Glide.with(context).load(smileys.get(position).first).into(image);
            //Picasso.get().load(smileys.get(position).first).into(image);
        }


    }


}
