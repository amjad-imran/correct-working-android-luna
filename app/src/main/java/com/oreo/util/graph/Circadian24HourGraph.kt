package com.oreo.util.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.widget.OverScroller
import androidx.core.graphics.toColorInt
import com.oreo.data.model.TimeWindow
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sin
import androidx.core.graphics.withTranslation
import com.noisefit_commans.utils.LOGS
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class Circadian24HourGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {


    private val totalHours = 24
    private val hourWidthPx = 200f

    private val totalWidth = totalHours * hourWidthPx
    private val bottomPaddingForLabels = 80f
    private val topPadding = 30f

//    var graphStartTime: LocalTime = LocalTime.of(9, 0)  // 9 AM today
//    var graphEndTime: LocalTime = LocalTime.of(8, 0)    // 8 AM next day
//
//    val totalHours: Int
//        get() {
//            val diff = Duration.between(graphStartTime, graphEndTime)
//            return if (diff.isNegative) 24 + diff.toHours().toInt() else diff.toHours().toInt()
//        }

//    fun getHourAt(index: Int): LocalTime {
//        return graphStartTime.plusHours(index.toLong() % 24)
//    }

    fun getGraphHeight(): Float{
        return height.toFloat()
    }
    private val energyPath = Path()

    var timeWindows: List<TimeWindow> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }


    private val hourLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }


    private val bottomAxisPaint = Paint().apply {
        color = "#99FFFFFF".toColorInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val topDottedAxisPaint = Paint().apply {
        color = "#26FFFFFF".toColorInt()
        strokeWidth = 4f
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

    private val bottomXPaint = Paint().apply {
        color = "#6E6F74".toColorInt()
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
        canvas.withTranslation(-scrollOffsetX, 0f) {
            drawHourLines(this)
            drawEnergyCurve(this)
            drawTopAndBottomAxis(this)
            drawTimeLabels(this)
            drawTimeWindows(this)
        }
        drawCurrentTimeLine(canvas)
    }

    fun redraw(){
        invalidate()
    }
    private fun drawHourLines(canvas: Canvas) {

        for (i in 0..totalHours) {
            val x = i * hourWidthPx
            hourLinePaint.shader = LinearGradient(
                x, topPadding,
                x,  getGraphHeight() - bottomPaddingForLabels,
                "#000000".toColorInt(), "#99FFFFFF".toColorInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawLine(x, topPadding, x,  getGraphHeight() - bottomPaddingForLabels, hourLinePaint)
        }
    }

    fun formatTo12Hour(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return time.format(formatter)
    }


    private fun drawTimeLabels(canvas: Canvas) {
        val labelY =  getGraphHeight().toFloat() - 20f
        for (i in 0..totalHours) {
//            val hour = getHourAt(i)


            val x = i * hourWidthPx
            val hrFormat = String.format("%02d:00", i % 24)
            val label = formatTo12Hour(LocalTime.parse(hrFormat,DateTimeFormatter.ofPattern("HH:mm")))
            //LOGS.d("hgdfhjfsdhjdsfhj $hour")
            val textWidth = bottomXPaint.measureText(label)
            val textX = x - textWidth / 2
            canvas.drawText(label, textX, labelY, bottomXPaint)
        }
    }

    private fun drawEnergyCurve(canvas: Canvas) {
        energyPath.reset()
        val usableHeight =  getGraphHeight() - bottomPaddingForLabels - topPadding

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
        val hrFormat = String.format("%02d:%02d", hour, minute)
        val label = formatTo12Hour(LocalTime.parse(hrFormat,DateTimeFormatter.ofPattern("HH:mm")))
        val x = ((hour + minute / 60f) * hourWidthPx) - scrollOffsetX
        val yTop = topPadding
        val yBottom =  getGraphHeight() - bottomPaddingForLabels

        // Draw vertical line
        canvas.drawLine(x, yTop, x, yBottom, currentTimeLinePaint)

        val circleRadius = 10f
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#3D4A3F".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE             // Stroke color
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        canvas.drawCircle(x, 20f, circleRadius, circlePaint)

        canvas.drawCircle(x, 20f, circleRadius, strokePaint)

        // Draw rounded box with time text
        val textPadding = 12f
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textWidth = textPaint.measureText(label)

        val boxLeft = x - textWidth / 2f - textPadding - 10
        val boxRight = x + textWidth / 2f + textPadding + 10
        val boxBottom =  getGraphHeight().toFloat()
        val boxTop = boxBottom - textHeight - 2 * textPadding

        val rect = RectF(boxLeft, boxTop, boxRight, boxBottom)

        val boxPaint = Paint().apply {
            color = "#4D4D4D".toColorInt()
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(rect, 16f, 16f, boxPaint)
        // Draw time text
        val textY = boxTop + textPadding - textPaint.ascent()
        canvas.drawText(label, x - textWidth / 2f, textY, textPaint)
    }


    private fun drawTopAndBottomAxis(canvas: Canvas) {
        val yTop = topPadding
        val yBottom =  getGraphHeight().toFloat() - bottomPaddingForLabels

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

    private fun drawTimeWindows(canvas: Canvas) {
        val rowHeight =  getGraphHeight() * 0.12f
        val rowSpacing = 8f
        val baseBottom =  getGraphHeight() - bottomPaddingForLabels - 20f
        val cornerRadius = 16f

        for (window in timeWindows) {
            val left = window.startHour * hourWidthPx
            val right = window.endHour * hourWidthPx

            val rowOffset = window.rowIndex * (rowHeight + rowSpacing)
            val bottom = baseBottom - rowOffset
            val top = bottom - rowHeight

            val rect = RectF(left, top, right, bottom)


            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    rect.left, rect.top,      // Start X,Y
                    rect.right, rect.top,     // End X,Y (horizontal line)
                    window.startColor,                // Start color
                    window.endColor,               // End color
                    Shader.TileMode.CLAMP
                )
            }

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            labelTextPaint.color = window.textColor
            // Draw label
            val labelY = top + (rowHeight / 2f) - (labelTextPaint.descent() + labelTextPaint.ascent()) / 2f
            val labelX = left + 108f // 8f is optional padding
            canvas.drawText(window.label, labelX, labelY, labelTextPaint)
//            canvas.drawText(window.label, (left + right) / 2f, labelY, labelTextPaint)
        }
    }


}
