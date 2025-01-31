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

    private val paint100 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 255
        style = Paint.Style.FILL
    }
    private val paint80 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 204
        style = Paint.Style.FILL
    }
    private val paint60 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 153
        style = Paint.Style.FILL
    }
    private val paint40 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 102
        style = Paint.Style.FILL
    }
    private val paint20 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 51
        style = Paint.Style.FILL
    }

    private var ovalWidth = 0f

    fun updateAmplitude(percent: Int) {
        var calculatedPercent = percent
        if (percent > 100) {
            calculatedPercent = 100
        } else if (percent < 60) {
            calculatedPercent = 60
        }
        ovalWidth = (calculatedPercent.toFloat() / 100) * width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f


        val viewHeight = height.toFloat()
        drawRoundRect(canvas, centerX, centerY, ovalWidth, viewHeight, paint20)

        val width40 = ovalWidth * 0.85f
        val height40 = height * 0.9f
        drawRoundRect(canvas, centerX, centerY, width40, height40, paint40)


        val width60 = ovalWidth * 0.7f
        val height60 = height * 0.8f
        drawRoundRect(canvas, centerX, centerY, width60, height60, paint60)

        val width80 = ovalWidth * 0.55f
        val height80 = height * 0.7f
        drawRoundRect(canvas, centerX, centerY, width80, height80, paint80)


        val width100 = ovalWidth * 0.4f
        val height100 = height * 0.6f
        drawRoundRect(canvas, centerX, centerY, width100, height100, paint100)

    }

    fun drawRoundRect(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        paint: Paint
    ) {
        canvas.drawRoundRect(
            centerX - width / 2,
            centerY - height / 2,
            centerX + width / 2,
            centerY + height / 2,
            height / 2,
            height / 2,
            paint
        )
    }
}