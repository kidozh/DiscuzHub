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

import java.util.List;


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
        if (attachmentList == null) {
            return 0;
        } else {
            return attachmentList.size();
        }
    }

    public static class UploadAttachmentViewHolder extends RecyclerView.ViewHolder {

        TextView uploadAttachmentNumber;
        TextView uploadAttachmentType;
        TextView uploadAttachmentFilename;

        public UploadAttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            uploadAttachmentNumber = itemView.findViewById(R.id.item_upload_attachment_number);
            uploadAttachmentType = itemView.findViewById(R.id.item_upload_attachment_type);
            uploadAttachmentFilename = itemView.findViewById(R.id.item_upload_attachment_filename);
        }
    }
}
