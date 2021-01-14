package com.kidozh.discuzhub.utilities

import android.content.Context
import android.graphics.Color
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.AppTheme

class ThemeUtils(context: Context) {
    val themeList = arrayListOf<AppTheme>(
            AppTheme(context.getColor(R.color.colorBelizahole), context.getColor(R.color.colorPeterRiver), context.getColor(R.color.colorPomegranate), R.string.theme_belizahole),
            AppTheme(context.getColor(R.color.MaterialColorPink), context.getColor(R.color.MaterialColorRed), context.getColor(R.color.MaterialColorCyan), R.string.theme_pink),
            AppTheme(context.getColor(R.color.MaterialColorPurple), context.getColor(R.color.MaterialColorDeepPurple), context.getColor(R.color.MaterialColorGreen), R.string.theme_purple),
            AppTheme(context.getColor(R.color.MaterialColorLightBlue), context.getColor(R.color.MaterialColorBlue), context.getColor(R.color.MaterialColorOrange), R.string.theme_blue),
            AppTheme(context.getColor(R.color.MaterialColorCyan), context.getColor(R.color.MaterialColorTeal), context.getColor(R.color.MaterialColorRed), R.string.theme_cyan),
            AppTheme(context.getColor(R.color.MaterialColorLightGreen), context.getColor(R.color.MaterialColorGreen), context.getColor(R.color.MaterialColorPurple), R.string.theme_green),
            AppTheme(context.getColor(R.color.MaterialColorLime), context.getColor(R.color.MaterialColorYellow), context.getColor(R.color.MaterialColorIndigo), R.string.theme_lime),
            AppTheme(context.getColor(R.color.MaterialColorYellow), context.getColor(R.color.MaterialColorAmber), context.getColor(R.color.MaterialColorPurple), R.string.theme_yellow),
            AppTheme(context.getColor(R.color.MaterialColorOrange), context.getColor(R.color.MaterialColorDeepOrange), context.getColor(R.color.MaterialColorBlue), R.string.theme_orange),
            AppTheme(context.getColor(R.color.MaterialColorGrey), context.getColor(R.color.MaterialColorBlueGrey), context.getColor(R.color.colorPeterRiver), R.string.theme_gray),
            AppTheme(context.getColor(R.color.colorWetasphalt), context.getColor(R.color.colorMidnightblue), context.getColor(R.color.colorEmerland), R.string.theme_deep_gray),
            AppTheme(Color.parseColor("#6200EE"), Color.parseColor("#3700B3"), Color.parseColor("#BB86FC"), R.string.theme_android_default),
            AppTheme(Color.parseColor("#6200EE"), Color.parseColor("#3700B3"), Color.parseColor("#03DAC5"), R.string.theme_android_default_2),
            AppTheme(Color.parseColor("#4CAF50"), Color.parseColor("#388E3C"), Color.parseColor("#536DFE"), R.string.theme_green_indigo),
            AppTheme(Color.parseColor("#673AB7"), Color.parseColor("#512DA8"), Color.parseColor("#7C4DFF"), R.string.theme_pure_deep_purple),
            AppTheme(Color.parseColor("#E91E63"), Color.parseColor("#C2185B"), Color.parseColor("#FF4081"), R.string.theme_pure_pink),
            AppTheme(Color.parseColor("#009688"), Color.parseColor("#00796B"), Color.parseColor("#009688"), R.string.theme_pure_teal),
            AppTheme(Color.parseColor("#3F51B5"), Color.parseColor("#303F9F"), Color.parseColor("#FF5722"), R.string.theme_indigo_deep_orange),
            AppTheme(Color.parseColor("#607D8B"), Color.parseColor("#455A64"), Color.parseColor("#9E9E9E"), R.string.theme_blue_gray_gray),
            AppTheme(Color.parseColor("#8BC34A"), Color.parseColor("#689F38"), Color.parseColor("#4CAF50"), R.string.theme_light_green_green),
            AppTheme(Color.parseColor("#3F51B5"), Color.parseColor("#303F9F"), Color.parseColor("#FF4081"), R.string.theme_indigo_pink),
            AppTheme(Color.parseColor("#795548"), Color.parseColor("#5D4037"), Color.parseColor("#795548"), R.string.theme_pure_brown),
            AppTheme(Color.parseColor("#3F51B5"), Color.parseColor("#303F9F"), Color.parseColor("#009688"), R.string.theme_indigo_teal),
            AppTheme(Color.parseColor("#3F51B5"), Color.parseColor("#303F9F"), Color.parseColor("#607D8B"), R.string.theme_indigo_blue_gray),
    )

    companion object{
        val styleList = intArrayOf(
                R.style.AppTheme_Default, R.style.AppTheme_Red, R.style.AppTheme_Purple,
                R.style.AppTheme_Indigo, R.style.AppTheme_Cyan, R.style.AppTheme_Green,
                R.style.AppTheme_Lime, R.style.AppTheme_Yellow, R.style.AppTheme_Orange,
                R.style.AppTheme_Gray, R.style.AppTheme_Black,R.style.AppTheme_AndroidDefault,
                R.style.AppTheme_AndroidDefault2,R.style.AppTheme_GreenIndigo,R.style.AppTheme_PureDeepPurple,
                R.style.AppTheme_PurePink,R.style.AppTheme_PureTeal, R.style.AppTheme_IndigoDeepOrange,
                R.style.AppTheme_BlueGrayGray,R.style.AppTheme_LightGreenGreen,R.style.AppTheme_IndigoPink,
                R.style.AppTheme_PureBrown,R.style.AppTheme_IndigoTeal, R.style.AppTheme_IndigoBlueGray
        )
    }
}