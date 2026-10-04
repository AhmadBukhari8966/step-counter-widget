package com.ahmadbukhari.stepcounter.widget

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager

/**
 * On Android 15 and later, the widget picker can show a live preview drawn by the widget
 * itself (see StepsWidget.providePreview). Android limits how often it can be updated,
 * so publish it once per app version.
 */
object WidgetPreviews {
    private const val PREFS = "widget_previews"
    private const val KEY_VERSION = "published_version"

    suspend fun publishIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val version = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        if (prefs.getLong(KEY_VERSION, -1) == version) return

        val result = runCatching {
            GlanceAppWidgetManager(context).setWidgetPreviews(StepsWidgetReceiver::class)
        }.getOrNull()
        if (result == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
            prefs.edit { putLong(KEY_VERSION, version) }
        }
    }
}
