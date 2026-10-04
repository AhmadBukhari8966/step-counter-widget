package com.ahmadbukhari.stepcounter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val BrandOrange = Color(0xFFFF9F0A)
val BrandPink = Color(0xFFFF375F)

/** The progress ring's gradient, the same as the iPhone app's. */
val RingColors = listOf(BrandOrange, BrandPink)

private val LightColors = lightColorScheme(primary = Color(0xFFD81B4A), secondary = Color(0xFFE07A00))
private val DarkColors = darkColorScheme(primary = BrandPink, secondary = BrandOrange)

/** Material You colors from the wallpaper on Android 12 and later, brand colors before that. */
@Composable
fun StepCounterTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
