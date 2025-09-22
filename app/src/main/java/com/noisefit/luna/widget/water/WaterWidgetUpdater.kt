package com.noisefit.luna.widget.water

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

object WaterWidgetUpdater {
    suspend fun updateAll(context: Context, currentMl: Int, goalMl: Int) {
        val mgr = GlanceAppWidgetManager(context)
        val ids = mgr.getGlanceIds(WaterIntakeWidget::class.java)
        ids.forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[PrefKeys.CurrentMl] = currentMl
                    this[PrefKeys.GoalMl] = goalMl
                }
            }
            WaterIntakeWidget().update(context, id)
        }
    }
}

