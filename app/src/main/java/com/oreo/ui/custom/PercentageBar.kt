package com.oreo.ui.custom


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS

class PercentageBar : View {
    private val intervalCount = 10
    private var lineColor = 0
    private var percentageColor = 0
    private var unitWidthLength = 0
    private var rectF: RectF? = null
    private var cornerLine = dp2px(10f).toFloat()
    private var cornerProgressLine = dp2px(2f).toFloat()
    private var width = 0
    private val linePaint = Paint()
    private val percentagePaint = Paint()
    private var dataList = ArrayList<Int>()
    private var isHighlighted = false

    constructor(context: Context?) : super(context) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    fun setPercentageColor1(percentageColor: Int) {
        this.percentageColor = percentageColor
    }

    private fun initPaint() {
        linePaint.apply {
            color = lineColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        percentagePaint.apply {
            color = percentageColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        rectF = RectF()
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PercentageBar)
        lineColor = ta.getColor(R.styleable.PercentageBar_lineColor, Color.GRAY)
        percentageColor = ta.getColor(R.styleable.PercentageBar_percentageColor, Color.RED)
//        interval = ta.getDimension(R.styleable.PercentageBar_interval, dp2px(2f).toFloat())
//        max = ta.getInt(R.styleable.PercentageBar_max, 100)
//        percentage = ta.getInt(R.styleable.PercentageBar_percentage, 50)
//        lineWidth = ta.getDimension(R.styleable.PercentageBar_lineWidth, dp2px(2f).toFloat())
        ta.recycle()
        initPaint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBgLines(canvas)
        drawProgress(canvas)

    }

    private fun drawBgLines(canvas: Canvas?) {
        unitWidthLength = ((width - 10) / intervalCount)

        for (i in 0..intervalCount) {
            rectF!!.left = (i * unitWidthLength).toFloat()
            rectF!!.top = 0f
            rectF!!.right = rectF!!.left + (unitWidthLength) / 4
            rectF!!.bottom = height.toFloat()
            canvas?.drawRoundRect(rectF!!, cornerLine, cornerLine, linePaint)
        }
    }


    fun updateData(dataList: ArrayList<Int>, isHighlighted: Boolean, color: Int) {
        this.dataList = dataList
        this.isHighlighted = isHighlighted
        this.percentageColor = color
        invalidate()
    }


    private fun drawProgress(canvas: Canvas?) {
//        if (this.dataList.isEmpty()) {
//            return
//        }



        val data = getData()


        var right = 0f
        var left = 0f

        ///0,0,0,1,0,0,1,0
        for (i in 0 until data.size) {
            val next = data.getOrNull(i + 1) ?: 0
            val current = data.getOrNull(i) ?: 0
            LOGS.d("sadsdadsasdsad index $current ")
            rectF!!.top = 10f
            rectF!!.bottom = height.toFloat() - 10f
            if (current == 0) {
//                if (!hasLeft) {
//                    hasLeft = false
                left = (i * unitWidthLength).toFloat()
                LOGS.d("sadsdadsasdsad index $i ---> left zero $left")

//                }
                continue
            }

            if (next == 0) {
                left = (i * unitWidthLength).toFloat()
                if (right == 0f) {
                    right = left + unitWidthLength
                } else {
                    right += left
                }
                rectF!!.left = left
                rectF!!.right = right
                canvas?.drawRoundRect(
                    rectF!!,
                    cornerProgressLine,
                    cornerProgressLine,
                    percentagePaint
                )
                right = 0f
            } else {
                right += unitWidthLength
            }


//            if()
//            if (next == 0) {
//                rectF!!.left = (left)
//                rectF!!.right = (right + unitWidthLength)
//                canvas?.drawRoundRect(
//                    rectF!!,
//                    cornerProgressLine,
//                    cornerProgressLine,
//                    percentagePaint
//                )
//                left = 0f
//                hasLeft = false
//                LOGS.d("sadsdadsasdsad index $i ---> draw $width --> $unitWidthLength ===> ${rectF!!.left} --> ${rectF!!.right} ")
//            } else {
//                if (!hasLeft) {
//                    hasLeft = true
//                    left = (i * unitWidthLength).toFloat()
//                    LOGS.d("sadsdadsasdsad index $i ---> left zero $left")
//
//                } else {
//                    right = (i * unitWidthLength).toFloat()
//                    LOGS.d("sadsdadsasdsad index $i ---> ignore left zero $right")
//                }
//            }


//            canvas?.drawRoundRect(rectF!!, cornerLine, cornerLine, percentagePaint)
        }


    }

    private fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun getData(): ArrayList<Int> {
        val data = ArrayList<Int>()
//        for (i in 0..2) {
//            data.add((1..10).random())
//        }
        data.add(0)
        data.add(0)
        data.add(0)
        data.add(1)
        data.add(0)
        data.add(1)
        data.add(0)
//        data.add(1)
//        data.add(1)
//        data.add(1)
//        data.add(0)
        return data
    }

}

//class PercentageBar : View {
//    private var linePaint: Paint? = null
//    private var width= 0
//    var lineColor = 0
////    var percentageColor = 0
////    var interval = 0f
////    var max = 0
////    var percentage = 0
////    var lineWidth = dp2px(1f).toFloat()
////    var cornerLine = dp2px(10f).toFloat()
////    var cornerPercentage = dp2px(2f).toFloat()
////    private var intervalReal = 0f
////    private var percentageIndex = 0f
////    private var count = 0
//    private var rectF: RectF? = null
//
//    constructor(context: Context?) : super(context) {
//        initPaint()
//    }
//
//    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
//        init(attrs)
//    }
//
//    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
//        context,
//        attrs,
//        defStyleAttr
//    ) {
//        init(attrs)
//    }
//
//    constructor(
//        context: Context?,
//        attrs: AttributeSet?,
//        defStyleAttr: Int,
//        defStyleRes: Int
//    ) : super(context, attrs, defStyleAttr, defStyleRes) {
//        init(attrs)
//    }
//
//    private fun init(attrs: AttributeSet?) {
//        val ta = context.obtainStyledAttributes(attrs, R.styleable.PercentageBar)
//        lineColor = ta.getColor(R.styleable.PercentageBar_lineColor, Color.GRAY)
////        percentageColor = ta.getColor(R.styleable.PercentageBar_percentageColor, Color.RED)
////        interval = ta.getDimension(R.styleable.PercentageBar_interval, dp2px(2f).toFloat())
////        max = ta.getInt(R.styleable.PercentageBar_max, 100)
////        percentage = ta.getInt(R.styleable.PercentageBar_percentage, 50)
////        lineWidth = ta.getDimension(R.styleable.PercentageBar_lineWidth, dp2px(2f).toFloat())
//        ta.recycle()
//        initPaint()
//    }
//
//    private fun initPaint() {
//        linePaint = Paint()
//        linePaint!!.color = lineColor
//        linePaint!!.style = Paint.Style.FILL
////        linePaint!!.strokeWidth = lineWidth
//        linePaint!!.isAntiAlias = true
//        rectF = RectF()
//    }
//
//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        width = w
////        count = Math.ceil((width / interval / 2).toDouble()).toInt()
////        intervalReal = width * 1f / count
////        percentageIndex = Math.ceil((count * (percentage * 1f / max)).toDouble()).toInt().toFloat()
////        Log.d("onSizeChanged", +count.toString() + "  " + intervalReal + " " + percentageIndex)
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        drawContent(canvas)
//    }
//
//    private fun drawContent(canvas: Canvas) {
//        Log.d("5555", "drawContent$count  $intervalReal")
//        linePaint!!.color = lineColor
//
//        val totalIntervalWidth = (intervalWidth + intervalSpacing) * intervalCount - intervalSpacing
//        val startX = (width - totalIntervalWidth) / 2
//
//        for (i in 0 until intervalCount) {
//            val left = startX + i * (intervalWidth + intervalSpacing)
//            val right = left + intervalWidth
//            val top = (height - intervalHeight) / 2
//            val bottom = top + intervalHeight
//
//            canvas?.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), linePaint)
//        }
////        for (i in 0 until count) {
////            if (i > percentageIndex) {
////                rectF!!.left = i * intervalReal
////                rectF!!.top = 0f
////                rectF!!.right = rectF!!.left + lineWidth
////                rectF!!.bottom = height.toFloat()
////                canvas.drawRoundRect(rectF!!, cornerLine, cornerLine, linePaint!!)
////            }
////        }
////        linePaint!!.color = percentageColor
////        rectF!!.left = 0f
////        rectF!!.top = 0f
////        rectF!!.right = percentageIndex * intervalReal + lineWidth
////        rectF!!.bottom = height.toFloat()
////        canvas.drawRoundRect(rectF!!, cornerPercentage, cornerPercentage, linePaint!!)
//    }
//
//    private fun dp2px(dpValue: Float): Int {
//        val scale = context.resources.displayMetrics.density
//        return (dpValue * scale + 0.5f).toInt()
//    }
//}
