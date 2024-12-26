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
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

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
    var max = 0.0f
    private var isMetric = false

    private var xMin = 0
    private var leftWith = 0f
    private var rightWith = 0f
    private var bottomWith = 0f
    private var topWith = 0f
    private var xTextSize = 0f
    private var yTextSize = 0f
    private var bgPaint: Paint? = null
    private var bgLeftPaint: Paint? = null
    private var bgRightPaint: Paint? = null
    private var bgTopPaint: Paint? = null
    private var bgTopSelectedPaint: Paint? = null
    private val selectedLinePath = Path()
    private var bgBottomPaint: Paint? = null
    private var showSelectedIndicator = true
    private var xTextPaint: Paint? = null
    private var yAxisPaint: Paint? = null
    private var gridPaint: Paint? = null
    private var baseAxisPaint: Paint? = null
    private var hAxisPaint: Paint? = null
    private var centerLinePaint: Paint? = null
    private var centerLineColor = 0
    private var lineSelectColor = 0
    private var centerLineWidth = 0f
    private var chartLinePaint: Paint? = null
    private var rectF: RectF? = null
    private var onChartScrollChangedListener: ScrollListener? = null

    private var verticalLineColor: Paint? = null
    private var pointColor: Paint? = null
    private var pointSelectedColor: Paint? = null
    private var topPaint: Paint? = null
    private var bottomPaint: Paint? = null
    private var topSelectedPaint: Paint? = null
    private var bottomSelectedPaint: Paint? = null


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

    constructor(context: Context?) : super(context) {
        initPaint()
        //        updateData();
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
        //        updateData();
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
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
        //max = ta.getInt(R.styleable.BarChart_xMax, 10)
        xMin = ta.getInt(R.styleable.BarChart_xMin, 0)
        xTextSize = ta.getDimension(R.styleable.BarChart_xTextSize, 8f)
        yTextSize = ta.getDimension(R.styleable.BarChart_yTextSize, 10f)
        leftWith = ta.getDimension(R.styleable.BarChart_leftWith, 16f)
        rightWith = ta.getDimension(R.styleable.BarChart_rightWith, 8f)
        bottomWith = ta.getDimension(R.styleable.BarChart_bottomWith, 16f)
        topWith = ta.getDimension(R.styleable.BarChart_topWith, 8f)
        chartLineColor = ta.getColor(R.styleable.BarChart_chartLineColor, -0x1000000)
        chartLineWidth = ta.getDimension(R.styleable.BarChart_chartLineWidth, 10f)
        centerLineWidth = ta.getDimension(R.styleable.BarChart_centerLineWidth, 10f)
        centerLineColor = ta.getColor(R.styleable.BarChart_centerLineColor, -0x1000000)
        lineSelectColor = ta.getColor(R.styleable.BarChart_lineSelectColor, 0x00000000)
        titleWidth = ta.getDimension(R.styleable.BarChart_titleWidth, 0f)
        showSelectedIndicator = ta.getBoolean(R.styleable.BarChart_showSelectedIndicator, true)
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false)
        ta.recycle()
        initPaint()
    }

    private fun initPaint() {


        verticalLineColor = Paint().apply {
            color = Color.parseColor("#19ffffff")
        }
        pointColor = Paint().apply {
            color = Color.parseColor("#83878d")
        }
        pointSelectedColor = Paint().apply {
            color = Color.parseColor("#FFFFFF")
        }
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

        xTextPaint = Paint().apply {
            textSize = xTextSize
            isAntiAlias = true
        }
        yAxisPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = yTextSize
            isAntiAlias = true
            alpha = 64
        }

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


        topPaint = Paint().apply {
            color = Color.parseColor("#832139")
        }
        topSelectedPaint = Paint().apply {
            color = Color.parseColor("#ff3358")
        }
        bottomPaint = Paint().apply {
            color = Color.parseColor("#546691")
        }
        bottomSelectedPaint = Paint().apply {
            color = Color.parseColor("#88a5ef")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawBottom(canvas)
        drawContent(canvas)
        drawRight(canvas)
        drawLeft(canvas)
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
            leftWith + (mWith - leftWith - rightWith) / 2f - 2 * unitHLenth, topWith
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f - unitHLenth / 5f, topWith
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f, topWith + unitHLenth / 5f
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f + unitHLenth / 5f, topWith
        )
        selectedLinePath.lineTo(
            leftWith + (mWith - leftWith - rightWith) / 2f + 2 * unitHLenth, topWith
        )
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint!!)
        canvas.drawLine(0f, 0f, mWith.toFloat(), 0f, verticalLineColor!!)


        val centerY = ((mHeight - bottomWith) / 2)

        canvas.drawLine(
            0f, centerY, mWith.toFloat(), centerY, baseAxisPaint!!
        )
        val sectionHeight = centerY / 5

        canvas.drawLine(
            0f, sectionHeight * 1, mWith.toFloat(), sectionHeight * 1, hAxisPaint!!
        )
        canvas.drawLine(
            0f, sectionHeight * 2, mWith.toFloat(), sectionHeight * 2, hAxisPaint!!
        )
        canvas.drawLine(
            0f, sectionHeight * 3, mWith.toFloat(), sectionHeight * 3, hAxisPaint!!
        )
        canvas.drawLine(
            0f, sectionHeight * 4, mWith.toFloat(), sectionHeight * 4, hAxisPaint!!
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
        canvas.drawLine(
            0f,
            centerY + sectionHeight * 4,
            mWith.toFloat(),
            centerY + sectionHeight * 4,
            gridPaint!!
        )


    }

    private fun drawRight(canvas: Canvas) {
        canvas.drawRect(mWith - rightWith, 0f, mWith.toFloat(), mHeight.toFloat(), bgRightPaint!!)
    }

    private fun drawBottom(canvas: Canvas) {
        canvas.drawRect(
            0f, mHeight - bottomWith, mWith.toFloat(), mHeight.toFloat(), bgBottomPaint!!
        )
    }

    private fun drawLeft(canvas: Canvas) {
        canvas.drawRect(0f, 0f, leftWith, mHeight.toFloat(), bgLeftPaint!!)


        //y axis
        val centerY = ((mHeight - bottomWith) / 2)

        val sectionHeight = centerY / 5
        val offset = dip2px(4f)
        val yGap = max.toFloat() / 4
        var yTop = max.toFloat()

        val yTextStart = mWith - dip2px(32f).toFloat()

        canvas.drawText(
            "+${formatFloat(yTop)}", yTextStart, sectionHeight * 1 - offset, yAxisPaint!!
        )
        yTop -= yGap
        canvas.drawText(
            "+${formatFloat(yTop)}", yTextStart, sectionHeight * 2 - offset, yAxisPaint!!
        )
        yTop -= yGap
        canvas.drawText(
            "+${formatFloat(yTop)}", yTextStart, sectionHeight * 3 - offset, yAxisPaint!!
        )
        yTop -= yGap
        canvas.drawText(
            "+${formatFloat(yTop)}", yTextStart, sectionHeight * 4 - offset, yAxisPaint!!
        )
        yTop = 0.0f
        canvas.drawText(
            "+0.0", yTextStart, centerY - offset, yAxisPaint!!
        )
        yTop -= yGap
        canvas.drawText(
            "${formatFloat(yTop)}", yTextStart, centerY + sectionHeight * 1 - offset, yAxisPaint!!
        )
        yTop -= yGap
        canvas.drawText(
            "${formatFloat(yTop)}", yTextStart, centerY + sectionHeight * 2 - offset, yAxisPaint!!
        )
        yTop -= yGap
        canvas.drawText(
            "${formatFloat(yTop)}", yTextStart, centerY + sectionHeight * 3 - offset, yAxisPaint!!
        )

        yTop -= yGap
        canvas.drawText(
            "${formatFloat(yTop)}", yTextStart, centerY + sectionHeight * 4 - offset, yAxisPaint!!
        )
    }

    fun updateDataWithMax(
        datas: List<ChartModel>,
        prefixList: List<ChartModel>,
        suffixList: List<ChartModel>,
        isMetric: Boolean
    ) {
        this.isMetric = isMetric

        list?.clear()
        list?.addAll(prefixList)
        list?.addAll(datas)
        list?.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size
        max = getMaxValue(datas)
        setToUnit()
        postInvalidate()
    }

    fun getMaxValue(datas: List<ChartModel>): Float {
        var max = getConvertedValue(2.0f)
        datas.forEach {
            if (abs(it.valueFloat) > max) {
                max = getConvertedValue(4.0f)
            }
        }
        return max
    }

    private fun getConvertedValue(value: Float): Float {
        return if (isMetric) {
            val convertedVal = AppConversionUtils.fahrenheitToCelsius(32 + value)
            return formatFloat(convertedVal)
        } else value
    }

    private fun formatFloat(value: Float): Float {
        val df = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
        return df.format(value).toFloat()
    }

    private fun drawContent(canvas: Canvas) {
        if (null == list || list.size <= 0) {
            return
        }

        var firstPosition = 0
        val tempOffset = offSet + moveOffSet
        if (tempOffset > (mWith - leftWith - rightWith) / 2 + unitHLenth) {
            firstPosition = ((tempOffset - (mWith - leftWith - rightWith) / 2) / unitHLenth).toInt()
        }
        val lastPosition = Math.min(firstPosition + hCount + 2, list.size)
        var current: ChartModel
        val centerY = ((mHeight - bottomWith) / 2)

        for (i in firstPosition until lastPosition) {
            current = list[i]
            val x =
                offSet + moveOffSet + (mWith - leftWith - rightWith) / 2 + leftWith - i * unitHLenth

            val blockHeight = centerY / 5

            canvas.drawLine(x, 0f, x, (mHeight - bottomWith - blockHeight), verticalLineColor!!)

            if (current.valueFloat > 0) {
                val convertedVal = if (max == 2.0f) {
                    if (current.valueFloat > 2.5) {
                        2.5f
                    } else {
                        current.valueFloat
                    }
                } else if (max == 4.0f) {
                    if (current.valueFloat > 5f) {
                        5f
                    } else {
                        current.valueFloat
                    }
                } else {
                    current.valueFloat
                }

                val percent = (convertedVal / max)
                val convertedHeight = (4.0f / 5.0f) * centerY

                val top = percent * convertedHeight

                val rectTop = RectF().apply {
                    left = x - chartLineWidth / 2f
                    this.top = centerY - top
                    right = x + chartLineWidth / 2f
                    bottom = centerY
                }

                val radius = chartLineWidth / 2f
                val corners = floatArrayOf(
                    radius, radius,   // Top left radius in px
                    radius, radius,   // Top right radius in px
                    0f, 0f,     // Bottom right radius in px
                    0f, 0f      // Bottom left radius in px
                )

                val path = Path()
                path.addRoundRect(rectTop, corners, Path.Direction.CW)
                canvas.drawPath(path, topPaint!!)

            } else if (current.valueFloat < 0) {
                val convertedVal = if (max == 2.0f) {
                    if (current.valueFloat < -2) {
                        -2.0f
                    } else {
                        current.valueFloat
                    }
                } else if (max == 4.0f) {
                    if (current.valueFloat < -4) {
                        -4.0f
                    } else {
                        current.valueFloat
                    }
                } else {
                    current.valueFloat
                }

                val percent = (convertedVal / max)
                val convertedHeight = (4.0f / 5.0f) * centerY

                val bottom = percent * convertedHeight

                val rectBottom = RectF().apply {
                    left = x - chartLineWidth / 2f
                    this.top = centerY
                    right = x + chartLineWidth / 2f
                    this.bottom = centerY - bottom
                }


                val radius = chartLineWidth / 2f
                val corners = floatArrayOf(
                    0f, 0f,   // Top left radius in px
                    0f, 0f,   // Top right radius in px
                    radius, radius,     // Bottom right radius in px
                    radius, radius      // Bottom left radius in px
                )

                val path = Path()
                path.addRoundRect(rectBottom, corners, Path.Direction.CW)
                canvas.drawPath(path, bottomPaint!!)
            } else {
                if (!current.index.isNullOrEmpty()) {
                    canvas.drawCircle(x, centerY, dip2px(2f).toFloat(), pointColor!!)
                }
            }


            val xText = list[i].index ?: ""
            xTextPaint?.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint?.color = xTextColor and -0x7f000001
            canvas.drawText(
                xText, x - xTextBounds!!.width() / 2f, mHeight - bottomWith, xTextPaint!!
            )
        }
        if (offSet + moveOffSet < 0 || offSet + moveOffSet > (list.size - 1) * unitHLenth) {
            return
        }
        val position = Math.round((offSet + moveOffSet) / unitHLenth)
        val x = (mWith - leftWith - rightWith) / 2 + leftWith

        if (moveOffSet == 0f) {
            val xText = list[position].index ?: ""
            xTextPaint?.color = xTextColor
            xTextPaint?.getTextBounds(xText, 0, xText.length, xTextBounds)
            canvas.drawText(
                xText, x - xTextBounds!!.width() / 2f, mHeight - bottomWith, xTextPaint!!
            )

            if (list[position].valueFloat > 0) {

                val convertedVal = if (max == 2.0f) {
                    if (list[position].valueFloat > 2.5) {
                        2.5f
                    } else {
                        list[position].valueFloat
                    }
                } else if (max == 4.0f) {
                    if (list[position].valueFloat > 5) {//
                        5.0f
                    } else {
                        list[position].valueFloat
                    }
                } else {
                    list[position].valueFloat
                }

                val percent = (convertedVal / max)
                val convertedHeight = (4.0f / 5.0f) * centerY

                val top = percent * convertedHeight

                //val top = ((convertedVal * 20) / 100) * centerY

                val rectTop = RectF().apply {
                    left = x - chartLineWidth / 2f
                    this.top = centerY - top
                    right = x + chartLineWidth / 2f
                    bottom = centerY//mHeight - bottomWith - chartLineWidth / 2f
                }

                val radius = chartLineWidth / 2f
                val corners = floatArrayOf(
                    radius, radius,   // Top left radius in px
                    radius, radius,   // Top right radius in px
                    0f, 0f,     // Bottom right radius in px
                    0f, 0f      // Bottom left radius in px
                )

                val path = Path()
                path.addRoundRect(rectTop, corners, Path.Direction.CW)
                canvas.drawPath(path, topSelectedPaint!!)

            } else if (list[position].valueFloat < 0) {
                val convertedVal = if (max == 2.0f) {
                    if (list[position].valueFloat < -2) {
                        -2.0f
                    } else {
                        list[position].valueFloat
                    }
                } else if (max == 4.0f) {
                    if (list[position].valueFloat < -4) {
                        -4.0f
                    } else {
                        list[position].valueFloat
                    }
                } else {
                    list[position].valueFloat
                }

                val percent = (convertedVal / max)
                val convertedHeight = (4.0f / 5.0f) * centerY

                val bottom = percent * convertedHeight


                val rectBottom = RectF().apply {
                    left = x - chartLineWidth / 2f
                    this.top = centerY
                    right = x + chartLineWidth / 2f
                    this.bottom = centerY - bottom//mHeight - bottomWith - chartLineWidth / 2f
                }


                val radius = chartLineWidth / 2f
                val corners = floatArrayOf(
                    0f, 0f,   // Top left radius in px
                    0f, 0f,   // Top right radius in px
                    radius, radius,     // Bottom right radius in px
                    radius, radius      // Bottom left radius in px
                )

                val path = Path()
                path.addRoundRect(rectBottom, corners, Path.Direction.CW)
                canvas.drawPath(path, bottomSelectedPaint!!)
            } else {
                if (!list[position].index.isNullOrEmpty()) {
                    canvas.drawCircle(x, centerY, dip2px(2f).toFloat(), pointSelectedColor!!)
                }
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