package com.ahmadbukhari.stepcounter.data

import java.time.Instant
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

/** The step total for one calendar day. */
data class DailySteps(val date: LocalDate, val steps: Long)

/**
 * Recent daily step totals. Cached on the device so the widget has something to show
 * when Health Connect can't be read.
 */
data class StepSnapshot(val fetchedAt: Instant, val days: List<DailySteps>) {

    /** Steps on [date]. Days the snapshot doesn't cover, such as a new day after midnight, count as 0. */
    fun stepsOn(date: LocalDate): Long = days.firstOrNull { it.date == date }?.steps ?: 0

    /** The [count] days ending on [lastDay], oldest first. */
    fun week(lastDay: LocalDate, count: Int = 7): List<DailySteps> =
        (count - 1 downTo 0).map { offset ->
            val day = lastDay.minusDays(offset.toLong())
            DailySteps(day, stepsOn(day))
        }

    fun toJson(): String = JSONObject()
        .put("fetchedAt", fetchedAt.toEpochMilli())
        .put(
            "days",
            JSONArray(days.map { JSONObject().put("date", it.date.toString()).put("steps", it.steps) }),
        )
        .toString()

    companion object {
        val EMPTY = StepSnapshot(Instant.EPOCH, emptyList())

        fun fromJson(json: String): StepSnapshot? = runCatching {
            val root = JSONObject(json)
            val days = root.getJSONArray("days")
            StepSnapshot(
                fetchedAt = Instant.ofEpochMilli(root.getLong("fetchedAt")),
                days = (0 until days.length()).map { index ->
                    val day = days.getJSONObject(index)
                    DailySteps(LocalDate.parse(day.getString("date")), day.getLong("steps"))
                },
            )
        }.getOrNull()

        /** Realistic sample data for previews. */
        fun sample(today: LocalDate = LocalDate.now()): StepSnapshot {
            val values = listOf(6_240L, 8_915L, 11_302L, 7_480L, 10_870L, 12_456L, 7_532L)
            return StepSnapshot(
                fetchedAt = Instant.now(),
                days = values.mapIndexed { index, steps ->
                    DailySteps(today.minusDays((values.size - 1 - index).toLong()), steps)
                },
            )
        }
    }
}

val List<DailySteps>.totalSteps: Long get() = sumOf { it.steps }

val List<DailySteps>.averageSteps: Long get() = if (isEmpty()) 0 else totalSteps / size

fun List<DailySteps>.daysMeeting(goal: Int): Int = count { it.steps >= goal }
