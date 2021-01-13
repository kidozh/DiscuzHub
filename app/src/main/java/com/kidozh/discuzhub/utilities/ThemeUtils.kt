package com.kidozh.discuzhub.utilities

import android.content.Context
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.AppTheme

class ThemeUtils(context: Context) {
    val themeList = arrayListOf<AppTheme>(
    AppTheme(context.getColor(R.color.colorBelizahole),context.getColor(R.color.colorPeterRiver),context.getColor(R.color.colorPomegranate),R.string.theme_belizahole),
    AppTheme(context.getColor(R.color.MaterialColorPink),context.getColor(R.color.MaterialColorRed),context.getColor(R.color.MaterialColorCyan),R.string.theme_pink),
    AppTheme(context.getColor(R.color.MaterialColorPurple),context.getColor(R.color.MaterialColorDeepPurple),context.getColor(R.color.MaterialColorGreen),R.string.theme_purple),
    AppTheme(context.getColor(R.color.MaterialColorLightBlue),context.getColor(R.color.MaterialColorBlue),context.getColor(R.color.MaterialColorOrange),R.string.theme_blue),
    AppTheme(context.getColor(R.color.MaterialColorCyan),context.getColor(R.color.MaterialColorTeal),context.getColor(R.color.MaterialColorRed),R.string.theme_cyan),
    AppTheme(context.getColor(R.color.MaterialColorLightGreen),context.getColor(R.color.MaterialColorGreen),context.getColor(R.color.MaterialColorPurple),R.string.theme_green),
    AppTheme(context.getColor(R.color.MaterialColorLime),context.getColor(R.color.MaterialColorYellow),context.getColor(R.color.MaterialColorIndigo),R.string.theme_lime),
    AppTheme(context.getColor(R.color.MaterialColorYellow),context.getColor(R.color.MaterialColorAmber),context.getColor(R.color.MaterialColorPurple),R.string.theme_yellow),
    AppTheme(context.getColor(R.color.MaterialColorOrange),context.getColor(R.color.MaterialColorDeepOrange),context.getColor(R.color.MaterialColorBlue),R.string.theme_orange),
    AppTheme(context.getColor(R.color.MaterialColorGrey),context.getColor(R.color.MaterialColorBlueGrey),context.getColor(R.color.colorPeterRiver),R.string.theme_gray),
    AppTheme(context.getColor(R.color.colorWetasphalt),context.getColor(R.color.colorMidnightblue),context.getColor(R.color.colorEmerland),R.string.theme_deep_gray),
    )
}