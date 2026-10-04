package com.ahmadbukhari.stepcounter.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ahmadbukhari.stepcounter.MainActivity
import com.ahmadbukhari.stepcounter.R
import com.ahmadbukhari.stepcounter.data.DailySteps
import com.ahmadbukhari.stepcounter.data.StepSnapshot
import com.ahmadbukhari.stepcounter.data.averageSteps
import com.ahmadbukhari.stepcounter.data.daysMeeting
import com.ahmadbukhari.stepcounter.data.totalSteps
import com.ahmadbukhari.stepcounter.ui.compactSteps
import com.ahmadbukhari.stepcounter.ui.formattedPercent
import com.ahmadbukhari.stepcounter.ui.formattedSteps
import com.ahmadbukhari.stepcounter.ui.formattedTime
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle

private val PADDING = 14.dp

/** Medium layouts start at roughly 4×2 cells, large at 4×4. */
private val MEDIUM_MIN_WIDTH = 240.dp
private val LARGE_MIN_HEIGHT = 230.dp

/** Everything the layouts show for today. */
private class WidgetModel(snapshot: StepSnapshot?, val goal: Int, val theme: WidgetTheme) {
    val today: LocalDate = LocalDate.now()
    val steps: Long = snapshot?.stepsOn(today) ?: 0
    val week: List<DailySteps> = (snapshot ?: StepSnapshot.EMPTY).week(today)
    val progress: Float = steps.toFloat() / goal.coerceAtLeast(1)
    val updatedAt = snapshot?.fetchedAt
    val goalReached = steps >= goal
    val remaining = (goal - steps).coerceAtLeast(0)
}

/** Text colors for a theme. */
private class WidgetColors(
    val primary: ColorProvider,
    val secondary: ColorProvider,
    val accent: ColorProvider,
    val buttonBackground: ColorProvider,
)

/** White on colored backgrounds, Material You colors on Classic. */
@Composable
private fun widgetColors(theme: WidgetTheme): WidgetColors =
    if (theme.lightContent) {
        WidgetColors(
            primary = ColorProvider(Color.White),
            secondary = ColorProvider(Color.White.copy(alpha = 0.8f)),
            accent = ColorProvider(Color.White),
            buttonBackground = ColorProvider(Color.White.copy(alpha = 0.22f)),
        )
    } else {
        WidgetColors(
            primary = GlanceTheme.colors.onSurface,
            secondary = GlanceTheme.colors.onSurfaceVariant,
            accent = ColorProvider(Color(BRAND_PINK)),
            buttonBackground = GlanceTheme.colors.secondaryContainer,
        )
    }

@Composable
fun StepsWidgetContent(status: WidgetStatus, snapshot: StepSnapshot?, goal: Int, theme: WidgetTheme) {
    val colors = widgetColors(theme)
    val background = when (val drawable = theme.background) {
        null -> GlanceModifier.background(GlanceTheme.colors.widgetBackground)
        else -> GlanceModifier.background(ImageProvider(drawable))
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .then(systemCornerRadius())
            .then(background)
            .clickable(actionStartActivity<MainActivity>())
            .padding(PADDING),
        contentAlignment = Alignment.Center,
    ) {
        when (status.state) {
            WidgetStatus.State.READY -> {
                val model = WidgetModel(snapshot, goal, theme)
                // Without background reads the refresh button opens the app, which refreshes on open.
                val refresh: Action =
                    if (status.canRefreshInBackground) actionRunCallback<RefreshAction>()
                    else actionStartActivity<MainActivity>()
                val size = LocalSize.current
                when {
                    size.width >= MEDIUM_MIN_WIDTH && size.height >= LARGE_MIN_HEIGHT -> LargeLayout(model, colors, refresh)
                    size.width >= MEDIUM_MIN_WIDTH -> MediumLayout(model, colors, refresh)
                    else -> SmallLayout(model, colors)
                }
            }
            WidgetStatus.State.NEEDS_PERMISSION ->
                Message(R.drawable.ic_steps, "Open Step Counter to connect Health Connect.", colors)
            WidgetStatus.State.UNAVAILABLE ->
                Message(R.drawable.ic_steps, "Health Connect isn't available on this device.", colors)
        }
    }
}

// MARK: - Small

