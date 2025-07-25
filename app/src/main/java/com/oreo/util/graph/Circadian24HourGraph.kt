package com.oreo.util.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.widget.OverScroller
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sin


class Circadian24HourGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {



    private val hourWidthPx = 200f
    private val totalHours = 24
    private val totalWidth = totalHours * hourWidthPx
    private val bottomPaddingForLabels = 50f

    private val energyPath = Path()

    private val hourLinePaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val bottomAxisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val topDottedAxisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val energyPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val currentTimeLinePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private var scrollOffsetX = 0f
    private var lastX = 0f
    private var lastY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var isBeingDragged = false

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            scrollOffsetX = (scrollOffsetX + dx).coerceIn(0f, totalWidth - width.toFloat())
            invalidate()
            return true
        }
    })

    init {
        setWillNotDraw(false)

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollOffsetX = calculateInitialScrollOffset()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                invalidate()
            }
        })
    }

    private fun calculateInitialScrollOffset(): Float {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val hourFraction = hour + minute / 60f
        val hourPosition = hourFraction * hourWidthPx
        val centerX = width / 2f
        return (hourPosition - centerX).coerceIn(0f, totalWidth - width.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(-scrollOffsetX, 0f)

        drawHourLines(canvas)
        drawEnergyCurve(canvas)
        drawTopAndBottomAxis(canvas)
        drawTimeLabels(canvas)

        canvas.restore()
        drawCurrentTimeLine(canvas)
    }

    private fun drawHourLines(canvas: Canvas) {
        for (i in 0..totalHours) {
            val x = i * hourWidthPx
            canvas.drawLine(x, 0f, x, height - bottomPaddingForLabels, hourLinePaint)
        }
    }

    private fun drawTimeLabels(canvas: Canvas) {
        val labelY = height.toFloat() - 10f
        for (i in 0..totalHours) {
            val x = i * hourWidthPx
            val label = String.format("%02d:00", i % 24)
            val textWidth = textPaint.measureText(label)
            val textX = x - textWidth / 2
            canvas.drawText(label, textX, labelY, textPaint)
        }
    }

    private fun drawEnergyCurve(canvas: Canvas) {
        energyPath.reset()
        val usableHeight = height - bottomPaddingForLabels

        for (i in 0 until totalHours) {
            val x = i * hourWidthPx
            val energy = getEnergyForHour(i)
            val y = usableHeight - (energy * usableHeight * 0.8f + usableHeight * 0.1f)
            if (i == 0) {
                energyPath.moveTo(x, y)
            } else {
                energyPath.lineTo(x, y)
            }
        }

        canvas.drawPath(energyPath, energyPaint)
    }

    private fun getEnergyForHour(hour: Int): Float {
        val radians = (hour - 8) / 12f * Math.PI
        return (0.5 + 0.5 * sin(radians)).toFloat()
    }

    private fun drawCurrentTimeLine(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val x = ((hour + minute / 60f) * hourWidthPx) - scrollOffsetX
        canvas.drawLine(x, 0f, x, height - bottomPaddingForLabels, currentTimeLinePaint)
    }

    private fun drawTopAndBottomAxis(canvas: Canvas) {
        val yTop = 2f
        val yBottom = height.toFloat() - bottomPaddingForLabels

        canvas.drawLine(0f, yTop, totalWidth, yTop, topDottedAxisPaint)
        canvas.drawLine(0f, yBottom, totalWidth, yBottom, bottomAxisPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                isBeingDragged = false
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = abs(event.x - lastX)
                val dy = abs(event.y - lastY)
                if (dx > touchSlop && dx > dy) {
                    isBeingDragged = true
                    parent.requestDisallowInterceptTouchEvent(true)
                } else if (dy > touchSlop && dy > dx) {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isBeingDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        gestureDetector.onTouchEvent(event)
        return true
    }
}
