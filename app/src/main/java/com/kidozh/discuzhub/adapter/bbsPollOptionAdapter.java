package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.utilities.bbsURLUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class bbsPollOptionAdapter extends RecyclerView.Adapter<bbsPollOptionAdapter.ViewHolder> {
    private final String TAG = bbsPollOptionAdapter.class.getSimpleName();

    List<bbsPollInfo.option> pollOptions;
    private Context context;

    public void setPollOptions(List<bbsPollInfo.option> pollOptions) {
        this.pollOptions = pollOptions;
        notifyDataSetChanged();
    }

    public List<bbsPollInfo.option> getPollOptions() {
        return pollOptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poll_option,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bbsPollInfo.option option = pollOptions.get(position);
        holder.pollOptionName.setText(option.name);
        holder.pollOptionProgressBar.setMax(100);
        holder.pollOptionProgressBar.setProgress((int) option.percent);
        Resources res = context.getResources();
        String votersNumberString = res.getQuantityString(R.plurals.poll_voter_number, option.voteNumber, option.voteNumber);
        holder.pollOptionVotePercent.setText(context.getString(R.string.poll_vote_percent_templates, option.percent, votersNumberString));
        if(option.imageInfo == null){
            String colorString = option.colorName;
            if(!colorString.startsWith("#")){
                colorString = "#" + colorString;
            }
            Log.d(TAG,"color "+colorString);
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(colorString));
            holder.pollOptionImage.setImageDrawable(colorDrawable);
            // holder.pollOptionProgressBar.setProgressDrawable(colorDrawable);

        }
        else {
            RequestOptions requestOptions = new RequestOptions()
                    //.centerCrop()
                    .placeholder(R.drawable.vector_drawable_image_wider_placeholder)
                    .error(R.drawable.vector_drawable_image_crash);
            Glide.with(context)
                    .load(bbsURLUtils.getBaseUrl()+"/"+option.imageInfo.bigURL)
                    .apply(requestOptions)
                    .into(holder.pollOptionImage);
        }
        // check the vote status
        if(option.checked){
            holder.pollOptionName.setAlpha(1);
            holder.pollOptionImage.setImageDrawable(context.getDrawable(R.drawable.vector_drawable_check_24px));
        }
        else {
            holder.pollOptionName.setAlpha(0.75f);
        }



    }

    @Override
    public int getItemCount() {
        if(pollOptions == null){
            return 0;
        }
        else {
            return pollOptions.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_poll_option_cardview)
        CardView pollOptionCardview;
        @BindView(R.id.item_poll_option_imageView)
        ImageView pollOptionImage;
        @BindView(R.id.item_poll_option_name)
        TextView pollOptionName;
        @BindView(R.id.item_poll_option_vote_progressBar)
        ProgressBar pollOptionProgressBar;
        @BindView(R.id.item_poll_option_vote_percent)
        TextView pollOptionVotePercent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
