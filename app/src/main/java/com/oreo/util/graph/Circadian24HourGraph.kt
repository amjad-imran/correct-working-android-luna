package com.oreo.util.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.TimeWindow
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin


class Circadian24HourGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var isScrollLocked = false

    private val hourWidthPx = 68f.dpToPixel()

    private val bottomPaddingForLabels = 16f.dpToPixel()
    private val topPadding = 30f

    var graphStartTime: LocalTime = LocalTime.of(6, 0)
    var graphEndTime: LocalTime = LocalTime.of(8, 0)

    val totalHours: Int
        get() {
            var hours = Duration.between(graphStartTime, graphEndTime).toHours().toInt()
            if (hours <= 0) hours += 24
            return hours
        }


    fun getTotalWidth(): Float {
        return totalHours * hourWidthPx
    }

    fun getHourAt(index: Int): LocalTime {
        return graphStartTime.plusHours(index.toLong() % 24)
    }

    fun getGraphHeight(): Float {
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
        color = "#19FFFFFF".toColorInt()
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

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                dx: Float,
                dy: Float
            ): Boolean {
                scrollOffsetX = (scrollOffsetX + dx).coerceIn(0f, getTotalWidth() - width.toFloat())
                invalidate()
                return true
            }
        })

    init {
        setWillNotDraw(false)

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollOffsetX = calculateInitialScrollOffset()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                invalidate()
            }
        })
    }

    private fun calculateInitialScrollOffset(): Float {
        val now = LocalTime.now()

        // Calculate offset from graphStartTime, handle next-day wrapping
        var offsetHours = Duration.between(graphStartTime, now).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24  // wrap around

        val hourPosition = offsetHours * hourWidthPx
        val centerX = width / 2f

        return (hourPosition - centerX).coerceIn(0f, getTotalWidth() - width.toFloat())
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

    fun redraw() {
        invalidate()
    }

    private fun drawHourLines(canvas: Canvas) {

        for (i in 0..totalHours) {
            val x = i * hourWidthPx
            hourLinePaint.shader = LinearGradient(
                x, topPadding,
                x, getGraphHeight() - bottomPaddingForLabels - 8f.dpToPixel(),
                "#19000000".toColorInt(), "#19FFFFFF".toColorInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawLine(
                x,
                topPadding,
                x,
                getGraphHeight() - bottomPaddingForLabels - 8f.dpToPixel(),
                hourLinePaint
            )
        }
    }

    fun formatTo12Hour(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return time.format(formatter)
    }


    private fun drawTimeLabels(canvas: Canvas) {
        val labelY = getGraphHeight().toFloat() - 20f
        val labelPadding = 10f

        for (i in 0..totalHours) {
            val hour = getHourAt(i)
            val x = i * hourWidthPx
            val label = formatTo12Hour(LocalTime.of(hour.hour, hour.minute))
            val textWidth = bottomXPaint.measureText(label)

            val textX = when (i) {
                0 -> (x + labelPadding)
                totalHours -> (x - textWidth - labelPadding)
                else -> (x - textWidth / 2)
            }

            canvas.drawText(label, textX, labelY, bottomXPaint)
        }
    }

    private fun drawEnergyCurve(canvas: Canvas) {
        energyPath.reset()
        val usableHeight =
            getGraphHeight() - bottomPaddingForLabels - topPadding - 2 * 22f.dpToPixel()

        val energyPoints = floatArrayOf(
            0.3f, 0.32f, 0.35f, 0.4f, 1.0f, 0.5f, 0.58f, 0.65f, 0.72f, 0.78f,
            0.83f, 0.87f, 0.89f, 0.88f, 0.86f, 0.83f, 0.79f, 0.75f, 0.7f, 0.64f,
            0.58f, 0.52f, 0.46f, 0.41f, 0.37f, 0.33f, 0.3f, 0.28f, 0.27f, 0.28f
        )

        // Convert energy points to screen coordinates
        val points = mutableListOf<PointF>()
        for (i in energyPoints.indices) {
            val x = i * hourWidthPx/*(i / (energyPoints.size - 1f)) * width*/
            val y =
                usableHeight - (energyPoints[i] * usableHeight * 0.8f + usableHeight * 0.1f)//top + height * (1f - energyPoints[i])
            points.add(PointF(x, y))
        }

        // Create smooth curve using cubic bezier splines
        energyPath.reset()
        energyPath.moveTo(points[0].x, points[0].y)

        // Calculate control points for smooth cubic bezier curves
        for (i in 1 until points.size) {
            val currentPoint = points[i]
            val previousPoint = points[i - 1]

            // Calculate control points for smooth transition
            val cp1x: Float
            val cp1y: Float
            val cp2x: Float
            val cp2y: Float

            if (i == 1) {
                // First curve
                val nextPoint = if (i + 1 < points.size) points[i + 1] else currentPoint
                cp1x = previousPoint.x + (currentPoint.x - previousPoint.x) * 0.3f
                cp1y = previousPoint.y + (currentPoint.y - previousPoint.y) * 0.1f
                cp2x = currentPoint.x - (nextPoint.x - previousPoint.x) * 0.1f
                cp2y = currentPoint.y - (nextPoint.y - previousPoint.y) * 0.1f
            } else if (i == points.size - 1) {
                // Last curve
                val prevPrevPoint = points[i - 2]
                cp1x = previousPoint.x + (currentPoint.x - prevPrevPoint.x) * 0.1f
                cp1y = previousPoint.y + (currentPoint.y - prevPrevPoint.y) * 0.1f
                cp2x = currentPoint.x - (currentPoint.x - previousPoint.x) * 0.3f
                cp2y = currentPoint.y - (currentPoint.y - previousPoint.y) * 0.1f
            } else {
                // Middle curves - use Catmull-Rom spline approach
                val prevPoint = points[i - 2]
                val nextPoint = points[i + 1]

                val tension = 0.25f // Controls curve tightness (0.0 to 0.5)

                cp1x = previousPoint.x + (currentPoint.x - prevPoint.x) * tension
                cp1y = previousPoint.y + (currentPoint.y - prevPoint.y) * tension
                cp2x = currentPoint.x - (nextPoint.x - previousPoint.x) * tension
                cp2y = currentPoint.y - (nextPoint.y - previousPoint.y) * tension
            }

            energyPath.cubicTo(cp1x, cp1y, cp2x, cp2y, currentPoint.x, currentPoint.y)
        }

        canvas.drawPath(energyPath, energyPaint)


        /* val usableHeight = getGraphHeight() - bottomPaddingForLabels - topPadding - 2 * 22f.dpToPixel()

         for (i in 0..totalHours) {
             val time = graphStartTime.plusHours(i.toLong() % 24)
             val x = i * hourWidthPx

             val energy = getEnergyForHour(time.hour)
             LOGS.d("sdfjkhskdfj $energy")
             val y = usableHeight - (energy * usableHeight * 0.8f + usableHeight * 0.1f)

             if (i == 0) {
                 energyPath.moveTo(x, y)
             } else {
                 energyPath.lineTo(x, y)
             }
         }

         canvas.drawPath(energyPath, energyPaint)*/
    }


    private fun getEnergyForHour(hour: Int): Float {

        /*return if(hour==10){
            1f
        }else if(hour==12){
            0.5f
        }else{
            0f
        }*/

        val radians = (hour - 8) / 12f * Math.PI
        return (0.5 + 0.5 * sin(radians)).toFloat()
    }

    private fun drawCurrentTimeLine(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = LocalTime.of(hour, minute)

        val label = formatTo12Hour(currentTime)

        var offsetHours = Duration.between(graphStartTime, currentTime).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24  // Wrap around for next day

        val x = (offsetHours * hourWidthPx) - scrollOffsetX

        val yTop = topPadding
        val yBottom = getGraphHeight() - bottomPaddingForLabels


        canvas.drawLine(x, yTop, x, yBottom, currentTimeLinePaint)

        val circleRadius = 10f

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#3D4A3F".toColorInt()
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        // Top circle marker
        canvas.drawCircle(x, 20f, circleRadius, circlePaint)
        canvas.drawCircle(x, 20f, circleRadius, strokePaint)

        // Rounded box with time label at bottom
        val textPadding = 12f
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textWidth = textPaint.measureText(label)

        val boxLeft = x - textWidth / 2f - textPadding
        val boxRight = x + textWidth / 2f + textPadding
        val boxBottom = getGraphHeight().toFloat()
        val boxTop = boxBottom - textHeight - 2 * textPadding

        val rect = RectF(boxLeft, boxTop, boxRight, boxBottom)

        val boxPaint = Paint().apply {
            color = "#4D4D4D".toColorInt()
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(rect, 16f, 16f, boxPaint)

        // Draw time text inside rounded box
        val textY = boxTop + textPadding - textPaint.ascent()
        canvas.drawText(label, x - textWidth / 2f, textY, textPaint)
    }


    private fun drawTopAndBottomAxis(canvas: Canvas) {
        val yTop = topPadding
        val yBottom = getGraphHeight().toFloat() - bottomPaddingForLabels - 8f.dpToPixel()

        canvas.drawLine(0f, yTop, getTotalWidth(), yTop, topDottedAxisPaint)
        canvas.drawLine(0f, yBottom, getTotalWidth(), yBottom, bottomAxisPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isScrollLocked) {
            false
        } else {
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

    private fun drawTimeWindows(canvas: Canvas) {
        val rowHeight = 22f.dpToPixel()//getGraphHeight() * 0.12f
        val rowSpacing = 4f.dpToPixel()
        val baseBottom = getGraphHeight() - bottomPaddingForLabels - 16f.dpToPixel()
        val cornerRadius = 16f

        for (window in timeWindows) {
            val startOffset = hoursFromStart(LocalTime.of(window.startHour.toInt(), 0))
            val endOffset = hoursFromStart(LocalTime.of(window.endHour.toInt(), 0))

            val left = startOffset * hourWidthPx
            val right = endOffset * hourWidthPx

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
            val labelY =
                top + (rowHeight / 2f) - (labelTextPaint.descent() + labelTextPaint.ascent()) / 2f
            val labelX = left + 100f
            canvas.drawText(window.label, labelX, labelY, labelTextPaint)
        }
    }

    fun hoursFromStart(time: LocalTime): Int {
        var hours = Duration.between(graphStartTime, time).toHours().toInt()
        if (hours < 0) hours += 24
        return hours
    }


}
