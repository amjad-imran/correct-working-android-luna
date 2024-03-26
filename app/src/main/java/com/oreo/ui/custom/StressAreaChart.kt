package com.oreo.ui.custom

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
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.noisefit.luna.R
import com.noisefit_commans.utils.DistanceUtil.convertMeterToKm
import com.noisefit_commans.utils.LOGS.d
import com.noisefit_commans.utils.LOGS.w
import com.oreo.data.model.ChartModel
import kotlin.math.min

class StressAreaChart : View {
    private var bgColor = 0
    private var bgLeftColor = 0
    private var bgRightColor = 0
    private var bgTopColor = 0
    private var bgBottomColor = 0
    private var isDistanceGraph = false
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
    var max = 0
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
    private var showLastCircle = false
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
    private var chartLineFillPaint: Paint? = null
    private var scaleNodePaint: Paint? = null
    private var glowDotBitmap: Bitmap? = null
    private var onChartScrollChangedListener: ScrollListener? = null
    private val path = Path()
    private val fillPath = Path()
    private var unitHLenth = 0f
    private var indicatorUnitLength = 0f
    private var showAvgValueText = false
    private var showExtremeLine = false
    private var startFromRight = false
    private var alwaysShowCircle = true
    private var mWith = 0
    private var mHeight = 0
    private var offSet = 0f
    private var indicatorOffSet = 0f
    private var xTextBounds: Rect? = null
    private val list: MutableList<ChartModel>? = ArrayList()
    private var prefixCount = 0
    private var mCurrentPos = -1
    private var suffixCount = 0
    private var canScroll = true
    private var showXAxis = true
    private var showSelectedIndicator = true
    private var titleWidth = 0f

