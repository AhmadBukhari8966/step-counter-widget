package com.ahmadbukhari.stepcounter.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.ahmadbukhari.stepcounter.widget.StepsWidget

object StepSync {
    /** Reads Health Connect and saves the totals. Returns true if they changed. */
    suspend fun fetchAndSave(context: Context): Boolean =
        StepStore.save(context, HealthSteps(context).fetchSnapshot())

    /** Reads Health Connect, saves the totals, and redraws the widgets if anything changed. */
    suspend fun refresh(context: Context) {
        if (fetchAndSave(context)) StepsWidget().updateAll(context)
    }
}
