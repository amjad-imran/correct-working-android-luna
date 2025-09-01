package com.oreo.util.graph

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import androidx.core.view.ViewCompat
import com.noisefit.luna.R
import com.noisefit_commans.ui.dpToPixel
import com.oreo.data.model.TimeWindow
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

class Circadian24HourGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var isScrollLocked = false

    private val hourWidthPx = 100f.dpToPixel()
    private val bottomPaddingForLabels = 16f.dpToPixel()
    private val topPadding = 10f.dpToPixel()

    private val horizontalPadding = 16f.dpToPixel()

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

    val totalHours: Int
        get() {
            var hours = Duration.between(graphStartTime, graphEndTime).toHours().toInt()
            if (hours <= 0) hours += 24
            return hours
        }

    private fun totalWidth(): Float = leadingPadPx + (totalHours * hourWidthPx) + horizontalPadding
    private fun hourAt(index: Int): LocalTime = graphStartTime.plusHours(index.toLong() % 24)
    private fun graphHeight(): Float = height.toFloat()

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

    private var scrollOffsetX = 0f
    private var lastX = 0f
    private var lastY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var isBeingDragged = false
    private val scroller = OverScroller(context)
    private var velocityTracker: VelocityTracker? = null
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    private var contentBitmap: Bitmap? = null
    private var contentCanvas: Canvas? = null
    private var contentValid = false
    private var leadingPadPx: Float = 0f

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

    private fun calculateInitialScrollOffset(): Float {
        leadingPadPx = width / 2f
        val now = LocalTime.now()

        var offsetHours = Duration.between(graphStartTime, now).toMinutes() / 60f
        if (offsetHours < 0) offsetHours += 24f  // wrap around

        val contentX = leadingPadPx + (offsetHours * hourWidthPx)
        val centerX = width / 2f
        val maxOffset = (totalWidth() - width.toFloat()).coerceAtLeast(0f)

        return (contentX - centerX).coerceIn(0f, maxOffset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!contentValid) {
            rebuildContentLayer()
        }

        contentBitmap?.let { bmp ->
            canvas.withTranslation(-scrollOffsetX, 0f) {
                drawBitmap(bmp, 0f, 0f, null)
            }
        }

        drawCurrentTimeLine(canvas)
    }

    private fun rebuildContentLayer() {
        if (width == 0 || height == 0) return
        leadingPadPx = width / 2f
        val w = max(totalWidth().toInt(), 1)
        val h = height

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
            val x = leadingPadPx + (i * hourWidthPx)
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
            val x = leadingPadPx + (i * hourWidthPx)
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

    private val curvePath = Path()
    private val fillPath = Path()
    private val tmpPos = ArrayList<Float>(512)

    private fun Int.withAlphaFraction(f: Float): Int {
        val a = ((f.coerceIn(0f, 1f) * 255f) + 0.5f).toInt()
        return (this and 0x00FFFFFF) or (a shl 24)
    }

    private fun drawEnergyCurveAndFill(canvas: Canvas, values: List<Float>) {
        if (values.size < 2) return

        val usableHeight =
            graphHeight() - bottomPaddingForLabels - topPadding - 3 * 22f.dpToPixel()
        val minuteWidth = hourWidthPx / 60f
        val lastX = (values.lastIndex) * minuteWidth

        fun yAt(v: Float) = usableHeight * (0.9f - 0.75f * v)

        curvePath.reset()
        fillPath.reset()
        tmpPos.clear()

        var x = leadingPadPx
        var y = yAt(values[0])
        curvePath.moveTo(x, y)
        tmpPos.add(0f)

        for (i in 1 until values.size) {
            x = leadingPadPx + (i * minuteWidth)
            y = yAt(values[i])
            curvePath.lineTo(x, y)
            tmpPos.add(((x - leadingPadPx) / lastX.coerceAtLeast(1f)).coerceIn(0f, 1f))
        }

        fillPath.set(curvePath)
        fillPath.lineTo(leadingPadPx + lastX, usableHeight)
        fillPath.lineTo(leadingPadPx, usableHeight)
        fillPath.close()

        val positions = FloatArray(tmpPos.size) { idx -> tmpPos[idx].coerceIn(0f, 1f) }
        val strokeColors = IntArray(values.size) { idx -> getColorForValue(values[idx]) }
        val fillColors =
            IntArray(values.size) { idx -> getColorForValueFill(values[idx]).withAlphaFraction(0.10f) }

        val lastXSafe = lastX.coerceAtLeast(1f)
        val strokeGradient =
            LinearGradient(leadingPadPx, 0f, leadingPadPx + lastXSafe, 0f, strokeColors, positions, Shader.TileMode.CLAMP)
        val fillGradient =
            LinearGradient(leadingPadPx, 0f, leadingPadPx + lastXSafe, 0f, fillColors, positions, Shader.TileMode.CLAMP)

        linePaint.shader = strokeGradient
        fillPaint.shader = fillGradient
        fillPaint.alpha = 255

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(curvePath, linePaint)
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

        val xInContent = leadingPadPx + (offsetHours * hourWidthPx)
        val xOnScreen = xInContent - scrollOffsetX
        val xLine = width / 2f

        val yTop = topPadding
        val yBottom = graphHeight() - bottomPaddingForLabels

        canvas.drawLine(xLine, yTop, xLine, yBottom, currentTimeLinePaint)

        canvas.drawCircle(xLine, 20f, 10f, strokePaint)

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
        canvas.drawLine(0f, yTop, totalWidth() - horizontalPadding, yTop, topDottedAxisPaint)
        canvas.drawLine(0f, yBottom, totalWidth()- horizontalPadding, yBottom, bottomAxisPaint)
    }

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
                        startFling(-vx.toInt())
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
        leadingPadPx = w / 2f
        scrollOffsetX = calculateInitialScrollOffset()
        requestRebuildContent()
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
            val left = leadingPadPx + (startOffset * hourWidthPx) + padding
            val right = leadingPadPx + (endOffset * hourWidthPx) - padding
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
            val labelY =
                top + (rowHeight / 2f) - (labelTextPaint.descent() + labelTextPaint.ascent()) / 2f
            val labelX = left + labelTextPaint.measureText(window.label) / 2 + 4f.dpToPixel()
            canvas.drawText(window.label, labelX, labelY, labelTextPaint)
        }

        if (leadingPadPx > 0f) {
            val rowIndex = 0
            val rowOffset = rowIndex * (rowHeight + rowSpacing)
            val bottom = baseBottom - rowOffset
            val top = bottom - rowHeight
            val left = 0f + 1.5f.dpToPixel()
            val right = leadingPadPx - 1.5f.dpToPixel()
            if (right > left) {
                tmpRect.set(left, top, right, bottom)
                windowPaint.shader = LinearGradient(
                    tmpRect.left, tmpRect.top,
                    tmpRect.right, tmpRect.top,
                    Color.parseColor("#33296F"), Color.parseColor("#634ED5"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(tmpRect, cornerRadius, cornerRadius, windowPaint)

                labelTextPaint.color = Color.parseColor("#9E91E8")
                val label = context.getString(R.string.text_sleep)
                val labelY = top + (rowHeight / 2f) - (labelTextPaint.descent() + labelTextPaint.ascent()) / 2f
                val labelX = left + labelTextPaint.measureText(label) / 2 + 4f.dpToPixel()
                canvas.drawText(label, labelX, labelY, labelTextPaint)
            }
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
