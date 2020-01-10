package com.kidozh.discuzhub.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.engine.Resource;
import com.kidozh.discuzhub.R;

import java.util.HashSet;
import java.util.Set;

public class WrapContentHeightViewPager extends ViewPager {
    private static final String TAG = WrapContentHeightViewPager.class.getSimpleName();

    public WrapContentHeightViewPager(Context context) {
        super(context);
    }

    public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = 0;
        @SuppressLint("DrawAllocation") Set<Integer> setIds = new HashSet<>();
        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            int id = child.getId();
            if(setIds.contains(id)){
                //continue;
            }
            else {
                setIds.add(id);
            }
            height += h;

            Log.d(TAG,"id "+child.getId()+ " " +" height "+h+" index "+i+"/"+getChildCount());
            //if(h > height) height = h;
        }

        if (height != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}