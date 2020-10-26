package com.kidozh.discuzhub.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;
import com.kidozh.discuzhub.databinding.ItemBbsAttachmentInfoBinding;
import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;


import java.io.InputStream;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class bbsAttachmentAdapter extends RecyclerView.Adapter<bbsAttachmentAdapter.bbsAttachmentViewHolder> {
    private static final String TAG = bbsAttachmentAdapter.class.getSimpleName();
    Context mContext;
    List<PostInfo.Attachment> attachmentInfoList;



    @NonNull
    @Override
    public bbsAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_attachment_info;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsAttachmentViewHolder(view);
    }

    void loadImageWithGlideInNetwork(bbsAttachmentViewHolder holder, PostInfo.Attachment attachmentInfo){
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(mContext));
        Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
        String source = URLUtils.getAttachmentURL(attachmentInfo);

        RequestOptions options = new RequestOptions()
                //.centerCrop()
                .placeholder(R.drawable.vector_drawable_image_wider_placeholder)
                .error(R.drawable.vector_drawable_image_crash);
        GlideUrl glideUrl = new GlideUrl(source,
                new LazyHeaders.Builder().addHeader("referer",source).build()
        );

        Glide.with(mContext)
                .load(glideUrl)
                .apply(options)
                .centerInside()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mAttachmentImageview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, showImageFullscreenActivity.class);
                                intent.putExtra("URL",source);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                mContext.startActivity(intent);
                            }
                        });
                        return false;
                    }
                })
                .into(holder.mAttachmentImageview);



    }

    private void renderPicture(bbsAttachmentViewHolder holder, int position){
        PostInfo.Attachment attachmentInfo = attachmentInfoList.get(position);

        if(NetworkUtils.canDownloadImageOrFile(mContext)){
            loadImageWithGlideInNetwork(holder,attachmentInfo);
        }
        else {
            // retrieve file from cache
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            String source = URLUtils.getAttachmentURL(attachmentInfo);

            RequestOptions options = new RequestOptions()
                    .centerInside()
                    .placeholder(R.drawable.vector_drawable_image_wider_placeholder)
                    .fallback(R.drawable.vector_drawable_image_wider_placeholder)
                    .error(R.drawable.vector_drawable_image_wider_placeholder);
            GlideUrl glideUrl = new GlideUrl(source,
                    new LazyHeaders.Builder().addHeader("referer",source).build()
            );

            Glide.with(mContext)
                    .asBitmap()
                    .load(glideUrl)
                    .apply(options)
                    .onlyRetrieveFromCache(true)
                    .fitCenter()
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            // if pressed it can be downloaded
                            holder.mAttachmentImageview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG,"holder "+holder);
                                    if(holder!=null && !holder.isPictureLoaded){
                                        loadImageWithGlideInNetwork(holder,attachmentInfo);
                                    }

                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            holder.mAttachmentImageview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, showImageFullscreenActivity.class);
                                    intent.putExtra("URL",source);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    mContext.startActivity(intent);
                                }
                            });

                            return false;
                        }
                    })
                    .into(holder.mAttachmentImageview);


        }
    }



    @Override
    public void onBindViewHolder(@NonNull bbsAttachmentViewHolder holder, int position) {
        PostInfo.Attachment attachmentInfo = attachmentInfoList.get(position);

        Log.d(TAG,"Cur attachment position : "+position+" filename "+attachmentInfo.filename);
        if(attachmentInfo.ext !=null){
            holder.mAttachmentBadge.setText(mContext.getString(R.string.bbs_thread_attachment_template,position+1,attachmentInfo.ext.toUpperCase()));
        }
        else {
            // parse it manually

            holder.mAttachmentBadge.setText(mContext.getString(R.string.bbs_thread_attachment_template,position+1,""));
        }

        holder.mAttachmentTitle.setText(attachmentInfo.filename);
        if(attachmentInfo.isImage()){
            renderPicture(holder,position);
        }
        else {
            String source = URLUtils.getAttachmentURL(attachmentInfo);

            holder.mAttachmentImageview.setImageDrawable(mContext.getDrawable(R.drawable.vector_drawable_attach_file_placeholder_24px));
            holder.mAttachmentImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(source));
                    Log.d(TAG, "Download by URL "+source);
                    request.setDestinationInExternalFilesDir(mContext,Environment.DIRECTORY_DOWNLOADS,attachmentInfo.filename);

                    DownloadManager downloadManager= (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                    request.setTitle(mContext.getString(R.string.bbs_downloading_file_template,attachmentInfo.filename));
                    request.setDescription(attachmentInfo.filename);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    if(downloadManager!=null){
                        downloadManager.enqueue(request);
                        Toasty.success(mContext,mContext.getString(R.string.bbs_downloading_attachment), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toasty.error(mContext,mContext.getString(R.string.bbs_downloading_attachment_failed), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
        if(attachmentInfo.attachSize!=null && attachmentInfo.attachSize.length()!=0){
            holder.mAttachmentSize.setText(attachmentInfo.attachSize);
            holder.mAttachmentSize.setVisibility(View.VISIBLE);
        }
        else {
            holder.mAttachmentSize.setVisibility(View.GONE);
        }
        if(attachmentInfo.downloads == 0){
            holder.mAttachmentDownloadTimes.setVisibility(View.GONE);
        }
        else {
            holder.mAttachmentDownloadTimes.setVisibility(View.VISIBLE);
            holder.mAttachmentDownloadTimes.setText(mContext.getString(R.string.attachment_download_time,attachmentInfo.downloads));
        }


    }

    @Override
    public int getItemCount() {
        if(attachmentInfoList==null){
            return 0;
        }
        else {
            return attachmentInfoList.size();
        }

    }


    public class bbsAttachmentViewHolder extends RecyclerView.ViewHolder{

        TextView mAttachmentTitle;
        ImageView mAttachmentImageview;
        TextView mAttachmentBadge;
        TextView mAttachmentSize;
        TextView mAttachmentDownloadTimes;
        Boolean isPictureLoaded = false;

        public ItemBbsAttachmentInfoBinding binding;


        public bbsAttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            mAttachmentTitle = itemView.findViewById(R.id.bbs_attachment_filename);
            mAttachmentImageview = itemView.findViewById(R.id.bbs_attachment_imageview);
            mAttachmentBadge = itemView.findViewById(R.id.bbs_attachment_badge);
            mAttachmentSize = itemView.findViewById(R.id.bbs_attachment_filesize);
            mAttachmentDownloadTimes = itemView.findViewById(R.id.bbs_attachment_download_times);

        }
    }
}
