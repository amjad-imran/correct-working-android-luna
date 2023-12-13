package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.noisefit.luna.R
import com.noisefit_commans.utils.DistanceUtil.convertMeterToKm
import com.noisefit_commans.utils.LOGS.d
import com.oreo.data.model.ChartModel

class BarChartTemp : View {
    private var bgColor = 0
    private var isDistanceGraph = false
    private var bgLeftColor = 0
    private var bgRightColor = 0
    private var bgTopColor = 0
    private var bgBottomColor = 0
    private var xTextColor = 0
    private var chartLineColor = 0
    private var chartLineWidth = 10f
    private var gridColor = 0
    private var hCount = 0
    var max = 0
    private var xMin = 0
    private var leftWith = 0f
    private var rightWith = 0f
    private var bottomWith = 0f
    private var topWith = 0f
    private var xTextSize = 0f
    private var bgPaint: Paint? = null
    private var bgLeftPaint: Paint? = null
    private var bgRightPaint: Paint? = null
    private var bgTopPaint: Paint? = null
    private var bgTopSelectedPaint: Paint? = null
    private val selectedLinePath = Path()
    private var bgBottomPaint: Paint? = null
    private var showSelectedIndicator = true
    private var xTextPaint: Paint? = null
    private var gridPaint: Paint? = null
    private var baseAxisPaint: Paint? = null
    private var hAxisPaint: Paint? = null
    private var centerLinePaint: Paint? = null
    private var centerLineColor = 0
    private var lineNormalColor = 0
    private var lineSelectColor = 0
    private var centerLineWidth = 0f
    private var chartLinePaint: Paint? = null
    private var rectF: RectF? = null
    private var onChartScrollChangedListener: ScrollListener? = null

    //    private Path path = new Path();
    //    private Path fillPath = new Path();
    private var unitHLenth = 0f
    private var indicatorUnitLength = 0f
    private var showAvgValueText = false
    private var mWith = 0
    private var mHeight = 0
    private var offSet = 0f
    private var indicatorOffSet = 0f
    private var xTextBounds: Rect? = null
    private val list: MutableList<ChartModel>? = ArrayList()
    private var prefixCount = 0
    private var suffixCount = 0
    private var titleWidth = 0f
    private var maxValue = 0
    private var minValue = 0
    private var avgValue = 0

