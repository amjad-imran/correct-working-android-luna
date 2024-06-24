package com.oreo.ui.custom.female

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LOGS.w
import com.oreo.data.model.PeriodChartModel
import com.oreo.ui.custom.ScrollListenerPeriod
import kotlin.math.min

class PeriodLineChart : View {
    private var bgColor = 0
    private var bgLeftColor = 0
    private var bgRightColor = 0
    private var bgTopColor = 0
    private var bgBottomColor = 0
    private var xTextColor = 0
    private var yTextColor = 0
    private var scaleColor = 0
    private var xLineColor = 0
    private var yLineColor = 0

    private var innerCircleColor = 0
    private var outCircleColor = 0

    private var chartLineColor = 0
    private var chartLineWidth = 10f

    private var scaleNodeColor = 0

    private var gridColor = 0

    private var hCount = 0

    var max: Int = 0
    private var xMin = 0

    private var leftWith = 0f
    private var rightWith = 0f
    private var bottomWith = 0f
    private var topWith = 0f

    private var xTextSize = 0f
    private var xTextSize2 = 0f
    private var yTextSize = 0f

    private var innerCircleRadius = 0f
    private var outCircleRadius = 0f
    private var scaleNodeRadius = 0f

    private val showLastCircle = false
    private var bgPaint: Paint? = null
    private var bgLeftPaint: Paint? = null
    private var bgRightPaint: Paint? = null
    private var bgTopPaint: Paint? = null
    private var bgTopSelectedPaint: Paint? = null
    private val selectedLinePath = Path()
    private var bgBottomPaint: Paint? = null

    private var xTextPaint: Paint? = null
    private var xTextPaint2: Paint? = null
    private var xLinePaint: Paint? = null
    private var gridPaint: Paint? = null
    private var centerLinePaint: Paint? = null
    private var centerLineColor = 0
    private var fillColorStart = 0
    private var fillColorEnd = 0
    private var centerLineWidth = 0f
    private var innerCirclePaint: Paint? = null
    private var outCirclePaint: Paint? = null
    private var chartLinePaint: Paint? = null
    private var scaleNodePaint: Paint? = null
    private var glowDotBitmapAbnormal: Bitmap? = null
    private var glowDotBitmapNormal: Bitmap? = null
    private var onChartScrollChangedListener: ScrollListenerPeriod? = null
    private val path = Path()
    private var unitHLenth = 0f
    private var indicatorUnitLength = 0f

    private var showAvgValueText = false
    private var startFromRight = false
    private var alwaysShowCircle = true
    private var mWith = 0
    private var mHeight = 0

    private var offSet = 0f
    private var indicatorOffSet = 0f

    private var xTextBounds: Rect? = null

    private val list: MutableList<PeriodChartModel>? = ArrayList()
    private var prefixCount = 0
    private var mCurrentPos = -1
    private var suffixCount = 0
    private var canScroll = true
    private var showXAxis = true
    private var titleWidth = 0f

    private var normalMin = 0
    private var normalMax = 0

    //    private int maxValue;
    //    private int minValue;
    private var avgValue: Int? = null

