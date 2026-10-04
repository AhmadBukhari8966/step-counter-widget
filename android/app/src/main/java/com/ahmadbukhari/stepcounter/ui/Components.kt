package com.ahmadbukhari.stepcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmadbukhari.stepcounter.data.DailySteps
import com.ahmadbukhari.stepcounter.data.daysMeeting
import java.time.LocalDate
import java.time.format.TextStyle

/** A circular progress ring with a gradient and rounded ends. [progress] above 1 keeps it full. */
@Composable
fun ProgressRing(progress: Float, colors: List<Color>, trackColor: Color, strokeWidth: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = strokeWidth.toPx()
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)
        drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))

        val clamped = progress.coerceIn(0f, 1f)
        if (clamped <= 0f) return@Canvas
        // Sweep gradients start at 3 o'clock; turn the drawing so the ring starts at 12.
        rotate(-90f) {
            drawArc(
                brush = Brush.sweepGradient(0f to colors.first(), clamped to colors.last(), center = center),
                startAngle = 0f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        // Covers the gradient seam at 12 o'clock.
        drawCircle(
            color = if (clamped >= 1f) colors.last() else colors.first(),
            radius = stroke / 2,
            center = Offset(center.x, topLeft.y),
        )
    }
}

/** Seven bars with a dashed goal line. Days that met the goal get the gradient; the rest are muted. */
@Composable
fun WeekChart(days: List<DailySteps>, goal: Int, colors: List<Color>, height: Dp, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val today = LocalDate.now()

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height)
                .semantics { contentDescription = "Last 7 days: ${days.daysMeeting(goal)} of ${days.size} days at goal" },
        ) {
            if (days.isEmpty()) return@Canvas
            val scaleMax = maxOf(goal.toLong(), days.maxOf { it.steps }, 1L).toFloat()
            val slot = size.width / days.size
            val barWidth = slot * 0.6f
            days.forEachIndexed { index, day ->
                val barHeight = maxOf(size.height * day.steps / scaleMax, 4.dp.toPx())
                val topLeft = Offset(index * slot + (slot - barWidth) / 2, size.height - barHeight)
                val barSize = Size(barWidth, barHeight)
                val radius = CornerRadius(6.dp.toPx())
                if (day.steps >= goal) {
                    drawRoundRect(Brush.verticalGradient(colors.reversed(), topLeft.y, size.height), topLeft, barSize, radius)
                } else {
                    drawRoundRect(muted, topLeft, barSize, radius)
                }
            }
            val goalY = size.height * (1 - goal / scaleMax)
            drawLine(
                color = lineColor,
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            )
        }
        val locale = LocalConfiguration.current.locales[0]
        Row(Modifier.fillMaxWidth()) {
            days.forEach { day ->
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (day.date == today) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
