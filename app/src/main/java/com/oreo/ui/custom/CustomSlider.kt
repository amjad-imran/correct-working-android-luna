package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.noisefit_commans.ui.dpToPixel
import kotlin.math.roundToInt

class CustomSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnValueChangeListener {
        fun onValueChanged(value: Int)
    }

    private var minValue = 0f
    private var maxValue = 100f
    private var stepSize = 1f
    private var currentValue = 0f
    private var valueChangeListener: OnValueChangeListener? = null

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0FFFFFFF")
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.FILL
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#626262")
        style = Paint.Style.FILL
    }

    private var trackRect = RectF()
    private var progressRect = RectF()
    private var isDragging = false
    private var trackHeight = 60f
    private var cornerRadius = 10f.dpToPixel()
    private var padding = 0f

    init {
        currentValue = minValue
    }

    fun setRange(min: Float, max: Float, step: Float) {
        minValue = min
        maxValue = max
        stepSize = step
        currentValue = minValue.coerceAtLeast(currentValue.coerceAtMost(maxValue))
        post {
            updateTrackBounds()
            invalidate()
        }
    }

    fun setValue(value: Float) {
        val newValue = snapToStep(value.coerceIn(minValue, maxValue))
        if (newValue != currentValue) {
            currentValue = newValue
            updateProgressBounds()
            valueChangeListener?.onValueChanged(currentValue.roundToInt())
            invalidate()
        }
    }

    fun setOnValueChangeListener(listener: OnValueChangeListener) {
        valueChangeListener = listener
    }

    private fun snapToStep(value: Float): Float {
        val steps = ((value - minValue) / stepSize).roundToInt()
        return minValue + (steps * stepSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTrackBounds()
    }

    private fun updateTrackBounds() {
        val centerY = height / 2f
        trackRect.set(
            padding,
            centerY - trackHeight / 2f,
            width - padding,
            centerY + trackHeight / 2f
        )
        updateProgressBounds()
    }

    private fun updateProgressBounds() {
        if (trackRect.isEmpty) return

        val progress = if (maxValue > minValue) {
            (currentValue - minValue) / (maxValue - minValue)
        } else 0f

        val progressWidth = trackRect.width() * progress

        progressRect.set(
            trackRect.left,
            trackRect.top,
            trackRect.left + progressWidth,
            trackRect.bottom
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        trackHeight = height.toFloat()

        canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, trackPaint)

        // Draw progress fill
        if (progressRect.width() > 0) {
            // Create a path for rounded corners only on the left side
            val path = Path()
            val radii = floatArrayOf(
                cornerRadius, cornerRadius, // top-left
                if (progressRect.right >= trackRect.right - cornerRadius) cornerRadius else 0f,
                if (progressRect.right >= trackRect.right - cornerRadius) cornerRadius else 0f, // top-right
                if (progressRect.right >= trackRect.right - cornerRadius) cornerRadius else 0f,
                if (progressRect.right >= trackRect.right - cornerRadius) cornerRadius else 0f, // bottom-right
                cornerRadius, cornerRadius  // bottom-left
            )
            path.addRoundRect(progressRect, radii, Path.Direction.CW)
            canvas.drawPath(path, progressPaint)
        }

        // Draw tick marks
        drawTickMarks(canvas)
    }

    private fun drawTickMarks(canvas: Canvas) {
        if (trackRect.isEmpty) return

        val tickCount = (((maxValue - minValue) / stepSize).toInt() + 1) * 2

        if (tickCount <= 1) return

        val tickSpacing = trackRect.width() / (tickCount - 1)
        val bigBarHeight = 6f.dpToPixel()
        val smallBarHeight = 3f.dpToPixel()

        for (i in 1 until tickCount - 1) {
            val x = trackRect.left + (i * tickSpacing)
            val startY = trackRect.centerY() - if (i % 2 == 0) bigBarHeight else smallBarHeight
            val endY = trackRect.centerY() + if (i % 2 == 0) bigBarHeight else smallBarHeight

            // Make tick marks more visible by using white color if they're over the progress
            val tickColor = if (x <= progressRect.right) {
                Color.WHITE
            } else {
                Color.parseColor("#626262")
            }
            tickPaint.color = tickColor

            canvas.drawLine(x, startY, x, endY, tickPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isPointInTrack(event.x, event.y)) {
                    isDragging = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    updateValueFromTouch(event.x)
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    updateValueFromTouch(event.x)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    parent.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isPointInTrack(x: Float, y: Float): Boolean {
        return trackRect.contains(x, y)
    }

    private fun updateValueFromTouch(touchX: Float) {
        val relativeX = touchX - trackRect.left
        val progress = (relativeX / trackRect.width()).coerceIn(0f, 1f)

        val newValue = minValue + (progress * (maxValue - minValue))
        setValue(newValue)
    }

    /*override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = widthMeasureSpec
        val desiredHeight = heightMeasureSpec

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }*/
}