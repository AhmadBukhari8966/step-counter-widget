package com.ahmadbukhari.stepcounter

import android.app.Application
import com.ahmadbukhari.stepcounter.widget.StepRefreshWorker

class StepCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        StepRefreshWorker.schedule(this)
    }
}
