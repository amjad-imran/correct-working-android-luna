package com.oreo.util.graph

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import androidx.core.view.ViewCompat
import com.noisefit_commans.ui.dpToPixel
import com.oreo.data.model.TimeWindow
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Circadian24HourGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var isScrollLocked = false

    private val hourWidthPx = 100f.dpToPixel()
    private val bottomPaddingForLabels = 16f.dpToPixel()
    private val topPadding = 30f

    private val fontGilroy =
        androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.noisefit_commans.R.font.gilroy_medium
        )

    var graphStartTime: LocalTime = LocalTime.of(6, 0)
    var graphEndTime: LocalTime = LocalTime.of(8, 0)

    private val energyGraph = ArrayList<Float>()
    var timeWindows: List<TimeWindow> = emptyList()
        set(value) {
            field = value
            requestRebuildContent()
        }

    // ---- Dimensions & helpers ----
    val totalHours: Int
        get() {
            var hours = Duration.between(graphStartTime, graphEndTime).toHours().toInt()
            if (hours <= 0) hours += 24
            return hours
        }

    private fun totalWidth(): Float = (totalHours * hourWidthPx)
    private fun hourAt(index: Int): LocalTime = graphStartTime.plusHours(index.toLong() % 24)
    private fun graphHeight(): Float = height.toFloat()

    // ---- Paints (no per-frame allocations) ----
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
    private val bottomAxisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#19FFFFFF".toColorInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val topDottedAxisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#26FFFFFF".toColorInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        typeface = fontGilroy
        textAlign = Paint.Align.LEFT
    }
    private val bottomXPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#6E6F74".toColorInt()
        textSize = 28f
        textAlign = Paint.Align.LEFT
    }
    private val currentTimeLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 5f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#4D4D4D".toColorInt()
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val tmpPath = Path()
    private val tmpRect = RectF()

    // ---- Smooth scrolling infra ----
    private var scrollOffsetX = 0f
    private var lastX = 0f
    private var lastY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var isBeingDragged = false
    private val scroller = OverScroller(context)
    private var velocityTracker: VelocityTracker? = null
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    // ---- Cached content layer to avoid re-drawing heavy stuff while scrolling ----
    private var contentBitmap: Bitmap? = null
    private var contentCanvas: Canvas? = null
    private var contentValid = false

    fun setDataSet(timeWindows: List<TimeWindow>, energyGraph: List<Float>?) {
        this.timeWindows = timeWindows
        this.energyGraph.clear()
        energyGraph?.let { this.energyGraph.addAll(it) }
        scrollOffsetX = calculateInitialScrollOffset()
        requestRebuildContent()
        invalidate()
    }

    init {
        setWillNotDraw(false)
        // Keep HW accelerated
        setLayerType(LAYER_TYPE_HARDWARE, null)

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollOffsetX = calculateInitialScrollOffset()
                requestRebuildContent()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                invalidate()
            }
        })
    }

    /*private fun calculateInitialScrollOffset(): Float {
        val now = LocalTime.now()
        var offsetHours = Duration.between(graphStartTime, now).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24
        val hourPosition = (offsetHours * hourWidthPx) + (width / 2f)
        val centerX = width / 2f
        return (hourPosition - centerX).coerceIn(0f, max(0f, totalWidth() - width.toFloat()))
    }*/

    private fun calculateInitialScrollOffset(): Float {
        val now = LocalTime.now()

        var offsetHours = Duration.between(graphStartTime, now).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24f  // wrap around

        val contentX = offsetHours * hourWidthPx
        val centerX = width / 2f
        val maxOffset = (totalWidth() - width.toFloat()).coerceAtLeast(0f)

        // Center "now" in the viewport
        return (contentX - centerX).coerceIn(0f, maxOffset)
    }

    // ---- Drawing ----
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Build (or rebuild) offscreen content if invalid
        if (!contentValid) {
            rebuildContentLayer()
        }

        // Draw cached, scrollable content
        contentBitmap?.let { bmp ->
            canvas.withTranslation(-scrollOffsetX, 0f) {
                drawBitmap(bmp, 0f, 0f, null)
            }
        }

        // Draw non-cached, per-frame elements (current time line overlay)
        drawCurrentTimeLine(canvas)
    }

    private fun rebuildContentLayer() {
        if (width == 0 || height == 0) return
        val w = max(totalWidth().toInt(), 1)
        val h = height

        // (Re)allocate bitmap only if size changed or null
        if (contentBitmap?.width != w || contentBitmap?.height != h) {
            contentBitmap?.recycle()
            contentBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            contentCanvas = Canvas(contentBitmap!!)
        }

        val c = contentCanvas ?: return
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        drawHourLines(c)
        drawTopAndBottomAxis(c)
        drawTimeLabels(c)
        drawEnergyCurveAndFill(c, energyGraph)
        drawTimeWindows(c)

        contentValid = true
    }

    private fun requestRebuildContent() {
        contentValid = false
    }

    private fun drawHourLines(canvas: Canvas) {
        val top = topPadding
        val bottom = graphHeight() - bottomPaddingForLabels - 8f.dpToPixel()
        for (i in 0..totalHours) {
            val x = i * hourWidthPx
            hourLinePaint.shader = LinearGradient(
                x, top, x, bottom,
                "#19000000".toColorInt(), "#19FFFFFF".toColorInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawLine(x, top, x, bottom, hourLinePaint)
        }
    }

    private fun formatTo12Hour(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return time.format(formatter)
    }

    private fun drawTimeLabels(canvas: Canvas) {
        val labelY = graphHeight() - 20f
        val labelPadding = 10f
        for (i in 0..totalHours) {
            val hour = hourAt(i)
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

    // Combined, allocation-free draw for curve + fill
    private fun drawEnergyCurveAndFill(canvas: Canvas, values: List<Float>) {
        if (values.size < 2) return

        val usableHeight =
            graphHeight() - bottomPaddingForLabels - topPadding - 3 * 22f.dpToPixel()

        val minuteWidth = hourWidthPx / 60f

        // Stroke segments
        for (i in 0 until values.size - 1) {
            val startX = i * minuteWidth
            val stopX = (i + 1) * minuteWidth
            val startY = usableHeight - (values[i] * usableHeight * 0.8f + usableHeight * 0.1f)
            val stopY = usableHeight - (values[i + 1] * usableHeight * 0.8f + usableHeight * 0.1f)

            val colorStart = getColorForValue(values[i])
            val colorEnd = getColorForValue(values[i + 1])
            linePaint.shader = LinearGradient(
                startX, startY, stopX, stopY,
                colorStart, colorEnd, Shader.TileMode.CLAMP
            )

            tmpPath.reset()
            tmpPath.moveTo(startX, startY)
            tmpPath.lineTo(stopX, stopY)
            canvas.drawPath(tmpPath, linePaint)
        }

        // Fill segments
        for (i in 0 until values.size - 1) {
            val startX = i * minuteWidth
            val stopX = (i + 1) * minuteWidth
            val startY = usableHeight - (values[i] * usableHeight * 0.8f + usableHeight * 0.1f)
            val stopY = usableHeight - (values[i + 1] * usableHeight * 0.8f + usableHeight * 0.1f)

            val colorStart = getColorForValueFill(values[i])
            val colorEnd = getColorForValueFill(values[i + 1])
            fillPaint.shader = LinearGradient(
                startX, startY, stopX, stopY,
                colorStart, colorEnd, Shader.TileMode.CLAMP
            )

            tmpPath.reset()
            tmpPath.moveTo(startX, startY)
            // simple straight edge looks crisp and is cheaper than cubic
            tmpPath.lineTo(stopX, stopY)
            tmpPath.lineTo(stopX, usableHeight)
            tmpPath.lineTo(startX, usableHeight)
            tmpPath.close()
            canvas.drawPath(tmpPath, fillPaint)
        }
    }

    private fun getColorForValue(value: Float): Int {
        val normalized = value.coerceIn(0f, 1f)
        val red = Color.parseColor("#A66363")
        val green = Color.parseColor("#84D56F")
        val r = (Color.red(red) + (Color.red(green) - Color.red(red)) * normalized).toInt()
        val g = (Color.green(red) + (Color.green(green) - Color.green(red)) * normalized).toInt()
        val b = (Color.blue(red) + (Color.blue(green) - Color.blue(red)) * normalized).toInt()
        return Color.rgb(r, g, b)
    }

    private fun getColorForValueFill(value: Float): Int {
        val normalized = value.coerceIn(0f, 1f)
        val red = Color.parseColor("#A66363")
        val green = Color.parseColor("#84D56F")
        val r = (Color.red(red) + (Color.red(green) - Color.red(red)) * normalized).toInt()
        val g = (Color.green(red) + (Color.green(green) - Color.green(red)) * normalized).toInt()
        val b = (Color.blue(red) + (Color.blue(green) - Color.blue(red)) * normalized).toInt()
        return Color.argb(10, r, g, b)
    }

    private fun drawCurrentTimeLine(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = LocalTime.of(hour, minute)
        val label = formatTo12Hour(currentTime)

        var offsetHours = Duration.between(graphStartTime, currentTime).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24f

        val xInContent = (offsetHours * hourWidthPx)
        val xOnScreen = xInContent - scrollOffsetX
        val xLine = width / 2f

        val yTop = topPadding
        val yBottom = graphHeight() - bottomPaddingForLabels

        // fixed center line
        canvas.drawLine(xLine, yTop, xLine, yBottom, currentTimeLinePaint)

        // small circle at top
        canvas.drawCircle(xLine, 20f, 10f, strokePaint)

        // time bubble near bottom aligned to content x
        val textPadding = 12f
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textWidth = textPaint.measureText(label)

        val boxLeft = xOnScreen - textWidth / 2f - textPadding
        val boxRight = xOnScreen + textWidth / 2f + textPadding
        val boxBottom = graphHeight()
        val boxTop = boxBottom - textHeight - 2 * textPadding

        tmpRect.set(boxLeft, boxTop, boxRight, boxBottom)
        canvas.drawRoundRect(tmpRect, 16f, 16f, boxPaint)

        val textY = boxTop + textPadding - textPaint.ascent()
        canvas.drawText(label, xOnScreen - textWidth / 2f, textY, textPaint)
    }

    private fun drawTopAndBottomAxis(canvas: Canvas) {
        val yTop = topPadding
        val yBottom = graphHeight() - bottomPaddingForLabels - 8f.dpToPixel()
        canvas.drawLine(0f, yTop, totalWidth(), yTop, topDottedAxisPaint)
        canvas.drawLine(0f, yBottom, totalWidth(), yBottom, bottomAxisPaint)
    }

    // ---- Touch + fling ----
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isScrollLocked) return false

        ensureVelocityTracker()
        velocityTracker?.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                isBeingDragged = false
                parent.requestDisallowInterceptTouchEvent(true)
                if (!scroller.isFinished) scroller.abortAnimation()
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY

                if (!isBeingDragged) {
                    val absDx = abs(dx)
                    val absDy = abs(dy)
                    if (absDx > touchSlop && absDx > absDy) {
                        isBeingDragged = true
                        parent.requestDisallowInterceptTouchEvent(true)
                    } else if (absDy > touchSlop && absDy > absDx) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }

                if (isBeingDragged) {
                    // Content moves opposite to finger
                    val newOffset = (scrollOffsetX - dx)
                        .coerceIn(0f, max(0f, totalWidth() - width.toFloat()))
                    if (newOffset != scrollOffsetX) {
                        scrollOffsetX = newOffset
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                    lastX = event.x
                    lastY = event.y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isBeingDragged && event.actionMasked == MotionEvent.ACTION_UP) {
                    velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    val vx = velocityTracker?.xVelocity ?: 0f
                    if (abs(vx) > minFlingVelocity) {
                        startFling(-vx.toInt()) // negative because content direction
                    }
                }
                isBeingDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
                recycleVelocityTracker()
            }
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scrollOffsetX = calculateInitialScrollOffset()
        requestRebuildContent()    // if you’re using the cached bitmap approach
        invalidate()
    }

    private fun startFling(velocityX: Int) {
        val maxX = max(0, (totalWidth() - width).toInt())
        scroller.fling(
            scrollOffsetX.toInt(), 0,
            velocityX, 0,
            0, maxX,
            0, 0
        )
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val newX = scroller.currX.toFloat()
            val clamped = newX.coerceIn(0f, max(0f, totalWidth() - width.toFloat()))
            if (clamped != scrollOffsetX) {
                scrollOffsetX = clamped
                ViewCompat.postInvalidateOnAnimation(this)
            } else if (!scroller.isFinished) {
                // Stop if we hit bounds
                scroller.abortAnimation()
            }
        }
    }

    private fun ensureVelocityTracker() {
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain()
    }
    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    // ---- Public helpers ----
    fun redraw() {
        requestRebuildContent()
        invalidate()
    }

    fun fromFloatHour(value: Float): LocalTime {
        val hour = floor(value).toInt()
        val minute = ((value - hour) * 60).toInt()
        return LocalTime.of(hour, minute)
    }

    private fun drawTimeWindows(canvas: Canvas) {
        val rowHeight = 22f.dpToPixel()
        val rowSpacing = 4f.dpToPixel()
        val baseBottom = graphHeight() - bottomPaddingForLabels - 16f.dpToPixel()
        val cornerRadius = 16f
        val padding = 1.5f.dpToPixel()

        for (window in timeWindows) {
            val startOffset = hoursFromStart(fromFloatHour(window.startHour))
            val endOffset = hoursFromEnd(fromFloatHour(window.endHour))
            val left = (startOffset * hourWidthPx) + padding
            val right = (endOffset * hourWidthPx) - padding
            val rowOffset = window.rowIndex * (rowHeight + rowSpacing)
            val bottom = baseBottom - rowOffset
            val top = bottom - rowHeight
            tmpRect.set(left, top, right, bottom)

            windowPaint.shader = LinearGradient(
                tmpRect.left, tmpRect.top,
                tmpRect.right, tmpRect.top,
                window.startColor, window.endColor,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(tmpRect, cornerRadius, cornerRadius, windowPaint)

            labelTextPaint.color = window.textColor
            val labelY = top + (rowHeight / 2f) - (labelTextPaint.descent() + labelTextPaint.ascent()) / 2f
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
