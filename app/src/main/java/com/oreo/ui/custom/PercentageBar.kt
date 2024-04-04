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
    private var unitWidthLength = 0.0f
    private var rectF: RectF? = null
    private var cornerLine = dp2px(10f).toFloat()
    private var cornerProgressLine = dp2px(2f).toFloat()
    private var width = 0
    private val linePaint = Paint()
    private val percentagePaint = Paint()
    private var dataList = ArrayList<Int>()
    private var dataSize = 0
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
        initPaint()
        drawBgLines(canvas)
        drawProgress(canvas)

    }

    private fun drawBgLines(canvas: Canvas?) {
        unitWidthLength = (width.toFloat() / (dataSize - 1))

        val dummyUnitLength = width / 100
        for (i in 0..200 step 2) {
            rectF!!.left = (i * dummyUnitLength).toFloat()
            rectF!!.top = 0f
            rectF!!.right = rectF!!.left + (dummyUnitLength)
            rectF!!.bottom = height.toFloat()
            canvas?.drawRoundRect(rectF!!, cornerLine, cornerLine, linePaint)
        }
    }


    fun updateData(
        dataList: List<Int>,
        dataSize: Int,
        color: Int,
        isHighlighted: Boolean
    ) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
        this.dataSize = dataSize

        this.isHighlighted = isHighlighted
        this.percentageColor = color
        invalidate()
    }


    private fun drawProgress(canvas: Canvas?) {
        val data = getData()

        rectF!!.top = 0f
        rectF!!.bottom = height.toFloat()

        var left = 0f
        var hasLeft = false
        val halfUnitWidth = unitWidthLength / 2
        for (i in 0 until data.size) {
            val next = data.getOrNull(i + 1) ?: 0
            val current = data.getOrNull(i) ?: 0
            if (current == 1) {
                if (!hasLeft) {
                    left = if (i == 0) {
                        0f
                    } else {
                        (i * unitWidthLength) - unitWidthLength
                    }
                    hasLeft = true
                }
                if (i == dataSize - 1 || next == 0) {
                    rectF!!.left = left
                    rectF!!.right = if (i == 0) {
                        halfUnitWidth
                    } else {
                        (i * unitWidthLength)
                    }
                    canvas?.drawRoundRect(
                        rectF!!,
                        cornerProgressLine,
                        cornerProgressLine,
                        percentagePaint
                    )
                    hasLeft = false
                }
            }
        }


    }

    private fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun getData(): ArrayList<Int> {
        val data = ArrayList<Int>()
        if (isHighlighted) {
            for (i in 0 until dataSize) {
                if (dataList.contains(i)) {
                    data.add(1)
                } else {
                    data.add(0)
                }
            }
        } else {
            for (i in 0 until dataSize) {
                if (i < dataList.size) {
                    data.add(1)
                } else {
                    data.add(0)
                }
            }
        }

        return data
    }
}
