package com.example.lifedots.wallpaper.renderer

import android.graphics.Color
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.WallpaperSettings

data class ThemeColors(
    val background: Int,
    val filledDot: Int,
    val emptyDot: Int,
    val todayDot: Int
) {
    companion object {
        fun fromSettings(settings: WallpaperSettings): ThemeColors {
            return when (settings.theme) {
                ThemeOption.LIGHT -> ThemeColors(
                    background = Color.parseColor("#F5F5F5"),
                    filledDot = Color.parseColor("#2C2C2C"),
                    emptyDot = Color.parseColor("#D0D0D0"),
                    todayDot = Color.parseColor("#4A90D9")
                )
                ThemeOption.DARK -> ThemeColors(
                    background = Color.parseColor("#1A1A1A"),
                    filledDot = Color.parseColor("#E0E0E0"),
                    emptyDot = Color.parseColor("#3A3A3A"),
                    todayDot = Color.parseColor("#5BA0E9")
                )
                ThemeOption.AMOLED -> ThemeColors(
                    background = Color.parseColor("#000000"),
                    filledDot = Color.parseColor("#FFFFFF"),
                    emptyDot = Color.parseColor("#2A2A2A"),
                    todayDot = Color.parseColor("#6AB0F9")
                )
                ThemeOption.LIQUID_GLASS -> ThemeColors(
                    background = 0xFF0B182C.toInt(),
                    filledDot = 0xFFD9F4FF.toInt(),
                    emptyDot = 0xFF456078.toInt(),
                    todayDot = 0xFF83F4DC.toInt()
                )
                ThemeOption.CUSTOM -> ThemeColors(
                    background = settings.customColors.backgroundColor,
                    filledDot = settings.customColors.filledDotColor,
                    emptyDot = settings.customColors.emptyDotColor,
                    todayDot = settings.customColors.todayDotColor
                )
            }
        }
    }
}

data class GridConfig(
    val cols: Int,
    val rows: Int,
    val cellSize: Float,
    val dotRadius: Float,
    val startX: Float,
    val startY: Float
)

enum class DotType {
    FILLED, EMPTY, TODAY
}
