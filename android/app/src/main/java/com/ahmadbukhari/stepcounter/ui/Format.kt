package com.ahmadbukhari.stepcounter.ui

import android.icu.text.CompactDecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** "8,432" */
fun Long.formattedSteps(): String = NumberFormat.getIntegerInstance().format(this)

/** "8.4K", for tight spaces. */
fun Long.compactSteps(): String =
    CompactDecimalFormat.getInstance(Locale.getDefault(), CompactDecimalFormat.CompactStyle.SHORT).format(this)

/** "84%" */
fun Float.formattedPercent(): String = NumberFormat.getPercentInstance().format(this.toDouble())

/** "2:41 PM" in the device's time zone and format. */
fun Instant.formattedTime(): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(atZone(ZoneId.systemDefault()))
