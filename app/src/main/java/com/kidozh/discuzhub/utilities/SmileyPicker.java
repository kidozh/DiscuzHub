package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.SmileyAdapter;
import com.kidozh.discuzhub.databinding.PopupwindowSmileyViewBinding;
import com.kidozh.discuzhub.entities.Smiley;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SmileyPicker extends PopupWindow {
    private static final String TAG = SmileyPicker.class.getSimpleName();
    private Context mContext;
    private OnItemClickListener listener;
    private SmileyAdapter adapter;
    private List<Smiley> allSmileyInfos = new ArrayList<>();
    int smileyCateNum = 0;

    private OkHttpClient client;

    TabLayout tab;
    RecyclerView recyclerView;

    PopupwindowSmileyViewBinding binding;


    public SmileyPicker(Context context) {
        super(context);
        mContext = context;
        configureClient();
        init();
        getSmileyInfo();
    }

    private void configureClient(){
        client = NetworkUtils.getPreferredClientWithCookieJar(mContext);
    }

    private void getSmileyInfo(){
        Request request = new Request.Builder()
                .url(URLUtils.getSmileyApiUrl())
                .build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&& response.body()!=null){
                    String s = response.body().string();
                    List<Smiley> smileyInfoList = bbsParseUtils.parseSmiley(s);
                    int cateNum = bbsParseUtils.parseSmileyCateNum(s);
                    smileyCateNum = cateNum;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            allSmileyInfos = smileyInfoList;
                            // update the UI
                            adapter.setSmileyInfos(smileyInfoList);
                            // interface with tab
                            for(int i=0;i<cateNum;i++){
                                tab.addTab(tab.newTab().setText(String.valueOf(i+1)));
                            }

                        }
                    });


                }
            }
        });
    }


    private void init() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.popupwindow_smiley_view, null);

        tab = v.findViewById(R.id.bbs_smiley_tab);
        recyclerView = v.findViewById(R.id.bbs_smiley_recyclerView);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 7, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SmileyAdapter(mContext, (v1, position) -> {
            ImageView img = (ImageView) v1;
            smileyClick(img.getDrawable(), position);
            dismiss();
        });

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                changeSmiley(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        recyclerView.setAdapter(adapter);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.rec_solid_primary_bg));
        setFocusable(true);
        setContentView(v);
    }


    private void changeSmiley(int position) {

        //getSmileys();
        setSmileyinCate(position);
        //adapter.notifyDataSetChanged();
    }


    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void itemClick(String str, Drawable a);
    }

    private void setSmileyinCate(int position){
        int cateNum = position;
        List<Smiley> cateSmileyInfo = new ArrayList<>();
        for(int i=0;i<allSmileyInfos.size();i++){
            Smiley smileyInfo = allSmileyInfos.get(i);
            if(smileyInfo.getCategory() == position){
                cateSmileyInfo.add(smileyInfo);
            }
        }
        adapter.setSmileyInfos(cateSmileyInfo);
    }

    private void smileyClick(Drawable d, int position) {

        if (position > adapter.getSmileyInfos().size()) {
            return;
        }


        String name = adapter.getSmileyInfos().get(position).getCode();
        Log.d(TAG,"get name "+name);

        //String name = smileys.get(position).second;

        if (listener != null) {
            listener.itemClick(name, d);
        }
    }
}