    //    private int maxValue;
    //    private int minValue;
    private var avgValue = 0
    private var linearGradient: LinearGradient? = null

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
        showSelectedIndicator = ta.getBoolean(R.styleable.LineChart_showSelectedIndicator, true)
        titleWidth = ta.getDimension(R.styleable.LineChart_titleWidth, 0f)
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false)
        showExtremeLine = ta.getBoolean(R.styleable.LineChart_showExtremeLine, true)
        startFromRight = ta.getBoolean(R.styleable.LineChart_startFromRight, false)
        alwaysShowCircle = ta.getBoolean(R.styleable.LineChart_alwaysShowCircle, true)
        ta.recycle()
        initPaint()
    }

    private fun initPaint() {
        bgPaint = Paint()
        bgPaint!!.setColor(bgColor)
        bgLeftPaint = Paint()
        bgLeftPaint!!.setColor(bgLeftColor)
        bgRightPaint = Paint()
        bgRightPaint!!.setColor(bgRightColor)
        bgTopPaint = Paint()
        bgTopPaint!!.setColor(bgTopColor)
        bgTopSelectedPaint = Paint()
        //        bgTopSelectedPaint.setColor(Color.WHITE);
//        bgTopSelectedPaint.setStyle(Paint.Style.STROKE);
//        bgTopSelectedPaint.setStrokeWidth(dip2px(2));
        bgBottomPaint = Paint()
        bgBottomPaint!!.setColor(bgBottomColor)
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
        xLinePaint!!.setColor(xLineColor)
        gridPaint = Paint()
        gridPaint!!.setColor(gridColor)
        centerLinePaint = Paint()
        centerLinePaint!!.setColor(centerLineColor)
        centerLinePaint!!.setAlpha(100)
        centerLinePaint!!.strokeWidth = centerLineWidth
        centerLinePaint!!.style = Paint.Style.STROKE
        centerLinePaint!!.setPathEffect(DashPathEffect(floatArrayOf(5f, 10f), 0f))
        innerCirclePaint = Paint()
        innerCirclePaint!!.setColor(innerCircleColor)
        innerCirclePaint!!.isAntiAlias = true
        outCirclePaint = Paint()
        outCirclePaint!!.setColor(outCircleColor)
        outCirclePaint!!.isAntiAlias = true
        chartLinePaint = Paint()
        chartLinePaint!!.strokeWidth = chartLineWidth
        chartLinePaint!!.setColor(chartLineColor)
        chartLinePaint!!.isAntiAlias = true
        chartLinePaint!!.style = Paint.Style.STROKE
        chartLineFillPaint = Paint()
        //        chartLineFillPaint.setColor(Color.GRAY);
        chartLineFillPaint!!.style = Paint.Style.FILL
        chartLineFillPaint!!.isAntiAlias = true
        val res = resources
        val bitmap =
            BitmapFactory.decodeResource(res, com.noisefit_commans.R.drawable.ic_glow_graph)
        glowDotBitmap = Bitmap.createScaledBitmap(bitmap, dip2px(40f), dip2px(40f), true)
        scaleNodePaint = Paint()
        scaleNodePaint!!.setColor(scaleNodeColor)
        scaleNodePaint!!.isAntiAlias = true
        xTextBounds = Rect()
    }

    /**
     * set data points
     *
     * @param datas      real data point
     * @param prefixList placeholder before real data point
     * @param suffixList placeholder after real data point
     */
    fun updateData(
        datas: List<ChartModel>,
        prefixList: List<ChartModel>,
        suffixList: List<ChartModel>
    ) {
        list!!.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size
        var item: ChartModel
        var sum = 0
        var count = 0
        for (i in datas.indices) {
            item = datas[i]
            if (item.value == 0) {
                continue
            }
            sum += item.value
            count += 1
        }
        if (count != 0) {
            avgValue = sum / count
        }
        postInvalidate()
    }

    fun updateChartLineColor() {
        chartLinePaint = Paint()
        chartLinePaint!!.strokeWidth = chartLineWidth
        chartLinePaint!!.setColor(chartLineColor)
        chartLinePaint!!.isAntiAlias = true
        chartLinePaint!!.style = Paint.Style.STROKE
    }

    fun updateDataWithMax(
        datas: List<ChartModel>, prefixList: List<ChartModel>,
        suffixList: List<ChartModel>, xMax1: Int, lineColor: Int,
        fillStartColor: Int, fillEndColor: Int
    ) {
        list!!.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size
        chartLineColor = lineColor
        fillColorStart = fillStartColor
        fillColorEnd = fillEndColor
        updateChartLineColor()
        max = 0
        var item: ChartModel
        var sum = 0
        var noneZeroValueCount = 0
        var count = 0
        for (i in datas.indices) {
            item = datas[i]
            if (item.value == 0) {
                continue
            }
            noneZeroValueCount += 1
            isDistanceGraph = datas[i].isDistanceGraph
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
            max = max + 5
        }
        d("NonZeroValuesxMax = " + max)
        postInvalidate()
    }

    fun updateDataWithMax(
        datas: List<ChartModel>,
        prefixList: List<ChartModel>,
        suffixList: List<ChartModel>,
        currentPos: Int
    ) {
        list!!.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size
        var noneZeroValueCount = 0
        max = 0
        var item: ChartModel
        var sum = 0
        var count = 0
        for (i in datas.indices) {
            item = datas[i]
            if (item.value == 0) {
                continue
            }
            isDistanceGraph = datas[i].isDistanceGraph
            noneZeroValueCount += 1
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


//        if (xMax < 20) {
//            xMax += 5;
//        } else if (xMax < 100) {
//            xMax += 50;
//        } else if (xMax < 1000) {
//            xMax += 500;
//        }
        max = 120
        if (currentPos != -1) {
            mCurrentPos = currentPos
            moveToPosition(currentPos)
        }
        postInvalidate()
    }

    fun updateDataWithMaxMin(
        datas: List<ChartModel>,
        prefixList: List<ChartModel>,
        suffixList: List<ChartModel>,
        offSet: Int,
        showLastCircle: Boolean
    ) {
        list!!.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size
        max = 0
        var item: ChartModel
        var sum = 0
        var count = 0
        for (i in datas.indices) {
            item = datas[i]
            if (item.value == 0) {
                continue
            }
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
        if (count != 0) {
            avgValue = sum / count
        }
        this.showLastCircle = showLastCircle
        max += offSet
        xMin -= offSet
        if (xMin < 0) {
            xMin = 0
        }
        d("dsasdasad " + avgValue + " " + max + " " + xMin)
        postInvalidate()
    }

    fun setOnChartScrollChangedListener(listener: ScrollListener?) {
        onChartScrollChangedListener = listener
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
        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            fillColorStart,
            fillColorEnd,
            Shader.TileMode.CLAMP
        )
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
        w("moveToPosition onSizeChanged")

        //callBack(true);
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
    }

    private fun drawTop(canvas: Canvas) {
        if (showSelectedIndicator) {
            canvas.drawRect(0f, 0f, mWith.toFloat(), topWith, bgTopPaint!!)
            //            selectedLinePath.moveTo(mWith / 2f - 2 * unitHLenth, topWith);
//            selectedLinePath.lineTo(mWith / 2f - unitHLenth / 5f, topWith);
//            selectedLinePath.lineTo(mWith / 2f, topWith + unitHLenth / 5f );
//            selectedLinePath.lineTo(mWith / 2f + unitHLenth / 5f, topWith);
//            selectedLinePath.lineTo(mWith / 2f + 2* unitHLenth, topWith);
            bgTopSelectedPaint!!.style = Paint.Style.FILL
            bgTopSelectedPaint!!.setColor(bgTopColor)
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint!!)
            bgTopSelectedPaint!!.style = Paint.Style.STROKE
            bgTopSelectedPaint!!.setColor(Color.WHITE)
            bgTopSelectedPaint!!.strokeWidth = dip2px(1f).toFloat()
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
        if (showXAxis) {
            canvas.drawLine(
                leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith,
                xLinePaint!!
            )
        }
    }

    private fun drawLeft(canvas: Canvas) {
        val maxStr: String
        val avgStr: String
        if (isDistanceGraph) {
            maxStr = convertMeterToKm(max)
            avgStr = convertMeterToKm(avgValue)
        } else {
            maxStr = max.toString()
            avgStr = avgValue.toString()
        }
        val minStr = xMin.toString()
        xTextPaint!!.setColor(xTextColor and -0x7f000001)
        if (showExtremeLine) {
            val max =
                mHeight - bottomWith - (max - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
            canvas.drawLine(leftWith, max, mWith.toFloat(), max, gridPaint!!)
            if (showAvgValueText) {
                xTextPaint!!.getTextBounds(maxStr, 0, maxStr.length, xTextBounds)
                canvas.drawText(
                    maxStr,
                    mWith - rightWith - xTextBounds!!.width() + dip2px(10f),
                    max + xTextBounds!!.height() / 2f + dip2px(10f),
                    xTextPaint!!
                )
            }
            val min =
                mHeight - bottomWith - 0 * (mHeight - topWith - bottomWith) / (this.max - xMin)
            canvas.drawLine(leftWith, min, mWith.toFloat(), min, gridPaint!!)
            if (showAvgValueText) {
                xTextPaint!!.getTextBounds(maxStr, 0, maxStr.length, xTextBounds)
                canvas.drawText(
                    minStr,
                    mWith - rightWith + dip2px(10f),
                    min + xTextBounds!!.height() / 2f - dip2px(10f),
                    xTextPaint!!
                )
            }
        }
        val avg =
            mHeight - bottomWith - (avgValue - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
        if (avgValue > 0) {
            canvas.drawLine(leftWith, avg, mWith - rightWith, avg, centerLinePaint!!)
        }
        if (showAvgValueText && avgValue > 0) {
            xTextPaint!!.getTextBounds(avgStr, 0, avgStr.length, xTextBounds)
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
        var firstPosition = 0
        val tempOffset = offSet + moveOffSet
        var divisor = 0.5f
        if (!showSelectedIndicator && startFromRight) {
            divisor = (hCount - 1) * 1f / hCount
        }
        if (tempOffset > (mWith - leftWith - rightWith) * divisor + unitHLenth) {
            firstPosition =
                ((tempOffset - (mWith - leftWith - rightWith) * divisor) / unitHLenth).toInt()
        }
        val lastPosition = min((firstPosition + hCount + 2).toDouble(), list.size.toDouble())
            .toInt()
        var current: ChartModel
        var next: ChartModel
        for (i in firstPosition until lastPosition) {
            current = list[i]
            val x =
                offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - i * unitHLenth
            val y =
                mHeight - bottomWith - (current.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
            path.reset()
            fillPath.reset()
            path.moveTo(x, y)
            if (i < list.size - 1) {
                next = list[i + 1]
                if (current.value > 0 && next.value > 0) {
                    val x1 =
                        offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth
                    val y1 =
                        mHeight - bottomWith - (next.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
                    path.cubicTo(x1 + (x - x1) / 4, y, x - (x - x1) / 4, y1, x1, y1)
                    fillPath.addPath(path)
                    //draw fill first
                    fillPath.lineTo(x1, mHeight - bottomWith)
                    fillPath.lineTo(x, mHeight - bottomWith)
                    chartLineFillPaint!!.setShader(linearGradient)
                    canvas.drawPath(fillPath, chartLineFillPaint!!)
                    //draw chart line second, need to cover fill color
                    canvas.drawPath(path, chartLinePaint!!)
                }
            }
            if (current.value > 0) {
                if (alwaysShowCircle) {
                    canvas.drawCircle(x, y, outCircleRadius, outCirclePaint!!)
                    canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint!!)
                } else {
                    if (i == firstPosition) {
                        next = list[i + 1]
                        if (next.value == 0) {
                            canvas.drawCircle(x, y, outCircleRadius, outCirclePaint!!)
                            canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint!!)
                        }
                    } else if (i == lastPosition - 1) {
                        val pre = list[i - 1]
                        if (pre.value == 0) {
                            canvas.drawCircle(x, y, outCircleRadius, outCirclePaint!!)
                            canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint!!)
                        }
                    } else {
                        val pre = list[i - 1]
                        next = list[i + 1]
                        if (pre.value == 0 && next.value == 0) {
                            canvas.drawCircle(x, y, outCircleRadius, outCirclePaint!!)
                            canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint!!)
                        }
                    }
                }
            }
            if (showXAxis) {
                val xText = list[i].index
                xTextPaint2!!.getTextBounds(xText, 0, xText!!.length, xTextBounds)
                xTextPaint2!!.setColor(xTextColor and -0x7f000001)
                xTextPaint!!.setColor(xTextColor and -0x7f000001)
                canvas.drawText(
                    xText,
                    x - xTextBounds!!.width() / 2f,
                    mHeight - bottomWith / 3,
                    xTextPaint2!!
                )
                if (showSelectedIndicator && list[i].formattedDate != null) {
                    val title = list[i].formattedDate
                    val xInd =
                        indicatorOffSet + moveOffSet * list.size * indicatorUnitLength / (list.size * unitHLenth) + (mWith - leftWith - rightWith) * divisor + leftWith - i * indicatorUnitLength
                    xTextPaint!!.getTextBounds(title, 0, title!!.length, xTextBounds)
                    canvas.drawText(
                        title,
                        xInd - xTextBounds!!.width() / 2f,
                        topWith / 2 + xTextBounds!!.height() / 2f,
                        xTextPaint!!
                    )
                }
            }
            canvas.drawLine(x, topWith, x, mHeight - bottomWith, gridPaint!!)
            if (showLastCircle) {
                if (i == 1 && current.value > 0) {
                    val width = (glowDotBitmap!!.getWidth() / 2).toFloat()
                    val height = (glowDotBitmap!!.getHeight() / 2).toFloat()
                    canvas.drawBitmap(glowDotBitmap!!, x - width, y - height, scaleNodePaint)

                    //canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
                }
            }
        }
        //        canvas.drawPath(fillPath, chartLineFillPaint);
        if (offSet + moveOffSet < 0 || offSet + moveOffSet > (list.size - 1) * unitHLenth) {
            return
        }
        //round
        val position = Math.round((offSet + moveOffSet) / unitHLenth)
        val x = (mWith - leftWith - rightWith) * divisor + leftWith
        val y: Float
        val y1 =
            mHeight - bottomWith - (list[position].value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
        if (list[position].value > 0 && (moveOffSet == 0f || offSet + moveOffSet == (list.size - 1) * unitHLenth)) {
            y = y1
            val width = (glowDotBitmap!!.getWidth() / 2).toFloat()
            val height = (glowDotBitmap!!.getHeight() / 2).toFloat()
            canvas.drawBitmap(glowDotBitmap!!, x - width, y - height, scaleNodePaint)
            //canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
        }
        if (moveOffSet == 0f && showXAxis) {
            val xText = list[position].index
            xTextPaint!!.setColor(xTextColor)
            xTextPaint2!!.setColor(xTextColor)
            xTextPaint2!!.getTextBounds(xText, 0, xText!!.length, xTextBounds)
            canvas.drawText(
                xText,
                x - xTextBounds!!.width() / 2f,
                mHeight - bottomWith / 3,
                xTextPaint2!!
            )
            if (showSelectedIndicator && list[position].formattedDate != null) {
                val title = list[position].formattedDate
                val xInd =
                    indicatorOffSet + moveOffSet * list.size * indicatorUnitLength / (list.size * unitHLenth) + (mWith - leftWith - rightWith) * divisor + leftWith - position * indicatorUnitLength
                xTextPaint!!.getTextBounds(title, 0, title!!.length, xTextBounds)
                canvas.drawText(
                    title,
                    xInd - xTextBounds!!.width() / 2f,
                    topWith / 2 + xTextBounds!!.height() / 2f,
                    xTextPaint!!
                )
            }
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
                w("moveToPosition MotionEvent.ACTION_MOVE")
                callBack(true)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                offSet += event.x - xDown
                resetData()
                setToUnit()
                w("moveToPosition MotionEvent.ACTION_UP")
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
