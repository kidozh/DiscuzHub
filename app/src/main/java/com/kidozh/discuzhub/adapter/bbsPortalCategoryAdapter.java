package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumCategorySection;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsPortalCategoryAdapter extends RecyclerView.Adapter<bbsPortalCategoryAdapter.bbsShowPortalViewHolder> {

    Context mContext;
    List<forumCategorySection> mCateList;
    public String jsonString;
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;

    bbsPortalCategoryAdapter(Context context){
        this.mContext = context;
    }

    public bbsPortalCategoryAdapter(Context context, String jsonString, bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
        this.mContext = context;
        this.jsonString = jsonString;
        this.bbsInfo = bbsInformation;
        this.curUser = userBriefInfo;
    }

    public void setmCateList(List<forumCategorySection> mCateList){
        this.mCateList = mCateList;
        android.os.Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });

    }

    @NonNull
    @Override
    public bbsShowPortalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_category;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsShowPortalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bbsShowPortalViewHolder holder, int position) {
        forumCategorySection categorySectionFid = mCateList.get(position);
        holder.mPortalCatagoryName.setText(categorySectionFid.name);
        if(categorySectionFid.forumFidList.size()>=4){
            holder.mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,4));
        }
        else {
            holder.mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,4));
        }

        bbsPortalCategoryForumAdapter adapter = new bbsPortalCategoryForumAdapter(mContext,jsonString,bbsInfo,curUser);
        holder.mRecyclerView.setAdapter(adapter);
        adapter.setmCateList(categorySectionFid.forumFidList);
    }

    @Override
    public int getItemCount() {
        if(mCateList == null){
            return 0;
        }
        else {
            return mCateList.size();
        }

    }

    public class bbsShowPortalViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.portal_catagory_name)
        TextView mPortalCatagoryName;
        @BindView(R.id.portal_catagory_recyclerview)
        RecyclerView mRecyclerView;

        public bbsShowPortalViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
