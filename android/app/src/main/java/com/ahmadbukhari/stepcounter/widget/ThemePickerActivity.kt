package com.ahmadbukhari.stepcounter.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.ahmadbukhari.stepcounter.ui.ProgressRing
import com.ahmadbukhari.stepcounter.ui.theme.StepCounterTheme
import kotlinx.coroutines.launch

/**
 * Lets the user pick a color theme for one widget. Android opens it from the widget's
 * settings (touch and hold the widget), and when the widget is added on older versions.
 */
class ThemePickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Leaving without choosing keeps the widget as it was.
        setResult(RESULT_OK, result)

        enableEdgeToEdge()
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@ThemePickerActivity)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            val state = getAppWidgetState<Preferences>(this@ThemePickerActivity, PreferencesGlanceStateDefinition, glanceId)
            val current = WidgetTheme.from(state[StepsWidget.THEME_KEY])

            setContent {
                StepCounterTheme {
                    ThemePicker(current) { theme ->
                        lifecycleScope.launch {
                            updateAppWidgetState(this@ThemePickerActivity, glanceId) { it[StepsWidget.THEME_KEY] = theme.name }
                            StepsWidget().update(this@ThemePickerActivity, glanceId)
                            setResult(RESULT_OK, result)
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePicker(current: WidgetTheme, onPick: (WidgetTheme) -> Unit) {
    var selected by remember { mutableStateOf(current) }
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Widget theme", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Choose how this widget looks. Each widget can have its own theme.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WidgetTheme.entries.forEach { theme ->
                ThemeRow(theme, isSelected = theme == selected) {
                    selected = theme
                    onPick(theme)
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(theme: WidgetTheme, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemeSwatch(theme)
        Text(
            theme.label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        RadioButton(selected = isSelected, onClick = onClick)
    }
}

/** A small widget-like square with the theme's background and ring. */
@Composable
private fun ThemeSwatch(theme: WidgetTheme) {
    val background = if (theme.backgroundColors.isEmpty()) {
        Brush.linearGradient(List(2) { MaterialTheme.colorScheme.surfaceContainerHigh })
    } else {
        Brush.linearGradient(theme.backgroundColors.map(::Color))
    }
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(12.dp),
    ) {
        ProgressRing(
            progress = 0.72f,
            colors = theme.ringColors.map(::Color),
            trackColor = Color(theme.trackColor),
            strokeWidth = 6.dp,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
