package com.noisefit.luna.widget.water

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WaterIntakeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaterIntakeWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Seed defaults for all instances when the first is added
        // Goal: 4L, Current: 0
        // For each instance, Glance will create a separate Preferences file
    }
}