@Composable
private fun SmallLayout(model: WidgetModel, colors: WidgetColors) {
    val size = LocalSize.current
    val headerHeight = 22.dp
    val ringSize = minOf(size.width - PADDING * 2, size.height - PADDING * 2 - headerHeight)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(headerHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(if (model.goalReached) R.drawable.ic_check_circle else R.drawable.ic_steps),
                contentDescription = null,
                modifier = GlanceModifier.size(14.dp),
                colorFilter = ColorFilter.tint(colors.accent),
            )
            Spacer(GlanceModifier.width(4.dp))
            Text("Steps", style = textStyle(colors.secondary, 12.sp, FontWeight.Medium))
            Spacer(GlanceModifier.defaultWeight())
            Text(model.progress.formattedPercent(), style = textStyle(colors.secondary, 12.sp, FontWeight.Medium))
        }
        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.Center) {
            Ring(model, ringSize, strokeWidth = ringSize * 0.11f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    model.steps.formattedSteps(),
                    style = textStyle(colors.primary, numberSize(ringSize, 0.21f), FontWeight.Bold),
                    maxLines = 1,
                )
                Text("of ${model.goal.toLong().compactSteps()}", style = textStyle(colors.secondary, 11.sp, FontWeight.Medium))
            }
        }
    }
}

// MARK: - Medium

@Composable
private fun MediumLayout(model: WidgetModel, colors: WidgetColors, refresh: Action) {
    val size = LocalSize.current
    val ringSize = size.height - PADDING * 2
    val spacing = 14.dp
    val chartWidth = size.width - PADDING * 2 - ringSize - spacing
    val headerHeight = 38.dp
    val labelHeight = 14.dp
    val chartHeight = size.height - PADDING * 2 - headerHeight - labelHeight - 6.dp

    Row(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = GlanceModifier.size(ringSize), contentAlignment = Alignment.Center) {
            Ring(model, ringSize, strokeWidth = ringSize * 0.1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    model.steps.formattedSteps(),
                    style = textStyle(colors.primary, numberSize(ringSize, 0.19f), FontWeight.Bold),
                    maxLines = 1,
                )
                Text("steps", style = textStyle(colors.secondary, 12.sp, FontWeight.Medium))
            }
        }
        Spacer(GlanceModifier.width(spacing))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Row(modifier = GlanceModifier.fillMaxWidth().height(headerHeight)) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(goalStatus(model), style = textStyle(colors.primary, 15.sp, FontWeight.Bold), maxLines = 1)
                    Text(
                        "${model.progress.formattedPercent()} of ${model.goal.toLong().formattedSteps()}",
                        style = textStyle(colors.secondary, 11.sp, FontWeight.Medium),
                        maxLines = 1,
                    )
                }
                RefreshButton(colors, refresh)
            }
            Spacer(GlanceModifier.height(6.dp))
            WeekChart(model, chartWidth, chartHeight, colors)
        }
    }
}

// MARK: - Large

@Composable
private fun LargeLayout(model: WidgetModel, colors: WidgetColors, refresh: Action) {
    val size = LocalSize.current
    val contentWidth = size.width - PADDING * 2
    val ringSize = 104.dp
    val headerHeight = 30.dp
    val statsHeight = 36.dp
    val labelHeight = 14.dp
    val chartHeight = size.height - PADDING * 2 - headerHeight - ringSize - statsHeight - labelHeight - 36.dp

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(headerHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_steps),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(colors.secondary),
            )
            Spacer(GlanceModifier.width(6.dp))
            Text("Today", style = textStyle(colors.secondary, 14.sp, FontWeight.Medium))
            Spacer(GlanceModifier.defaultWeight())
            model.updatedAt?.let {
                Text("Updated ${it.formattedTime()}", style = textStyle(colors.secondary, 11.sp, FontWeight.Normal))
                Spacer(GlanceModifier.width(6.dp))
            }
            RefreshButton(colors, refresh)
        }
        Spacer(GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = GlanceModifier.size(ringSize), contentAlignment = Alignment.Center) {
                Ring(model, ringSize, strokeWidth = 13.dp)
                Text(model.progress.formattedPercent(), style = textStyle(colors.primary, 20.sp, FontWeight.Bold))
            }
            Spacer(GlanceModifier.width(16.dp))
            Column {
                Text(model.steps.formattedSteps(), style = textStyle(colors.primary, 38.sp, FontWeight.Bold), maxLines = 1)
                Text(
                    "of ${model.goal.toLong().formattedSteps()} steps",
                    style = textStyle(colors.secondary, 14.sp, FontWeight.Medium),
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(goalStatus(model), style = textStyle(colors.accent, 13.sp, FontWeight.Bold))
            }
        }
        Spacer(GlanceModifier.height(14.dp))
        WeekChart(model, contentWidth, chartHeight, colors)
        Spacer(GlanceModifier.height(14.dp))
        Row(modifier = GlanceModifier.fillMaxWidth().height(statsHeight)) {
            Stat("7-day total", model.week.totalSteps.compactSteps(), colors, GlanceModifier.defaultWeight())
            Stat("Daily avg", model.week.averageSteps.formattedSteps(), colors, GlanceModifier.defaultWeight())
            Stat("Goals met", "${model.week.daysMeeting(model.goal)}/${model.week.size}", colors, GlanceModifier)
        }
    }
}

