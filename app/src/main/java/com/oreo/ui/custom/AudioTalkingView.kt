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

    // Update width based on amplitude
    fun updateAmplitude(percent: Int) {
        ovalWidth = (percent.toFloat() / 100) * width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw an oval with fixed height and dynamic width
        val viewHeight = height.toFloat()

        canvas.drawRoundRect(
            centerX - ovalWidth / 2,
            centerY - viewHeight / 2,
            centerX + ovalWidth / 2,
            centerY + viewHeight / 2,
            viewHeight / 2,
            viewHeight / 2,
            paint20
        )

        val width40 = ovalWidth * 0.8f
        canvas.drawRoundRect(
            centerX - width40 / 2,
            centerY - viewHeight / 2,
            centerX + width40 / 2,
            centerY + viewHeight / 2,
            viewHeight / 2,
            viewHeight / 2,
            paint40
        )


        val width60 = ovalWidth * 0.6f
        canvas.drawRoundRect(
            centerX - width60 / 2,
            centerY - viewHeight / 2,
            centerX + width60 / 2,
            centerY + viewHeight / 2,
            viewHeight / 2,
            viewHeight / 2,
            paint60
        )

        val width80 = ovalWidth * 0.4f
        canvas.drawRoundRect(
            centerX - width80 / 2,
            centerY - viewHeight / 2,
            centerX + width80 / 2,
            centerY + viewHeight / 2,
            viewHeight / 2,
            viewHeight / 2,
            paint80
        )

        val width100 = ovalWidth * 0.2f
        canvas.drawRoundRect(
            centerX - width100 / 2,
            centerY - viewHeight / 2,
            centerX + width100 / 2,
            centerY + viewHeight / 2,
            viewHeight / 2,
            viewHeight / 2,
            paint80
        )
    }
}