package com.ahmadbukhari.stepcounter.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.Period

/**
 * Reads step counts from Health Connect, Android's shared store for health data.
 * Used by both the app and the widget.
 */
class HealthSteps(private val context: Context) {

    enum class Availability { AVAILABLE, NEEDS_INSTALL_OR_UPDATE, UNAVAILABLE }

    /** Health Connect is built into Android 14 and later; older versions need the Health Connect app. */
    val availability: Availability
        get() = when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> Availability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.NEEDS_INSTALL_OR_UPDATE
            else -> Availability.UNAVAILABLE
        }

    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun grantedPermissions(): Set<String> = client.permissionController.getGrantedPermissions()

    suspend fun canReadSteps(): Boolean = READ_STEPS in grantedPermissions()

    /**
     * Widgets refresh while the app isn't open. Health Connect only allows that with a
     * separate background-read permission, which some devices don't offer yet.
     */
    suspend fun canReadInBackground(): Boolean =
        supportsBackgroundReads() && grantedPermissions().containsAll(setOf(READ_STEPS, READ_IN_BACKGROUND))

    private fun supportsBackgroundReads(): Boolean =
        client.features.getFeatureStatus(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND) ==
            HealthConnectFeatures.FEATURE_STATUS_AVAILABLE

    fun permissionsToRequest(): Set<String> =
        if (supportsBackgroundReads()) setOf(READ_STEPS, READ_IN_BACKGROUND) else setOf(READ_STEPS)

    /**
     * Daily step totals for the last [dayCount] days, including today. Health Connect's
     * aggregation merges the phone, a watch and fitness apps without double counting.
     */
    suspend fun fetchSnapshot(dayCount: Int = 7, today: LocalDate = LocalDate.now()): StepSnapshot {
        val firstDay = today.minusDays(dayCount - 1L)
        val totals = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(firstDay.atStartOfDay(), today.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
            ),
        ).associate { it.startTime.toLocalDate() to (it.result[StepsRecord.COUNT_TOTAL] ?: 0L) }

        val days = (0 until dayCount).map { offset ->
            val day = firstDay.plusDays(offset.toLong())
            DailySteps(day, totals[day] ?: 0L)
        }
        return StepSnapshot(Instant.now(), days)
    }

    companion object {
        val READ_STEPS: String = HealthPermission.getReadPermission(StepsRecord::class)
        const val READ_IN_BACKGROUND: String = HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
    }
}
