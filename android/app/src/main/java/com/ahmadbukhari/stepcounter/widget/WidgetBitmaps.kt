package com.ahmadbukhari.stepcounter.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import androidx.core.graphics.createBitmap
import com.ahmadbukhari.stepcounter.data.DailySteps

/**
 * Widgets can't draw shapes, so the ring and the bar chart are drawn into bitmaps
 * at the exact pixel size they're shown at.
 */
object WidgetBitmaps {

    /** A progress ring with a gradient and rounded ends. [progress] above 1 keeps it full. */
    fun ring(sizePx: Int, strokePx: Float, progress: Float, theme: WidgetTheme): Bitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        val center = sizePx / 2f
        val inset = strokePx / 2f
        val bounds = RectF(inset, inset, sizePx - inset, sizePx - inset)

        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokePx
            color = theme.trackColor
        }
        canvas.drawArc(bounds, 0f, 360f, false, track)

        val clamped = progress.coerceIn(0f, 1f)
        if (clamped <= 0f) return bitmap

        val (startColor, endColor) = theme.ringColors[0] to theme.ringColors[1]
        val gradient = SweepGradient(center, center, intArrayOf(startColor, endColor), floatArrayOf(0f, clamped))
        // The sweep starts at 3 o'clock; turn it so it starts at 12.
        gradient.setLocalMatrix(Matrix().apply { setRotate(-90f, center, center) })

        val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokePx
            strokeCap = Paint.Cap.ROUND
            shader = gradient
        }
        canvas.drawArc(bounds, -90f, 360f * clamped, false, arc)

        // Covers the gradient seam at 12 o'clock: the start cap before the goal,
        // the end of the lap (with a soft shadow) once the ring is full.
        val lapped = clamped >= 1f
        val cap = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (lapped) endColor else startColor
            if (lapped) setShadowLayer(strokePx / 8f, strokePx / 8f, 0f, 0x4D000000)
        }
        canvas.drawCircle(center, inset, strokePx / 2f, cap)
        return bitmap
    }

    /**
     * Seven bars, one per day, with a dashed goal line. Days that met the goal get the theme
     * gradient; the rest are muted. Bars sit centered in equal slots so labels can line up below.
     */
    fun bars(widthPx: Int, heightPx: Int, days: List<DailySteps>, goal: Int, theme: WidgetTheme, density: Float): Bitmap {
        val bitmap = createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1))
        if (days.isEmpty()) return bitmap
        val canvas = Canvas(bitmap)
        val scaleMax = maxOf(goal.toLong(), days.maxOf { it.steps }, 1L).toFloat()
        val slot = widthPx / days.size.toFloat()
        val barWidth = slot * 0.62f
        val radius = 3f * density
        val minHeight = 4f * density

        val gradient = LinearGradient(
            0f, heightPx.toFloat(), 0f, 0f,
            theme.ringColors[0], theme.ringColors[1], Shader.TileMode.CLAMP,
        )
        val metPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.mutedBarColor }

        days.forEachIndexed { index, day ->
            val barHeight = maxOf(heightPx * day.steps / scaleMax, minHeight)
            val left = index * slot + (slot - barWidth) / 2f
            val rect = RectF(left, heightPx - barHeight, left + barWidth, heightPx.toFloat())
            canvas.drawRoundRect(rect, radius, radius, if (day.steps >= goal) metPaint else mutedPaint)
        }

        val goalY = heightPx * (1f - goal / scaleMax)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = density
            color = if (theme.lightContent) 0x99FFFFFF.toInt() else 0x99808080.toInt()
            pathEffect = DashPathEffect(floatArrayOf(2f * density, 3f * density), 0f)
        }
        canvas.drawLine(0f, goalY, widthPx.toFloat(), goalY, line)
        return bitmap
    }
}
