package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.noisefit_commans.utils.LOGS

class MovementChartAuto(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val xTextPaint = Paint()
    private val timePaint = Paint()
    private val inactivePaint = Paint()
    private val lowPaint = Paint()
    private val mediumPaint = Paint()
    private val highPaint = Paint()
    private val lineWidth: Float // Spacing between lines in pixels
    private val lineSpacing: Float // Spacing between lines in pixels
    private val numLines = 288

    var dataList = ArrayList<Int>()
    var workoutList = ArrayList<String?>()

    var viewWidth = 0
    var viewHeight = 0
    var barBottomMax = 0
    var barCenter = 0

    init {

        viewHeight = dpToPx(130).toInt()


        val fontGilroy = ResourcesCompat.getFont(context, com.noisefit_commans.R.font.gilroy_medium)
        xTextPaint.textSize = dpToPx(10)
        xTextPaint.color = Color.parseColor("#ffffff")
        xTextPaint.alpha = 160
        xTextPaint.typeface = fontGilroy
        xTextPaint.isAntiAlias = true


        timePaint.color = Color.parseColor("#ffffff")
        timePaint.alpha = 20
        timePaint.isAntiAlias = true
        inactivePaint.color = Color.parseColor("#ffffff")
        inactivePaint.alpha = 30
        inactivePaint.isAntiAlias = true
        lowPaint.color = Color.parseColor("#2d525b")
        lowPaint.isAntiAlias = true
        mediumPaint.color = Color.parseColor("#8ed3f1")
        mediumPaint.isAntiAlias = true
        highPaint.color = Color.parseColor("#ffffff")
        highPaint.isAntiAlias = true

        lineWidth = resources.displayMetrics.density * 2 // 2dp spacing
        lineSpacing = resources.displayMetrics.density * 2 // 2dp spacing
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        barBottomMax = height - dpToPx(30).toInt()
        barCenter = barBottomMax / 2

        var x = 0f


        dataList.forEachIndexed { index, it ->
            val end = x + lineWidth
            var barHalfHeight = 0f
            var barPaint = inactivePaint
            when (it) {
                0 -> {//inactive
                    barHalfHeight = dpToPx(6)
                    barPaint = inactivePaint
                }

                1 -> {//low
                    barHalfHeight = dpToPx(16)
                    barPaint = lowPaint
                }

                2 -> {//medium
                    barHalfHeight = dpToPx(33)
                    barPaint = mediumPaint
                }

                3 -> {//high
                    barHalfHeight = dpToPx(50)
                    barPaint = highPaint

                }

                else -> {// treat as inactive
                    barHalfHeight = dpToPx(6)
                    barPaint = inactivePaint
                }
            }

            setTime(canvas, index, x)

            /*canvas.drawRect(
                x,
                barCenter - barHalfHeight,
                end,
                barCenter + barHalfHeight,
                barPaint
            )*/


            val rectf = RectF(
                x,
                barCenter - barHalfHeight,
                end,
                barCenter + barHalfHeight,
            )


            if (!workoutList[index].isNullOrEmpty()) {
                canvas.drawText(workoutList[index]!!, x, barCenter - barHalfHeight - dpToPx(16), xTextPaint)
            }

            canvas.drawRoundRect(rectf, dpToPx(2), dpToPx(2), barPaint)

            x = end + lineSpacing
        }

    }

    private fun setTime(canvas: Canvas, position: Int, currentXPos: Float) {
        val value = when (position) {
            0 -> "12 am"
            15 -> "4 am"
            31 -> "8 am"
            47 -> "12 pm"
            63 -> "4 pm"
            79 -> "8 pm"
            95 -> "12 am"
            else -> null
        }
        if (!value.isNullOrEmpty()) {

            canvas.drawRect(
                currentXPos,
                0f,
                currentXPos + dpToPx(1),
                barBottomMax.toFloat(),
                timePaint
            )

            when (position) {
                0 -> {
                    canvas.drawText(value, 0f, barBottomMax.toFloat() + dpToPx(16), xTextPaint)
                }

                (dataList.size - 1) -> {
                    val textWidth = xTextPaint.measureText(value)

                    canvas.drawText(
                        value,
                        currentXPos - textWidth,
                        barBottomMax.toFloat() + dpToPx(16),
                        xTextPaint
                    )
                }

                else -> {
                    val textWidth = xTextPaint.measureText(value)

                    canvas.drawText(
                        value,
                        currentXPos - textWidth / 2,
                        barBottomMax.toFloat() + dpToPx(16),
                        xTextPaint
                    )
                }
            }
        }
    }


    fun setData(list: List<Int>, workoutList: List<String?>) {
        LOGS.w("MovementChart data set ${list.size}")
        dataList.clear()
        dataList.addAll(list)

        this.workoutList.clear()
        this.workoutList.addAll(workoutList)

        invalidate()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = (dataList.size * lineWidth) + (dataList.size * lineSpacing)
        setMeasuredDimension(width.toInt(), viewHeight)

    }

    fun dpToPx(px: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }
}
