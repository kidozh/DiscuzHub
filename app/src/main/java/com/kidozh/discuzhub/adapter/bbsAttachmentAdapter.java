package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



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
                .asBitmap()
                .load(source)
                .apply(options)
                .centerInside()
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
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

    @Override
    public void onBindViewHolder(@NonNull bbsAttachmentViewHolder holder, int position) {
        threadCommentInfo.attachmentInfo attachmentInfo = attachmentInfoList.get(position);
        holder.mAttachmentBadge.setText(String.format(mContext.getString(R.string.bbs_thread_attachment_template),position+1));
        holder.mAttachmentTitle.setText(attachmentInfo.filename);
        Log.d(TAG,"Cur attachment position : "+position+" filename "+attachmentInfo.filename);
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
