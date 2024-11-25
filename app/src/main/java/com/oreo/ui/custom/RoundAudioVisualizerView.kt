package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class RoundAudioVisualizerView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var smoothedRmsDb: Float = 0f
    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Center of the view
        val centerX = width / 2f
        val centerY = height / 2f

        // Calculate radius using smoothed RMS value
        val maxRadius = min(centerX, centerY)
        val scaledRms = (smoothedRmsDb / 10f).coerceIn(0.6f, 1f) // Scale RMS between 0.2 and 1.0
        val radius = maxRadius * scaledRms

        // Draw the circle
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    fun updateRms(rms: Float) {
        // Apply smoothing (weighted average)
        val smoothingFactor = 0.9f // Adjust for more/less smoothing
        smoothedRmsDb = smoothedRmsDb * smoothingFactor + rms * (1 - smoothingFactor)

        // Invalidate to trigger redraw
        invalidate()
    }
}

