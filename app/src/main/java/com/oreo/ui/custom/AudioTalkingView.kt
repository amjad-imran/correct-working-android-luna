package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class AudioTalkingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val baseColor = Color.parseColor("#E2E6F1")


    private val paints = listOf(
        createPaint(0x1A), // 20
        createPaint(0x33), // 40
        createPaint(0x4D), // 60
        createPaint(0x66), // 80
        createPaint(0xFF)  // 100
    )

    private val widthMultipliers = listOf(1.0f, 0.85f, 0.70f, 0.55f, 0.45f)

    private var ovalWidth = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var heightLayer = 0f

    private fun createPaint(alpha: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = baseColor and 0x00FFFFFF or (alpha shl 24)
        style = Paint.Style.FILL
    }

    fun updateAmplitude(percent: Int) {
        ovalWidth = (percent.coerceIn(60, 100).toFloat() / 100) * width
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        heightLayer = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0..4) {
            drawRoundRect(
                canvas,
                ovalWidth * widthMultipliers[i],
                paints[i]
            )
        }
    }

    private fun drawRoundRect(
        canvas: Canvas,
        width: Float,
        paint: Paint
    ) {
        val halfWidth = width / 2
        val halfHeight = heightLayer / 2
        val radius = heightLayer / 2

        canvas.drawRoundRect(
            centerX - halfWidth,
            centerY - halfHeight,
            centerX + halfWidth,
            centerY + halfHeight,
            radius,
            radius,
            paint
        )
    }
}