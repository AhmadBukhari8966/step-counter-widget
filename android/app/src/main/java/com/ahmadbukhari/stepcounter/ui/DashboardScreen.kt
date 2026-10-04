package com.ahmadbukhari.stepcounter.ui

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.ahmadbukhari.stepcounter.R
import com.ahmadbukhari.stepcounter.data.DailySteps
import com.ahmadbukhari.stepcounter.data.StepSnapshot
import com.ahmadbukhari.stepcounter.data.StepStore
import com.ahmadbukhari.stepcounter.data.averageSteps
import com.ahmadbukhari.stepcounter.data.daysMeeting
import com.ahmadbukhari.stepcounter.data.totalSteps
import com.ahmadbukhari.stepcounter.ui.theme.RingColors
import com.ahmadbukhari.stepcounter.widget.StepsWidgetReceiver
import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: StepsViewModel) {
    val snapshot by viewModel.snapshot.collectAsState()
    val goal by viewModel.goal.collectAsState()
    val today = LocalDate.now()
    val data = snapshot ?: StepSnapshot.EMPTY
    val week = data.week(today)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = { Text("Steps") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(painterResource(R.drawable.ic_refresh), contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item { TodayCard(data.stepsOn(today), goal, snapshot?.fetchedAt) }
                viewModel.errorMessage?.let { message ->
                    item { InfoCard(title = "Couldn't refresh", body = message) }
                }
                item { WeekCard(week, goal) }
                item { GoalCard(goal, onChange = viewModel::changeGoal) }
                if (week.all { it.steps == 0L }) {
                    item { NoStepsCard() }
                }
                item { WidgetCard(viewModel.canRefreshInBackground) }
            }
        }
    }
}

@Composable
private fun DashboardCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().widthIn(max = 640.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Box(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
private fun TodayCard(steps: Long, goal: Int, updatedAt: Instant?) {
    val progress = steps.toFloat() / goal.coerceAtLeast(1)
    DashboardCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clearAndSetSemantics {
                        contentDescription = "${steps.formattedSteps()} steps today, " +
                            "${progress.formattedPercent()} of your ${goal.toLong().formattedSteps()} step goal"
                    },
                contentAlignment = Alignment.Center,
            ) {
                ProgressRing(
                    progress = progress,
                    colors = RingColors,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeWidth = 22.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(if (steps >= goal) R.drawable.ic_check_circle else R.drawable.ic_steps),
                        contentDescription = null,
                        tint = RingColors.last(),
                        modifier = Modifier.size(28.dp),
                    )
                    Text(steps.formattedSteps(), fontSize = 46.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(
                        "of ${goal.toLong().formattedSteps()} steps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.size(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Progress", progress.formattedPercent(), Modifier.weight(1f))
                StatTile(
                    if (steps >= goal) "Over goal" else "To go",
                    abs(goal - steps).formattedSteps(),
                    Modifier.weight(1f),
                )
                StatTile("Updated", updatedAt?.formattedTime() ?: "–", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(title: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeekCard(week: List<DailySteps>, goal: Int) {
    DashboardCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text("Last 7 days", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    "Avg ${week.averageSteps.formattedSteps()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WeekChart(week, goal, RingColors, height = 170.dp)
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "${week.daysMeeting(goal)} of ${week.size} days at goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${week.totalSteps.formattedSteps()} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GoalCard(goal: Int, onChange: (Int) -> Unit) {
    DashboardCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Daily goal", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${goal.toLong().formattedSteps()} steps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(
                    onClick = { onChange(-StepStore.GOAL_STEP) },
                    enabled = goal > StepStore.GOAL_RANGE.first,
                ) {
                    Icon(painterResource(R.drawable.ic_remove), contentDescription = "Decrease goal")
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(
                    onClick = { onChange(StepStore.GOAL_STEP) },
                    enabled = goal < StepStore.GOAL_RANGE.last,
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = "Increase goal")
                }
            }
            Text(
                "Your widgets show progress toward this goal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoStepsCard() {
    val context = LocalContext.current
    InfoCard(
        title = "No steps showing?",
        body = "Step Counter shows steps that are saved in Health Connect, for example by your phone, Google Fit, " +
            "Fitbit or Samsung Health. Check that one of them shares steps with Health Connect, and that Step " +
            "Counter is allowed to read them.",
        action = "Open Health Connect" to { openHealthConnectSettings(context) },
    )
}

@Composable
private fun WidgetCard(canRefreshInBackground: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val canPin = AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    DashboardCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add a widget", style = MaterialTheme.typography.titleMedium)
            Text(
                "Touch and hold an empty spot on your Home Screen, tap Widgets, and find Step Counter. " +
                    "To change its colors, touch and hold the widget and choose its settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!canRefreshInBackground) {
                Text(
                    "Right now the widget refreshes when you open Step Counter. To let it refresh on its own, " +
                        "allow Step Counter to access data in the background in Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { openHealthConnectSettings(context) }) { Text("Open Health Connect") }
            }
            if (canPin) {
                Button(
                    onClick = {
                        scope.launch {
                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(StepsWidgetReceiver::class.java)
                        }
                    },
                ) { Text("Add to Home Screen") }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String, action: Pair<String, () -> Unit>? = null) {
    DashboardCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
            action?.let { (label, onClick) -> TextButton(onClick = onClick) { Text(label) } }
        }
    }
}
