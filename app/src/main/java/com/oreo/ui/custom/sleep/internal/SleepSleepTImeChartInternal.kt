package com.oreo.ui.custom.sleep.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
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
import com.google.gson.Gson
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.averageWithoutZeroGeneric
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.custom.sleep.SleepTimeModel
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.max


class SleepSleepTImeChartInternal constructor(context: Context?, attrs: AttributeSet?) :
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

    lateinit var linePaintHour: Paint
    lateinit var linePaintHourI: Paint
    lateinit var linePaintNeed: Paint
    lateinit var linePaintNeedI: Paint

    lateinit var avgTextPaint: Paint
    lateinit var avgLinePaint: Paint
    lateinit var avgBackPaintHour: Paint
    lateinit var avgBackPaintNeed: Paint

    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0
    var dataStepWidth = 0F

    private var isInteracting = false
    private var vibrationUtils: VibrationUtils? = null
    private var listener: SleepSingleBarAction? = null
    private var touchX = 0f


    //HashMap<Position,Pair<StartX,EndX>>
    private val dataPosition = ArrayList<Pair<Int, Float>>()

    private val yAxisRange = ArrayList<Pair<Int, String>>()
    private val xAxisRange = ArrayList<LocalDate>()

    private var lastSentValuePos: Int? = null

    private val endPadding = dip2px(30f)
    private var selectedPeriod: InternalSelectedPeriod? = null
    lateinit var xOverlayLinePaint: Paint
    lateinit var avgLineFillPaint: Paint

    private var mMax = 0L
    private var offset = 120L


    /**
     * Pair(deep,rem)
     */
    private val dataSet = ArrayList<SleepTimeModel>()


    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

        avgLineFillPaint = Paint()

        avgBackPaintHour = Paint().apply {
            this.color = Color.parseColor("#465c8a")
        }

        avgBackPaintNeed = Paint().apply {
            this.color = Color.parseColor("#7858cc")
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

        linePaintHour = Paint().apply {
            this.color = Color.parseColor("#465c8a")
            strokeWidth = dip2px(1f).toFloat()
        }
        linePaintHourI = Paint().apply {
            this.color = Color.parseColor("#66465c8a")
            strokeWidth = dip2px(1f).toFloat()
        }
        linePaintNeed = Paint().apply {
            this.color = Color.parseColor("#465c8a")
            strokeWidth = dip2px(1f).toFloat()
        }
        linePaintNeedI = Paint().apply {
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

        drawOverlay(canvas)
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

            val isSelectedPosition = selectedPosition == index

            if (it.startTime != 0L) {
                val actualPos = getYAxisValue(it.startTime)

                dataPosition.add(Pair(index, start))

                if (index + 1 < maxDataSize && dataSet[index + 1].startTime != 0L) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement.startTime)
                    canvas.drawLine(
                        start + dataStepWidth / 2,
                        actualPos,
                        start + dataStepWidth + dataStepWidth / 2,
                        actualPosNext,
                        if (isInteracting) linePaintHourI else linePaintHour
                    )
                }

                if (isSelectedPosition) {
                    canvas.drawCircle(
                        start + dataStepWidth / 2,
                        actualPos,
                        circleRadiusBig,
                        circlePaint
                    )
                }

            }

            if (it.endTime != 0L) {
                val needPos = getYAxisValue(it.endTime)

                if (index + 1 < maxDataSize && dataSet[index + 1].endTime != 0L) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement.endTime)
                    canvas.drawLine(
                        start + dataStepWidth / 2,
                        needPos,
                        start + dataStepWidth + dataStepWidth / 2,
                        actualPosNext,
                        if (isInteracting) linePaintNeedI else linePaintNeed
                    )
                }
                if (isSelectedPosition) {
                    canvas.drawCircle(
                        start + dataStepWidth / 2,
                        needPos,
                        circleRadiusBig,
                        circlePaint
                    )
                }

            }

            if (isSelectedPosition && isInteracting) {
                val center = start + dataStepWidth / 2

                canvas.drawRect(
                    RectF(
                        center - 2f,
                        topHeight.toFloat(),
                        center + 2f,
                        height.toFloat() - bottomHeight
                    ),
                    circlePaint
                )

                val widthHalf = dip2px(6f)
                val rectFTopI = RectF().apply {
                    this.left = center - widthHalf
                    this.right = center + widthHalf
                    this.top = topHeight.toFloat()
                    this.bottom = topHeight.toFloat() + dip2px(2f)
                }

                canvas.drawRect(rectFTopI, circlePaint)
            }

            start += dataStepWidth
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


        var lastPos = 0
        xAxisRange.forEachIndexed { index, value ->

            val dataSize = getDataSize(value)
            val filterValues = try {
                dataSet.subList(lastPos, lastPos + dataSize)
            } catch (exp: Exception) {
                ArrayList()
            }
            lastPos += dataSize

            val startTime =
                filterValues.map { it.startTime.toInt() }.averageWithoutZeroGeneric()
            val endTime = filterValues.map { it.endTime.toInt() }.averageWithoutZeroGeneric()


            if (startTime != 0) {

                val pos = getYAxisValue(startTime.toLong())
                val end = start.toFloat() + stepWidth

                val (hourVal, minute) = ApplicationUtils.getFormattedSleepDuration(
                    startTime
                )

                val text = String.format(locale = Locale.US, "%d:%02d", hourVal, minute)

                val overlayColor = getAvgBarColor()
                xOverlayLinePaint.color = overlayColor
                xOverlayLinePaint.getTextBounds(text, 0, text.length, textBounds)

                canvas.drawText(
                    text,
                    start.toFloat() + stepWidth / 2 - textBounds.width() / 2,
                    pos + paddingText + textBounds.height(),
                    xOverlayLinePaint
                )

                canvas.drawLine(
                    start.toFloat(), pos, end, pos, xOverlayLinePaint
                )

            }

            if (endTime != 0) {

                val pos = getYAxisValue(endTime.toLong())
                val end = start.toFloat() + stepWidth

                val (hourVal, minute) = ApplicationUtils.getFormattedSleepDuration(
                    endTime
                )

                val text = String.format(locale = Locale.US, "%d:%02d", hourVal, minute)

                val overlayColor = getAvgBarColor()

                xOverlayLinePaint.color = overlayColor
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

    private fun getDaysOfMonth(yearMonth: YearMonth): Int {
        return yearMonth.lengthOfMonth()
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

    private fun getYAxisValue(value: Long): Float {
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
                start,
                topHeight.toFloat(),
                start,
                height.toFloat() - bottomHeight,
                gridLinePaint
            )
            start += stepWidth
        }
    }

    private fun drawYAxis(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        val textBounds = Rect()
        val textPadding = dip2px(2f)

        yAxisRange.forEachIndexed { index, value ->

            val text = value.second
            xAxisPaint.getTextBounds(text, 0, text.length, textBounds)

            if (index == 0) {
                gridLinePaint.strokeWidth = dip2px(1f).toFloat()
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat() - textPadding,
                    getYAxisValue(value.first + offset),
                    xAxisPaint
                )
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first + offset),
                    availableWidth,
                    getYAxisValue(value.first + offset),
                    xLinePaint
                )
            } else if (index == yAxisRange.size - 1) {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)
                val yAxis = getYAxisValue(value.first + offset) + textBounds.height()

                if ((yAxis - textBounds.height()) > topHeight) {
                    canvas.drawText(
                        text,
                        width.toFloat() - textBounds.width() - textPadding,
                        getYAxisValue(value.first + offset) + textBounds.height(),
                        xAxisPaint
                    )
                    gridLinePaint.strokeWidth = dip2px(2f).toFloat()

                    canvas.drawLine(
                        0f,
                        getYAxisValue(value.first + offset),
                        availableWidth,
                        getYAxisValue(value.first + offset),
                        gridLinePaint
                    )
                }


            } else {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)

                val yAxis = getYAxisValue(value.first + offset) + textBounds.height() / 2
                if ((yAxis - textBounds.height()) > topHeight) {
                    canvas.drawText(
                        text,
                        width - textBounds.width().toFloat() - textPadding,
                        yAxis,
                        xAxisPaint
                    )
                    gridLinePaint.strokeWidth = dip2px(1f).toFloat()

                    canvas.drawLine(
                        0f,
                        getYAxisValue(value.first + offset),
                        availableWidth,
                        getYAxisValue(value.first + offset),
                        gridLinePaint
                    )
                }


            }
        }
        gridLinePaint.strokeWidth = dip2px(2f).toFloat()

        canvas.drawLine(
            0f,
            topHeight.toFloat(),
            availableWidth,
            topHeight.toFloat(),
            gridLinePaint
        )
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

    /**
     * array list of values -> Pair(actual sleep minutes, need minutes)
     * selected position
     */
    fun setDataSet(
        list: List<SleepTimeModel>,
        yAxisRange: List<Pair<Int, String>>,
        xAxisRange: List<LocalDate>,
        selectedPeriod: InternalSelectedPeriod?,
    ) {
        LOGS.d("sdfkjshdkfj ${Gson().toJson(list)}")
        dataPosition.clear()

        this.selectedPeriod = selectedPeriod

        this.yAxisRange.clear()
        this.yAxisRange.addAll(yAxisRange)

        this.xAxisRange.clear()
        this.xAxisRange.addAll(xAxisRange)

        mMax = 0
        offset = 60 * 6L

        list.forEach {
            if (it.endTime > mMax) {
                mMax = it.endTime
            }
        }

        mMax = (mMax + offset * 2)

        dataSet.clear()
        dataSet.addAll(list)

        invalidate()
    }

    var startX: Float? = null

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


    /**
     * current, previous value
     * @return color
     */
    private fun getAvgBarColorRestorativeLogic(currentValue: Int?, previousValue: Int?): Int {
        if (currentValue == null) return Color.WHITE
        if (previousValue == null) return Color.WHITE

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

    private fun getAvgBarColor(): Int {
        return Color.WHITE
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


}