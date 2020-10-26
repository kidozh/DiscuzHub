package com.kidozh.discuzhub.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.UserMedal.MedalFragment.OnListFragmentInteractionListener;
import com.kidozh.discuzhub.results.UserProfileResult;

import java.util.List;


/**
 * {@link RecyclerView.Adapter} that can display a {@link } and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MedalAdapter extends RecyclerView.Adapter<MedalAdapter.ViewHolder> {

    private List<UserProfileResult.Medal> medalList;
    private Context context;

    public void setMedalList(List<UserProfileResult.Medal> medalList) {
        this.medalList = medalList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medal_info, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        UserProfileResult.Medal medal = medalList.get(position);
        holder.medalName.setText(medal.name);
        holder.medalDescription.setText(medal.description);
        Glide.with(context)
                .load(medal.getMedalImageURL())
                .into(holder.medalAvatar);
        holder.medalIndex.setText(String.valueOf(position+1));
    }

    @Override
    public int getItemCount() {
        if(medalList == null){
            return 0;
        }
        else {
            return medalList.size();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        
        ImageView medalAvatar;
        TextView medalDescription;
        TextView medalName;
        TextView medalIndex;

        public ViewHolder(View view) {
            super(view);
            medalAvatar = view.findViewById(R.id.medal_avatar);
            medalDescription = view.findViewById(R.id.medal_description);
            medalName = view.findViewById(R.id.medal_name);
            medalIndex = view.findViewById(R.id.medal_index);
        }

    }
}
