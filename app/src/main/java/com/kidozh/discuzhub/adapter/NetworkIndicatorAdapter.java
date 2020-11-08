package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorFailedBinding;
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorLoadAllBinding;
import com.kidozh.discuzhub.databinding.ItemNetworkIndicatorLoadingBinding;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.utilities.ConstUtils;

public class NetworkIndicatorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int networkStatus = 0;
    private Context context;
    private ErrorMessage errorMessage;

    private OnRefreshBtnListener mListener;

    @Override
    public long getItemId(int position) {
        return networkStatus;
    }

    @Override
    public int getItemViewType(int position) {
        switch (networkStatus){
            case ConstUtils.NETWORK_STATUS_LOADING:{

                return R.layout.item_network_indicator_loading;
            }
            case ConstUtils.NETWORK_STATUS_LOADED_ALL:{
                return R.layout.item_network_indicator_load_all;
            }
            case ConstUtils.NETWORK_STATUS_FAILED:{
                return R.layout.item_network_indicator_failed;
            }
            default:{
                return 0;
            }
        }
    }
    
    

    public void setNetStatus(int networkStatus) {
        this.networkStatus = networkStatus;
        notifyDataSetChanged();
    }

    public void setLoadingStatus(){
        if(networkStatus == ConstUtils.NETWORK_STATUS_SUCCESSFULLY){
            this.networkStatus = ConstUtils.NETWORK_STATUS_LOADING;
            notifyItemInserted(0);
        }
        else {
            this.networkStatus = ConstUtils.NETWORK_STATUS_LOADING;
            notifyItemChanged(0);
        }
    }

    public void setLoadSuccessfulStatus(){
        this.networkStatus = ConstUtils.NETWORK_STATUS_SUCCESSFULLY;
        notifyItemRemoved(0);
    }

    public void setLoadedAllStatus(){
        if(networkStatus == ConstUtils.NETWORK_STATUS_SUCCESSFULLY){
            this.networkStatus = ConstUtils.NETWORK_STATUS_LOADED_ALL;
            notifyItemInserted(0);
        }
        else {
            this.networkStatus = ConstUtils.NETWORK_STATUS_LOADED_ALL;
            notifyItemChanged(0);
        }
    }

    public void setErrorStatus(@NonNull ErrorMessage errorMessage) {

        if(networkStatus == ConstUtils.NETWORK_STATUS_SUCCESSFULLY){
            this.networkStatus = ConstUtils.NETWORK_STATUS_FAILED;
            this.errorMessage = errorMessage;
            notifyItemInserted(0);
        }
        else {
            this.networkStatus = ConstUtils.NETWORK_STATUS_FAILED;
            this.errorMessage = errorMessage;
            notifyItemChanged(0);
        }

    }

    public NetworkIndicatorAdapter(){
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        if(context instanceof OnRefreshBtnListener){
            mListener = (OnRefreshBtnListener) context;
        }
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        switch (networkStatus){
            case ConstUtils.NETWORK_STATUS_LOADING:{
                ItemNetworkIndicatorLoadingBinding binding = ItemNetworkIndicatorLoadingBinding.inflate(layoutInflater,parent,false);
                return new NetworkIndicatorLoadingViewHolder(binding);
            }
            case ConstUtils.NETWORK_STATUS_LOADED_ALL:{
                ItemNetworkIndicatorLoadAllBinding binding = ItemNetworkIndicatorLoadAllBinding.inflate(layoutInflater,parent,false);
                return new NetworkIndicatorLoadAllViewHolder(binding);
            }
            case ConstUtils.NETWORK_STATUS_SUCCESSFULLY:{

            }
            case ConstUtils.NETWORK_STATUS_FAILED:{
                ItemNetworkIndicatorFailedBinding binding = ItemNetworkIndicatorFailedBinding.inflate(layoutInflater,parent,false);
                return new NetworkIndicatorLoadFailedViewHolder(binding);
            }
        }
        ItemNetworkIndicatorLoadingBinding binding = ItemNetworkIndicatorLoadingBinding.inflate(layoutInflater,parent,false);
        return new NetworkIndicatorLoadingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof NetworkIndicatorLoadFailedViewHolder && networkStatus == ConstUtils.NETWORK_STATUS_FAILED){
            ((NetworkIndicatorLoadFailedViewHolder) holder).binding.errorValue.setText(errorMessage.key);
            ((NetworkIndicatorLoadFailedViewHolder) holder).binding.errorContent.setText(errorMessage.content);
            if(errorMessage.errorIconResource != 0){
                ((NetworkIndicatorLoadFailedViewHolder) holder).binding.errorIcon.setImageResource(errorMessage.errorIconResource);
            }
            else {
                ((NetworkIndicatorLoadFailedViewHolder) holder).binding.errorIcon.setImageResource(ErrorMessage.getDefaultErrorIconResource());
            }
            ((NetworkIndicatorLoadFailedViewHolder) holder).binding.retryButton.setOnClickListener(v -> {
                if(mListener!=null){
                    mListener.onRefreshBtnClicked();
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        switch (networkStatus){
            case ConstUtils.NETWORK_STATUS_LOADING:
            case ConstUtils.NETWORK_STATUS_LOADED_ALL:
            case ConstUtils.NETWORK_STATUS_FAILED: {
                return 1;
            }
            case ConstUtils.NETWORK_STATUS_SUCCESSFULLY:{
                return 0;
            }
        }
        return 0;
    }

    public static class NetworkIndicatorLoadingViewHolder extends RecyclerView.ViewHolder{
        @NonNull
        ItemNetworkIndicatorLoadingBinding binding;
        NetworkIndicatorLoadingViewHolder(@NonNull ItemNetworkIndicatorLoadingBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class NetworkIndicatorLoadAllViewHolder extends RecyclerView.ViewHolder{
        @NonNull
        ItemNetworkIndicatorLoadAllBinding binding;
        NetworkIndicatorLoadAllViewHolder(@NonNull ItemNetworkIndicatorLoadAllBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class NetworkIndicatorLoadFailedViewHolder extends RecyclerView.ViewHolder{
        @NonNull
        ItemNetworkIndicatorFailedBinding binding;
        NetworkIndicatorLoadFailedViewHolder(@NonNull ItemNetworkIndicatorFailedBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnRefreshBtnListener{
        public void onRefreshBtnClicked();
    }
}