// MARK: - Pieces

@Composable
private fun Ring(model: WidgetModel, size: Dp, strokeWidth: Dp) {
    val density = LocalContext.current.resources.displayMetrics.density
    val sizePx = (size.value * density).toInt().coerceAtLeast(1)
    Image(
        provider = ImageProvider(WidgetBitmaps.ring(sizePx, strokeWidth.value * density, model.progress, model.theme)),
        contentDescription = "${model.steps.formattedSteps()} steps, ${model.progress.formattedPercent()} of your goal",
        modifier = GlanceModifier.size(size),
    )
}

@Composable
private fun WeekChart(model: WidgetModel, width: Dp, height: Dp, colors: WidgetColors) {
    val density = LocalContext.current.resources.displayMetrics.density
    val widthPx = (width.value * density).toInt()
    val heightPx = (height.value * density).toInt()
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Image(
            provider = ImageProvider(WidgetBitmaps.bars(widthPx, heightPx, model.week, model.goal, model.theme, density)),
            contentDescription = "Last 7 days: ${model.week.daysMeeting(model.goal)} of ${model.week.size} days at goal",
            modifier = GlanceModifier.width(width).height(height),
            contentScale = ContentScale.FillBounds,
        )
        val locale = LocalContext.current.resources.configuration.locales[0]
        Row(modifier = GlanceModifier.width(width)) {
            model.week.forEach { day ->
                val isToday = day.date == model.today
                Text(
                    day.date.dayOfWeek.getDisplayName(DateTextStyle.NARROW, locale),
                    style = textStyle(colors.secondary, 10.sp, if (isToday) FontWeight.Bold else FontWeight.Medium)
                        .copy(textAlign = TextAlign.Center),
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
        }
    }
}

/** A plain Box rather than CircleIconButton, which draws translucent backgrounds as dark gray. */
@Composable
private fun RefreshButton(colors: WidgetColors, onClick: Action) {
    Box(
        modifier = GlanceModifier
            .size(30.dp)
            .cornerRadius(15.dp)
            .background(colors.buttonBackground)
            .clickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_refresh),
            contentDescription = "Refresh",
            modifier = GlanceModifier.size(17.dp),
            colorFilter = ColorFilter.tint(colors.secondary),
        )
    }
}

@Composable
private fun Stat(title: String, value: String, colors: WidgetColors, modifier: GlanceModifier) {
    Column(modifier = modifier) {
        Text(title, style = textStyle(colors.secondary, 11.sp, FontWeight.Medium))
        Text(value, style = textStyle(colors.primary, 15.sp, FontWeight.Bold))
    }
}

@Composable
private fun Message(icon: Int, text: String, colors: WidgetColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            modifier = GlanceModifier.size(28.dp),
            colorFilter = ColorFilter.tint(colors.accent),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(text, style = textStyle(colors.secondary, 13.sp, FontWeight.Medium).copy(textAlign = TextAlign.Center))
    }
}

/** Rounds the widget like the launcher's other widgets. Android 11 and earlier don't round widgets. */
private fun systemCornerRadius(): GlanceModifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        GlanceModifier
    }

private fun goalStatus(model: WidgetModel): String =
    if (model.goalReached) "Goal reached!" else "${model.remaining.formattedSteps()} to go"

private fun textStyle(color: ColorProvider, size: TextUnit, weight: FontWeight) =
    TextStyle(color = color, fontSize = size, fontWeight = weight)

/** Scales the number inside the ring with the ring, within readable limits. */
private fun numberSize(ringSize: Dp, factor: Float): TextUnit = (ringSize.value * factor).coerceIn(16f, 30f).sp
