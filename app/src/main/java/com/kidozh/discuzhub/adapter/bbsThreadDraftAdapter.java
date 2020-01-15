package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.bbsPostThreadActivity;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsThreadDraft;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.timeDisplayUtils;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsThreadDraftAdapter extends RecyclerView.Adapter<bbsThreadDraftAdapter.ViewHolder> {

    List<bbsThreadDraft> bbsThreadDraftList;
    Context context;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInfo;

    public bbsThreadDraftAdapter(bbsInformation bbsInfo,forumUserBriefInfo userBriefInfo){
        this.userBriefInfo = userBriefInfo;
        this.bbsInfo = bbsInfo;
    }

    public void setBbsThreadDraftList(List<bbsThreadDraft> bbsThreadDraftList) {
        this.bbsThreadDraftList = bbsThreadDraftList;
        notifyDataSetChanged();
    }

    public List<bbsThreadDraft> getBbsThreadDraftList() {
        return bbsThreadDraftList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_thread_draft;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bbsThreadDraft threadDraft = bbsThreadDraftList.get(position);
        if(threadDraft.typeName.isEmpty()){
            holder.bbsThreadDraftType.setVisibility(View.GONE);
        }
        else {
            holder.bbsThreadDraftType.setVisibility(View.VISIBLE);
            holder.bbsThreadDraftType.setText(threadDraft.typeName);
        }
        holder.bbsThreadDraftMessage.setText(threadDraft.content);
        holder.bbsThreadDraftType.setText(threadDraft.typeName);
        if(threadDraft.subject.isEmpty()){
            holder.bbsThreadDraftSubject.setText(R.string.bbs_not_set);
        }
        else {
            holder.bbsThreadDraftSubject.setText(threadDraft.subject);
        }

        holder.bbsThreadDraftForum.setText(threadDraft.forumName);
        holder.bbsThreadDraftUpdateTime.setText(timeDisplayUtils.getLocalePastTimeString(context,threadDraft.lastUpdateAt));
        holder.bbsThreadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, bbsPostThreadActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(bbsConstUtils.PASS_THREAD_DRAFT_KEY,threadDraft);
                intent.putExtra("fid",threadDraft.fid);
                intent.putExtra("fid_name",threadDraft.forumName);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        if(bbsThreadDraftList == null){
            return 0;
        }
        else {
            return bbsThreadDraftList.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bbs_thread_draft_type)
        TextView bbsThreadDraftType;
        @BindView(R.id.bbs_thread_draft_subject)
        TextView bbsThreadDraftSubject;
        @BindView(R.id.bbs_thread_draft_message)
        TextView bbsThreadDraftMessage;
        @BindView(R.id.bbs_thread_draft_update_time)
        TextView bbsThreadDraftUpdateTime;
        @BindView(R.id.bbs_thread_draft_forum)
        TextView bbsThreadDraftForum;
        @BindView(R.id.bbs_thread_draft_cardview)
        CardView bbsThreadCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
