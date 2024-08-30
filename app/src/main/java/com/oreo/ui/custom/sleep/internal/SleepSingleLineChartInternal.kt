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
import androidx.core.graphics.ColorUtils
import com.google.gson.Gson
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.averageWithZeroGenericFloat
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.averageWithoutZeroGeneric
import com.noisefit_commans.common.averageWithoutZeroGenericFloat
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.sleep2.internal.DEFAULT_LONG_PRESS_TIMEOUT
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt


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
    private val xAxisRange = ArrayList<LocalDate>()
    private var lastSentValuePos: Int? = null

    private val endPadding = dip2px(30f)
    private var mAverage: Pair<Float, String>? = null
    private var showOverlay = false
    private var nonNullDataCount: Int = 0
    private var launchState: SleepInternalLaunchState? = null
    private var selectedPeriod: InternalSelectedPeriod? = null


    private val dataSet = ArrayList<GraphDataModel>()
    private var mSelectedPosition: Int? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

        avgLineFillPaint = Paint()

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
            this.color = Color.parseColor("#66465c8a")
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

        /* selectedDayPaint = Paint().apply {
             shader = linearGradient
         }*/
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

        if (selectedPeriod == InternalSelectedPeriod.WEEK) {
            if (dataSet.size % 7 != 0) {
                return
            }
        }


        val availableWidth = width.toFloat() - endPadding
        val stepWidth = availableWidth / xAxisRange.size
        var start = 0
        val paddingText = dip2px(4f)

        val textBounds = Rect()

        var previousValue: Float? = null

        var lastPos = 0

        xAxisRange.forEachIndexed { index, value ->

            val dataSize = getDataSize(value)
            val filterValues = try {
                dataSet.subList(lastPos, (lastPos + dataSize))
            } catch (exp: Exception) {
                ArrayList()
            }
            lastPos += dataSize


            val avgValue = if (launchState == SleepInternalLaunchState.RESTFULNESS) {
                filterValues.mapNotNull { it.value1 }.averageWithZeroGenericFloat()
            } else {
                filterValues.mapNotNull { it.value1 }.averageWithoutZeroGenericFloat()
            }

            val zeroCondition = if (launchState == SleepInternalLaunchState.RESTFULNESS) {
                true
            } else {
                avgValue != 0.0f
            }

            if (zeroCondition) {

                val roundedAvg = if (launchState == SleepInternalLaunchState.SLEEP_DURATION ||
                    launchState == SleepInternalLaunchState.REM_SLEEP ||
                    launchState == SleepInternalLaunchState.DEEP_SLEEP ||
                    launchState == SleepInternalLaunchState.RESTFULNESS ||
                    launchState == SleepInternalLaunchState.RESPIRATORY_RATE ||
                    launchState == SleepInternalLaunchState.RESTING_HEART_RATE ||
                    launchState == SleepInternalLaunchState.EFFICIENCY ||
                    launchState == SleepInternalLaunchState.LATENCY
                ) {
                    avgValue.roundToInt().toFloat()
                } else {
                    avgValue
                }


                val pos = getYAxisValue(roundedAvg.toFloat())
                val end = start.toFloat() + stepWidth

                val text = if (launchState == SleepInternalLaunchState.SLEEP_DURATION) {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
                        roundedAvg.roundToInt()
                    )
                    String.format(locale = Locale.US, "%d:%02d", hour, minute)
                } else if (launchState == SleepInternalLaunchState.REM_SLEEP ||
                    launchState == SleepInternalLaunchState.DEEP_SLEEP ||
                    launchState == SleepInternalLaunchState.RESTFULNESS ||
                    launchState == SleepInternalLaunchState.RESPIRATORY_RATE ||
                    launchState == SleepInternalLaunchState.RESTING_HEART_RATE ||
                    launchState == SleepInternalLaunchState.EFFICIENCY ||
                    launchState == SleepInternalLaunchState.HRV ||
                    launchState == SleepInternalLaunchState.LATENCY
                ) {
                    "${roundedAvg.roundToInt()}"
                } else if (
                    launchState == SleepInternalLaunchState.SKIN_TEMPERATURE
                ) {
                    String.format(locale = Locale.US, "%.1f", roundedAvg)
                } else {
                    "${String.format(locale = Locale.US, "%.1f", roundedAvg)}%"
                }


                val overlayColor = getAvgBarColor(roundedAvg, previousValue)
                val gradient = LinearGradient(
                    0f,
                    pos,
                    0f,
                    pos + dip2px(40f),
                    ColorUtils.setAlphaComponent(overlayColor, 80),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                xOverlayLinePaint.color = overlayColor
                xOverlayLinePaint.getTextBounds(text, 0, text.length, textBounds)

                canvas.drawText(
                    text,
                    start.toFloat() + stepWidth / 2 - textBounds.width() / 2,
                    pos - paddingText,
                    xOverlayLinePaint
                )

                canvas.drawRoundRect(
                    start.toFloat(),
                    pos,
                    end,
                    pos - dip2px(2f),
                    dip2px(2f).toFloat(),
                    dip2px(2f).toFloat(),
                    xOverlayLinePaint
                )

                val path = Path()
                path.reset()
                path.moveTo(start.toFloat(), pos)
                path.lineTo(end, pos)
                var endY = pos + dip2px(40f).toFloat()

                if (endY > (height - bottomHeight)) {
                    endY = (height - bottomHeight).toFloat()
                }

                path.lineTo(end, endY)
                path.lineTo(start.toFloat(), endY)

                avgLineFillPaint.setShader(gradient)
                canvas.drawPath(path, avgLineFillPaint)
                previousValue = avgValue
            }

            start += stepWidth.toInt()
        }

    }

    private fun getDataSize(date: LocalDate): Int {
        return if (selectedPeriod == InternalSelectedPeriod.MONTH) {
            getDaysOfMonth(date.yearMonth)
        } else {
            7
        }
    }

    private fun drawContent(canvas: Canvas) {

        val availableWidth = (width - endPadding).toFloat()
        dataStepWidth = availableWidth / dataSet.size
        var start = 0f
        val circleRadiusBig = dip2px(4f).toFloat()
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


        var isDataNull = true
        val consider0 = launchState == SleepInternalLaunchState.RESTFULNESS
        dataSet.forEachIndexed { index, it ->


            val zeroCond = if (consider0) {
                true
            } else {
                it.value1 != 0f
            }

            if (it.value1 != null && zeroCond) {
                isDataNull = false
                val isSelectedPosition = selectedPosition == index
                val actualPos = getYAxisValue(it.value1 ?: 0.0f)

                //dataPosition[index] = Pair(start, start + stepWidth)
                dataPosition.add(Pair(index, start))

                val zeroCondNext = if (consider0) {
                    true
                } else {
                    (index + 1 < maxDataSize) && dataSet[index + 1].value1 != 0.0f
                }

                if (index + 1 < maxDataSize && dataSet[index + 1].value1 != null && zeroCondNext) {
                    val nextElement = dataSet[index + 1].value1

                    val actualPosNext = getYAxisValue(nextElement ?: 0.0f)
                    canvas.drawLine(
                        start,
                        actualPos,
                        start + dataStepWidth,
                        actualPosNext,
                        if (isInteracting) linePaintI else linePaint
                    )
                }

                if (isSelectedPosition && isInteracting) {
                    val center = start

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
                        start, actualPos, circleRadiusBig, circlePaint
                    )
                }
            }
            start += dataStepWidth
        }
        if (isDataNull) {
            showNoRecordAvailable(canvas, availableWidth)
        }
    }

    private fun showNoRecordAvailable(canvas: Canvas, availableWidth: Float) {
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

    private fun showAverage(canvas: Canvas, availableWidth: Float) {
        if (mAverage != null && nonNullDataCount > 1) {
            val textBounds = Rect()
            avgTextPaint.getTextBounds(mAverage!!.second, 0, mAverage!!.second.length, textBounds)

            val textX = width - textBounds.width().toFloat() - dip2px(6f)
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

    private fun getYAxisValue(value: Float): Float {
        val percent = (value / mMax.toFloat()) * 100
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
                    getYAxisValue(value.first.toFloat()),
                    xAxisPaint
                )
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first.toFloat()),
                    availableWidth,
                    getYAxisValue(value.first.toFloat()),
                    xLinePaint
                )
            } else if (index == yAxisRange.size - 1) {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat(),
                    getYAxisValue(value.first.toFloat()) + textBounds.height(),
                    xAxisPaint
                )
                gridLinePaint.strokeWidth = dip2px(2f).toFloat()
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first.toFloat()),
                    availableWidth,
                    getYAxisValue(value.first.toFloat()),
                    gridLinePaint
                )
            } else {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat(),
                    getYAxisValue(value.first.toFloat()) + textBounds.height() / 2,
                    xAxisPaint
                )
                gridLinePaint.strokeWidth = dip2px(1f).toFloat()
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first.toFloat()),
                    availableWidth,
                    getYAxisValue(value.first.toFloat()),
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
            val displayMonth = if (selectedPeriod == InternalSelectedPeriod.MONTH) {
                it.format(DateTimeFormatter.ofPattern("MMM"))
            } else {
                //for week
                val weekFields = WeekFields.of(Locale.getDefault())
                val weekNumber = it.get(weekFields.weekOfWeekBasedYear())
                "W$weekNumber"
            }
            val textWidth = xAxisPaint.measureText(displayMonth)
            xAxisPaint.getTextBounds(displayMonth, 0, displayMonth.length, xTextBounds)
            val textStart = start + (stepWidth / 2 - textWidth / 2)
            canvas.drawText(displayMonth, textStart, textY, xAxisPaint)
            start += stepWidth.toInt()
        }
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun setDataSet(
        list: List<GraphDataModel>,
        yAxisRange: List<Pair<Int, String>>,
        xAxisRange: List<LocalDate>,
        maxValue: Int,
        avgValue: Pair<Float, String>?,
        selectedPosition: Int,
        showOverlay: Boolean = false,
        launchState: SleepInternalLaunchState?,
        selectedPeriod: InternalSelectedPeriod?,
        nonNullDataCount: Int
    ) {
        this.showOverlay = showOverlay
        this.launchState = launchState
        this.selectedPeriod = selectedPeriod
        this.nonNullDataCount = nonNullDataCount

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
                    mLongPressed, DEFAULT_LONG_PRESS_TIMEOUT
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

    private fun getDaysOfMonth(yearMonth: YearMonth): Int {
        return yearMonth.lengthOfMonth()
    }


    /**
     * current, previous value
     * @return color
     */
    private fun getAvgBarColor(currentValue: Float?, previousValue: Float?): Int {
        if (currentValue == null) return Color.WHITE
        if (previousValue == null) return Color.WHITE

        if (launchState == SleepInternalLaunchState.RESTING_HEART_RATE ||
            launchState == SleepInternalLaunchState.RESTFULNESS ||
            launchState == SleepInternalLaunchState.SKIN_TEMPERATURE
        ) {
            if (currentValue <= previousValue) {
                return Color.parseColor("#29cc74")
            }

            val currentPercentRaise =
                ((currentValue.toFloat() - previousValue.toFloat()) / previousValue) * 100

            return if (currentPercentRaise < 0) {//green
                Color.parseColor("#29cc74")
            } else if (currentPercentRaise >= 0 && currentPercentRaise < 2) {//yellow
                Color.parseColor("#ffbb6b")
            } else {//red
                Color.parseColor("#ff7c94")
            }
        } else {
            if (currentValue >= previousValue) {
                return Color.parseColor("#29cc74")
            }

            val currentPercentRaise =
                ((currentValue.toFloat() - previousValue.toFloat()) / previousValue) * 100

            return if (currentPercentRaise >= 0) {//green
                Color.parseColor("#29cc74")
            } else if (currentPercentRaise > -2) {//yellow
                Color.parseColor("#ffbb6b")
            } else {//red
                Color.parseColor("#ff7c94")
            }
        }
    }


}