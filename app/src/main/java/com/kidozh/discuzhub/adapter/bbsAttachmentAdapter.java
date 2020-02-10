package com.kidozh.discuzhub.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.utilities.bbsURLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;


import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;


public class bbsAttachmentAdapter extends RecyclerView.Adapter<bbsAttachmentAdapter.bbsAttachmentViewHolder> {
    private static final String TAG = bbsAttachmentAdapter.class.getSimpleName();
    Context mContext;
    List<threadCommentInfo.attachmentInfo> attachmentInfoList;

    bbsAttachmentAdapter(Context context){
        this.mContext = context;
    }

    @NonNull
    @Override
    public bbsAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_attachment_info;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new bbsAttachmentViewHolder(view);
    }

    void loadImageWithGlideInNetwork(bbsAttachmentViewHolder holder, threadCommentInfo.attachmentInfo attachmentInfo){
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(mContext));
        Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        String source;
        if(attachmentInfo.urlPrefix.startsWith("http:")||attachmentInfo.urlPrefix.startsWith("https:")){
            source = attachmentInfo.urlPrefix + attachmentInfo.relativeUrl;
        }
        else {
            // add a base url
            source = bbsURLUtils.getBaseUrl()+"/"+ attachmentInfo.urlPrefix + attachmentInfo.relativeUrl;
        }


        RequestOptions options = new RequestOptions()
                //.centerCrop()
                .placeholder(R.drawable.vector_drawable_image_wider_placeholder)
                .error(R.drawable.vector_drawable_image_crash);



        Glide.with(mContext)
                .load(source)
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
        threadCommentInfo.attachmentInfo attachmentInfo = attachmentInfoList.get(position);

        if(networkUtils.canDownloadImageOrFile(mContext)){
            loadImageWithGlideInNetwork(holder,attachmentInfo);
        }
        else {
            // retrieve file from cache
            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(networkUtils.getPreferredClient(mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            String source;

            if(attachmentInfo.urlPrefix.startsWith("http:")||attachmentInfo.urlPrefix.startsWith("https:")){
                source = attachmentInfo.urlPrefix + attachmentInfo.relativeUrl;
            }
            else {
                // add a base url
                source = bbsURLUtils.getBaseUrl()+"/"+ attachmentInfo.urlPrefix + attachmentInfo.relativeUrl;
            }

            //String source = bbsURLUtils.getAttachmentImageUrl(attachmentInfo.relativeUrl);


            RequestOptions options = new RequestOptions()
                    .centerInside()
                    .placeholder(R.drawable.vector_drawable_image_wider_placeholder)
                    .fallback(R.drawable.vector_drawable_image_wider_placeholder)
                    .error(R.drawable.vector_drawable_image_wider_placeholder);

            Glide.with(mContext)
                    .asBitmap()
                    .load(source)
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
        threadCommentInfo.attachmentInfo attachmentInfo = attachmentInfoList.get(position);

        Log.d(TAG,"Cur attachment position : "+position+" filename "+attachmentInfo.filename);
        String[] fileNameSegement = attachmentInfo.filename.split("\\.");
        String fileSuffix = "";
        if(fileNameSegement.length >0){
            fileSuffix = fileNameSegement[fileNameSegement.length-1];
        }
        else {
            fileSuffix = "";
        }

        Set<String> commonPictureSuffixSet = new HashSet<String>(
                Arrays.asList("gif","jpg","png","bmp")
        );
        fileSuffix = fileSuffix.toLowerCase();
        holder.mAttachmentBadge.setText(mContext.getString(R.string.bbs_thread_attachment_template,position+1,fileSuffix.toUpperCase()));
        holder.mAttachmentTitle.setText(attachmentInfo.filename);
        if(commonPictureSuffixSet.contains(fileSuffix)){
            renderPicture(holder,position);
        }
        else {
            String source;

            if(attachmentInfo.urlPrefix.startsWith("http:")||attachmentInfo.urlPrefix.startsWith("https:")){
                source = attachmentInfo.urlPrefix + attachmentInfo.relativeUrl;
            }
            else {
                // add a base url
                source = bbsURLUtils.getBaseUrl()+"/"+ attachmentInfo.urlPrefix + attachmentInfo.relativeUrl;
            }
            holder.mAttachmentImageview.setImageDrawable(mContext.getDrawable(R.drawable.vector_drawable_attach_file_placeholder_24px));
            holder.mAttachmentImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(source));
                    request.setDestinationInExternalFilesDir(mContext,Environment.DIRECTORY_DOWNLOADS,attachmentInfo.filename);
                    //request.setDestinationInExternalFilesDir(,mContext.getExternalFilesDir(null),attachmentInfo.filename);
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

        @BindView(R.id.bbs_attachment_filename)
        TextView mAttachmentTitle;
        @BindView(R.id.bbs_attachment_imageview)
        ImageView mAttachmentImageview;
        @BindView(R.id.bbs_attachment_badge)
        TextView mAttachmentBadge;
        Boolean isPictureLoaded = false;

        public bbsAttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
