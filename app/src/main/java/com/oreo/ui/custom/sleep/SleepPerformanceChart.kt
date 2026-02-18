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


class SleepPerformanceChart constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint
    lateinit var xLinePaint: Paint
    lateinit var gridLinePaint: Paint
    lateinit var barPaint: Paint
    lateinit var selectedDayPaint: Paint
    lateinit var barPaintTop: Paint
    lateinit var barTextPaint: Paint
    private val bottomHeight = dip2px(30f)
    private val topHeight = dip2px(20f)
    private var linearGradient: LinearGradient? = null
    private var mHeight = 0

    private val dataSet = ArrayList<Int?>()
    private var mSelectedPosition: Int? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        val fontGoogleSans =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.google_sans_flex_medium)

        xAxisPaint = Paint().apply {
            this.color = Color.parseColor("#40ffffff")
            this.typeface = fontGoogleSans
            this.textSize = dip2px(12f).toFloat()
        }

        barTextPaint = Paint().apply {
            this.color = Color.parseColor("#96ffffff")
            this.typeface = fontGoogleSans
            this.textSize = dip2px(12f).toFloat()
        }


        xLinePaint = Paint().apply {
            this.color = Color.parseColor("#20FFFFFF")
        }
        gridLinePaint = Paint().apply {
            this.color = Color.parseColor("#07ffffff")
        }
        barPaint = Paint().apply {
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
        drawContent(canvas)
    }

    private fun drawContent(canvas: Canvas) {

        val stepWidth = width / 7
        val barWidth = stepWidth / 2
        var start = 0f
        val rectRadius = dip2px(1f).toFloat()


        dataSet.forEachIndexed { index, it ->


            if (index + 1 == mSelectedPosition) {
                val padding = dip2px(4f)
                val rectFSelected = RectF(
                    start + padding,
                    topHeight.toFloat(),
                    start + stepWidth - padding,
                    height.toFloat() - bottomHeight
                )

                canvas.drawRect(
                    rectFSelected,
                    selectedDayPaint
                )

            }


            if (it != null) {

                val top = getYAxisValue(it)

                val rectF = RectF(
                    start + barWidth / 2,
                    top,
                    start + barWidth + barWidth / 2,
                    height.toFloat() - bottomHeight
                )


                canvas.drawRoundRect(
                    rectF,
                    rectRadius,
                    rectRadius,
                    barPaint
                )

                rectF.bottom = rectF.top + dip2px(2f)

                canvas.drawRoundRect(
                    rectF,
                    rectRadius,
                    rectRadius,
                    barPaintTop
                )

                val text = "$it%"
                val xTextBounds = Rect()
                barTextPaint.getTextBounds(text, 0, text.length, xTextBounds)
                val textStart = start + stepWidth / 2 - xTextBounds.width() / 2
                canvas.drawText(text, textStart, top - xTextBounds.height(), barTextPaint)
            }

            start += stepWidth

        }

    }

    fun getYAxisValue(value: Int): Float {
        val availableHeight = height - bottomHeight - topHeight
        return topHeight + availableHeight - (availableHeight * value.toFloat() / 100)
    }

    private fun drawBackGrid(canvas: Canvas) {
        canvas.drawLine(0f, getYAxisValue(0), width.toFloat(), getYAxisValue(0), xLinePaint)

        gridLinePaint.strokeWidth = dip2px(1f).toFloat()
        canvas.drawLine(0f, getYAxisValue(25), width.toFloat(), getYAxisValue(25), gridLinePaint)
        canvas.drawLine(0f, getYAxisValue(50), width.toFloat(), getYAxisValue(50), gridLinePaint)
        canvas.drawLine(0f, getYAxisValue(75), width.toFloat(), getYAxisValue(75), gridLinePaint)


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

        gridLinePaint.strokeWidth = dip2px(2f).toFloat()
        canvas.drawLine(0f, getYAxisValue(100), width.toFloat(), getYAxisValue(100), gridLinePaint)


    }

    private fun drawXAxis(canvas: Canvas) {

        val stepWidth = width / 7

        val days = arrayListOf(context.getString(R.string.text_mon),
            context.getString(R.string.text_tue),
            context.getString(R.string.text_wed),
            context.getString(R.string.text_thu),
            context.getString(R.string.text_fri),
            context.getString(R.string.text_sat),
            context.getString(R.string.text_sun))
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

    fun setDataSet(list: List<Int?>, selectedPosition: Int) {
        dataSet.clear()
        dataSet.addAll(list)
        mSelectedPosition = selectedPosition
        invalidate()
    }


}