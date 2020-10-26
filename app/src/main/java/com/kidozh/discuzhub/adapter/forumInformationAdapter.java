package com.kidozh.discuzhub.adapter;


import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.snackbar.Snackbar;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.LoginActivity;
import com.kidozh.discuzhub.activities.ui.bbsDetailedInformation.bbsShowInformationActivity;
import com.kidozh.discuzhub.activities.bbsShowPortalActivity;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;

import java.io.InputStream;
import java.util.List;
import com.kidozh.discuzhub.database.BBSInformationDatabase;


public class forumInformationAdapter extends RecyclerView.Adapter<forumInformationAdapter.ViewHolder> {
    public static String TAG = forumInformationAdapter.class.getSimpleName();
    private List<bbsInformation> bbsInformationList;
    private Context context;
    private AppCompatActivity activity;
    public forumInformationAdapter(Context context){
        this.context = context;
    }
    public forumInformationAdapter(Context context, AppCompatActivity activity){
        this.context = context;
        this.activity = activity;
    }

    public Context getContext() {
        return context;
    }


    public List<bbsInformation> getBbsInformationList() {
        return bbsInformationList;
    }

    @NonNull
    @Override
    public forumInformationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG,"BBS info "+bbsInformationList);
        if(bbsInformationList!=null && bbsInformationList.size()!=0){
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_forum_information,parent,false));
        }
        else {
            return new EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.content_empty_bbs_information,parent,false));
        }

    }

    public void setBbsInformationList(List<bbsInformation> bbsInformationList) {
        this.bbsInformationList = bbsInformationList;
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull forumInformationAdapter.ViewHolder holder, int position) {
        if(bbsInformationList == null || bbsInformationList.size() == 0){
            return;
        }
        bbsInformation forumInfo = bbsInformationList.get(position);

        holder.forumHost.setText(forumInfo.base_url);
        holder.forumName.setText(forumInfo.site_name);
        holder.forumSiteId.setText(forumInfo.mysite_id);
        holder.forumPostNumber.setText(numberFormatUtils.getShortNumberText(forumInfo.total_posts));
        holder.forumMemberNumber.setText(numberFormatUtils.getShortNumberText(forumInfo.total_members));
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context));
        URLUtils.setBBS(forumInfo);
        Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class,factory);

        Glide.with(context)
                .load(URLUtils.getBBSLogoUrl())
                .error(R.drawable.vector_drawable_bbs)
                .placeholder(R.drawable.vector_drawable_bbs)
                .centerInside()
                .into(holder.forumAvatar);


        if(forumInfo.useSafeClient && forumInfo.base_url.startsWith("https://")){
            holder.forumIntegrityStatus.setImageResource(R.drawable.vector_drawable_safe_lock);
        }
        else {
            holder.forumIntegrityStatus.setImageResource(R.drawable.vector_drawable_warn);
        }
        if(forumInfo.qqConnect){
            holder.qqConnectIcon.setVisibility(View.VISIBLE);
            holder.qqConnectLabel.setVisibility(View.GONE);
        }
        else {
            holder.qqConnectIcon.setVisibility(View.GONE);
            holder.qqConnectLabel.setVisibility(View.GONE);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        holder.forumUserRecyclerview.setLayoutManager(linearLayoutManager);
        forumUsersAdapter adapter = new forumUsersAdapter(this.context, forumInfo);
        holder.forumUserRecyclerview.setAdapter(adapter);
        holder.forumUserRecyclerview.setHasFixedSize(true);
        holder.forumUserRecyclerview.setNestedScrollingEnabled(false);

        // render forum user info
        LiveData<List<forumUserBriefInfo>> bbsUserInfoLiveDatas = forumUserBriefInfoDatabase
                .getInstance(context)
                .getforumUserBriefInfoDao()
                .getAllUserByBBSID(forumInfo.getId());
        adapter.setUserList(bbsUserInfoLiveDatas.getValue());
        Log.d(TAG,"Find exsiting user "+bbsUserInfoLiveDatas.getValue() + " id "+forumInfo.getId());
        bbsUserInfoLiveDatas.observe(activity, new Observer<List<forumUserBriefInfo>>() {
            @Override
            public void onChanged(List<forumUserBriefInfo> forumUserBriefInfos) {
                if(forumUserBriefInfos!=null){
                    Log.d(TAG,"Find exsiting user "+forumUserBriefInfos.size() + " id "+forumInfo.getId());
                }
                adapter.setUserList(forumUserBriefInfos);
            }
        });

        holder.forumInfoCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, bbsShowInformationActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create(holder.forumAvatar, "bbs_info_avatar"),
                        Pair.create(holder.forumName, "bbs_info_name")
                );

                Bundle bundle = options.toBundle();

                activity.startActivity(intent,bundle);
            }
        });
        holder.enterAsAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // enter the annoymous mode
                //Intent intent = new Intent(activity, bbsShowCategoryForumActivity.class);
                Intent intent = new Intent(activity, bbsShowPortalActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                activity.startActivity(intent);
            }
        });

        holder.addAnAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,forumInfo);
                intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY, (forumUserBriefInfo) null);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create(holder.forumName, "bbs_info_name"),
                        Pair.create(holder.forumAvatar, "bbs_info_avatar"),
                        Pair.create(holder.forumHost,"bbs_info_bbs_url")

                );

                Bundle bundle = options.toBundle();
                context.startActivity(intent,bundle);
            }
        });

        if(forumInfo.hideRegister){
            holder.registerBtn.setVisibility(View.GONE);
        }
        else {
            holder.registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // dialog to ensure
                    Log.d(TAG,"You pressed register btn");
                    new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.bbs_register_an_account)+" "+forumInfo.site_name)
                            //setMessage是用来显示字符串的
                            .setMessage(R.string.bbs_register_account_notification)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    URLUtils.setBBS(forumInfo);
                                    Uri uri = Uri.parse(URLUtils.getBBSRegisterUrl(forumInfo.register_name));
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    context.startActivity(intent);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            });
        }



    }

    @Override
    public int getItemCount() {
        if (bbsInformationList == null || bbsInformationList.size() == 0){
            return 0;
        }
        else {
            return bbsInformationList.size();
        }

    }

    public void deleteItem(int position){
        bbsInformationList.remove(position);
        notifyDataSetChanged();

    }

    public void showUndoSnackbar(final bbsInformation deletedForumInfo, final int position) {
        Log.d(TAG,"SHOW REMOVED POS "+position);
        new removeNewForumInformationTask(deletedForumInfo, context).execute();

        View view = activity.findViewById(R.id.forum_information_coordinatorLayout);
        Snackbar snackbar = Snackbar.make(view, R.string.delete_forum_information,
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.bbs_undo_delete, new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                undoDelete(deletedForumInfo,position);
            }
        });
        snackbar.show();
    }

    public void undoDelete(bbsInformation deletedForumInfo, int position) {
        // insert to database
        bbsInformationList.add(position,deletedForumInfo);
        notifyDataSetChanged();
        new addNewForumInformationTask(deletedForumInfo, context).execute();
    }

    public static class addNewForumInformationTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation forumInfo;
        private Context context;
        public addNewForumInformationTask(bbsInformation bbsInformation, Context context){
            this.forumInfo = bbsInformation;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            BBSInformationDatabase.getInstance(context)
                    .getForumInformationDao().insert(forumInfo);
            Log.d(TAG, "add forum into database"+forumInfo.site_name);
            return null;
        }

    }

    public static class removeNewForumInformationTask extends AsyncTask<Void, Void, Void> {
        private bbsInformation forumInfo;
        private Context context;
        public removeNewForumInformationTask(bbsInformation bbsInformation, Context context){
            this.forumInfo = bbsInformation;
            this.context = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            BBSInformationDatabase
                    .getInstance(context)
                    .getForumInformationDao().delete(forumInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG,"Remove forum "+this.forumInfo.site_name);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        
        ImageView forumAvatar;
        TextView forumName;
        TextView forumHost;
        TextView forumSiteId;
        TextView forumPostNumber;
        TextView forumMemberNumber;
        RecyclerView forumUserRecyclerview;
        ImageView forumIntegrityStatus;
        ImageView qqConnectIcon;
        TextView qqConnectLabel;
        Button enterAsAnonymous;
        Button registerBtn;
        CardView forumInfoCardview;
        Button addAnAccountBtn;
        
        ViewHolder(View view){
            super(view);
            forumAvatar = view.findViewById(R.id.item_forum_information_avatar);
            forumName = view.findViewById(R.id.item_forum_information_name);
            forumHost = view.findViewById(R.id.item_forum_information_host);
            forumSiteId = view.findViewById(R.id.item_forum_information_siteid);
            forumPostNumber = view.findViewById(R.id.item_forum_information_post_number);
            forumMemberNumber = view.findViewById(R.id.item_forum_information_member_number);
            forumUserRecyclerview = view.findViewById(R.id.item_forum_information_user_recyclerview);
            forumIntegrityStatus = view.findViewById(R.id.item_forum_information_integrity);
            qqConnectIcon = view.findViewById(R.id.item_forum_information_qq_connect);
            qqConnectLabel = view.findViewById(R.id.item_forum_information_qq_connect_label);
            enterAsAnonymous = view.findViewById(R.id.item_forum_information_anonymous_btn);
            registerBtn = view.findViewById(R.id.item_forum_information_sign_in_btn);
            forumInfoCardview = view.findViewById(R.id.item_forum_information_cardview);
            addAnAccountBtn = view.findViewById(R.id.item_forum_information_add_an_account_btn);
        }

    }

    private class EmptyViewHolder extends ViewHolder{
        EmptyViewHolder(View view){
            super(view);
        }
    }
}
