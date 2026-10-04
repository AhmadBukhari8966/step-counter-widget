package com.ahmadbukhari.stepcounter.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.ahmadbukhari.stepcounter.data.HealthSteps
import com.ahmadbukhari.stepcounter.data.StepSnapshot
import com.ahmadbukhari.stepcounter.data.StepStore
import com.ahmadbukhari.stepcounter.data.StepSync

/** The Home Screen widget. It adapts its layout to the size the user picks. */
class StepsWidget : GlanceAppWidget() {

    // Exact sizes so the ring and chart bitmaps are drawn at the size they're shown.
    override val sizeMode = SizeMode.Exact

    // Holds each widget's theme.
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val status = WidgetStatus.load(context)
        val initialSnapshot = StepStore.snapshot(context)
        val initialGoal = StepStore.goal(context)

        provideContent {
            // Follow the shared store, so a refresh or a new goal shows up right away.
            val snapshot by StepStore.snapshotFlow(context).collectAsState(initialSnapshot)
            val goal by StepStore.goalFlow(context).collectAsState(initialGoal)
            val theme = WidgetTheme.from(currentState(THEME_KEY))

            GlanceTheme {
                StepsWidgetContent(status, snapshot, goal, theme)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            GlanceTheme {
                StepsWidgetContent(
                    status = WidgetStatus(WidgetStatus.State.READY, canRefreshInBackground = false),
                    snapshot = StepSnapshot.sample(),
                    goal = StepStore.DEFAULT_GOAL,
                    theme = WidgetTheme.CLASSIC,
                )
            }
        }
    }

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
    }
}

class StepsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StepsWidget()
}

/** Whether the widget can show steps, and whether its refresh button can read Health Connect itself. */
data class WidgetStatus(val state: State, val canRefreshInBackground: Boolean) {
    enum class State { READY, NEEDS_PERMISSION, UNAVAILABLE }

    companion object {
        /**
         * Reads fresh totals when Health Connect allows a background read; otherwise the
         * widget shows the last totals the app saved.
         */
        suspend fun load(context: Context): WidgetStatus {
            val health = HealthSteps(context)
            val hasSnapshot = StepStore.snapshot(context) != null
            if (health.availability != HealthSteps.Availability.AVAILABLE) {
                return WidgetStatus(if (hasSnapshot) State.READY else State.UNAVAILABLE, canRefreshInBackground = false)
            }

            val canRead = runCatching { health.canReadSteps() }.getOrDefault(false)
            val canReadInBackground = canRead && runCatching { health.canReadInBackground() }.getOrDefault(false)
            if (canReadInBackground) runCatching { StepSync.fetchAndSave(context) }

            val state = if (canRead || hasSnapshot) State.READY else State.NEEDS_PERMISSION
            return WidgetStatus(state, canReadInBackground)
        }
    }
}

/** The refresh button, used when Health Connect allows background reads. */
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching { StepSync.fetchAndSave(context) }
        StepsWidget().update(context, glanceId)
    }
}
