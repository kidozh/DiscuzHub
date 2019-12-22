package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Pair;
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
import com.kidozh.discuzhub.adapter.bbsSmileyAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by free2 on 16-7-19.
 * smiley picker
 * 表情选择器
 */

public class bbsSmileyPicker extends PopupWindow {

    private Context mContext;
    private OnItemClickListener listener;
    private bbsSmileyAdapter adapter;
    private List<Pair<String, String>> smileys = new ArrayList<>();

    private static final int SMILEY_TB = 1;
    private static final int SMILEY_JGZ = 2;
    private static final int SMILEY_ACN = 3;

    private int smileyType = SMILEY_TB;


    public bbsSmileyPicker(Context context) {
        super(context);
        mContext = context;
        init();
    }


    private void init() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.popupwindow_smiley_view, null);
        TabLayout tab = v.findViewById(R.id.bbs_smiley_tab);
        RecyclerView recyclerView = v.findViewById(R.id.bbs_smiley_recyclerView);

        getSmileys();
        tab.addTab(tab.newTab().setText("贴吧"));
        tab.addTab(tab.newTab().setText("金馆长"));
        tab.addTab(tab.newTab().setText("AC娘"));
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 7, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new bbsSmileyAdapter(mContext, (v1, position) -> {
            ImageView img = (ImageView) v1;
            smileyClick(img.getDrawable(), position);
            dismiss();
        }, smileys);

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
        switch (position) {
            case 0:
                smileyType = SMILEY_TB;
                break;
            case 1:
                smileyType = SMILEY_JGZ;
                break;
            case 2:
                smileyType = SMILEY_ACN;
                break;
            default:
                throw new IndexOutOfBoundsException("unknown index: " + position);
        }
        getSmileys();
        adapter.notifyDataSetChanged();
    }


    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void itemClick(String str, Drawable a);
    }

    private void getSmileys() {
//        smileys.clear();
//        String smileyDir = "file:///android_asset/smiley/";
//        int stringId = R.array.smiley_tieba;
//        if (smileyType == SMILEY_TB) {
//            stringId = R.array.smiley_tieba;
//        } else if (smileyType == SMILEY_JGZ) {
//            stringId = R.array.smiley_jgz;
//        } else if (smileyType == SMILEY_ACN) {
//            stringId = R.array.smiley_acn;
//        }
//        String[] smileyArray = mContext.getResources().getStringArray(stringId);
//        for (String aSmileyArray : smileyArray) {
//            String path = smileyDir + aSmileyArray.split(",")[0];
//            String name = aSmileyArray.split(",")[1];
//            //Log.e("TAG", "" + name);
//            smileys.add(new Pair<>(path, name));
//        }
    }

    private void smileyClick(Drawable d, int position) {
        if (position > smileys.size()) {
            return;
        }

        String name = smileys.get(position).second;

        if (listener != null) {
            listener.itemClick(name, d);
        }
    }
}