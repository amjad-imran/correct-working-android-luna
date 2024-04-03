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
    private var unitWidthLength = 0
    private val intervalHeight = 20 // Height of each interval in pixels
    private var rectF: RectF? = null
    private var cornerLine = dp2px(10f).toFloat()
    private var width = 0
    private val linePaint = Paint()

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

    private fun initPaint() {
        linePaint.color = lineColor
        linePaint.style = Paint.Style.FILL
        linePaint.isAntiAlias = true
        rectF = RectF()
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PercentageBar)
        lineColor = ta.getColor(R.styleable.PercentageBar_lineColor, Color.GRAY)
//        percentageColor = ta.getColor(R.styleable.PercentageBar_percentageColor, Color.RED)
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


        unitWidthLength =
            ((width) / intervalCount)



        for (i in 0 until intervalCount) {

//            rectF!!.left = i * intervalReal
////                rectF!!.top = 0f
////                rectF!!.right = rectF!!.left + lineWidth
//                rectF!!.bottom = height.toFloat()
//            val left = i * unitWidthLength
//            val right = .1
//            val top = 0
//            val bottom = intervalHeight

            LOGS.d("sadsdadsasdsad $left --> $right --> ${top} ---> $bottom")
            rectF!!.left = (i * unitWidthLength).toFloat()
            rectF!!.top = 0f
            rectF!!.right = rectF!!.left + unitWidthLength
            rectF!!.bottom = height.toFloat()
//            canvas?.drawRect(
//                left.toFloat(),
//                top.toFloat(),
//                right.toFloat(),
//                bottom.toFloat(),
//                linePaint
//            )
            canvas?.drawRoundRect(rectF!!, cornerLine, cornerLine, linePaint)
        }
    }

    private fun calculateIntervalWidth(screenWidth: Int): Int {
        // Adjust this formula as per your requirements
        return (screenWidth * 0.6 / intervalCount).toInt() // 60% of screen width divided by interval count
    }

    private fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
//    private fun calculateTotalIntervalWidth(screenWidth: Int): Int {
//        return (intervalCount * (calculateIntervalWidth(screenWidth) + intervalSpacing) - intervalSpacing)
//    }
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
