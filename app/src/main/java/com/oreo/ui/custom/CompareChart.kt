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
    private var lineWidth: Float = 0f // Spacing between lines in pixels

    var dataList = ArrayList<Int>()

    var drawType: Int = 0

    init {


        inactivePaint.color = Color.parseColor("#19ffffff")
        inactivePaint.alpha = 50
        inactivePaint.isAntiAlias = true

        barPaint.color = Color.parseColor("#ffffff")
        barPaint.isAntiAlias = true


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lineWidth = width.toFloat() / (dataList.size * 2)

        var x = 0f
        dataList.forEachIndexed { index, it ->
            val barPaints: Paint
            val end = x + lineWidth
            if (it == drawType) {
                barPaints = barPaint
            } else {
                barPaints = inactivePaint
            }

            val rectf = RectF(
                x,
                0f,
                end,
                height.toFloat(),
            )
            canvas.drawRoundRect(rectf, dpToPx(2), dpToPx(2), barPaints)

            x = end + lineWidth
        }

    }


    fun setData(list: List<Int>) {
        dataList.clear()
        dataList.addAll(list)
        invalidate()
        requestLayout()
    }

    fun dpToPx(px: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    fun setDrawData(list: List<Int>, type: Int, colorCode: Int) {
        dataList.clear()
        dataList.addAll(list)
        drawType = type
        barPaint.color = colorCode
        barPaint.isAntiAlias = true
        invalidate()
        requestLayout()
    }
}
