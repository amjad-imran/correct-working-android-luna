package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.LOGS

class CompareChart(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val inactivePaint = Paint()
    private val barPaint = Paint()
    private var lineWidth: Float = 0f // Spacing between lines in pixels

    var dataList = ArrayList<Int>()

    var drawType: Int = 0

    init {


        inactivePaint.color = Color.parseColor("#19ffffff")
        //inactivePaint.alpha = 50
        inactivePaint.isAntiAlias = true

        barPaint.color = Color.parseColor("#ffffff")
        barPaint.isAntiAlias = true


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lineWidth = width.toFloat() / (dataList.size * 2)

        var x = 0f
        var barPaints: Paint?=null

        dataList.forEachIndexed { index, it ->
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
            barPaints?.let {
                canvas.drawRoundRect(rectf, dpToPx(2), dpToPx(2), it)
            }

            x = end + lineWidth
        }
    }

    fun dpToPx(px: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    fun updateInitData(type: Int, colorCode: Int){
        drawType = type
        barPaint.color = colorCode
    }

    fun setDrawData(list: List<Int>, isSelectedMode: Boolean = true) {
        if (!isSelectedMode) {
            val selectedList = list.filter {
                it == drawType
            }
            val capacity = 96 - selectedList.size

            val appendList = Array(capacity) { 255 }
            val mutableList = (selectedList as MutableList)
            mutableList.addAll(appendList)

            dataList.clear()
            dataList.addAll(mutableList)
        } else {
            dataList.clear()
            dataList.addAll(list)
        }

        barPaint.isAntiAlias = true
        invalidate()
        requestLayout()
    }
}
