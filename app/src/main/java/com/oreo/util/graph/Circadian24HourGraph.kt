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
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.noisefit_commans.data.model.circadian.EnergyGraph
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.TimeWindow
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.FloatArray
import kotlin.floatArrayOf
import kotlin.math.abs
import kotlin.math.floor


class Circadian24HourGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var isScrollLocked = false

    private val hourWidthPx = 100f.dpToPixel()

    private val bottomPaddingForLabels = 16f.dpToPixel()
    private val topPadding = 30f

    val fontGilroy =
        ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)


    var graphStartTime: LocalTime = LocalTime.of(6, 0)
    var graphEndTime: LocalTime = LocalTime.of(8, 0)

    val totalHours: Int
        get() {
            var hours = Duration.between(graphStartTime, graphEndTime).toHours().toInt()
            if (hours <= 0) hours += 24
            return hours
        }


    fun getTotalWidth(): Float {
        return (totalHours * hourWidthPx)
    }

    fun getHourAt(index: Int): LocalTime {
        return graphStartTime.plusHours(index.toLong() % 24)
    }

    fun getGraphHeight(): Float {
        return height.toFloat()
    }

    private val energyGraph = ArrayList<Float>()

    var timeWindows: List<TimeWindow> = emptyList()
        set(value) {
            field = value
        }


    fun setDataSet(timeWindows: List<TimeWindow>, energyGraph: List<Float>?) {
        this.timeWindows = timeWindows
        this.energyGraph.clear()
        energyGraph?.let {
            this.energyGraph.addAll(it)
        }
        scrollOffsetX = calculateInitialScrollOffset()
        invalidate()
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 10f.dpToPixel()
        typeface = fontGilroy
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
        typeface = fontGilroy
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

        var offsetHours = Duration.between(graphStartTime, now).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24  // wrap around

        val hourPosition = (offsetHours * hourWidthPx) + (width / 2f)
        val centerX = width / 2f

        return (hourPosition - centerX).coerceIn(0f, getTotalWidth() - width.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.withTranslation(-scrollOffsetX, 0f) {
            drawHourLines(this)
            drawEnergyCurve2(
                this,
                energyGraph)
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

    private fun drawEnergyCurve2(canvas: Canvas, values: List<Float>) {
        var linePaint: Paint? = null
        linePaint = Paint().apply {
            strokeWidth = 5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val usableHeight =
            getGraphHeight() - bottomPaddingForLabels - topPadding - 3 * 22f.dpToPixel()

        val minuteWidth = hourWidthPx/60f
        for (i in 0 until values.size - 1) {
            val startX = i * minuteWidth 
            val stopX = (i + 1) * minuteWidth
            val startY = usableHeight - (values[i] * usableHeight * 0.8f + usableHeight * 0.1f)
            val stopY = usableHeight - (values[i + 1] * usableHeight * 0.8f + usableHeight * 0.1f)

            val controlX1 = startX + (stopX - startX) / 2
            val controlY1 = startY
            val controlX2 = stopX - (stopX - startX) / 2
            val controlY2 = stopY

            val colorStart = getColorForValue(values[i])
            val colorEnd = getColorForValue(values[i + 1])

            val segmentGradient = LinearGradient(
                startX, startY, stopX, stopY,
                colorStart, colorEnd,
                Shader.TileMode.CLAMP
            )

            val segmentPath = Path()
            segmentPath.moveTo(startX, startY)
            segmentPath.lineTo(stopX,stopY)
            //segmentPath.cubicTo(controlX1, controlY1, controlX2, controlY2, stopX, stopY)
            linePaint?.shader = segmentGradient
            canvas.drawPath(segmentPath, linePaint!!)
        }

        drawFilledSegments(canvas, values)

    }

    private fun drawFilledSegments(canvas: Canvas, values: List<Float>) {
        val usableHeight =
            getGraphHeight() - bottomPaddingForLabels - topPadding - 3 * 22f.dpToPixel()
        val fillPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        for (i in 0 until values.size - 1) {

            val startX = i * hourWidthPx
            val stopX = (i + 1) * hourWidthPx
            val startY = usableHeight - (values[i] * usableHeight * 0.8f + usableHeight * 0.1f)
            val stopY = usableHeight - (values[i + 1] * usableHeight * 0.8f + usableHeight * 0.1f)

            val controlX1 = startX + (stopX - startX) / 2
            val controlY1 = startY
            val controlX2 = stopX - (stopX - startX) / 2
            val controlY2 = stopY

            val colorStart = getColorForValueFill(values[i])
            val colorEnd = getColorForValueFill(values[i + 1])

            // Create gradient for this fill segment
            val segmentGradient = LinearGradient(
                startX, startY, stopX, stopY,
                colorStart, colorEnd,
                Shader.TileMode.CLAMP
            )

            // Create fill path for this segment
            val fillSegmentPath = Path()
            fillSegmentPath.moveTo(startX, startY)
            fillSegmentPath.cubicTo(controlX1, controlY1, controlX2, controlY2, stopX, stopY)
            fillSegmentPath.lineTo(stopX, usableHeight)  // Line to bottom
            fillSegmentPath.lineTo(startX, usableHeight) // Line to bottom left
            fillSegmentPath.close() // Close the shape

            // Apply gradient and draw fill segment
            fillPaint?.shader = segmentGradient
            canvas.drawPath(fillSegmentPath, fillPaint!!)
        }
    }

    private fun getColorForValue(value: Float): Int {
        val normalizedValue = value.coerceIn(0f, 1f)
        val red = Color.parseColor("#A66363")
        val green = Color.parseColor("#84D56F")

        val redR = Color.red(red)
        val redG = Color.green(red)
        val redB = Color.blue(red)

        val greenR = Color.red(green)
        val greenG = Color.green(green)
        val greenB = Color.blue(green)
        val r = (redR + (greenR - redR) * normalizedValue).toInt()
        val g = (redG + (greenG - redG) * normalizedValue).toInt()
        val b = (redB + (greenB - redB) * normalizedValue).toInt()
        return Color.rgb(r, g, b)
    }

    private fun getColorForValueFill(value: Float): Int {
        val normalizedValue = value.coerceIn(0f, 1f)
        val red = Color.parseColor("#A66363")
        val green = Color.parseColor("#84D56F")

        val redR = Color.red(red)
        val redG = Color.green(red)
        val redB = Color.blue(red)

        val greenR = Color.red(green)
        val greenG = Color.green(green)
        val greenB = Color.blue(green)
        val r = (redR + (greenR - redR) * normalizedValue).toInt()
        val g = (redG + (greenG - redG) * normalizedValue).toInt()
        val b = (redB + (greenB - redB) * normalizedValue).toInt()
        return Color.argb(10, r, g, b)
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

        val xLine = width / 2f

        val yTop = topPadding
        val yBottom = getGraphHeight() - bottomPaddingForLabels

        canvas.drawLine(xLine, yTop, xLine, yBottom, currentTimeLinePaint)


        val circleRadius = 10f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        canvas.drawCircle(xLine, 20f, circleRadius, strokePaint)

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


    fun fromFloatHour(value: Float): LocalTime {
        val hour = floor(value).toInt()
        val minute = ((value - hour) * 60).toInt()
        return LocalTime.of(hour, minute)
    }

    private fun drawTimeWindows(canvas: Canvas) {
        val rowHeight = 22f.dpToPixel()//getGraphHeight() * 0.12f
        val rowSpacing = 4f.dpToPixel()
        val baseBottom = getGraphHeight() - bottomPaddingForLabels - 16f.dpToPixel()
        val cornerRadius = 16f
        val padding = 1.5f.dpToPixel()

        for (window in timeWindows) {

            val startOffset = hoursFromStart(fromFloatHour(window.startHour))
            val endOffset = hoursFromEnd(fromFloatHour(window.endHour))


            val left = (startOffset * hourWidthPx) + padding // No padding here
            val right = (endOffset * hourWidthPx) - padding // No padding here

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

            val labelX = left + labelTextPaint.measureText(window.label) / 2 + 4f.dpToPixel()
            canvas.drawText(window.label, labelX, labelY, labelTextPaint)
        }
    }

    fun hoursFromStart(time: LocalTime): Float {
        val minutes = Duration.between(graphStartTime, time).toMinutes()
        var hours = minutes / 60f

        if (hours < 0f) hours += 24f
        return hours
    }

    fun hoursFromEnd(time: LocalTime): Float {
        val minutes = Duration.between(graphStartTime, time).toMinutes()
        var hours = minutes / 60f
        if (hours <= 0f) hours += 24f
        return hours
    }


}
