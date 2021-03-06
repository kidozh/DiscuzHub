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
import com.kidozh.discuzhub.activities.FullImageActivity;
import com.kidozh.discuzhub.entities.Poll;
import com.kidozh.discuzhub.utilities.URLUtils;

import java.util.ArrayList;
import java.util.List;



public class PollOptionAdapter extends RecyclerView.Adapter<PollOptionAdapter.ViewHolder> {
    private final String TAG = PollOptionAdapter.class.getSimpleName();

    public List<Poll.option> pollOptions = new ArrayList<>();
    private Context context;

    public void setPollOptions(List<Poll.option> pollOptions) {
        int oldSize = this.pollOptions.size();
        this.pollOptions.clear();
        notifyItemRangeRemoved(0,oldSize);
        this.pollOptions.addAll(pollOptions);
        notifyItemRangeInserted(0,pollOptions.size());
    }

    public void refreshOptionStatus(int position){
        notifyItemChanged(position);
    }

    public List<Poll.option> getPollOptions() {
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
        Poll.option option = pollOptions.get(position);
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
                    Intent intent = new Intent(context, FullImageActivity.class);
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
        
        CardView pollOptionCardview;
        TextView pollOptionVoteNumber;
        Button pollOptionWatchPicture;
        TextView pollOptionName;
        ProgressBar pollOptionProgressBar;
        TextView pollOptionVotePercent;
        ImageView pollOptionCheckIcon;
        TextView pollOptionPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pollOptionCardview = itemView.findViewById(R.id.item_poll_option_cardview);
            pollOptionVoteNumber = itemView.findViewById(R.id.item_poll_option_vote_number);
            pollOptionWatchPicture = itemView.findViewById(R.id.item_poll_option_watch_picture);
            pollOptionName = itemView.findViewById(R.id.item_poll_option_name);
            pollOptionProgressBar = itemView.findViewById(R.id.item_poll_option_vote_progressBar);
            pollOptionVotePercent = itemView.findViewById(R.id.item_poll_option_vote_percent);
            pollOptionCheckIcon = itemView.findViewById(R.id.item_poll_option_check_icon);
            pollOptionPosition = itemView.findViewById(R.id.item_poll_option_position);
        }
    }
}
