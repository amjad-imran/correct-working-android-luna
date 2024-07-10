package com.noisefit.ui.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS

class CircularProgressView(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {

    companion object {
        const val ARC_FULL_ROTATION_DEGREE = 360
        const val PERCENTAGE_DIVIDER = 100.0
        const val PERCENTAGE_VALUE_HOLDER = "percentage"
    }

    private var currentPercentage = 0
    private var sWidth = 0f
    private var circleWidth = 0
    private val ovalSpace = RectF()
    private var parentArcColor = Color.GREEN
    private var fillArcColor = Color.GRAY
    private var parentArcPaint = Paint()
    private var fillArcPaint = Paint()

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView)
        try {
            sWidth = ta.getInt(R.styleable.CircularProgressView_sWidth, 24).toFloat()
            fillArcColor = ta.getInt(R.styleable.CircularProgressView_arcColor, Color.GRAY)
            parentArcColor = ta.getInt(R.styleable.CircularProgressView_arcBgColor, Color.GREEN)
            circleWidth = ta.getInt(R.styleable.CircularProgressView_width, 120)
            parentArcPaint = Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true
                color = parentArcColor
                strokeWidth = sWidth

            }

            fillArcPaint = Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true
                color = fillArcColor
                strokeWidth = sWidth
                strokeCap = Paint.Cap.ROUND
            }

        } finally {
            ta.recycle()
        }
    }


    override fun onDraw(canvas: Canvas) {
        setSpace()
        canvas.let {
            drawBackgroundArc(it)
            drawInnerArc(it)
        }
    }

    private fun setSpace() {
        val horizontalCenter = (width.div(2)).toFloat()
        val verticalCenter = (height.div(2)).toFloat()
        val ovalSize = circleWidth
        ovalSpace.set(
            horizontalCenter - ovalSize,
            verticalCenter - ovalSize,
            horizontalCenter + ovalSize,
            verticalCenter + ovalSize
        )
    }

    private fun drawBackgroundArc(it: Canvas) {
        it.drawArc(ovalSpace, 0f, 360f, false, parentArcPaint)
    }

    private fun drawInnerArc(canvas: Canvas) {
        val percentageToFill = getCurrentPercentageToFill()
        canvas.drawArc(ovalSpace, 270f, percentageToFill, false, fillArcPaint)
    }

    private fun getCurrentPercentageToFill() =
        (ARC_FULL_ROTATION_DEGREE * (currentPercentage / PERCENTAGE_DIVIDER)).toFloat()

    fun animateProgress(progress: Float) {
        var progress1 = progress
        var mDuration = 1000
        if (progress == 0f) {
            progress1 = 1f
            mDuration = 0
        }
        val valuesHolder = PropertyValuesHolder.ofFloat("percentage", 0f, progress1)
        LOGS.d("fsadffdsafasdfds $valuesHolder")
        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = mDuration.toLong()
            addUpdateListener {
                val percentage = it.getAnimatedValue(PERCENTAGE_VALUE_HOLDER) as Float
                currentPercentage = percentage.toInt()

                invalidate()

            }
        }
        animator.start()
    }
}