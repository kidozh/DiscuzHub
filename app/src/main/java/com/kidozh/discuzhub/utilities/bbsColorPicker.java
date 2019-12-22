package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.kidozh.discuzhub.R;

/**
 * Created by free2 on 16-7-19.
 * color picker on edit bar
 * 发帖的时候颜色选择器
 */

public class bbsColorPicker extends PopupWindow implements AdapterView.OnItemClickListener {

    private Context mContext;
    private GridView gridView;
    private OnItemSelectListener listener;
    private MyAdapter adapter;
    private String[][] colorDatas = null;

    public bbsColorPicker(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public static int px2dip(int pxValue)
    {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static float dip2px(float dipValue)
    {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return  (dipValue * scale + 0.5f);
    }

    public static int dip2px(int dipValue)
    {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return  (int) (dipValue * scale + 0.5f);
    }


    private void init() {
        String[] colors = mContext.getResources().getStringArray(R.array.bbs_color_list);
        for (int i = 0; i < colors.length; i++) {
            if (colorDatas == null) {
                colorDatas = new String[colors.length][2];
            }
            colorDatas[i][0] = colors[i].split(",")[0];
            colorDatas[i][1] = colors[i].split(",")[1];
        }

        gridView = new GridView(mContext);
        gridView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        gridView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorBackgroundDefault));
        gridView.setNumColumns(8);
        gridView.setPadding(dip2px( 8), dip2px( 12), dip2px(8), dip2px(12));
        gridView.setGravity(Gravity.CENTER);
        gridView.setHorizontalSpacing(dip2px(4));
        gridView.setVerticalSpacing(dip2px(12));
        gridView.setOnItemClickListener(this);

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.rec_solid_primary_bg));
        setFocusable(true);
        setContentView(gridView);

        adapter = new MyAdapter();
        gridView.setAdapter(adapter);
    }

    public void setListener(OnItemSelectListener listener) {
        this.listener = listener;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (listener != null) {
            listener.itemClick(i, view, colorDatas[i][0]);
        }
        dismiss();
    }

    public interface OnItemSelectListener {
        void itemClick(int pos, View v, String color);
    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return colorDatas.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        public int getColor(Context c, String str) {

            // style="color: #EC1282;">
            int color = ContextCompat.getColor(c, R.color.colorPrimary);
            if (str.contains("color")) {
                int start = str.indexOf("color");
                int end = str.indexOf(";", start);
                String temp = str.substring(start, end);

                int startC = temp.indexOf("#");

                String colorStr = temp.substring(startC).trim();
                try {
                    color = Color.parseColor(colorStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (str.startsWith("#")) {
                try {
                    color = Color.parseColor(str);
                } catch (Exception e) {
                    Log.e("color", color + "");
                    e.printStackTrace();
                }
            }
            return color;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View colorView;
            colorView = new View(mContext);
            colorView.setLayoutParams(new GridView.LayoutParams(dip2px(20), dip2px(20)));//设置ImageView对象布局
            colorView.setPadding(4, 4, 4, 4);//设置间距
            int color = getColor(mContext, colorDatas[i][1]);
            colorView.setBackgroundColor(color);
            return colorView;
        }
    }
}