package com.oreo.ui.custom.sleep

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat


class SleepPerformanceChart constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    lateinit var xAxisPaint: Paint


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


        /*
                backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                backgroundPaint.setColor(Color.parseColor("#19ffffff"))
                backgroundPaint.style = Paint.Style.STROKE
                backgroundPaint.strokeWidth = strokeWidth
                backgroundPaint.strokeCap = Paint.Cap.ROUND

                progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                //progressPaint.setColor(-0x9dff12)
                progressPaint.style = Paint.Style.STROKE
                progressPaint.strokeWidth = strokeWidth
                progressPaint.strokeCap = Paint.Cap.ROUND*/


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)


    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackGrid(canvas)
        drawXAxis(canvas)
        drawContent(canvas)
    }

    private fun drawContent(canvas: Canvas) {

    }

    private fun drawBackGrid(canvas: Canvas) {

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


}