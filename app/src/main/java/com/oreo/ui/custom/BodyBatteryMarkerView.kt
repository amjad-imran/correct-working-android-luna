package com.oreo.ui.custom

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.noisefit.luna.R
import java.util.Locale

/**
 * Custom marker view for body battery chart in BlankTestFragment.
 * Displays value (0-100) and time (HH:MM) when user touches the chart.
 *
 * @param context The context
 * @param frequencyMinutes The interval between data points (default 15 minutes)
 */
class BodyBatteryMarkerView(
    context: Context,
    private val frequencyMinutes: Int = 15
) : MarkerView(context, R.layout.marker_body_battery) {

    private val tvMarkerValue: TextView = findViewById(R.id.tvMarkerValue)
    private val tvMarkerTime: TextView = findViewById(R.id.tvMarkerTime)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return

        val value = e.y.toInt()
        val xMinutes = e.x.toInt()
        val hours = xMinutes / 60
        val minutes = xMinutes % 60
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)

        tvMarkerValue.text = "$value%"
        tvMarkerTime.text = timeStr

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // Position the marker centered above the selected point
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
}
