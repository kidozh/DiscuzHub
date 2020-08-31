package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.showImageFullscreenActivity;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.utilities.URLUtils;

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
        holder.pollOptionPosition.setText(String.valueOf(position+1));
        //Resources res = context.getResources();
        //String votersNumberString = res.getQuantityString(R.plurals.poll_voter_number, option.voteNumber, option.voteNumber);
        holder.pollOptionVotePercent.setText(context.getString(R.string.bbs_poll_vote_percent,String.valueOf(option.percent)));
        holder.pollOptionVoteNumber.setText(String.valueOf(option.voteNumber));
        String colorString = option.colorName;
        if(!colorString.startsWith("#")){
            colorString = "#" + colorString;
        }
        Log.d(TAG,"color "+colorString);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(colorString));
        holder.pollOptionProgressBar.setProgressTintList(ColorStateList.valueOf(colorDrawable.getColor()));
        if(option.imageInfo == null){

            //holder.pollOptionImage.setImageDrawable(colorDrawable);
            // holder.pollOptionProgressBar.setProgressDrawable(colorDrawable);
            holder.pollOptionWatchPicture.setVisibility(View.GONE);
        }
        else {
            String imageUrl = URLUtils.getBaseUrl()+"/"+option.imageInfo.bigURL;
            holder.pollOptionWatchPicture.setVisibility(View.VISIBLE);
            holder.pollOptionWatchPicture.setClickable(true);
            holder.pollOptionWatchPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, showImageFullscreenActivity.class);
                    intent.putExtra("URL",imageUrl);
                    context.startActivity(intent);
                }
            });
        }
        // check the vote status
        if(option.checked){
            holder.pollOptionCheckIcon.setColorFilter(context.getColor(R.color.colorPrimary));
            holder.pollOptionCheckIcon.setVisibility(View.VISIBLE);

        }
        else {
            holder.pollOptionCheckIcon.setVisibility(View.GONE);
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
        @BindView(R.id.item_poll_option_vote_number)
        TextView pollOptionVoteNumber;
        @BindView(R.id.item_poll_option_watch_picture)
        Button pollOptionWatchPicture;
        @BindView(R.id.item_poll_option_name)
        TextView pollOptionName;
        @BindView(R.id.item_poll_option_vote_progressBar)
        ProgressBar pollOptionProgressBar;
        @BindView(R.id.item_poll_option_vote_percent)
        TextView pollOptionVotePercent;
        @BindView(R.id.item_poll_option_check_icon)
        ImageView pollOptionCheckIcon;
        @BindView(R.id.item_poll_option_position)
        TextView pollOptionPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
