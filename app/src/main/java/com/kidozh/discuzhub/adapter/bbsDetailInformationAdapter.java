package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsDetailInformationAdapter extends RecyclerView.Adapter<bbsDetailInformationAdapter.ViewHolder> {

    private List<bbsKV> bbsInformationList;
    private Context context;

    public bbsDetailInformationAdapter(List<bbsKV> bbsInformationList){
        this.bbsInformationList = bbsInformationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_information;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bbsKV bbsInfo = bbsInformationList.get(position);
        holder.bbsInformationAvatar.setImageResource(bbsInfo.resource);
        holder.bbsInformationTitle.setText(bbsInfo.key);
        if(bbsInfo.value == null ||bbsInfo.value.equals("")){
            holder.bbsInformationValue.setVisibility(View.GONE);
        }
        else {
            holder.bbsInformationValue.setVisibility(View.VISIBLE);
        }
        holder.bbsInformationValue.setText(bbsInfo.value);
    }

    @Override
    public int getItemCount() {
        if(bbsInformationList == null){
            return 0;
        }
        else {
            return bbsInformationList.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        ImageView bbsInformationAvatar;
        TextView bbsInformationTitle;
        TextView bbsInformationValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bbsInformationAvatar = itemView.findViewById(R.id.item_bbs_information_avatar);
            bbsInformationTitle = itemView.findViewById(R.id.item_bbs_information_title);
            bbsInformationValue = itemView.findViewById(R.id.item_bbs_information_value);
        }
    }

    public static class bbsKV{
        public String key,value;
        public int resource;
        public bbsKV(int resource, String key, String value){
            this.resource = resource;
            this.key = key;
            this.value = value;
        }
    }
}