    lateinit var normalBarPaint: Paint


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
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LineChart)
        bgColor = ta.getColor(R.styleable.LineChart_bgColor, -0x1)
        bgLeftColor = ta.getColor(R.styleable.LineChart_bgLeftColor, -0x1)
        bgRightColor = ta.getColor(R.styleable.LineChart_bgRightColor, 0xfffffff)
        bgTopColor = ta.getColor(R.styleable.LineChart_bgTopColor, -0x1)
        bgBottomColor = ta.getColor(R.styleable.LineChart_bgBottomColor, -0x1)
        xTextColor = ta.getColor(R.styleable.LineChart_xTextColor, -0x1000000)
        yTextColor = ta.getColor(R.styleable.LineChart_yTextColor, -0x1000000)
        scaleColor = ta.getColor(R.styleable.LineChart_scaleColor, -0x1000000)
        xLineColor = ta.getColor(R.styleable.LineChart_xLineColor, -0x1000000)
        yLineColor = ta.getColor(R.styleable.LineChart_yTextColor, -0x1000000)
        gridColor = ta.getColor(R.styleable.LineChart_gridColor, -0xff01)
        hCount = ta.getInt(R.styleable.LineChart_hCount, 5)
        max = ta.getInt(R.styleable.LineChart_xMax, 10)
        xMin = ta.getInt(R.styleable.LineChart_xMin, 0)
        xTextSize = ta.getDimension(R.styleable.LineChart_xTextSize, 8f)
        xTextSize2 = ta.getDimension(R.styleable.LineChart_xTextSize2, 8f)
        yTextSize = ta.getDimension(R.styleable.LineChart_yTextSize, 8f)
        leftWith = ta.getDimension(R.styleable.LineChart_leftWith, 16f)
        rightWith = ta.getDimension(R.styleable.LineChart_rightWith, 8f)
        bottomWith = ta.getDimension(R.styleable.LineChart_bottomWith, 16f)
        topWith = ta.getDimension(R.styleable.LineChart_topWith, 8f)
        innerCircleColor = ta.getColor(R.styleable.LineChart_innerCircleColor, -0x1)
        outCircleColor = ta.getColor(R.styleable.LineChart_outCircleColor, -0x1000000)
        innerCircleRadius = ta.getDimension(R.styleable.LineChart_innerCircleRadius, 2f)
        outCircleRadius = ta.getDimension(R.styleable.LineChart_outCircleRadius, 0f)
        chartLineColor = ta.getColor(R.styleable.LineChart_chartLineColor, -0x1000000)
        chartLineWidth = ta.getDimension(R.styleable.LineChart_chartLineWidth, 10f)
        scaleNodeColor = ta.getColor(R.styleable.LineChart_scaleNodeColor, -0x1000000)
        scaleNodeRadius = ta.getDimension(R.styleable.LineChart_scaleNodeRadius, 3f)
        centerLineWidth = ta.getDimension(R.styleable.LineChart_centerLineWidth, 10f)
        centerLineColor = ta.getColor(R.styleable.LineChart_centerLineColor, -0x1000000)
        fillColorStart = ta.getColor(R.styleable.LineChart_fillColorStart, -0x7f000001)
        fillColorEnd = ta.getColor(R.styleable.LineChart_fillColorEnd, 0x00000000)
        canScroll = ta.getBoolean(R.styleable.LineChart_canScroll, true)
        showXAxis = ta.getBoolean(R.styleable.LineChart_showXAxis, true)
        titleWidth = ta.getDimension(R.styleable.LineChart_titleWidth, 0f)
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false)

        startFromRight = ta.getBoolean(R.styleable.LineChart_startFromRight, false)
        alwaysShowCircle = ta.getBoolean(R.styleable.LineChart_alwaysShowCircle, true)
        ta.recycle()
        initPaint()
    }

    private fun initPaint() {

        val shaderNormal: Shader = LinearGradient(
            0f,
            0f,
            0f,
            dip2px(100f).toFloat(),
            ContextCompat.getColor(context, R.color.period_normal_start),
            ContextCompat.getColor(context, R.color.period_normal_end),
            Shader.TileMode.CLAMP
        )

        normalBarPaint = Paint()
        normalBarPaint.shader = shaderNormal


        bgPaint = Paint()
        bgPaint!!.color = bgColor

        bgLeftPaint = Paint()
        bgLeftPaint!!.color = bgLeftColor

        bgRightPaint = Paint()
        bgRightPaint!!.color = bgRightColor

        bgTopPaint = Paint()
        bgTopPaint!!.color = bgTopColor

        bgTopSelectedPaint = Paint()

        //        bgTopSelectedPaint.setColor(Color.WHITE);
//        bgTopSelectedPaint.setStyle(Paint.Style.STROKE);
//        bgTopSelectedPaint.setStrokeWidth(dip2px(2));
        bgBottomPaint = Paint()
        bgBottomPaint!!.color = bgBottomColor


        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)
        xTextPaint = Paint()
        xTextPaint!!.textSize = xTextSize
        xTextPaint!!.setTypeface(fontGilroy)
        xTextPaint!!.isAntiAlias = true

        xTextPaint2 = Paint()
        xTextPaint2!!.textSize = xTextSize2
        xTextPaint2!!.setTypeface(fontGilroy)
        xTextPaint2!!.isAntiAlias = true

        xLinePaint = Paint()
        xLinePaint!!.color = xLineColor

        gridPaint = Paint()
        gridPaint!!.color = gridColor

        centerLinePaint = Paint()
        centerLinePaint!!.color = centerLineColor
        centerLinePaint!!.alpha = 100
        centerLinePaint!!.strokeWidth = centerLineWidth
        centerLinePaint!!.style = Paint.Style.STROKE
        centerLinePaint!!.setPathEffect(DashPathEffect(floatArrayOf(5f, 10f), 0f))

        innerCirclePaint = Paint()
        innerCirclePaint!!.color = innerCircleColor
        innerCirclePaint!!.isAntiAlias = true

        outCirclePaint = Paint()
        outCirclePaint!!.color = outCircleColor
        outCirclePaint!!.isAntiAlias = true

        chartLinePaint = Paint()
        chartLinePaint!!.strokeWidth = chartLineWidth
        chartLinePaint!!.color = chartLineColor
        chartLinePaint!!.isAntiAlias = true
        chartLinePaint!!.style = Paint.Style.STROKE


        val res = resources
        val bitmap =
            BitmapFactory.decodeResource(res, com.noisefit_commans.R.drawable.ic_glow_graph)
        glowDotBitmapAbnormal = Bitmap.createScaledBitmap(bitmap, dip2px(40f), dip2px(40f), true)

        val bitmapNormal =
            BitmapFactory.decodeResource(res, com.noisefit_commans.R.drawable.ic_glow_graph)
        glowDotBitmapNormal =
            Bitmap.createScaledBitmap(bitmapNormal, dip2px(40f), dip2px(40f), true)

        scaleNodePaint = Paint()
        scaleNodePaint!!.color = scaleNodeColor
        scaleNodePaint!!.isAntiAlias = true

        xTextBounds = Rect()
    }

    fun updateChartLineColor() {
        chartLinePaint = Paint()
        chartLinePaint!!.strokeWidth = chartLineWidth
        chartLinePaint!!.color = chartLineColor
        chartLinePaint!!.isAntiAlias = true
        chartLinePaint!!.style = Paint.Style.STROKE
    }


    fun updateData(
        datas: List<PeriodChartModel>,
        prefixList: List<PeriodChartModel>,
        suffixList: List<PeriodChartModel>,
        currentPos: Int,
        normalMin: Int,
        normalMax: Int,
        averageValue: Int? = null,
        buffer:Int
    ) {
        list!!.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size
        this.normalMin = normalMin
        this.normalMax = normalMax

        var noneZeroValueCount = 0
        max = 0
        var item: PeriodChartModel
        var sum = 0
        var count = 0
        for (i in datas.indices) {
            item = datas[i]
            if (item.value == 0) {
                continue
            }
            noneZeroValueCount += 1

            if (max == 0) {
                max = item.value
            }
            if (xMin == 0) {
                xMin = item.value
            }

            if (item.value > max) {
                max = item.value
            }

            if (item.value > 0 && item.value < xMin) {
                xMin = item.value
            }

            sum += item.value
            count += 1
        }

        max += buffer
        xMin = if (xMin < buffer) {
            0
        } else {
            xMin - buffer
        }

        avgValue = averageValue

        /*if (noneZeroValueCount <= datas.size / 2) {
            avgValue = 0
        } else {
            if (count != 0) {
                avgValue = sum / count
            }
        }*/

        if (currentPos != -1) {
            mCurrentPos = currentPos
            moveToPosition(currentPos)
        }

        postInvalidate()
    }

    fun setOnChartScrollChangedListener(listener: ScrollListenerPeriod?) {
        this.onChartScrollChangedListener = listener
    }

    fun moveToPosition(position: Int) {
        w("moveToPosition " + position + "     " + list!!.size)
        if (position < 0 || position >= list.size) {
            // Invalid position, do nothing or handle the error as needed
            return
        }

        // Calculate the offset based on the desired position
        val desiredOffset = position * unitHLenth
        val desiredIndicatorOffset = position * indicatorUnitLength

        // Set the offset and indicator offset to move to the desired position
        offSet = desiredOffset
        indicatorOffSet = desiredIndicatorOffset

        // Trigger a redraw of the view to reflect the new position
        //invalidate();
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWith = w
        mHeight = h

        unitHLenth = (mWith - leftWith - rightWith) / hCount
        offSet = if (mCurrentPos != -1) {
            mCurrentPos * unitHLenth
        } else {
            prefixCount * unitHLenth
        }

        indicatorUnitLength = if (titleWidth == 0f) {
            mWith / 2f
        } else {
            titleWidth
        }
        indicatorOffSet = if (mCurrentPos != -1) {
            mCurrentPos * indicatorUnitLength
        } else {
            prefixCount * indicatorUnitLength
        }

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawTop(canvas)
        drawBottom(canvas)
        drawLeft(canvas)
        drawContent(canvas)
        drawRight(canvas)
    }


    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint!!)


        if (normalMin != 0 && normalMax != 0) {
            val yTop =
                mHeight - bottomWith - (normalMax - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

            val yBottom =
                mHeight - bottomWith - (normalMin - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

            val rectF = RectF(0f, yTop, mWith.toFloat(), yBottom)
            canvas.drawRect(
                rectF,
                normalBarPaint
            )
        }
    }

    private fun drawTop(canvas: Canvas) {
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
        if (showXAxis) {
            canvas.drawLine(
                leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith,
                xLinePaint!!
            )
        }
    }


   /* fun calculateYAxisValues(min: Int, max: Int): List<Int> {
        val yAxisValues = mutableListOf<Int>()

        var newMin = min - 10
        if (newMin < 0) {
            newMin = 0
        }
        val newMax = max + 10
        var currentVal = newMin

        while (currentVal < newMax) {
            if (currentVal % 5 == 0) {
                yAxisValues.add(currentVal)
            }
            currentVal++
        }

        return yAxisValues
    }*/

    fun calculateYAxisValues(min: Int, max: Int): List<Int> {
        val yAxisValues = mutableListOf<Int>()

        var currentVal = min

        val step = (max - min) / 5

        for (i in 0 until 6) {
            yAxisValues.add(currentVal)
            currentVal += step
        }

        return yAxisValues
    }

    private fun drawLeft(canvas: Canvas) {

        xTextPaint!!.color = xTextColor

        var currentVal = xMin
        val valueToPlot = calculateYAxisValues(xMin, max)//ArrayList<Int>()

        /*while (currentVal < max) {
            if (currentVal % 5 == 0) {
                valueToPlot.add(currentVal)
            }
            currentVal++
        }*/

        val marginEnd = dip2px(8f)
        val lineSpacing = dip2px(4f)
        xTextPaint!!.setColor(Color.parseColor("#7affffff"))

        valueToPlot.forEach {
            val y =
                mHeight - bottomWith - (it - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)


            if (y < (mHeight - bottomWith)) {
                canvas.drawLine(leftWith, y, mWith.toFloat(), y, gridPaint!!)
                xTextPaint!!.getTextBounds(it.toString(), 0, it.toString().length, xTextBounds)

                canvas.drawText(
                    it.toString(),
                    mWith.toFloat() - xTextBounds!!.width() - marginEnd,
                    y + xTextBounds!!.height() + lineSpacing,
                    xTextPaint!!
                )
            }
        }


        //FOr average value
        if (avgValue != null) {
            val avgStr = avgValue.toString() + " days"
            val avg =
                mHeight - bottomWith - (avgValue!! - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            if (avgValue!! > 0) {
                canvas.drawLine(leftWith, avg, mWith - rightWith, avg, centerLinePaint!!)
            }

            xTextPaint!!.setColor(Color.parseColor("#ffffff"))

            xTextPaint!!.getTextBounds(avgStr, 0, avgStr.length, xTextBounds)
            val dim = xTextPaint!!.measureText(avgStr)
            canvas.drawText(
                avgStr,
                mWith - dim - dip2px(8f),
                avg + xTextBounds!!.height() + dip2px(6f),
                xTextPaint!!
            )
        }

    }

    private fun drawContent(canvas: Canvas) {
        if (null == list || list.isEmpty()) {
            return
        }
        var firstPosition = 0
        val tempOffset = offSet + moveOffSet
        var divisor = 0.5f
        if (startFromRight) {
            divisor = (hCount - 1) * 1f / hCount
        }
        if (tempOffset > (mWith - leftWith - rightWith) * divisor + unitHLenth) {
            firstPosition =
                ((tempOffset - (mWith - leftWith - rightWith) * divisor) / unitHLenth).toInt()
        }
        val lastPosition = min((firstPosition + hCount + 2).toDouble(), list.size.toDouble())
            .toInt()
        var current: PeriodChartModel
        var next: PeriodChartModel
        for (i in firstPosition until lastPosition) {
            current = list[i]
            val x =
                offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - i * unitHLenth
            val y =
                mHeight - bottomWith - (current.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
            path.reset()
            path.moveTo(x, y)
            if (i < list.size - 1) {
                next = list[i + 1]
                if (current.value > 0 && next.value > 0) {
                    val x1 =
                        offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth
                    val y1 =
                        mHeight - bottomWith - (next.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
                    path.lineTo(x1, y1)
                    canvas.drawPath(path, chartLinePaint!!)
                }
            }

            if (current.value > 0) {
                if (current.isNormal) {
                    innerCirclePaint!!.color = Color.parseColor("#29cc74")
                } else {
                    innerCirclePaint!!.color = Color.parseColor("#ff84d5")
                }
                canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint!!)
            }

            if (showXAxis) {
                val monthText = list[i].month
                val dayText = list[i].day

                xTextPaint2!!.getTextBounds(monthText, 0, monthText!!.length, xTextBounds)
                xTextPaint2!!.color = xTextColor and -0x7f000001
                xTextPaint!!.color = xTextColor and -0x7f000001
                canvas.drawText(
                    monthText,
                    x - xTextBounds!!.width() / 2f,
                    mHeight - bottomWith / 2,
                    xTextPaint2!!
                )

                val height = (xTextBounds!!.height() + dip2px(4f)).toFloat()

                xTextPaint2!!.getTextBounds(dayText, 0, dayText!!.length, xTextBounds)

                canvas.drawText(
                    dayText,
                    x - xTextBounds!!.width() / 2f,
                    (mHeight - bottomWith / 2) + height,
                    xTextPaint2!!
                )
            }

            if (showLastCircle) {
                if (i == 1 && current.value > 0) {
                    val width = (glowDotBitmapAbnormal!!.width / 2).toFloat()
                    val height = (glowDotBitmapAbnormal!!.height / 2).toFloat()

                    if (current.isNormal) {
                        canvas.drawBitmap(
                            glowDotBitmapNormal!!,
                            x - width,
                            y - height,
                            scaleNodePaint
                        )
                    } else {
                        canvas.drawBitmap(
                            glowDotBitmapAbnormal!!,
                            x - width,
                            y - height,
                            scaleNodePaint
                        )
                    }
                }
            }
        }

        if ((offSet + moveOffSet) < 0 || (offSet + moveOffSet) > (list.size - 1) * unitHLenth) {
            return
        }
        //round
        val position = Math.round((offSet + moveOffSet) / unitHLenth)

        val x = (mWith - leftWith - rightWith) * divisor + leftWith
        val y: Float

        val y1 =
            mHeight - bottomWith - (list[position].value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
        if (list[position].value > 0 && (moveOffSet == 0f || (offSet + moveOffSet) == (list.size - 1) * unitHLenth)) {
            y = y1

            val width = (glowDotBitmapAbnormal!!.width / 2).toFloat()
            val height = (glowDotBitmapAbnormal!!.height / 2).toFloat()
            if (list[position].isNormal) {
                canvas.drawBitmap(glowDotBitmapNormal!!, x - width, y - height, scaleNodePaint)
            } else {
                canvas.drawBitmap(glowDotBitmapAbnormal!!, x - width, y - height, scaleNodePaint)
            }
            //canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
        }
        if (moveOffSet == 0f && showXAxis) {
            val monthText = list[position].month
            val dayText = list[position].day

            xTextPaint!!.color = xTextColor
            xTextPaint2!!.color = xTextColor
            xTextPaint2!!.getTextBounds(monthText, 0, monthText!!.length, xTextBounds)
            canvas.drawText(
                monthText,
                x - xTextBounds!!.width() / 2f,
                mHeight - bottomWith / 2,
                xTextPaint2!!
            )

            val height = (xTextBounds!!.height() + dip2px(4f)).toFloat()

            xTextPaint2!!.getTextBounds(dayText, 0, dayText!!.length, xTextBounds)

            canvas.drawText(
                dayText,
                x - xTextBounds!!.width() / 2f,
                (mHeight - bottomWith / 2) + height,
                xTextPaint2!!
            )
        }
    }


    private var xDown = 0f
    private var moveOffSet = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canScroll) {
            return false
        }
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
        val tempPosition = if ((offSet + moveOffSet) >= (list.size - 1) * unitH) {
            list.size - 1
        } else if ((offSet + moveOffSet) < 0) {
            0
        } else {
            Math.round(((offSet + moveOffSet) / unitH))
        }

        if (scrollPosition == tempPosition) {
            return
        }


        scrollPosition = tempPosition
        if (isSelected) {
            onChartScrollChangedListener!!.onPositionSelected(scrollPosition, list[scrollPosition])
        } else {
            onChartScrollChangedListener!!.onScrolling(scrollPosition, list[scrollPosition])
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
        if (offSet > (list!!.size - suffixCount - 1) * unitHLenth) {
            offSet = (list.size - suffixCount - 1) * unitHLenth
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

