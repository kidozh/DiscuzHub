package com.kidozh.discuzhub.adapter;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import androidx.fragment.app.FragmentManager;
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
import com.kidozh.discuzhub.activities.BaseStatusActivity;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;
import com.kidozh.discuzhub.databinding.ItemBbsAttachmentInfoBinding;
import com.kidozh.discuzhub.dialogs.DownloadAttachmentDialogFragment;
import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;


import java.io.InputStream;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.bbsAttachmentViewHolder> {
    private static final String TAG = AttachmentAdapter.class.getSimpleName();
    Context mContext;
    private List<PostInfo.Attachment> attachmentInfoList;

    public void setAttachmentInfoList(@NonNull List<PostInfo.Attachment> attachmentInfoList) {
        this.attachmentInfoList = attachmentInfoList;
        notifyItemRangeChanged(0,attachmentInfoList.size());
    }

    public List<PostInfo.Attachment> getAttachmentInfoList() {
        return attachmentInfoList;
    }

    @NonNull
    @Override
    public bbsAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        ItemBbsAttachmentInfoBinding binding = ItemBbsAttachmentInfoBinding.inflate(inflater,parent,shouldAttachToParentImmediately);
        
        return new bbsAttachmentViewHolder(binding);
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
                        holder.binding.attachmentCardview.setOnClickListener(new View.OnClickListener() {
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
                .into(holder.binding.bbsAttachmentImageview);



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
                            holder.binding.attachmentCardview.setOnClickListener(new View.OnClickListener() {
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
                            holder.binding.attachmentCardview.setOnClickListener(new View.OnClickListener() {
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
                    .into(holder.binding.bbsAttachmentImageview);


        }
    }



    @Override
    public void onBindViewHolder(@NonNull bbsAttachmentViewHolder holder, int position) {
        PostInfo.Attachment attachmentInfo = attachmentInfoList.get(position);

        Log.d(TAG,"Cur attachment position : "+position+" filename "+attachmentInfo.filename);
        if(attachmentInfo.price != 0){
            holder.binding.attachmentPriceIcon.setVisibility(View.VISIBLE);
            if(attachmentInfo.payed){
                holder.binding.attachmentPriceIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_attachment_payed_24px));
            }
            else {
                holder.binding.attachmentPriceIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_attachment_price_24px));
            }
        }
        else {
            holder.binding.attachmentPriceIcon.setVisibility(View.GONE);
        }
        if(attachmentInfo.ext !=null){
            holder.binding.bbsAttachmentExt.setText(attachmentInfo.ext);
        }
        else {
            holder.binding.bbsAttachmentExt.setText(R.string.attachment_no_ext);
        }

        holder.binding.bbsAttachmentFilename.setText(attachmentInfo.filename);
        if(attachmentInfo.isImage()){
            renderPicture(holder,position);
        }
        else {

            String ext = attachmentInfo.ext;
            // decide picture
            if(ext !=null){
                ext = ext.toLowerCase();
                String extTemplate = "ic_ext_icon_%s_32px";
                Resources resources = mContext.getResources();
                int attachmentResourceId = resources.getIdentifier(String.format(extTemplate,ext)
                        ,"drawable",
                        mContext.getPackageName());
                // find a valid resources
                if(attachmentResourceId != 0){
                    holder.binding.bbsAttachmentImageview.setImageDrawable(mContext.getDrawable(attachmentResourceId));
                }
                else {
                    holder.binding.bbsAttachmentImageview.setImageDrawable(mContext.getDrawable(R.drawable.ic_ext_icon_file_not_found_32px));
                }


            }
            else {
                holder.binding.bbsAttachmentImageview.setImageDrawable(mContext.getDrawable(R.drawable.ic_ext_icon_file_not_found_32px));
            }
            // for download information
            holder.binding.attachmentCardview.setOnClickListener(v -> {
                // show the information in dialog
                FragmentManager fragmentManager = ((BaseStatusActivity) mContext).getSupportFragmentManager();
                DownloadAttachmentDialogFragment fragment = new DownloadAttachmentDialogFragment(attachmentInfo);
                fragment.show(fragmentManager,DownloadAttachmentDialogFragment.class.getSimpleName());

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


    public static class bbsAttachmentViewHolder extends RecyclerView.ViewHolder{

        Boolean isPictureLoaded = false;

        public ItemBbsAttachmentInfoBinding binding;


        public bbsAttachmentViewHolder(ItemBbsAttachmentInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            
        }
    }
}
