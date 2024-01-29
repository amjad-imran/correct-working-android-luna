package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.noisefit_commans.utils.LOGS

class CompareChart(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val inactivePaint = Paint()
    private val barPaint = Paint()
    private val lineWidth: Float // Spacing between lines in pixels
    private val lineSpacing: Float // Spacing between lines in pixels

    var dataList = ArrayList<Int>()

    var viewWidth = 0
    var viewHeight = 0
    var barBottomMax = 0
    var barCenter = 0
    var drawType: Int = 0

    init {

        viewHeight = dpToPx(30).toInt()

        inactivePaint.color = Color.parseColor("#19ffffff")
        inactivePaint.alpha = 50
        inactivePaint.isAntiAlias = true

        barPaint.color = Color.parseColor("#ffffff")
        barPaint.isAntiAlias = true

        lineWidth = resources.displayMetrics.density * 2 // 2dp spacing
        viewWidth = resources.displayMetrics.widthPixels
        lineSpacing = resources.displayMetrics.density * 2 // 2dp spacing
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var x = 0f
        dataList.forEachIndexed { index, it ->
            var barHalfHeight = 0f
            val barPaints: Paint
            val end = x + lineWidth
            if (it == drawType) {
                barHalfHeight = dpToPx(10)
                barPaints = barPaint
            } else {
                barHalfHeight = dpToPx(10)
                barPaints = inactivePaint
            }

            val rectf = RectF(
                x,
                barCenter - barHalfHeight,
                end,
                barCenter + barHalfHeight,
            )
            canvas.drawRoundRect(rectf, dpToPx(2), dpToPx(2), barPaints)

            x = end + lineSpacing
        }

    }


    fun setData(list: List<Int>) {
        LOGS.w("MovementChart data set ${list.size}")
        dataList.clear()
        dataList.addAll(list)
        invalidate()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

//        val width = (dataList.size * lineWidth) + (dataList.size * lineSpacing)
        val width = viewWidth
        setMeasuredDimension(width.toInt(), viewHeight)

    }

    fun dpToPx(px: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    fun setDrawData(list: ArrayList<Int>, type: Int, colorCode: Int) {
        dataList.clear()
        dataList = list
        drawType = type
        barPaint.color = colorCode
        barPaint.isAntiAlias = true
        invalidate()
        requestLayout()
    }
}
