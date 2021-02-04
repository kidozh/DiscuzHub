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
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.Forum;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.DiscuzIndexResult;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;

import java.util.ArrayList;
import java.util.List;



public class ForumCategoryAdapter extends RecyclerView.Adapter<ForumCategoryAdapter.bbsShowPortalViewHolder> {
    private final static String TAG = ForumCategoryAdapter.class.getSimpleName();
    Context mContext;
    List<DiscuzIndexResult.ForumCategory> forumCategoryList = new ArrayList<>();
    Discuz bbsInfo;
    forumUserBriefInfo curUser;
    List<Forum> allForum;

    ForumCategoryAdapter(Context context){
        this.mContext = context;
    }

    public ForumCategoryAdapter(Discuz Discuz, forumUserBriefInfo userBriefInfo){

        this.bbsInfo = Discuz;
        this.curUser = userBriefInfo;
    }

    public void setForumCategoryList(@NonNull List<DiscuzIndexResult.ForumCategory> forumCategoryList, List<Forum> allForum) {
        int oldSize = this.forumCategoryList.size();
        this.forumCategoryList.clear();
        notifyItemRangeRemoved(0,oldSize);
        this.forumCategoryList.addAll(forumCategoryList);
        this.allForum = allForum;

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
        DiscuzIndexResult.ForumCategory category = forumCategoryList.get(position);
        holder.mPortalCatagoryName.setText(category.name);
        if(UserPreferenceUtils.conciseRecyclerView(mContext)){
            holder.mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,4));
        }
        else {
            holder.mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2));
            //holder.mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        }

        ForumAdapter adapter = new ForumAdapter(bbsInfo,curUser);
        holder.mRecyclerView.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(mContext));
        holder.mRecyclerView.setAdapter(adapter);
        List<Forum> forumListInTheCategory = category.getForumListInTheCategory(allForum);
        adapter.setForumList(forumListInTheCategory);



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
