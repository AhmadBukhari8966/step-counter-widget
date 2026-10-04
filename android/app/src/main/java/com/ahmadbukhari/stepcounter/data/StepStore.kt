package com.ahmadbukhari.stepcounter.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.stepDataStore by preferencesDataStore(name = "steps")

/** The latest step totals and the daily goal, shared by the app and the widget. */
object StepStore {
    const val DEFAULT_GOAL = 10_000
    val GOAL_RANGE = 1_000..50_000
    const val GOAL_STEP = 500

    private val SNAPSHOT = stringPreferencesKey("snapshot")
    private val GOAL = intPreferencesKey("daily_goal")

    fun snapshotFlow(context: Context): Flow<StepSnapshot?> =
        context.stepDataStore.data.map { prefs -> prefs[SNAPSHOT]?.let(StepSnapshot::fromJson) }

    fun goalFlow(context: Context): Flow<Int> =
        context.stepDataStore.data.map { prefs -> prefs[GOAL] ?: DEFAULT_GOAL }

    suspend fun snapshot(context: Context): StepSnapshot? = snapshotFlow(context).first()

    suspend fun goal(context: Context): Int = goalFlow(context).first()

    suspend fun setGoal(context: Context, goal: Int) {
        context.stepDataStore.edit { it[GOAL] = goal.coerceIn(GOAL_RANGE) }
    }

    /**
     * Saves [snapshot] unless a newer one is already stored (the app and widget both write).
     * Returns true if the daily totals changed.
     */
    suspend fun save(context: Context, snapshot: StepSnapshot): Boolean {
        var changed = false
        context.stepDataStore.edit { prefs ->
            val existing = prefs[SNAPSHOT]?.let(StepSnapshot::fromJson)
            if (existing != null && existing.fetchedAt > snapshot.fetchedAt) return@edit
            changed = existing?.days != snapshot.days
            prefs[SNAPSHOT] = snapshot.toJson()
        }
        return changed
    }
}
