package com.oreo.ui.custom.sleep.internal

import android.R.attr.startX
import android.R.attr.startY
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import com.noisefit.util.ApplicationUtils.getFormattedSleepDuration
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils


class SleepHourVsNeedChartInternal constructor(context: Context?, attrs: AttributeSet?) :
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

    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0
    var dataStepWidth = 0F

    var mMax = 0

    private var isInteracting = false
    private var vibrationUtils: VibrationUtils? = null
    private var listener: SleepSingleBarAction? = null
    private var touchX = 0f


    //HashMap<Position,Pair<StartX,EndX>>
    private val dataPosition = ArrayList<Pair<Int, Float>>()

    private val yAxisRange = ArrayList<Pair<Int, String>>()
    private var lastSentValuePos: Int? = null

    private val endPadding = dip2px(30f)


    /**
     * Pair(deep,rem)
     */
    private val dataSet = ArrayList<GraphDataModel>()
    private var mSelectedPosition: Int? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

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
            strokeWidth = dip2px(2f).toFloat()
        }
        linePaintHourI = Paint().apply {
            this.color = Color.parseColor("#66465c8a")
            strokeWidth = dip2px(2f).toFloat()
        }
        linePaintNeed = Paint().apply {
            this.color = Color.parseColor("#7858cc")
            strokeWidth = dip2px(2f).toFloat()
        }
        linePaintNeedI = Paint().apply {
            this.color = Color.parseColor("#667858cc")
            strokeWidth = dip2px(2f).toFloat()
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
    }

    private fun drawContent(canvas: Canvas) {
        val availableWidth = (width - endPadding).toFloat()

        dataStepWidth = availableWidth / 7

        var start = 0f
        val circleRadius = dip2px(2f).toFloat()
        val circleRadiusBig = dip2px(4f).toFloat()
        val paddingHorizontal = dip2px(4f)
        val textPadding = dip2px(6f)
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
                    rectFSelected,
                    selectedDayPaint
                )

            }

            val isSelectedPosition = selectedPosition == index

            if (it.value1 != null) {
                val actualPos = getYAxisValue(it.value1 ?: 0)

                dataPosition.add(Pair(index, start))

                if (index + 1 < maxDataSize && dataSet[index + 1].value1 != null) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement.value1 ?: 0)
                    canvas.drawLine(
                        start + dataStepWidth / 2,
                        actualPos,
                        start + dataStepWidth + dataStepWidth / 2,
                        actualPosNext,
                        if (isInteracting) linePaintHourI else linePaintHour
                    )
                }

                canvas.drawCircle(
                    start + dataStepWidth / 2,
                    actualPos,
                    if (isInteracting && isSelectedPosition) circleRadiusBig else circleRadius,
                    if (isInteracting && isSelectedPosition.not()) circlePaintI else circlePaint
                )

                if (isSelectedPosition.not()) {
                    val (hour, minute) = getFormattedSleepDuration(it.value1 ?: 0)

                    val text = String.format("%d:%02d", hour, minute)
                    val xTextBounds = Rect()
                    if (isInteracting) {
                        textPaintHourI.getTextBounds(text, 0, text.length, xTextBounds)
                    } else {
                        textPaintHour.getTextBounds(text, 0, text.length, xTextBounds)
                    }
                    val textStart = start + dataStepWidth / 2 - xTextBounds.width() / 2
                    val yPos = if ((it.value1 ?: 0) > (it.value2 ?: 0)) {
                        actualPos - textPadding
                    } else {
                        actualPos + xTextBounds.height() + textPadding
                    }

                    canvas.drawText(
                        text,
                        textStart,
                        yPos/*actualPos + xTextBounds.height() + textPadding*/,
                        if (isInteracting) textPaintHourI else textPaintHour
                    )
                }
            }

            if (it.value2 != null) {
                val needPos = getYAxisValue(it.value2 ?: 0)

                if (index + 1 < maxDataSize && dataSet[index + 1].value2 != null) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement.value2 ?: 0)
                    canvas.drawLine(
                        start + dataStepWidth / 2,
                        needPos,
                        start + dataStepWidth + dataStepWidth / 2,
                        actualPosNext,
                        if (isInteracting) linePaintNeedI else linePaintNeed
                    )
                }

                canvas.drawCircle(
                    start + dataStepWidth / 2,
                    needPos,
                    if (isInteracting && isSelectedPosition) circleRadiusBig else circleRadius,
                    if (isInteracting && isSelectedPosition.not()) circlePaintI else circlePaint
                )


                if (isSelectedPosition.not()) {
                    val (hour, minute) = getFormattedSleepDuration(it.value2 ?: 0)

                    val text = String.format("%d:%02d", hour, minute)
                    val xTextBounds = Rect()
                    if (isInteracting) {
                        textPaintNeedI.getTextBounds(text, 0, text.length, xTextBounds)
                    } else {
                        textPaintNeed.getTextBounds(text, 0, text.length, xTextBounds)
                    }
                    val textStart = start + dataStepWidth / 2 - xTextBounds.width() / 2

                    val yPos = if ((it.value1 ?: 0) > (it.value2 ?: 0)) {
                        needPos + xTextBounds.height() + textPadding
                    } else {
                        needPos - textPadding
                    }
                    canvas.drawText(
                        text,
                        textStart,
                        yPos/*needPos - textPadding*/,
                        if (isInteracting) textPaintNeedI else textPaintNeed
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

        val stepWidth = (availableWidth) / 7
        var start = 0f
        for (i in 0..7) {
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
    }

    private fun drawXAxis(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        val stepWidth = availableWidth / 7

        val days = arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        var start = 0
        val xTextBounds = Rect()
        days.forEach {
            val textWidth = xAxisPaint.measureText(it)

            xAxisPaint.getTextBounds(it, 0, it.length, xTextBounds)

            val textStart = start + (stepWidth / 2 - textWidth / 2)
            canvas.drawText(it, textStart, height - xTextBounds.height().toFloat(), xAxisPaint)
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
        list: List<GraphDataModel>,
        yAxisRange: List<Pair<Int, String>>,
        maxValue: Int,
        selectedPosition: Int
    ) {
        dataPosition.clear()

        this.yAxisRange.clear()
        this.yAxisRange.addAll(yAxisRange)

        dataSet.clear()
        dataSet.addAll(list)

        mSelectedPosition = selectedPosition
        mMax = maxValue
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