package com.kidozh.discuzhub.utilities

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.SimpleItemAnimator
import jp.wasabeef.recyclerview.animators.*

object AnimationUtils {

    fun getRecyclerviewAnimation(context: Context): ItemAnimator? {
        if (UserPreferenceUtils.getEnableRecyclerviewAnimate(context)) {
            val animationType = UserPreferenceUtils.getRecyclerviewAnimateType(context)
            when (animationType) {
                "LandingAnimator" -> return LandingAnimator()
                "ScaleInAnimator" -> return ScaleInAnimator()
                "ScaleInTopAnimator" -> return ScaleInTopAnimator()
                "ScaleInBottomAnimator" -> return ScaleInBottomAnimator()
                "ScaleInLeftAnimator" -> return ScaleInLeftAnimator()
                "ScaleInRightAnimator" -> return ScaleInRightAnimator()

                "FadeInAnimator" -> return FadeInAnimator()
                "FadeInDownAnimator" -> return FadeInDownAnimator()
                "FadeInUpAnimator" -> return FadeInUpAnimator()
                "FadeInLeftAnimator" -> return FadeInLeftAnimator()
                "FadeInRightAnimator" -> return FadeInRightAnimator()

                "FlipInTopXAnimator" -> return FlipInTopXAnimator()
                "FlipInBottomXAnimator" -> return FlipInBottomXAnimator()
                "FlipInLeftYAnimator" -> return FlipInLeftYAnimator()
                "FlipInRightYAnimator" -> return FlipInRightYAnimator()

                "SlideInLeftAnimator" -> return SlideInLeftAnimator()
                "SlideInRightAnimator" -> return SlideInRightAnimator()
                "SlideInDownAnimator" -> return SlideInDownAnimator()
                "SlideInUpAnimator" -> return SlideInUpAnimator()


                "OvershootInLeftAnimator" -> return OvershootInLeftAnimator()
                "OvershootInRightAnimator" -> return OvershootInRightAnimator()

                else -> {
                    return LandingAnimator()
                }
            }
        }
        return null;
    }


    fun configureRecyclerviewAnimation(context: Context, recyclerView: RecyclerView) {
        if (UserPreferenceUtils.getEnableRecyclerviewAnimate(context)) {
            val animationType = UserPreferenceUtils.getRecyclerviewAnimateType(context)
            when (animationType) {
                "LandingAnimator" -> recyclerView.itemAnimator = LandingAnimator()
                "ScaleInAnimator" -> recyclerView.itemAnimator = ScaleInAnimator()
                "ScaleInTopAnimator" -> recyclerView.itemAnimator = ScaleInTopAnimator()
                "ScaleInBottomAnimator" -> recyclerView.itemAnimator = ScaleInBottomAnimator()
                "ScaleInLeftAnimator" -> recyclerView.itemAnimator = ScaleInLeftAnimator()
                "ScaleInRightAnimator" -> recyclerView.itemAnimator = ScaleInRightAnimator()

                "FadeInAnimator" -> recyclerView.itemAnimator = FadeInAnimator()
                "FadeInDownAnimator" -> recyclerView.itemAnimator = FadeInDownAnimator()
                "FadeInUpAnimator" -> recyclerView.itemAnimator = FadeInUpAnimator()
                "FadeInLeftAnimator" -> recyclerView.itemAnimator = FadeInLeftAnimator()
                "FadeInRightAnimator" -> recyclerView.itemAnimator = FadeInRightAnimator()

                "FlipInTopXAnimator" -> recyclerView.itemAnimator = FlipInTopXAnimator()
                "FlipInBottomXAnimator" -> recyclerView.itemAnimator = FlipInBottomXAnimator()
                "FlipInLeftYAnimator" -> recyclerView.itemAnimator = FlipInLeftYAnimator()
                "FlipInRightYAnimator" -> recyclerView.itemAnimator = FlipInRightYAnimator()

                "SlideInLeftAnimator" -> recyclerView.itemAnimator = SlideInLeftAnimator()
                "SlideInRightAnimator" -> recyclerView.itemAnimator = SlideInRightAnimator()
                "SlideInDownAnimator" -> recyclerView.itemAnimator = SlideInDownAnimator()
                "SlideInUpAnimator" -> recyclerView.itemAnimator = SlideInUpAnimator()


                "OvershootInLeftAnimator" -> recyclerView.itemAnimator = OvershootInLeftAnimator()
                "OvershootInRightAnimator" -> recyclerView.itemAnimator = OvershootInRightAnimator()

                else ->{
                    recyclerView.itemAnimator = LandingAnimator()
                }
            }
        }
    }
}