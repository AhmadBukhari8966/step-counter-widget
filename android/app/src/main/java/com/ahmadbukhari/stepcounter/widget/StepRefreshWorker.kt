package com.ahmadbukhari.stepcounter.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ahmadbukhari.stepcounter.data.HealthSteps
import com.ahmadbukhari.stepcounter.data.StepSync
import java.util.concurrent.TimeUnit

/**
 * Keeps the widget current while the app is closed: reads Health Connect when it allows
 * background reads, and redraws the widget so it starts the new day at 0 after midnight.
 */
class StepRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val health = HealthSteps(applicationContext)
        if (health.availability == HealthSteps.Availability.AVAILABLE &&
            runCatching { health.canReadInBackground() }.getOrDefault(false)
        ) {
            runCatching { StepSync.fetchAndSave(applicationContext) }
        }
        StepsWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        /** Android runs periodic work at most every 15 minutes, and may wait longer to save battery. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StepRefreshWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("step-refresh", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
