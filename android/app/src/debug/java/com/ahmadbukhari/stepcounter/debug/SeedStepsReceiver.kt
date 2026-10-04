package com.ahmadbukhari.stepcounter.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug builds only. Loads a known week of steps into Health Connect for testing:
 *
 *   adb shell pm grant com.ahmadbukhari.stepcounter android.permission.health.WRITE_STEPS
 *   adb shell am broadcast -a com.ahmadbukhari.stepcounter.SEED_STEPS -p com.ahmadbukhari.stepcounter
 *   adb shell am broadcast -a com.ahmadbukhari.stepcounter.SEED_STEPS -p com.ahmadbukhari.stepcounter --ei add 1000
 */
class SeedStepsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val add = intent.getIntExtra("add", 0)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = HealthConnectClient.getOrCreate(context)
                if (add > 0) addSteps(client, add) else seedWeek(client)
            } catch (e: Exception) {
                Log.e(TAG, "Seeding failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun seedWeek(client: HealthConnectClient) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val firstDay = today.minusDays(6)
        // Start clean so reruns give the same totals. Apps can only delete their own records.
        client.deleteRecords(
            StepsRecord::class,
            TimeRangeFilter.between(firstDay.atStartOfDay(zone).toInstant(), Instant.now()),
        )

        val totals = listOf(6_240L, 8_915L, 11_302L, 7_480L, 10_870L, 12_456L, 7_532L)
        val records = totals.flatMapIndexed { index, total ->
            val day = firstDay.plusDays(index.toLong()).atStartOfDay(zone)
            val isToday = index == totals.lastIndex
            val slots = if (isToday) {
                val now = ZonedDateTime.now(zone)
                listOf(day.plusMinutes(30) to day.plusMinutes(60), now.minusMinutes(40) to now.minusMinutes(10))
            } else {
                listOf(8L, 12L, 17L, 20L).map { hour -> day.plusHours(hour) to day.plusHours(hour).plusMinutes(30) }
            }
            val share = total / slots.size
            slots.mapIndexed { slot, (start, end) ->
                val count = if (slot == slots.lastIndex) total - share * (slots.size - 1) else share
                stepsRecord(start, end, count)
            }
        }
        client.insertRecords(records)
        Log.i(TAG, "Seeded ${records.size} step records")
    }

    private suspend fun addSteps(client: HealthConnectClient, count: Int) {
        val now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        client.insertRecords(listOf(stepsRecord(now.minusMinutes(5), now.minusSeconds(1), count.toLong())))
        Log.i(TAG, "Added $count steps")
    }

    private fun stepsRecord(start: ZonedDateTime, end: ZonedDateTime, count: Long) = StepsRecord(
        startTime = start.toInstant(),
        startZoneOffset = start.offset,
        endTime = end.toInstant(),
        endZoneOffset = end.offset,
        count = count,
        metadata = Metadata.manualEntry(),
    )

    private companion object {
        const val TAG = "SeedSteps"
    }
}
