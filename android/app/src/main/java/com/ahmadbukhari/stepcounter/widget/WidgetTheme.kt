package com.ahmadbukhari.stepcounter.widget

import androidx.annotation.DrawableRes
import com.ahmadbukhari.stepcounter.R

const val BRAND_ORANGE = 0xFFFF9F0A.toInt()
const val BRAND_PINK = 0xFFFF375F.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

/** Color themes for the Home Screen widget, chosen per widget. */
enum class WidgetTheme(
    val label: String,
    /** Gradient behind the widget, or null for the system's Material You background. */
    @param:DrawableRes val background: Int?,
    /** Gradient colors for previews in the theme picker. */
    val backgroundColors: IntArray,
    /** Gradient along the progress ring and the bars for days that met the goal. */
    val ringColors: IntArray,
    /** White text and track, for themes with a colored background. */
    val lightContent: Boolean,
) {
    CLASSIC("Classic", null, intArrayOf(), intArrayOf(BRAND_ORANGE, BRAND_PINK), lightContent = false),
    SUNSET(
        "Sunset", R.drawable.widget_bg_sunset,
        intArrayOf(0xFFFF9500.toInt(), 0xFFF5325C.toInt()), intArrayOf(WHITE, WHITE), lightContent = true,
    ),
    OCEAN(
        "Ocean", R.drawable.widget_bg_ocean,
        intArrayOf(0xFF1D6FE0.toInt(), 0xFF14A8C4.toInt()), intArrayOf(WHITE, WHITE), lightContent = true,
    ),
    FOREST(
        "Forest", R.drawable.widget_bg_forest,
        intArrayOf(0xFF11804A.toInt(), 0xFF45B046.toInt()), intArrayOf(WHITE, WHITE), lightContent = true,
    ),
    MIDNIGHT(
        "Midnight", R.drawable.widget_bg_midnight,
        intArrayOf(0xFF262B4F.toInt(), 0xFF0C0D1A.toInt()),
        intArrayOf(0xFF64D2FF.toInt(), 0xFFBF5AF2.toInt()), lightContent = true,
    );

    /** Ring track. The gray for Classic reads well on both light and dark backgrounds. */
    val trackColor: Int get() = if (lightContent) 0x40FFFFFF else 0x33808080

    /** Bars for days that missed the goal. */
    val mutedBarColor: Int get() = if (lightContent) 0x59FFFFFF else 0x4D808080

    companion object {
        fun from(name: String?): WidgetTheme = entries.firstOrNull { it.name == name } ?: CLASSIC
    }
}
