package com.noisefit.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class BloodOxygenBar : View {
    private var progress = 0
    private var rectBack: RectF? = null
    private var track: RectF? = null
    private var backRectPaint: Paint? = null
    private var trackColor: Paint? = null
    private var circlePaint: Paint? = null
    private var hr1ValuePaint: Paint? = null
    private var hr2ValuePaint: Paint? = null
    private var hr3ValuePaint: Paint? = null
    private var hr4ValuePaint: Paint? = null
    private var backgroundColor = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        rectBack = RectF(0f, 0f, this.width.toFloat(), dpToPx(16, getContext()).toFloat())
        track = RectF(
            dpToPx(6, getContext()).toFloat(),
            dpToPx(7, getContext()).toFloat(),
            (this.width - dpToPx(6, getContext())).toFloat(),
            dpToPx(9, getContext()).toFloat()
        )
        backRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundColor = Color.parseColor("#262b2f")
        backRectPaint!!.color = backgroundColor
        trackColor = Paint(Paint.ANTI_ALIAS_FLAG)
        trackColor!!.color = Color.parseColor("#171a1d")
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint!!.color = Color.parseColor("#ffffff")
        hr1ValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        hr1ValuePaint!!.color = Color.parseColor("#264957")
        hr2ValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        hr2ValuePaint!!.color = Color.parseColor("#426d6b")
        hr3ValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        hr3ValuePaint!!.color = Color.parseColor("#569f99")
        hr4ValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        hr4ValuePaint!!.color = Color.parseColor("#6cd0c6")
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //TODO move to constructor
        rectBack = RectF(0f, 0f, this.width.toFloat(), dpToPx(16, context).toFloat())
        /*track = new RectF(0,
                dpToPx(7, getContext()),
                this.getWidth(),
                dpToPx(9, getContext()));*/track = RectF(
            dpToPx(6, context).toFloat(),
            dpToPx(7, context).toFloat(),
            (this.width - dpToPx(6, context)).toFloat(),
            dpToPx(9, context).toFloat()
        )
        canvas.drawRoundRect(rectBack!!, 10f, 10f, backRectPaint!!)
        canvas.drawRoundRect(track!!, 2f, 2f, trackColor!!)
        val trackWidth = (this.width - dpToPx(12, context)).toFloat()
        val maxSubTrackWidth = trackWidth / 4
        val offset = dpToPx(6, context).toFloat()
        if (progress > 1) {
            canvas.drawRect(
                offset,
                dpToPx(6, context).toFloat(),
                offset + maxSubTrackWidth,
                dpToPx(10, context).toFloat(), hr1ValuePaint!!
            )
        }
        if (progress > 25) {
            canvas.drawRect(
                offset + maxSubTrackWidth,
                dpToPx(6, context).toFloat(),
                offset + maxSubTrackWidth * 2,
                dpToPx(10, context).toFloat(), hr2ValuePaint!!
            )
        }
        if (progress > 50) {
            canvas.drawRect(
                offset + maxSubTrackWidth * 2,
                dpToPx(6, context).toFloat(),
                maxSubTrackWidth * 3 + offset,
                dpToPx(10, context).toFloat(), hr3ValuePaint!!
            )
        }
        if (progress > 75) {
            canvas.drawRect(
                offset + maxSubTrackWidth * 3,
                dpToPx(6, context).toFloat(),
                maxSubTrackWidth * 4 + offset,
                dpToPx(10, context).toFloat(), hr4ValuePaint!!
            )
        }
        val circleXAxis = dpToPx(6, context) + progress * trackWidth / 100
        canvas.drawCircle(
            circleXAxis,
            dpToPx(8, context).toFloat(),
            dpToPx(5, context).toFloat(),
            circlePaint!!
        )
    }

    override fun setBackgroundColor(colorRes: Int) {
        backgroundColor = colorRes
        backRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backRectPaint!!.color = backgroundColor
        invalidate()
    }

    fun setProgress(progress: Int) {

        this.progress = when (progress) {
            in 1 until 25 -> 25
            in 26 until 50 -> 50
            in 51 until 75 -> 75
            in 76 until 100 -> 100
            else -> progress
        }
        invalidate()
    }

    fun dpToPx(dp: Int, context: Context): Int {
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
            )
        )
    }
}