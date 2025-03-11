package com.oreo.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit.luna.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class StepsProgressBarArch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Float = 30f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    private val dotDrawable = ContextCompat.getDrawable(context, R.drawable.image_progress_dot_glow)


    private val startColor = Color.parseColor("#39E190")
    private val endColor = Color.parseColor("#F5FFF8")

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#1AFFFFFF")
        strokeWidth = 10f
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 20f
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val arcRect = RectF()

    private val startAngle = 135f
    private val sweepAngle = 270f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        val radius = (min(width, height) / 2) - (progressPaint.strokeWidth / 2)

        arcRect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        canvas.drawArc(arcRect, startAngle, sweepAngle, false, backgroundPaint)

        val fraction = progress / 100f
        val progressAngle = fraction * sweepAngle


        val fractionOfCircle = (progressAngle / 360f).coerceIn(0f, 1f)

        val gradient = SweepGradient(
            centerX,
            centerY,
            intArrayOf(startColor, endColor, endColor), // 3 stops
            floatArrayOf(0f, fractionOfCircle, 1f)
        ).apply {
            val matrix = Matrix()
            matrix.postRotate(startAngle, centerX, centerY)
            setLocalMatrix(matrix)
        }

        progressPaint.shader = gradient

        canvas.drawArc(arcRect, startAngle, progressAngle, false, progressPaint)


        val endAngleRadians = Math.toRadians((startAngle + progressAngle).toDouble())
        val dotX = centerX + radius * cos(endAngleRadians)
        val dotY = centerY + radius * sin(endAngleRadians)
        //canvas.drawCircle(dotX.toFloat(), dotY.toFloat(), 10f, dotPaint)


        if(dotDrawable!=null){
            val dotWidth = dotDrawable.intrinsicWidth
            val dotHeight = dotDrawable.intrinsicHeight

            val left = (dotX - dotWidth / 2).toInt()
            val top = (dotY - dotHeight / 2).toInt()
            val right = (dotX + dotWidth / 2).toInt()
            val bottom = (dotY + dotHeight / 2).toInt()

            dotDrawable.setBounds(left, top, right, bottom)
            dotDrawable.draw(canvas)
        }

    }

}