    constructor(context: Context?) : super(context) {
        initPaint()
        //        updateData();
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
        //        updateData();
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
        //        updateData();
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BarChart)
        bgColor = ta.getColor(R.styleable.BarChart_bgColor, -0x1)
        bgLeftColor = ta.getColor(R.styleable.BarChart_bgLeftColor, -0x1)
        bgRightColor = ta.getColor(R.styleable.BarChart_bgRightColor, 0xfffffff)
        bgTopColor = ta.getColor(R.styleable.BarChart_bgTopColor, -0x1)
        bgBottomColor = ta.getColor(R.styleable.BarChart_bgBottomColor, -0x1)
        xTextColor = ta.getColor(R.styleable.BarChart_xTextColor, -0x1000000)
        gridColor = ta.getColor(R.styleable.BarChart_gridColor, -0xff01)
        hCount = ta.getInt(R.styleable.BarChart_hCount, 5)
        max = ta.getInt(R.styleable.BarChart_xMax, 10)
        xMin = ta.getInt(R.styleable.BarChart_xMin, 0)
        xTextSize = ta.getDimension(R.styleable.BarChart_xTextSize, 8f)
        leftWith = ta.getDimension(R.styleable.BarChart_leftWith, 16f)
        rightWith = ta.getDimension(R.styleable.BarChart_rightWith, 8f)
        bottomWith = ta.getDimension(R.styleable.BarChart_bottomWith, 16f)
        topWith = ta.getDimension(R.styleable.BarChart_topWith, 8f)
        chartLineColor = ta.getColor(R.styleable.BarChart_chartLineColor, -0x1000000)
        chartLineWidth = ta.getDimension(R.styleable.BarChart_chartLineWidth, 10f)
        centerLineWidth = ta.getDimension(R.styleable.BarChart_centerLineWidth, 10f)
        centerLineColor = ta.getColor(R.styleable.BarChart_centerLineColor, -0x1000000)
        lineNormalColor = ta.getColor(R.styleable.BarChart_lineNormalColor, -0x7f000001)
        lineSelectColor = ta.getColor(R.styleable.BarChart_lineSelectColor, 0x00000000)
        titleWidth = ta.getDimension(R.styleable.BarChart_titleWidth, 0f)
        showSelectedIndicator = ta.getBoolean(R.styleable.BarChart_showSelectedIndicator, true)
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false)
        ta.recycle()
        initPaint()
    }

    private fun initPaint() {
        bgPaint = Paint()
        bgPaint?.color = bgColor
        bgLeftPaint = Paint()
        bgLeftPaint?.color = bgLeftColor
        bgRightPaint = Paint()
        bgRightPaint?.color = bgRightColor
        bgTopPaint = Paint()
        bgTopPaint?.color = bgTopColor
        bgTopSelectedPaint = Paint()
        bgBottomPaint = Paint()
        bgBottomPaint?.color = bgBottomColor
        xTextPaint = Paint()
        xTextPaint?.textSize = xTextSize
        xTextPaint?.isAntiAlias = true
        gridPaint = Paint()
        gridPaint?.color = gridColor
        baseAxisPaint = Paint().apply {
            color = Color.parseColor("#424951")
        }
        hAxisPaint = Paint().apply {
            color = Color.parseColor("#ffffff")
            alpha = 24
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
            style = Paint.Style.STROKE
        }


        centerLinePaint = Paint()
        centerLinePaint?.color = centerLineColor
        centerLinePaint?.alpha = 100
        centerLinePaint?.strokeWidth = centerLineWidth
        centerLinePaint?.style = Paint.Style.STROKE
        centerLinePaint?.pathEffect = DashPathEffect(floatArrayOf(5f, 10f), 0f)
        chartLinePaint = Paint()
        chartLinePaint?.strokeWidth = chartLineWidth
        chartLinePaint?.color = chartLineColor
        chartLinePaint?.isAntiAlias = true
        chartLinePaint?.style = Paint.Style.STROKE
        rectF = RectF()
        xTextBounds = Rect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawTop(canvas)
        drawBottom(canvas)
        drawContent(canvas)
        drawRight(canvas)
        drawLeft(canvas)
    }

    fun updateDataWithMax(
        datas: List<ChartModel>, prefixList: List<ChartModel>, suffixList: List<ChartModel>,
        xMax1: Int, lineNormalColor: Int, lineSelectColor: Int
    ) {
        list?.clear()
        list?.addAll(prefixList)
        list?.addAll(datas)
        list?.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size


//        int noneZeroValueCount = 0;
        max = 0
        var item: ChartModel
        var sum = 0
        var count = 0
        var noneZeroValueCount = 0
        for (i in datas.indices) {
            item = datas[i]
            if (item.value == 0) {
                continue
            }
            noneZeroValueCount += 1
            isDistanceGraph = datas[i].isDistanceGraph
            //            noneZeroValueCount += 1;
            if (max == 0) {
                max = item.value
            }
            if (item.value > max) {
                max = item.value
            }
            sum += item.value
            count += 1
        }
        if (noneZeroValueCount <= datas.size / 2) {
            avgValue = 0
        } else {
            if (count != 0) {
                avgValue = sum / count
            }
        }
        if (xMax1 == 100) {
            max = xMax1
        } else {
            val perOfMax = max * 20 / 100
            d("NonZeroValuesBarMax = $perOfMax")
            max += perOfMax
        }
        d("NonZeroValuesBarMax = " + max)
        this.lineNormalColor = lineNormalColor
        this.lineSelectColor = lineSelectColor
        postInvalidate()
    }

    fun setOnChartScrollChangedListener(listener: ScrollListener?) {
        onChartScrollChangedListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWith = w
        mHeight = h
        unitHLenth = (mWith - leftWith - rightWith) / hCount
        offSet = prefixCount * unitHLenth
        indicatorUnitLength = if (titleWidth == 0f) {
            mWith / 3f
        } else {
            titleWidth
        }
        indicatorOffSet = prefixCount * indicatorUnitLength
        selectedLinePath.moveTo(
            leftWith + (mWith - leftWith - rightWith) / 2f - 2 * unitHLenth,
            topWith
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f - unitHLenth / 5f,
            topWith
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f,
            topWith + unitHLenth / 5f
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f + unitHLenth / 5f,
            topWith
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f + 2 * unitHLenth,
            topWith
        )
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint!!)

        val centerY = ((mHeight - bottomWith) / 2)
        canvas.drawLine(
            0f,
            centerY,
            mWith.toFloat(),
            centerY,
            baseAxisPaint!!
        )
        val sectionHeight = centerY / 4

        canvas.drawLine(
            0f,
            sectionHeight * 1,
            mWith.toFloat(),
            sectionHeight * 1,
            hAxisPaint!!
        )
        canvas.drawLine(
            0f,
            sectionHeight * 2,
            mWith.toFloat(),
            sectionHeight * 2,
            hAxisPaint!!
        )
        canvas.drawLine(
            0f,
            sectionHeight * 3,
            mWith.toFloat(),
            sectionHeight * 3,
            hAxisPaint!!
        )
        canvas.drawLine(
            0f,
            centerY + sectionHeight * 1,
            mWith.toFloat(),
            centerY + sectionHeight * 1,
            hAxisPaint!!
        )
        canvas.drawLine(
            0f,
            centerY + sectionHeight * 2,
            mWith.toFloat(),
            centerY + sectionHeight * 2,
            hAxisPaint!!
        )
        canvas.drawLine(
            0f,
            centerY + sectionHeight * 3,
            mWith.toFloat(),
            centerY + sectionHeight * 3,
            hAxisPaint!!
        )


    }

    private fun drawTop(canvas: Canvas) {
        if (showSelectedIndicator) {
            canvas.drawRect(0f, 0f, mWith.toFloat(), topWith, bgTopPaint!!)
            bgTopSelectedPaint?.style = Paint.Style.FILL
            bgTopSelectedPaint?.color = bgTopColor
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint!!)
            bgTopSelectedPaint?.style = Paint.Style.STROKE
            bgTopSelectedPaint?.color = Color.WHITE
            bgTopSelectedPaint?.strokeWidth = dip2px(2f).toFloat()
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint!!)
        }
    }

    private fun drawRight(canvas: Canvas) {
        canvas.drawRect(mWith - rightWith, 0f, mWith.toFloat(), mHeight.toFloat(), bgRightPaint!!)
    }

    private fun drawBottom(canvas: Canvas) {
        canvas.drawRect(
            0f,
            mHeight - bottomWith,
            mWith.toFloat(),
            mHeight.toFloat(),
            bgBottomPaint!!
        )
        //        canvas.drawLine(leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith,
//                xLinePaint);
    }

    private fun drawLeft(canvas: Canvas) {
        canvas.drawRect(0f, 0f, leftWith, mHeight.toFloat(), bgLeftPaint!!)
        maxValue = max
        minValue = 0
        val maxStr: String
        val avgStr: String
        if (isDistanceGraph) {
            maxStr = convertMeterToKm(maxValue)
            avgStr = convertMeterToKm(avgValue)
        } else {
            maxStr = maxValue.toString()
            avgStr = avgValue.toString()
        }
        val minStr = minValue.toString()
        xTextPaint?.color = xTextColor and -0x7f000001
        val max = mHeight - bottomWith - max * (mHeight - topWith - bottomWith) / (max - xMin)
        canvas.drawLine(leftWith, max, mWith.toFloat(), max, gridPaint!!)
        xTextPaint?.getTextBounds(maxStr, 0, maxStr.length, xTextBounds)
        canvas.drawText(
            maxStr,
            mWith - rightWith - xTextBounds!!.width() + dip2px(10f),
            max + xTextBounds!!.height() / 2f + dip2px(10f),
            xTextPaint!!
        )
        val min = mHeight - bottomWith - 0 * (mHeight - topWith - bottomWith) / (this.max - xMin)
        canvas.drawLine(leftWith, min, mWith.toFloat(), min, gridPaint!!)
        xTextPaint?.getTextBounds(maxStr, 0, maxStr.length, xTextBounds)
        canvas.drawText(
            minStr,
            mWith - rightWith + dip2px(10f),
            min + xTextBounds!!.height() / 2f - dip2px(10f),
            xTextPaint!!
        )
        val avg =
            mHeight - bottomWith - avgValue * (mHeight - topWith - bottomWith) / (this.max - xMin)
        if (avgValue > 0) {
            canvas.drawLine(leftWith, avg, mWith.toFloat(), avg, centerLinePaint!!)
        }
        if (showAvgValueText && avgValue > 0) {
            xTextPaint?.getTextBounds(avgStr, 0, avgStr.length, xTextBounds)
            canvas.drawText(
                avgStr,
                leftWith + dip2px(5f),
                avg - xTextBounds!!.height(),
                xTextPaint!!
            )
        }
    }

    private fun drawContent(canvas: Canvas) {
//        float unitVLenth = (mHeight - topWith - bottomWith) / vCount;
//        drawGrid(canvas, unitVLenth);
        if (null == list || list.size <= 0) {
            return
        }
        //        float unitHLenth = (mWith - leftWith - rightWith) / hCount;
        var firstPosition = 0
        val tempOffset = offSet + moveOffSet
        if (tempOffset > (mWith - leftWith - rightWith) / 2 + unitHLenth) {
            firstPosition = ((tempOffset - (mWith - leftWith - rightWith) / 2) / unitHLenth).toInt()
        }
        val lastPosition = Math.min(firstPosition + hCount + 2, list.size)
        var current: ChartModel
        for (i in firstPosition until lastPosition) {
            current = list[i]
            val x =
                offSet + moveOffSet + (mWith - leftWith - rightWith) / 2 + leftWith - i * unitHLenth
            val y =
                mHeight - bottomWith - current.value * (mHeight - topWith - bottomWith) / (max - xMin)

            val centerY = ((mHeight - bottomWith) / 2)


            if (current.value > 0) {
                rectF?.left = x - chartLineWidth / 2f
                rectF?.top = y
                rectF?.right = x + chartLineWidth / 2f
                rectF?.bottom = mHeight - bottomWith - chartLineWidth / 2f
                chartLinePaint?.color = lineNormalColor
                canvas.drawRoundRect(
                    rectF!!,
                    chartLineWidth / 2f,
                    chartLineWidth / 2f,
                    chartLinePaint!!
                )
            }
            val xText = list[i].index ?: ""
            xTextPaint?.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint?.color = xTextColor and -0x7f000001
            canvas.drawText(
                xText,
                x - xTextBounds!!.width() / 2f,
                mHeight - bottomWith / 3,
                xTextPaint!!
            )
            if (showSelectedIndicator) {
                val title = list[i].date ?: ""
                val xInd =
                    indicatorOffSet + moveOffSet * list.size * indicatorUnitLength / (list.size * unitHLenth) + (mWith - leftWith - rightWith) / 2 + leftWith - i * indicatorUnitLength
                xTextPaint?.getTextBounds(title, 0, title.length, xTextBounds)
                canvas.drawText(
                    title,
                    xInd - xTextBounds!!.width() / 2f,
                    topWith / 2 + xTextBounds!!.height() / 2f,
                    xTextPaint!!
                )
            }
        }
        if (offSet + moveOffSet < 0 || offSet + moveOffSet > (list.size - 1) * unitHLenth) {
            return
        }
        val position = Math.round((offSet + moveOffSet) / unitHLenth)
        val x = (mWith - leftWith - rightWith) / 2 + leftWith
        val y: Float
        val temp = (offSet + moveOffSet) % unitHLenth
        val y1 =
            mHeight - bottomWith - list[position].value * (mHeight - topWith - bottomWith) / (max - xMin)
        y = if (moveOffSet == 0f || offSet + moveOffSet == (list.size - 1) * unitHLenth) {
            y1
        } else {
            val y2 =
                mHeight - bottomWith - list[position + 1].value * (mHeight - topWith - bottomWith) / (max - xMin)
            y1 + temp * (y2 - y1) / unitHLenth
        }
        //        canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
        if (moveOffSet == 0f) {
            val xText = list[position].index ?: ""
            xTextPaint?.color = xTextColor
            xTextPaint?.getTextBounds(xText, 0, xText.length, xTextBounds)
            canvas.drawText(
                xText,
                x - xTextBounds!!.width() / 2f,
                mHeight - bottomWith / 3,
                xTextPaint!!
            )
            if (showSelectedIndicator) {
                val title = list[position].date ?: ""
                val xInd =
                    indicatorOffSet + moveOffSet * list.size * indicatorUnitLength / (list.size * unitHLenth) + (mWith - leftWith - rightWith) / 2 + leftWith - position * indicatorUnitLength
                xTextPaint?.getTextBounds(title, 0, title!!.length, xTextBounds)
                canvas.drawText(
                    title,
                    xInd - xTextBounds!!.width() / 2f,
                    topWith / 2 + xTextBounds!!.height() / 2f,
                    xTextPaint!!
                )
            }
            if (list[position].value > 0) {
                rectF?.left = x - chartLineWidth / 2f
                rectF?.top = y
                rectF?.right = x + chartLineWidth / 2f
                rectF?.bottom = mHeight - bottomWith - chartLineWidth / 2f
                chartLinePaint?.color = lineSelectColor
                canvas.drawRoundRect(
                    rectF!!,
                    chartLineWidth / 2f,
                    chartLineWidth / 2f,
                    chartLinePaint!!
                )
            }
        }
    }

    private var xDown = 0f
    private var moveOffSet = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                xDown = event.x
            }

            MotionEvent.ACTION_MOVE -> {
                moveOffSet = event.x - xDown
                callBack(true)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                offSet += event.x - xDown
                resetData()
                setToUnit()
                callBack(true)
                invalidate()
            }

            else -> {}
        }
        return true
    }

    private var scrollPosition = -1
    private fun callBack(isSelected: Boolean) {
        if (null == onChartScrollChangedListener || null == list || list.size <= 0) {
            return
        }
        val unitH = (mWith - leftWith - rightWith) / hCount
        val tempPosition: Int
        tempPosition = if (offSet + moveOffSet >= (list.size - 1) * unitH) {
            list.size - 1
        } else if (offSet + moveOffSet < 0) {
            0
        } else {
            Math.round((offSet + moveOffSet) / unitH)
        }
        if (scrollPosition == tempPosition) {
            return
        }
        scrollPosition = tempPosition
        if (isSelected) {
            onChartScrollChangedListener?.onPositionSelected(scrollPosition, list[scrollPosition])
        } else {
            onChartScrollChangedListener?.onScrolling(scrollPosition, list[scrollPosition])
        }
    }

    private fun resetData() {
        xDown = 0f
        moveOffSet = 0f
    }

    private fun setToUnit() {
        if (offSet < prefixCount * unitHLenth) {
            offSet = prefixCount * unitHLenth
            indicatorOffSet = prefixCount * indicatorUnitLength
            return
        }
        if (offSet > (list!!.size - 1 - suffixCount) * unitHLenth) {
            offSet = (list.size - 1 - suffixCount) * unitHLenth
            indicatorOffSet = (list.size - suffixCount - 1) * indicatorUnitLength
            return
        }
        val temp = offSet % unitHLenth
        val position =
            (if (temp < unitHLenth / 2) offSet / unitHLenth else offSet / unitHLenth + 1).toInt()
        offSet = unitHLenth * position
        indicatorOffSet = indicatorUnitLength * position
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun sp2px(spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }
}