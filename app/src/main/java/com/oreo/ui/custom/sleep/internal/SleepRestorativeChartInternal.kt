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


class SleepRestorativeChartInternal constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint
    lateinit var xLinePaint: Paint
    lateinit var gridLinePaint: Paint
    lateinit var barPaintDeep: Paint
    lateinit var barPaintDeepI: Paint
    lateinit var barPaintRem: Paint
    lateinit var barPaintRemI: Paint
    lateinit var selectedDayPaint: Paint
    lateinit var barPaintTop: Paint
    lateinit var barTextPaint: Paint
    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0

    var mMax = 0

    private var isInteracting = false
    private var vibrationUtils: VibrationUtils? = null
    private var listener: SleepSingleBarAction? = null
    private var touchX = 0f
    private val endPadding = dip2px(30f)


    //HashMap<Position,Pair<StartX,EndX>>
    private val dataPosition = HashMap<Int, Pair<Float, Float>>()
    private var lastSentValuePos: Int? = null
    private val yAxisRange = ArrayList<Pair<Int, String>>()


    /**
     * Pair(deep,rem)
     */
    private val dataSet = ArrayList<Pair<Int?, Int?>>()
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

        barTextPaint = Paint().apply {
            this.color = Color.parseColor("#96ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }


        xLinePaint = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
        }
        gridLinePaint = Paint().apply {
            this.color = Color.parseColor("#07ffffff")
        }
        barPaintDeep = Paint().apply {
            this.color = Color.parseColor("#c3a3e3")
        }
        barPaintDeepI = Paint().apply {
            this.color = Color.parseColor("#66c3a3e3")
        }
        barPaintRem = Paint().apply {
            this.color = Color.parseColor("#7858cc")
        }
        barPaintRemI = Paint().apply {
            this.color = Color.parseColor("#667858cc")
        }
        barPaintTop = Paint().apply {
            this.color = Color.parseColor("#ffffff")
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
        val stepWidth = availableWidth / 7

        val barWidth = stepWidth / 2
        var start = 0f
        val rectRadius = dip2px(1f).toFloat()
        val padding = dip2px(1f).toFloat()
        val paddingHorizontal = dip2px(4f)

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


            if (index + 1 == mSelectedPosition) {
                val rectFSelected = RectF(
                    start + paddingHorizontal,
                    topHeight.toFloat(),
                    start + stepWidth - paddingHorizontal,
                    height.toFloat() - bottomHeight
                )

                canvas.drawRect(
                    rectFSelected,
                    selectedDayPaint
                )

            }


            val sum = (it.first ?: 0) + (it.second ?: 0)
            if (sum != 0) {

                val top = getYAxisValue(sum)
                val isSelectedPosition = selectedPosition == index


                val remEnd = getYAxisValue(it.second ?: 0)
                dataPosition[index] = Pair(start, start + stepWidth)


                var topRectF: RectF? = null
                if (it.second != 0) {
                    val rectFRem = RectF(
                        start + barWidth / 2,
                        remEnd + padding,
                        start + barWidth + barWidth / 2,
                        height.toFloat() - bottomHeight
                    )
                    topRectF = rectFRem


                    canvas.drawRoundRect(
                        rectFRem,
                        rectRadius,
                        rectRadius,
                        if (isInteracting && isSelectedPosition.not()) barPaintDeepI else barPaintDeep
                    )
                }

                if (it.first != 0) {
                    val rectFDeep = RectF(
                        start + barWidth / 2,
                        top,
                        start + barWidth + barWidth / 2,
                        remEnd - padding
                    )
                    topRectF = rectFDeep

                    canvas.drawRoundRect(
                        rectFDeep,
                        rectRadius,
                        rectRadius,
                        if (isInteracting && isSelectedPosition.not()) barPaintRemI else barPaintRem
                    )
                }

                if (isSelectedPosition && isInteracting && topRectF != null) {

                    val center = topRectF.left + (topRectF.right - topRectF.left) / 2

                    canvas.drawRect(
                        RectF(center - 2f, topHeight.toFloat(), center + 2f, topRectF.top),
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
                    val (hour, minute) = getFormattedSleepDuration(sum)

                    val text = String.format("%d:%02d", hour, minute)
                    val xTextBounds = Rect()
                    barTextPaint.getTextBounds(text, 0, text.length, xTextBounds)
                    val textStart = start + stepWidth / 2 - xTextBounds.width() / 2
                    canvas.drawText(text, textStart, top - xTextBounds.height(), barTextPaint)
                }
            }

            start += stepWidth

        }

    }

    private fun performHapticFeedbackCustom() {
        vibrationUtils?.vibrate(HAPTIC_VIBRATION)
    }

    private fun getSelectedPosition(): Int? {
        if (isInteracting.not()) return null
        val position = dataPosition.filterValues {
            touchX > it.first && touchX < it.second
        }
        if (position.isEmpty()) return null

        return position.keys.single()
    }

    private fun getYAxisValue(value: Int): Float {
        val percent = (value.toFloat() / mMax.toFloat()) * 100
        val availableHeight = height - bottomHeight - topHeight
        return topHeight + availableHeight - (availableHeight * percent / 100)
    }

    private fun drawBackGrid(canvas: Canvas) {
        val availableWidth = width.toFloat() - endPadding

        val stepWidth = availableWidth / 7
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

        /*

        val text0 = "0%"
        val text25 = "25%"
        val text50 = "50%"
        val text75 = "75%"
        val text100 = "100%"

        val textBounds = Rect()

        xAxisPaint.getTextBounds(text0, 0, text0.length, textBounds)
        canvas.drawText(text0, width - textBounds.width().toFloat(), getYAxisValue(0), xAxisPaint)

        xAxisPaint.getTextBounds(text25, 0, text25.length, textBounds)
        canvas.drawText(
            text25,
            width - textBounds.width().toFloat(),
            getYAxisValue(25) + textBounds.height() / 2,
            xAxisPaint
        )

        xAxisPaint.getTextBounds(text50, 0, text50.length, textBounds)
        canvas.drawText(
            text50,
            width - textBounds.width().toFloat(),
            getYAxisValue(50) + textBounds.height() / 2,
            xAxisPaint
        )

        xAxisPaint.getTextBounds(text75, 0, text75.length, textBounds)
        canvas.drawText(
            text75,
            width - textBounds.width().toFloat(),
            getYAxisValue(75) + textBounds.height() / 2,
            xAxisPaint
        )

        xAxisPaint.getTextBounds(text100, 0, text100.length, textBounds)
        canvas.drawText(
            text100,
            width - textBounds.width().toFloat(),
            getYAxisValue(100) + textBounds.height(),
            xAxisPaint
        )

        //canvas.drawText("0%", width - xTextPaint.measureText(text0), )
        canvas.drawLine(0f, getYAxisValue(0), availableWidth, getYAxisValue(0), xLinePaint)

        gridLinePaint.strokeWidth = dip2px(1f).toFloat()
        canvas.drawLine(0f, getYAxisValue(25), availableWidth, getYAxisValue(25), gridLinePaint)
        canvas.drawLine(0f, getYAxisValue(50), availableWidth, getYAxisValue(50), gridLinePaint)
        canvas.drawLine(0f, getYAxisValue(75), availableWidth, getYAxisValue(75), gridLinePaint)

        gridLinePaint.strokeWidth = dip2px(2f).toFloat()
        canvas.drawLine(0f, getYAxisValue(100), availableWidth, getYAxisValue(100), gridLinePaint)*/

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
     * array list of values -> Pair(deep minutes, rem minutes)
     * selected position
     */
    fun setDataSet(
        list: List<Pair<Int?, Int?>>,
        yAxisRange: List<Pair<Int, String>>,
        maxValue: Int,
        selectedPosition: Int
    ) {
        dataPosition.clear()
        dataSet.clear()
        dataSet.addAll(list)
        mSelectedPosition = selectedPosition

        this.yAxisRange.clear()
        this.yAxisRange.addAll(yAxisRange)

        mMax = maxValue

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

interface SleepMultiBarAction {
    fun onValueSelected(
        position: Int,
    )

    fun isInteractionOnGoing(onGoing: Boolean)
}