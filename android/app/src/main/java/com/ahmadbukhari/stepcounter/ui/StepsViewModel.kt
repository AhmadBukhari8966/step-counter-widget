package com.ahmadbukhari.stepcounter.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmadbukhari.stepcounter.data.HealthSteps
import com.ahmadbukhari.stepcounter.data.StepSnapshot
import com.ahmadbukhari.stepcounter.data.StepStore
import com.ahmadbukhari.stepcounter.data.StepSync
import com.ahmadbukhari.stepcounter.widget.StepsWidget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StepsViewModel(application: Application) : AndroidViewModel(application) {

    enum class Access { CHECKING, UNAVAILABLE, NEEDS_INSTALL, NOT_CONNECTED, CONNECTED }

    private val health = HealthSteps(application)

    val snapshot: StateFlow<StepSnapshot?> =
        StepStore.snapshotFlow(application).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val goal: StateFlow<Int> =
        StepStore.goalFlow(application).stateIn(viewModelScope, SharingStarted.Eagerly, StepStore.DEFAULT_GOAL)

    var access by mutableStateOf(Access.CHECKING)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var permissionDenied by mutableStateOf(false)
        private set
    var canRefreshInBackground by mutableStateOf(true)
        private set

    fun permissionsToRequest(): Set<String> = health.permissionsToRequest()

    /** Called whenever the app comes to the foreground. */
    fun refreshIfConnected() {
        viewModelScope.launch {
            checkAccess()
            if (access == Access.CONNECTED) refreshNow()
        }
    }

    fun onPermissionResult(granted: Set<String>) {
        permissionDenied = HealthSteps.READ_STEPS !in granted
        refreshIfConnected()
    }

    fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    fun changeGoal(delta: Int) {
        viewModelScope.launch {
            StepStore.setGoal(getApplication(), goal.value + delta)
            StepsWidget().updateAll(getApplication())
        }
    }

    private suspend fun checkAccess() {
        access = when (health.availability) {
            HealthSteps.Availability.AVAILABLE ->
                if (runCatching { health.canReadSteps() }.getOrDefault(false)) Access.CONNECTED else Access.NOT_CONNECTED
            HealthSteps.Availability.NEEDS_INSTALL_OR_UPDATE -> Access.NEEDS_INSTALL
            HealthSteps.Availability.UNAVAILABLE -> Access.UNAVAILABLE
        }
        if (access == Access.CONNECTED) {
            canRefreshInBackground = runCatching { health.canReadInBackground() }.getOrDefault(false)
        }
    }

    private suspend fun refreshNow() {
        if (isRefreshing) return
        isRefreshing = true
        try {
            StepSync.refresh(getApplication())
            errorMessage = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Couldn't read Health Connect."
        } finally {
            isRefreshing = false
        }
    }
}
