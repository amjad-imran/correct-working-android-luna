package com.oreo.ui.custom.sleep

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.noisefit.luna.R
import com.noisefit.util.ApplicationUtils.getFormattedSleepDuration
import com.noisefit_commans.utils.LOGS


class SleepHourVsNeedChart constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint
    lateinit var xLinePaint: Paint
    lateinit var gridLinePaint: Paint
    lateinit var circlePaint: Paint
    lateinit var selectedDayPaint: Paint
    lateinit var textPaintHour: Paint
    lateinit var textPaintNeed: Paint

    lateinit var linePaintHour: Paint
    lateinit var linePaintNeed: Paint


    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0

    var mMax = 0


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

        textPaintHour = Paint().apply {
            this.color = Color.parseColor("#96ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }
        textPaintNeed = Paint().apply {
            this.color = Color.parseColor("#ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(12f).toFloat()
        }

        linePaintHour = Paint().apply {
            this.color = Color.parseColor("#465c8a")
            strokeWidth = dip2px(2f).toFloat()
        }
        linePaintNeed = Paint().apply {
            this.color = Color.parseColor("#7858cc")
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
        drawContent(canvas)
    }

    private fun drawContent(canvas: Canvas) {

        val stepWidth = width / 7
        var start = 0f
        val circleRadius = dip2px(2f).toFloat()
        val paddingHorizontal = dip2px(4f)
        val textPadding = dip2px(6f)
        val maxDataSize = dataSet.size

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


            if (it.first != null) {
                val actualPos = getYAxisValue(it.first ?: 0)

                if (index + 1 < maxDataSize && dataSet[index + 1].first != null) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement.first ?: 0)
                    canvas.drawLine(
                        start + stepWidth / 2,
                        actualPos,
                        start + stepWidth + stepWidth / 2,
                        actualPosNext,
                        linePaintHour
                    )
                }



                canvas.drawCircle(
                    start + stepWidth / 2,
                    actualPos,
                    circleRadius,
                    circlePaint
                )


                val (hour, minute) = getFormattedSleepDuration(it.first ?: 0)

                val text = String.format("%d:%02d", hour, minute)
                val xTextBounds = Rect()
                textPaintHour.getTextBounds(text, 0, text.length, xTextBounds)
                val textStart = start + stepWidth / 2 - xTextBounds.width() / 2
                canvas.drawText(
                    text,
                    textStart,
                    actualPos + xTextBounds.height() + textPadding,
                    textPaintHour
                )
            }

            if (it.second != null) {
                val needPos = getYAxisValue(it.second ?: 0)

                if (index + 1 < maxDataSize && dataSet[index + 1].second != null) {
                    val nextElement = dataSet[index + 1]

                    val actualPosNext = getYAxisValue(nextElement.second ?: 0)
                    canvas.drawLine(
                        start + stepWidth / 2,
                        needPos,
                        start + stepWidth + stepWidth / 2,
                        actualPosNext,
                        linePaintNeed
                    )
                }

                canvas.drawCircle(
                    start + stepWidth / 2,
                    needPos,
                    circleRadius,
                    circlePaint
                )

                val (hour, minute) = getFormattedSleepDuration(it.second ?: 0)

                val text = String.format("%d:%02d", hour, minute)
                val xTextBounds = Rect()
                textPaintNeed.getTextBounds(text, 0, text.length, xTextBounds)
                val textStart = start + stepWidth / 2 - xTextBounds.width() / 2
                canvas.drawText(
                    text,
                    textStart,
                    needPos - textPadding,
                    textPaintNeed
                )
            }
            start += stepWidth

        }

    }

    private fun getYAxisValue(value: Int): Float {
        val percent = (value.toFloat() / mMax.toFloat()) * 100
        val availableHeight = height - bottomHeight - topHeight
        return topHeight + availableHeight - (availableHeight * percent / 100)
    }

    private fun drawBackGrid(canvas: Canvas) {

        val stepWidth = width / 7
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

        canvas.drawLine(0f, getYAxisValue(0), width.toFloat(), getYAxisValue(0), xLinePaint)

        gridLinePaint.strokeWidth = dip2px(1f).toFloat()

        val heightStep = mMax / 4
        var heightStart = heightStep
        for (i in 1 until 5) {
            canvas.drawLine(
                0f,
                getYAxisValue(heightStart),
                width.toFloat(),
                getYAxisValue(heightStart),
                gridLinePaint
            )
            if (i == 4) {
                gridLinePaint.strokeWidth = dip2px(2f).toFloat()
                canvas.drawLine(
                    0f,
                    getYAxisValue(heightStart),
                    width.toFloat(),
                    getYAxisValue(heightStart),
                    gridLinePaint
                )
            }

            heightStart += heightStep
        }


    }

    private fun drawXAxis(canvas: Canvas) {

        val stepWidth = width / 7

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

    /**
     * array list of values -> Pair(actual sleep minutes, need minutes)
     * selected position
     */
    fun setDataSet(list: List<Pair<Int?, Int?>>, selectedPosition: Int) {
        dataSet.clear()
        dataSet.addAll(list)
        mSelectedPosition = selectedPosition

        mMax = 0
        list.forEach {

            var max = it.first ?: 0
            if ((it.second ?: 0) > max) {
                max = it.second ?: 0
            }

            if (max > mMax) {
                mMax = max
            }
        }

        mMax += ((0.2) * mMax).toInt()

        invalidate()
    }


}