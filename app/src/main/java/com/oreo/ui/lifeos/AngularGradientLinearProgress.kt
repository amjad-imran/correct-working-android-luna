package com.oreo.ui.lifeos

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class AngularGradientLinearProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 0f..1f
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    // Same as .white.opacity(0.2) → #33FFFFFF
    var trackColor: Int = Color.parseColor("#33FFFFFF")
        set(value) {
            field = value
            trackPaint.color = value
            invalidate()
        }

    // Colors from your Figma / iOS gradient
    private val gradientColors = intArrayOf(
        Color.parseColor("#FFFFB8"), // (1.00, 1.00, 0.72)
        Color.parseColor("#A6EAF7"), // (0.65, 0.92, 0.97)
        Color.parseColor("#AEA2F7"), // (0.68, 0.64, 0.97)
        Color.parseColor("#F2C7EF"), // (0.95, 0.78, 0.94)
        Color.parseColor("#FFFFB8")  // back to first
    )

    // Stops from iOS code
    private val gradientStops = floatArrayOf(
        0.00f,
        0.29f,
        0.52f,
        0.75f,
        1.00f
    )

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = trackColor
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        // opacity(0.8)
        alpha = (0.8f * 255).roundToInt()
    }

    private val fullRect = RectF()
    private val clipRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fullRect.set(0f, 0f, w.toFloat(), h.toFloat())
        createAngularShader(w, h)
    }

    private fun createAngularShader(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

        val cx = w / 2f
        val cy = h / 2f

        // Angular gradient == SweepGradient in Android
        val shader = SweepGradient(
            cx,
            cy,
            gradientColors,
            gradientStops
        )

        // Match SwiftUI: center at (0.5,0.5), angle = 109.6°
        val matrix = Matrix()
        matrix.postRotate(109.6f, cx, cy)
        shader.setLocalMatrix(matrix)

        indicatorPaint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = height / 2f

        // 1) Track (capsule)
        canvas.drawRoundRect(fullRect, radius, radius, trackPaint)

        // 2) Indicator clipped by progress
        val w = width * progress
        if (w <= 0f) return

        clipRect.set(0f, 0f, w, height.toFloat())

        val save = canvas.save()
        canvas.clipRect(clipRect)
        canvas.drawRoundRect(fullRect, radius, radius, indicatorPaint)
        canvas.restoreToCount(save)
    }
}
