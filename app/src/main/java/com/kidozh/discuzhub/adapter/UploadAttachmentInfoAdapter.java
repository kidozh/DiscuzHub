package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.UploadAttachment;
import com.kidozh.discuzhub.utilities.bbsParseUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadAttachmentInfoAdapter extends RecyclerView.Adapter<UploadAttachmentInfoAdapter.UploadAttachmentViewHolder> {

    Context context;
    List<UploadAttachment> attachmentList;

    public void setAttachmentList(List<UploadAttachment> attachmentList) {
        this.attachmentList = attachmentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UploadAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_upload_attachment,parent,false);
        return new UploadAttachmentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UploadAttachmentViewHolder holder, int position) {
        UploadAttachment uploadAttachment = attachmentList.get(position);
        holder.uploadAttachmentNumber.setText(String.valueOf(position+1));
        holder.uploadAttachmentFilename.setText(uploadAttachment.fileName);
        holder.uploadAttachmentType.setText(String.valueOf(uploadAttachment.aid));
    }

    @Override
    public int getItemCount() {
        if(attachmentList == null){
            return 0;
        }
        else {
            return attachmentList.size();
        }
    }

    public class UploadAttachmentViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_upload_attachment_number)
        TextView uploadAttachmentNumber;
        @BindView(R.id.item_upload_attachment_type)
        TextView uploadAttachmentType;
        @BindView(R.id.item_upload_attachment_filename)
        TextView uploadAttachmentFilename;

        public UploadAttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
