package com.oreo.ui.custom.sleep.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import java.util.Locale


class SleepSingleLineChartInternal constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint
    lateinit var xLinePaint: Paint
    lateinit var gridLinePaint: Paint
    lateinit var circlePaint: Paint
    lateinit var circlePaintI: Paint
    lateinit var selectedDayPaint: Paint
    lateinit var textPaintHour: Paint
    lateinit var textPaintHourI: Paint
    lateinit var textPaintNeed: Paint
    lateinit var textPaintNeedI: Paint
    lateinit var avgTextPaint: Paint
    lateinit var avgLinePaint: Paint
    lateinit var avgBackPaint: Paint
    lateinit var xOverlayLinePaint: Paint
    lateinit var avgLineFillPaint: Paint


    lateinit var linePaint: Paint
    lateinit var linePaintI: Paint

    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0

    var mMax = 0

    private var isInteracting = false
    private var vibrationUtils: VibrationUtils? = null
    private var listener: SleepSingleBarAction? = null
    private var touchX = 0f
    private var startX: Float? = null
    var dataStepWidth = 0F


    //HashMap<Position,Pair<StartX,EndX>>
    //private val dataPosition = HashMap<Int, Pair<Float, Float>>()
    private val dataPosition = ArrayList<Pair<Int, Float>>()

    private val yAxisRange = ArrayList<Pair<Int, String>>()
    private val xAxisRange = ArrayList<String>()
    private var lastSentValuePos: Int? = null

    private val endPadding = dip2px(30f)
    var mAverage: Pair<Int, String>? = null
    private var showOverlay = false
    private var launchState: SleepInternalLaunchState? = null


    /**
     * Pair(deep,rem)
     */
    private val dataSet = ArrayList<Int?>()
    private var mSelectedPosition: Int? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

        avgLineFillPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        avgBackPaint = Paint().apply {
            this.color = Color.parseColor("#28ffffff")
        }
        xOverlayLinePaint = Paint().apply {
            this.color = Color.parseColor("#29cc74")
            strokeWidth = dip2px(2f).toFloat()
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }

        avgTextPaint = Paint().apply {
            this.color = Color.parseColor("#ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(9f).toFloat()
        }

        avgLinePaint = Paint().apply {
            this.color = Color.parseColor("#FFFFFF")
            this.style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
        }

        xAxisPaint = Paint().apply {
            this.color = Color.parseColor("#40ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }

        textPaintHour = Paint().apply {
            this.color = Color.parseColor("#96ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }
        textPaintHourI = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }
        textPaintNeed = Paint().apply {
            this.color = Color.parseColor("#ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }
        textPaintNeedI = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }

        linePaint = Paint().apply {
            this.color = Color.parseColor("#465c8a")
            strokeWidth = dip2px(1f).toFloat()
        }
        linePaintI = Paint().apply {
            this.color = Color.parseColor("#66465c8a")
            strokeWidth = dip2px(1f).toFloat()
        }

        xLinePaint = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
        }
        gridLinePaint = Paint().apply {
            this.color = Color.parseColor("#07ffffff")
        }
        circlePaint = Paint().apply {
            this.color = Color.parseColor("#ffffff")
        }
        circlePaintI = Paint().apply {
            this.color = Color.parseColor("#66ffffff")
        }
        selectedDayPaint = Paint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h

        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            mHeight.toFloat(),
            Color.parseColor("#11ffffff"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )

        selectedDayPaint = Paint().apply {
            shader = linearGradient
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackGrid(canvas)
        drawXAxis(canvas)
        drawYAxis(canvas)
        drawContent(canvas)
        if (showOverlay) {
            drawOverlay(canvas)
        }
    }

    private fun drawOverlay(canvas: Canvas) {
        if (isInteracting) return
        if (dataSet.isEmpty()) return

        val availableWidth = width.toFloat() - endPadding

        val stepWidth = availableWidth / xAxisRange.size
        var start = 0
        val paddingText = dip2px(4f)

        val textBounds = Rect()

        xAxisRange.forEachIndexed { index, value ->

            val filterValues = dataSet.subList(index * 7, index * 7 + 7)

            val avgValue = filterValues.filterNotNull().averageWithoutZero()


            if (avgValue != 0) {

                val pos = getYAxisValue(avgValue)
                val end = start.toFloat() + stepWidth

                val text = if (launchState == SleepInternalLaunchState.SLEEP_DURATION) {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
                        avgValue
                    )
                    String.format(locale = Locale.US, "%d:%02d", hour, minute)
                } else if (launchState == SleepInternalLaunchState.REM_SLEEP ||
                    launchState == SleepInternalLaunchState.DEEP_SLEEP ||
                    launchState == SleepInternalLaunchState.RESTFULNESS ||
                    launchState == SleepInternalLaunchState.LATENCY
                ) {
                    "$avgValue"
                } else {
                    "$avgValue%"
                }
                xOverlayLinePaint.getTextBounds(text, 0, text.length, textBounds)

                canvas.drawText(
                    text,
                    start.toFloat() + stepWidth / 2 - textBounds.width() / 2,
                    pos - paddingText,
                    xOverlayLinePaint
                )

                canvas.drawLine(
                    start.toFloat(), pos, end, pos, xOverlayLinePaint
                )


                val path = Path()
                path.reset()
                path.moveTo(start.toFloat(), pos)
                path.lineTo(end, pos)
                path.lineTo(end, pos + dip2px(50f).toFloat())
                path.lineTo(start.toFloat(), pos + dip2px(50f).toFloat())

                avgLineFillPaint.setShader(linearGradient)

                canvas.drawPath(path, avgLineFillPaint)
            }

            start += stepWidth.toInt()
        }

    }

    private fun drawContent(canvas: Canvas) {

        val availableWidth = (width - endPadding).toFloat()
        dataStepWidth = availableWidth / dataSet.size
        var start = 0f
        val circleRadiusBig = dip2px(4f).toFloat()
        val paddingHorizontal = dip2px(4f)
        val maxDataSize = dataSet.size

        val selectedPosition = getSelectedPosition()

        if (selectedPosition != null && isInteracting) {
            if (lastSentValuePos == null) {
                performHapticFeedbackCustom()
                lastSentValuePos = selectedPosition
            } else {
                if (lastSentValuePos != selectedPosition) {
                    performHapticFeedbackCustom()
                    lastSentValuePos = selectedPosition
                }
            }

            listener?.onValueSelected(selectedPosition)
        }

        if (dataSet.isEmpty()) {
            val noDataText = "No record available"
            val textBounds = Rect()
            xAxisPaint.getTextBounds(noDataText, 0, noDataText.length, textBounds)

            canvas.drawText(
                noDataText,
                availableWidth / 2 - textBounds.width() / 2,
                (height).toFloat() / 2,
                xAxisPaint
            )
        }

        dataSet.forEachIndexed { index, it ->
            if (mSelectedPosition != -1 && index + 1 == mSelectedPosition) {
                val rectFSelected = RectF(
                    start + paddingHorizontal,
                    topHeight.toFloat(),
                    start + dataStepWidth - paddingHorizontal,
                    height.toFloat() - bottomHeight
                )

                canvas.drawRect(
                    rectFSelected, selectedDayPaint
                )
            }


            if (it != null) {
                val isSelectedPosition = selectedPosition == index
                val actualPos = getYAxisValue(it ?: 0)

                //dataPosition[index] = Pair(start, start + stepWidth)
                dataPosition.add(Pair(index, start))

                if (index + 1 < maxDataSize && dataSet[index + 1] != null) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement ?: 0)
                    canvas.drawLine(
                        start + dataStepWidth / 2,
                        actualPos,
                        start + dataStepWidth + dataStepWidth / 2,
                        actualPosNext,
                        if (isInteracting) linePaintI else linePaint
                    )
                }

                if (isSelectedPosition && isInteracting) {
                    val center = start + dataStepWidth / 2

                    canvas.drawRect(
                        RectF(
                            center - 2f,
                            topHeight.toFloat(),
                            center + 2f,
                            height.toFloat() - bottomHeight
                        ), circlePaint
                    )

                    val widthHalf = dip2px(6f)
                    val rectFTopI = RectF().apply {
                        this.left = center - widthHalf
                        this.right = center + widthHalf
                        this.top = topHeight.toFloat()
                        this.bottom = topHeight.toFloat() + dip2px(2f)
                    }

                    canvas.drawRect(rectFTopI, circlePaint)

                    canvas.drawCircle(
                        start + dataStepWidth / 2, actualPos, circleRadiusBig, circlePaint
                    )
                }
            }
            start += dataStepWidth
        }

    }

    private fun showAverage(canvas: Canvas, availableWidth: Float) {
        if (mAverage != null) {
            val textBounds = Rect()
            avgTextPaint.getTextBounds(mAverage!!.second, 0, mAverage!!.second.length, textBounds)

            val textX = width - textBounds.width().toFloat()- dip2px(6f)
            val textY = getYAxisValue(mAverage!!.first) + textBounds.height() / 2


            canvas.drawRoundRect(
                RectF(
                    textX - dip2px(3f),
                    textY - textBounds.height() - dip2px(3f),
                    textX + textBounds.width() + dip2px(3f),
                    textY + dip2px(3f)
                ), dip2px(2f).toFloat(), dip2px(2f).toFloat(), avgBackPaint
            )

            canvas.drawText(
                mAverage!!.second, textX, textY, avgTextPaint
            )

            canvas.drawLine(
                0f,
                getYAxisValue(mAverage!!.first),
                availableWidth,
                getYAxisValue(mAverage!!.first),
                avgLinePaint
            )
        }
    }

    private fun performHapticFeedbackCustom() {
        vibrationUtils?.vibrate(HAPTIC_VIBRATION)
    }


    private fun getSelectedPosition(): Int? {
        if (isInteracting.not()) return null

        dataPosition.forEach {
            val endPos = it.second + dataStepWidth
            if (touchX < endPos) {
                return it.first
            }
        }
        return null
    }

    private fun getYAxisValue(value: Int): Float {
        val percent = (value.toFloat() / mMax.toFloat()) * 100
        val availableHeight = height - bottomHeight - topHeight
        return topHeight + availableHeight - (availableHeight * percent / 100)
    }

    private fun drawBackGrid(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        val stepWidth = (availableWidth) / xAxisRange.size
        var start = 0f
        for (i in 0..xAxisRange.size) {
            canvas.drawLine(
                start, topHeight.toFloat(), start, height.toFloat() - bottomHeight, gridLinePaint
            )
            start += stepWidth
        }
    }

    private fun drawYAxis(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding


        val textBounds = Rect()

        yAxisRange.forEachIndexed { index, value ->

            val text = value.second
            xAxisPaint.getTextBounds(text, 0, text.length, textBounds)

            if (index == 0) {
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat(),
                    getYAxisValue(value.first),
                    xAxisPaint
                )
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first),
                    availableWidth,
                    getYAxisValue(value.first),
                    xLinePaint
                )
            } else if (index == yAxisRange.size - 1) {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat(),
                    getYAxisValue(value.first) + textBounds.height(),
                    xAxisPaint
                )
                gridLinePaint.strokeWidth = dip2px(2f).toFloat()
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first),
                    availableWidth,
                    getYAxisValue(value.first),
                    gridLinePaint
                )
            } else {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat(),
                    getYAxisValue(value.first) + textBounds.height() / 2,
                    xAxisPaint
                )
                gridLinePaint.strokeWidth = dip2px(1f).toFloat()
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first),
                    availableWidth,
                    getYAxisValue(value.first),
                    gridLinePaint
                )
            }

        }

        showAverage(canvas, availableWidth)
    }

    private fun drawXAxis(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        val stepWidth = availableWidth / xAxisRange.size
        var start = 0
        val xTextBounds = Rect()
        val textY = height - dip2px(12f).toFloat()

        xAxisRange.forEach {
            val textWidth = xAxisPaint.measureText(it)
            xAxisPaint.getTextBounds(it, 0, it.length, xTextBounds)
            val textStart = start + (stepWidth / 2 - textWidth / 2)
            canvas.drawText(it, textStart, textY, xAxisPaint)
            start += stepWidth.toInt()
        }
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * array list of values -> Pair(actual sleep minutes, need minutes)
     * selected position
     */
    fun setDataSet(
        list: List<Int?>,
        yAxisRange: List<Pair<Int, String>>,
        xAxisRange: List<String>,
        maxValue: Int,
        avgValue: Pair<Int, String>?,
        selectedPosition: Int,
        showOverlay: Boolean = false,
        launchState: SleepInternalLaunchState?
    ) {
        this.showOverlay = showOverlay
        this.launchState = launchState

        dataPosition.clear()

        this.yAxisRange.clear()
        this.yAxisRange.addAll(yAxisRange)

        this.xAxisRange.clear()
        this.xAxisRange.addAll(xAxisRange)

        dataSet.clear()
        dataSet.addAll(list)

        mSelectedPosition = selectedPosition
        mMax = maxValue
        mAverage = avgValue

        invalidate()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val parent = parent
        parent.requestDisallowInterceptTouchEvent(true)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                touchX = event.x
                handler.postDelayed(
                    mLongPressed, ViewConfiguration.getLongPressTimeout().toLong()
                )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isInteracting) {
                    touchX = event.x
                    invalidate()
                } else {
                    val dx = event.x - startX!!
                    if (Math.abs(dx) > 0) {
                        handler.removeCallbacks(mLongPressed)
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(mLongPressed)
                isInteracting = false
                listener?.isInteractionOnGoing(false)
                touchX = 0.0f
                invalidate()
                return true
            }
        }

        return false
    }


    private val handler = Handler(Looper.getMainLooper())
    private var mLongPressed = Runnable {
        isInteracting = true
        invalidate()

        vibrationUtils?.vibrate(HAPTIC_VIBRATION)
        listener?.isInteractionOnGoing(true)
    }

    fun setVibrationUtil(vibrationUtils: VibrationUtils) {
        this.vibrationUtils = vibrationUtils
    }

    fun setClickListener(listener: SleepSingleBarAction?) {
        this.listener = listener
    }

    //TODO
    private fun getDaysOfMonth(month: String): Int {
        return when (month.lowercase()) {
            "jan" -> 31
            "feb" -> 28
            "mar" -> 31
            "apr" -> 30
            "may" -> 31
            "jun" -> 30
            "jul" -> 31
            "aug" -> 31
            "sep" -> 30
            "oct" -> 31
            "nov" -> 30
            "dec" -> 31
            else -> 30
        }
    }


}

enum class AvgBarType {
    PERCENT, TIME, DEFAULT
}