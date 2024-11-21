package com.oreo.ui.custom.sleep.internal

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
import com.noisefit.luna.R
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.custom.sleep.SleepTimeModel
import com.oreo.ui.sleep2.internal.DEFAULT_LONG_PRESS_TIMEOUT


class SleepTimeChartInternal constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint
    lateinit var xLinePaint: Paint
    lateinit var gridLinePaint: Paint
    lateinit var barPaintDeep: Paint
    lateinit var selectedDayPaint: Paint
    lateinit var barPaintTop: Paint
    lateinit var barPaintTopInteracting: Paint
    lateinit var barPaintInteracting: Paint
    lateinit var barPaint: Paint


    lateinit var barTextPaint: Paint
    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0
    private val endPadding = dip2px(30f)


    var mMax = 0L
    var offset = 120L

    private var isInteracting = false
    private var vibrationUtils: VibrationUtils? = null
    private var listener: SleepSingleBarAction? = null
    private var touchX = 0f
    private val dataPosition = ArrayList<Pair<Int, Float>>()
    private var lastSentValuePos: Int? = null
    var dataStepWidth = 0F
    private var mSelectedPosition: Int? = null


    /**
     * Pair(deep,rem)
     */
    private val dataSet = ArrayList<SleepTimeModel>()
    private val yAxisRange = ArrayList<Pair<Int, String>>()

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
            this.color = Color.parseColor("#465c8a")
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


        dataStepWidth = availableWidth / 7

        val barWidth = dataStepWidth / 2
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
            listener?.onValueSelected(selectedPosition)
        }


        dataSet.forEachIndexed { index, it ->


            if (it.endTime != 0L) {

                val top = getYAxisValue((it.endTime + offset).toFloat())
                val bottom = getYAxisValue((it.startTime + offset).toFloat())
                val isSelectedPosition = selectedPosition == index

                dataPosition.add(Pair(index, start))


                val rectFRem = RectF(
                    start + barWidth / 2,
                    top + padding,
                    start + barWidth + barWidth / 2,
                    bottom
                )

                canvas.drawRoundRect(
                    rectFRem,
                    rectRadius,
                    rectRadius,
                    if (isInteracting && isSelectedPosition.not()) {
                        barPaintInteracting
                    } else barPaint
                )

                val rectTop = RectF().apply {
                    this.left = rectFRem.left
                    this.right = rectFRem.right
                    this.top = rectFRem.top
                    this.bottom = rectFRem.top + dip2px(2f)
                }

                val rectBottom = RectF().apply {
                    this.left = rectFRem.left
                    this.right = rectFRem.right
                    this.top = rectFRem.bottom - dip2px(2f)
                    this.bottom = rectFRem.bottom
                }

                canvas.drawRoundRect(
                    rectTop,
                    rectRadius,
                    rectRadius,
                    if (isInteracting && isSelectedPosition.not()) {
                        barPaintTopInteracting
                    } else barPaintTop
                )

                canvas.drawRoundRect(
                    rectBottom,
                    rectRadius,
                    rectRadius,
                    if (isInteracting && isSelectedPosition.not()) {
                        barPaintTopInteracting
                    } else barPaintTop
                )

                val textTop = it.endTimeString
                val xTextBounds = Rect()
                barTextPaint.getTextBounds(textTop, 0, textTop.length, xTextBounds)

                val textStart = start + dataStepWidth / 2 - xTextBounds.width() / 2
                canvas.drawText(textTop, textStart, top - xTextBounds.height(), barTextPaint)


                val textBottom = it.startTimeText
                barTextPaint.getTextBounds(textBottom, 0, textBottom.length, xTextBounds)
                val textStartBottom = start + dataStepWidth / 2 - xTextBounds.width() / 2
                canvas.drawText(
                    textBottom,
                    textStartBottom,
                    bottom + xTextBounds.height() * 2,
                    barTextPaint
                )

                if (isSelectedPosition && isInteracting) {

                    val center = rectTop.left + (rectTop.right - rectTop.left) / 2

                    canvas.drawRect(
                        RectF(
                            center - 2f,
                            topHeight.toFloat(),
                            center + 2f,
                            height - bottomHeight.toFloat()
                        ),
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

    private fun getYAxisValue(value: Float): Float {
        val percent = (value / mMax.toFloat()) * 100
        val availableHeight = height - bottomHeight - topHeight
        return topHeight + availableHeight - (availableHeight * percent / 100)
    }

    private fun drawBackGrid(canvas: Canvas) {

        val availableWidth = (width - endPadding).toFloat()

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

    private fun drawXAxis(canvas: Canvas) {
        val availableWidth = (width - endPadding).toFloat()

        val stepWidth = availableWidth / 7

        val days = arrayListOf(context.getString(R.string.text_mon),
            context.getString(R.string.text_tue),
            context.getString(R.string.text_wed),
            context.getString(R.string.text_thu),
            context.getString(R.string.text_fri),
            context.getString(R.string.text_sat),
            context.getString(R.string.text_sun))
        var start = 0.0f
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


    fun setDataSet(list: List<SleepTimeModel>, yAxisRange: List<Pair<Int, String>>) {
        dataSet.clear()
        dataSet.addAll(list)

        this.yAxisRange.clear()
        this.yAxisRange.addAll(yAxisRange)

        mMax = 0
        offset = 60 * 6L
        list.forEach {
            if (it.endTime > mMax) {
                mMax = it.endTime
            }
        }

        mMax = (mMax + offset * 2)


        invalidate()
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
                    getYAxisValue(value.first.toFloat() + offset),
                    xAxisPaint
                )
                canvas.drawLine(
                    0f,
                    getYAxisValue(value.first.toFloat() + offset),
                    availableWidth,
                    getYAxisValue(value.first.toFloat() + offset),
                    xLinePaint
                )
            } else if (index == yAxisRange.size - 1) {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)
                val yAxis = getYAxisValue(value.first.toFloat() + offset) + textBounds.height()

                if ((yAxis - textBounds.height()) > topHeight) {
                    canvas.drawText(
                        text,
                        width - textBounds.width().toFloat() - textPadding,
                        getYAxisValue(value.first.toFloat() + offset) + textBounds.height(),
                        xAxisPaint
                    )
                    gridLinePaint.strokeWidth = dip2px(2f).toFloat()

                    canvas.drawLine(
                        0f,
                        getYAxisValue(value.first.toFloat() + offset),
                        availableWidth,
                        getYAxisValue(value.first.toFloat() + offset),
                        gridLinePaint
                    )
                }


            } else {
                xAxisPaint.getTextBounds(text, 0, text.length, textBounds)

                val yAxis = getYAxisValue(value.first.toFloat() + offset) + textBounds.height() / 2
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
                        getYAxisValue(value.first.toFloat() + offset),
                        availableWidth,
                        getYAxisValue(value.first.toFloat() + offset),
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