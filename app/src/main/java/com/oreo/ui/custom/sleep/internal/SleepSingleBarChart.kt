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
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.sleep2.internal.DEFAULT_LONG_PRESS_TIMEOUT
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt


class SleepSingleBarChart constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint
    lateinit var xLinePaint: Paint
    lateinit var xTextPaint: Paint
    lateinit var gridLinePaint: Paint
    lateinit var barPaint: Paint
    lateinit var barPaintInteracting: Paint
    lateinit var selectedDayPaint: Paint
    lateinit var barPaintTop: Paint
    lateinit var barPaintTopInteracting: Paint
    lateinit var barTextPaint: Paint
    lateinit var avgBackPaint: Paint
    lateinit var avgTextPaint: Paint
    lateinit var avgLinePaint: Paint
    lateinit var barTextPaintI: Paint
    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private val endPadding = dip2px(40f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0
    lateinit var optimalPaint: Paint

    private val dataSet = ArrayList<GraphDataModel>()
    private var mSelectedPosition: Int? = null
    private var contributorType: SleepInternalLaunchState? = null
    private var optimalRange: Pair<Float, Float>? = null

    private var isInteracting = false
    private var vibrationUtils: VibrationUtils? = null
    private var listener: SleepSingleBarAction? = null
    private var touchX = 0f
    var dataStepWidth = 0F


    //HashMap<Position,Pair<StartX,EndX>>
    private val dataPosition = ArrayList<Pair<Int, Float>>()
    private var lastSentValuePos: Int? = null

    private val yAxisRange = ArrayList<Pair<Int, String>>()
    var mMax = 0
    var mMin = 0
    var mAverage: Pair<Float, String>? = null
    var nonNullDataCount: Int = 0


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

        optimalPaint = Paint().apply {
            this.color = Color.parseColor("#19a3eeff")
        }

        barTextPaint = Paint().apply {
            this.color = Color.parseColor("#96ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }
        avgBackPaint = Paint().apply {
            this.color = Color.parseColor("#28ffffff")
        }
        barTextPaintI = Paint().apply {
            this.color = Color.parseColor("#40ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }


        xLinePaint = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
        }
        xTextPaint = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
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
        gridLinePaint = Paint().apply {
            this.color = Color.parseColor("#07ffffff")
        }
        barPaint = Paint().apply {
            this.color = Color.parseColor("#465c8a")
        }
        barPaintInteracting = Paint().apply {
            this.color = Color.parseColor("#66465c8a")
        }
        barPaintTop = Paint().apply {
            this.color = Color.parseColor("#ffffff")
        }
        barPaintTopInteracting = Paint().apply {
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
        drawYAxis(canvas)
        drawXAxis(canvas)
        drawContent(canvas)
    }

    private fun drawContent(canvas: Canvas) {

        val availableWidth = (width - endPadding).toFloat()
        dataStepWidth = availableWidth / 7
        val barWidth = dataStepWidth / 2
        var start = 0f
        val rectRadius = dip2px(1f).toFloat()

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
            showNoRecordAvailable(canvas, availableWidth)
        }

        dataSet.forEachIndexed { index, it ->

            if (index + 1 == mSelectedPosition) {
                val padding = dip2px(4f)
                val rectFSelected = RectF(
                    start + padding,
                    topHeight.toFloat(),
                    start + dataStepWidth - padding,
                    height.toFloat() - bottomHeight
                )

                canvas.drawRect(
                    rectFSelected, selectedDayPaint
                )

            }


            if (it.value1 != null) {

                val top = getYAxisValue(it.value1)
                val isSelectedPosition = selectedPosition == index

                dataPosition.add(Pair(index, start))


                val rectF = RectF(
                    start + barWidth / 2,
                    top,
                    start + barWidth + barWidth / 2,
                    height.toFloat() - bottomHeight
                )

                canvas.drawRoundRect(
                    rectF, rectRadius, rectRadius, if (isInteracting && isSelectedPosition.not()) {
                        barPaintInteracting
                    } else barPaint
                )

                val rectFTop = RectF().apply {
                    this.left = rectF.left
                    this.right = rectF.right
                    this.top = rectF.top
                    this.bottom = rectF.top + dip2px(2f)
                }

                canvas.drawRoundRect(
                    rectFTop,
                    rectRadius,
                    rectRadius,
                    if (isInteracting && isSelectedPosition.not()) {
                        barPaintTopInteracting
                    } else barPaintTop
                )

                if (isSelectedPosition && isInteracting) {

                    val center = rectFTop.left + (rectFTop.right - rectFTop.left) / 2

                    canvas.drawRect(
                        RectF(center - 2f, topHeight.toFloat(), center + 2f, rectFTop.top),
                        barPaintTop
                    )

                    val widthHalf = dip2px(6f)
                    val rectFTopI = RectF().apply {
                        this.left = center - widthHalf
                        this.right = center + widthHalf
                        this.top = topHeight.toFloat()
                        this.bottom = topHeight.toFloat() + dip2px(2f)
                    }

                    canvas.drawRect(rectFTopI, barPaintTop)
                }


                if (isSelectedPosition.not()) {
                    val text = if (contributorType == SleepInternalLaunchState.SLEEP_PERFORMANCE
                        || contributorType == SleepInternalLaunchState.BLOOD_OXYGEN
                    ) {
                        "${it.value1.roundToInt()}%"
                    } else if (contributorType == SleepInternalLaunchState.REM_SLEEP || contributorType == SleepInternalLaunchState.DEEP_SLEEP) {
                        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(it.value1.roundToInt())
                        String.format(locale = Locale.US, "%02d:%02d", hour, minute)
                    } else {
                        "${it.value1.roundToInt()}"
                    }
                    val xTextBounds = Rect()
                    if (isInteracting) {
                        barTextPaintI.getTextBounds(text, 0, text.length, xTextBounds)
                    } else {
                        barTextPaint.getTextBounds(text, 0, text.length, xTextBounds)

                    }
                    val textStart = start + dataStepWidth / 2 - xTextBounds.width() / 2
                    canvas.drawText(
                        text,
                        textStart,
                        top - xTextBounds.height(),
                        if (isInteracting) barTextPaintI else barTextPaint
                    )
                }

            }
            start += dataStepWidth
        }

        showAverage(canvas, availableWidth)
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

    fun getYAxisValue(value: Float): Float {
        val percent = ((value - mMin) / (mMax - mMin).toFloat()) * 100
        val availableHeight = height - bottomHeight - topHeight
        return topHeight + availableHeight - (availableHeight * percent / 100)
    }

    private fun drawBackGrid(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        gridLinePaint.strokeWidth = dip2px(1f).toFloat()

        val stepWidth = availableWidth / 7
        var start = 0f
        for (i in 0..7) {
            canvas.drawLine(
                start, topHeight.toFloat(), start, height.toFloat() - bottomHeight, gridLinePaint
            )
            start += stepWidth
        }

        optimalRange?.let {
            var min = getYAxisValue(it.first)
            var max = getYAxisValue(it.second)

            if (max < topHeight) {
                max = topHeight.toFloat()
            }

            canvas.drawRect(
                RectF(
                    0f,
                    min,
                    availableWidth,
                    max
                ), optimalPaint
            )
        }
    }

    private fun showAverage(canvas: Canvas, availableWidth: Float) {
        if (mAverage != null && nonNullDataCount > 1) {
            val textBounds = Rect()

            avgTextPaint.getTextBounds(mAverage!!.second, 0, mAverage!!.second.length, textBounds)

            val textX = width - textBounds.width().toFloat() - dip2px(3f)
            val textY = getYAxisValue(mAverage!!.first) + textBounds.height() / 2


            canvas.drawRoundRect(
                RectF(
                    textX - dip2px(3f),
                    textY - textBounds.height() - dip2px(3f),
                    textX + textBounds.width() + dip2px(3f),
                    textY + dip2px(3f)
                ),
                dip2px(2f).toFloat(),
                dip2px(2f).toFloat(),
                avgBackPaint
            )

            canvas.drawText(
                mAverage!!.second,
                textX,
                textY,
                avgTextPaint
            )

            canvas.drawLine(
                0f,
                getYAxisValue(mAverage!!.first),
                availableWidth,
                getYAxisValue(mAverage!!.first),
                avgLinePaint
            )
        }

        if (nonNullDataCount == 0) {
            showNoRecordAvailable(canvas, availableWidth)

        }
    }

    private fun drawYAxis(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        val textBounds = Rect()
        val offsetWidth = dip2px(2f)

        yAxisRange.forEachIndexed { index, value ->

            val text = value.second
            xAxisPaint.getTextBounds(text, 0, text.length, textBounds)

            if (index == 0) {
                gridLinePaint.strokeWidth = dip2px(1f).toFloat()
                canvas.drawText(
                    text,
                    width - textBounds.width().toFloat() - offsetWidth,
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
                    width - textBounds.width().toFloat() - offsetWidth,
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
                    width - textBounds.width().toFloat() - offsetWidth,
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
    }

    private fun drawXAxis(canvas: Canvas) {

        val stepWidth = (width - endPadding) / 7

        val days = arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        var start = 0
        val xTextBounds = Rect()
        days.forEach {
            val textWidth = xAxisPaint.measureText(it)

            xAxisPaint.getTextBounds(it, 0, it.length, xTextBounds)

            val textStart = start + (stepWidth / 2 - textWidth / 2)
            canvas.drawText(it, textStart, height - xTextBounds.height().toFloat(), xAxisPaint)
            start += stepWidth
        }
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun setDataSet(
        list: List<GraphDataModel>,
        yAxisRange: List<Pair<Int, String>>,
        avgValue: Pair<Float, String>?,
        selectedPosition: Int,
        contributorType: SleepInternalLaunchState?,
        optimalRange: Pair<Float, Float>?,
        nonNullDataCount: Int
    ) {
        dataPosition.clear()
        dataSet.clear()
        dataSet.addAll(list)
        mSelectedPosition = selectedPosition
        this.contributorType = contributorType
        this.optimalRange = optimalRange
        this.nonNullDataCount = nonNullDataCount


        this.yAxisRange.clear()
        this.yAxisRange.addAll(yAxisRange)
        mAverage = avgValue
        mMax = yAxisRange.last().first
        mMin = yAxisRange.first().first

        invalidate()
    }


    var startX: Float? = null
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val parent = parent
        parent.requestDisallowInterceptTouchEvent(true)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                startX = event.x

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


}

interface SleepSingleBarAction {
    fun onValueSelected(
        position: Int,
    )

    fun isInteractionOnGoing(onGoing: Boolean)
}