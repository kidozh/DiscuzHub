package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.util.ArrayList;
import java.util.List;



public class ForumCategoryAdapter extends RecyclerView.Adapter<ForumCategoryAdapter.bbsShowPortalViewHolder> {
    private final static String TAG = ForumCategoryAdapter.class.getSimpleName();
    Context mContext;
    List<BBSIndexResult.ForumCategory> forumCategoryList = new ArrayList<>();
    bbsInformation bbsInfo;
    forumUserBriefInfo curUser;
    List<ForumInfo> allForumInfo;

    ForumCategoryAdapter(Context context){
        this.mContext = context;
    }

    public ForumCategoryAdapter( bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){

        this.bbsInfo = bbsInformation;
        this.curUser = userBriefInfo;
    }

    public void setForumCategoryList(@NonNull List<BBSIndexResult.ForumCategory> forumCategoryList, List<ForumInfo> allForumInfo) {
        int oldSize = this.forumCategoryList.size();
        this.forumCategoryList.clear();
        notifyItemRangeRemoved(0,oldSize);
        this.forumCategoryList.addAll(forumCategoryList);
        this.allForumInfo = allForumInfo;

        //notifyDataSetChanged();
        notifyItemRangeInserted(0,forumCategoryList.size());
        Log.d(TAG,"insert number "+getItemCount()+" "+this.forumCategoryList);
    }


    @NonNull
    @Override
    public bbsShowPortalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        mContext = context;
        int layoutIdForListItem = R.layout.item_forum_category;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsShowPortalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bbsShowPortalViewHolder holder, int position) {
        BBSIndexResult.ForumCategory category = forumCategoryList.get(position);
        holder.mPortalCatagoryName.setText(category.name);
        if(UserPreferenceUtils.conciseRecyclerView(mContext)){
            holder.mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,4));
        }
        else {
            holder.mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2));
            //holder.mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        }

        ForumAdapter adapter = new ForumAdapter(bbsInfo,curUser);
        holder.mRecyclerView.setAdapter(adapter);
        List<ForumInfo> forumInfoListInTheCategory = category.getForumListInTheCategory(allForumInfo);
        adapter.setForumInfoList(forumInfoListInTheCategory);



    }

    @Override
    public int getItemCount() {
        if(forumCategoryList == null){
            return 0;
        }
        else {
            return forumCategoryList.size();
        }

    }

    public class bbsShowPortalViewHolder extends RecyclerView.ViewHolder{
        TextView mPortalCatagoryName;
        RecyclerView mRecyclerView;
        ImageView mPortalCategoryIcon;

        public bbsShowPortalViewHolder(@NonNull View itemView) {
            super(itemView);
            mPortalCatagoryName = itemView.findViewById(R.id.portal_catagory_name);
            mRecyclerView = itemView.findViewById(R.id.portal_catagory_recyclerview);
            mPortalCategoryIcon = itemView.findViewById(R.id.portal_category_icon);
        }
    }
}
