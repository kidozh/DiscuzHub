package com.kidozh.discuzhub.adapter;

import android.content.Context;
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

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.SuggestURLInfo;
import com.kidozh.discuzhub.results.AddCheckResult;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IntroSuggestionAdapter extends RecyclerView.Adapter<IntroSuggestionAdapter.IntroSuggestionViewHolder> {
    private final static String TAG = IntroSuggestionAdapter.class.getSimpleName();
    private List<SuggestURLInfo> suggestURLInfoList = new ArrayList<>();
    private Context context;

    private boolean useSafeClient = true;

    private OnClickSuggestionListener mListener;

    public interface OnClickSuggestionListener{
        void onClickSuggestion(SuggestURLInfo suggestURLInfo);
    }

    public void setSuggestURLInfoList(List<SuggestURLInfo> suggestURLInfoList) {
        this.suggestURLInfoList = suggestURLInfoList;
        notifyDataSetChanged();
    }

    public void setUseSafeClient(boolean useSafeClient) {
        this.useSafeClient = useSafeClient;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IntroSuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // bind listener
        if(context instanceof OnClickSuggestionListener){
            mListener = (OnClickSuggestionListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnClickSuggestionListener");
        }

        return new IntroSuggestionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_intro_url_suggestion,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull IntroSuggestionViewHolder holder, int position) {
        SuggestURLInfo suggestURLInfo = suggestURLInfoList.get(position);
        holder.urlTextview.setText(suggestURLInfo.url);
        holder.descriptionTextview.setText(suggestURLInfo.name);
        holder.urlSuggestionCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickSuggestion(suggestURLInfo);
            }
        });
        if(!suggestURLInfo.valid){
            holder.checkProgressbar.setVisibility(View.VISIBLE);
            holder.suggestionOKIcon.setVisibility(View.GONE);
            queryBBSInfo(suggestURLInfo.url,useSafeClient, holder);
        }
        else {
            holder.checkProgressbar.setVisibility(View.GONE);
            holder.suggestionOKIcon.setVisibility(View.VISIBLE);
            holder.suggestionOKIcon.setImageDrawable(context.getDrawable(R.drawable.ic_suggestion_check_ok_circle_24px));
        }


    }

    @Override
    public int getItemCount() {
        if(suggestURLInfoList == null){
            return 0;
        }
        else {
            return suggestURLInfoList.size();
        }

    }

    private void queryBBSInfo(String base_url, Boolean useSafeClient, IntroSuggestionViewHolder holder){
        holder.checkProgressbar.setVisibility(View.VISIBLE);
        holder.suggestionOKIcon.setVisibility(View.GONE);
        URLUtils.setBaseUrl(base_url);
        String query_url = URLUtils.getBBSForumInformationUrl();
        // judge the url
        OkHttpClient client = NetworkUtils.getPreferredClient(context,useSafeClient);
        Request request;
        try{
            URL url = new URL(query_url);
            request = new Request.Builder().url(query_url).build();

        }
        catch (Exception e){
            holder.checkProgressbar.setVisibility(View.GONE);
            holder.suggestionOKIcon.setVisibility(View.VISIBLE);
            holder.suggestionOKIcon.setImageDrawable(context.getDrawable(R.drawable.ic_suggestion_check_error_outline_24px));
            e.printStackTrace();
            return ;
        }
        Call call = client.newCall(request);

        Log.d(TAG,"Query check URL "+query_url);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                holder.checkProgressbar.post(new Runnable() {
                    @Override
                    public void run() {
                        holder.checkProgressbar.setVisibility(View.GONE);
                        holder.suggestionOKIcon.setVisibility(View.VISIBLE);
                        holder.suggestionOKIcon.setImageDrawable(context.getDrawable(R.drawable.ic_suggestion_check_error_outline_24px));
                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s;
                if(response.isSuccessful() && response.body()!=null){
                    s = response.body().string();
                    Log.d(TAG,"check response " +s);
                    AddCheckResult checkResult = bbsParseUtils.parseCheckInfoResult(s);
                    if(checkResult != null && checkResult.siteName!=null){
                        holder.checkProgressbar.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.checkProgressbar.setVisibility(View.GONE);
                                holder.suggestionOKIcon.setVisibility(View.VISIBLE);
                                holder.suggestionOKIcon.setImageDrawable(context.getDrawable(R.drawable.ic_suggestion_check_circle_outline_24px));
                            }
                        });
                    }
                    else {
                        holder.checkProgressbar.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.checkProgressbar.setVisibility(View.GONE);
                                holder.suggestionOKIcon.setVisibility(View.VISIBLE);
                                holder.suggestionOKIcon.setImageDrawable(context.getDrawable(R.drawable.ic_suggestion_check_error_outline_24px));
                            }
                        });
                    }
                }
                else {
                    holder.checkProgressbar.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.checkProgressbar.setVisibility(View.GONE);
                            holder.suggestionOKIcon.setVisibility(View.VISIBLE);
                            holder.suggestionOKIcon.setImageDrawable(context.getDrawable(R.drawable.ic_suggestion_check_error_outline_24px));
                        }
                    });
                }
            }
        });
    }

    public static class IntroSuggestionViewHolder extends RecyclerView.ViewHolder{
        
        CardView urlSuggestionCardview;
        TextView urlTextview;
        TextView descriptionTextview;
        ProgressBar checkProgressbar;
        ImageView suggestionOKIcon;

        public IntroSuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            urlSuggestionCardview = itemView.findViewById(R.id.item_intro_url_suggestion_cardview);
            urlTextview = itemView.findViewById(R.id.item_intro_url_suggestion_url_textview);
            descriptionTextview = itemView.findViewById(R.id.item_intro_url_suggestion_description);
            checkProgressbar = itemView.findViewById(R.id.item_intro_url_suggestion_progressBar);
            suggestionOKIcon = itemView.findViewById(R.id.item_intro_url_suggestion_ok_icon);
        }
    }
}
