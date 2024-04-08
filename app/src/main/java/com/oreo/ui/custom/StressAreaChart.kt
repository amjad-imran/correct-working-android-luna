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
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LOGS.w
import com.oreo.data.model.ChartModelStress
import com.oreo.data.model.Section
import com.oreo.data.model.StressDNDataModel
import kotlin.math.min

class StressAreaChart : View {
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
    private var centerLineWidth = 0f
    private var innerCirclePaint: Paint? = null
    private var outCirclePaint: Paint? = null

    private var lineCalmPaint: Paint? = null
    private var lineFocussedPaint: Paint? = null
    private var lineStressedPaint: Paint? = null


    lateinit var paintVerticalBar: Paint
    lateinit var paintHorizontalBar: Paint

    lateinit var barCalmPaint: Paint
    lateinit var barFocussedPaint: Paint
    lateinit var barStressedPaint: Paint

    private var chartLineFillPaint: Paint? = null
    private var scaleNodePaint: Paint? = null
    private var glowDotBitmap: Bitmap? = null
    private var onChartScrollChangedListener: ScrollListenerStress? = null

    private val calmPath = Path()
    private val focussedPath = Path()
    private val stressedPath = Path()

    private val calmFillPath = Path()
    private val focussedFillPath = Path()
    private val stressedFillPath = Path()

    private var unitHLenth = 0f
    private var indicatorUnitLength = 0f
    private var showAvgValueText = false
    private var showExtremeLine = false
    private var alwaysShowCircle = true
    private var mWith = 0
    private var mHeight = 0
    private var offSet = 0f
    private var indicatorOffSet = 0f
    private var xTextBounds: Rect? = null
    private val list: MutableList<ChartModelStress> = ArrayList()
    private var prefixCount = 0
    private var mCurrentPos = -1
    private var suffixCount = 0
    private var canScroll = true
    private var showXAxis = true
    private var titleWidth = 0f

    private val phase = 1.2f

    //    private int maxValue;
    //    private int minValue;
    private var linearGradientCalm: LinearGradient? = null
    private var linearGradientFocussed: LinearGradient? = null
    private var linearGradientStressed: LinearGradient? = null

    //

    lateinit var topCombinedPaint: Paint
    private var combineTextSize = 0f
    private var bitmapMap = HashMap<Int, Bitmap>()
    private var workoutPaint: Paint? = null
    private var rectF: RectF? = null
    private var resMap: MutableMap<Int, Triple<LinearGradient, Bitmap?, String?>>? = null
    private var stressDNDataModel: StressDNDataModel? = null

    constructor(context: Context?) : super(context) {
        resMap = HashMap()
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
        scaleNodeColor = ta.getColor(R.styleable.LineChart_scaleNodeColor, -0x1000000)
        scaleNodeRadius = ta.getDimension(R.styleable.LineChart_scaleNodeRadius, 3f)
        centerLineWidth = ta.getDimension(R.styleable.LineChart_centerLineWidth, 10f)
        centerLineColor = ta.getColor(R.styleable.LineChart_centerLineColor, -0x1000000)
        canScroll = ta.getBoolean(R.styleable.LineChart_canScroll, true)
        showXAxis = ta.getBoolean(R.styleable.LineChart_showXAxis, true)
        titleWidth = ta.getDimension(R.styleable.LineChart_titleWidth, 0f)
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false)
        showExtremeLine = ta.getBoolean(R.styleable.LineChart_showExtremeLine, true)
        alwaysShowCircle = ta.getBoolean(R.styleable.LineChart_alwaysShowCircle, true)
        ta.recycle()
        resMap = HashMap()
        initPaint()
    }

    private fun initPaint() {
        rectF = RectF()
        barCalmPaint = Paint().apply {
            setColor(Color.parseColor("#10c3a3"))
        }

        barFocussedPaint = Paint().apply {
            setColor(Color.parseColor("#ffed91"))
        }

        barStressedPaint = Paint().apply {
            setColor(Color.parseColor("#ffae62"))
        }

        paintVerticalBar = Paint().apply {
            setColor(Color.parseColor("#51ffffff"))
        }
        paintHorizontalBar = Paint().apply {
            setColor(Color.parseColor("#19ffffff"))
        }

        lineCalmPaint = Paint().apply {
            strokeWidth = dip2px(2f).toFloat()
            setColor(Color.parseColor("#CC3fe8b5"))
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        lineFocussedPaint = Paint().apply {
            strokeWidth = dip2px(2f).toFloat()
            setColor(Color.parseColor("#CCffed91"))
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        lineStressedPaint = Paint().apply {
            strokeWidth = dip2px(2f).toFloat()
            setColor(Color.parseColor("#CCffad60"))
            isAntiAlias = true
            style = Paint.Style.STROKE
        }


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

    fun updateData(
        datas: List<ChartModelStress>, prefixList: List<ChartModelStress>,
        suffixList: List<ChartModelStress>
    ) {
        list.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size

        max = 1440
        postInvalidate()
    }

    fun setOnChartScrollChangedListener(listener: ScrollListenerStress?) {
        onChartScrollChangedListener = listener
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        linearGradientCalm = LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            Color.parseColor("#CC155f61"),
            Color.parseColor("#CC081e30"),
            Shader.TileMode.CLAMP
        )
        linearGradientFocussed = LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            Color.parseColor("#CC6a6640"),
            Color.parseColor("#CC19262c"),
            Shader.TileMode.CLAMP
        )
        linearGradientStressed = LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            Color.parseColor("#CC866040"),
            Color.parseColor("#CC26262b"),
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
        generateResMap()
        drawBg(canvas)
        drawBottom(canvas)
        //drawLeft(canvas)
        drawContent(canvas)
        drawRight(canvas)
    }

    private fun generateResMap() {
        /* if (stressDNDataModel?.sections == null) return
         var section: Section
         for (i in stressDNDataModel!!.sections!!.indices) {
             section = stressDNDataModel!!.sections!![i]
             resMap!![i] = Triple(
                 LinearGradient(
                     0f,
                     0f,
                     0f,
                     mHeight - bottomWith,
                     section.color,
                     Color.TRANSPARENT,
                     Shader.TileMode.CLAMP
                 ), BitmapFactory.decodeResource(resources, section.imageRes), section.imageUrl
             )
         }*/
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint!!)
        canvas.drawLine(0f, topWith, mWith.toFloat(), topWith, paintHorizontalBar)
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
        val maxStr = "24"

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
    }

    private fun getYAxisValue(value: Int): Float {
        return (mHeight - bottomWith - (value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin))
    }

    private fun drawContent(canvas: Canvas) {

        if (list.size <= 0) {
            return
        }
        var firstPosition = 0
        val tempOffset = offSet + moveOffSet
        val divisor = 0.5f

        if (tempOffset > (mWith - leftWith - rightWith) * divisor + unitHLenth) {
            firstPosition =
                ((tempOffset - (mWith - leftWith - rightWith) * divisor) / unitHLenth).toInt()
        }
        val lastPosition = min((firstPosition + hCount + 2).toDouble(), list.size.toDouble())
            .toInt()
        var current: ChartModelStress
        var next: ChartModelStress
        for (i in firstPosition until lastPosition) {
            current = list[i]
            val x =
                offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - i * unitHLenth
            val calmY = getYAxisValue(current.calm)

            val focussedY =
                getYAxisValue(current.calm + current.focussed)

            val stressedY =
                getYAxisValue(current.calm + current.focussed + current.stressed)

            LOGS.d("Y_VALUES $calmY - $focussedY - $stressedY")

            calmPath.reset()
            calmFillPath.reset()

            focussedPath.reset()
            focussedFillPath.reset()

            stressedPath.reset()
            stressedFillPath.reset()


            calmPath.moveTo(x, calmY)
            focussedPath.moveTo(x, focussedY)
            stressedPath.moveTo(x, stressedY)

            if (i < list.size - 1) {
                next = list[i + 1]
                val isCurrentAllZero =
                    current.calm == 0 && current.focussed == 0 && current.stressed == 0
                val isNextAllZero = next.calm == 0 && next.focussed == 0 && next.stressed == 0

                if (!isCurrentAllZero && !isNextAllZero) {

                    if (current.stressed >= 0 && next.stressed >= 0) {
                        val x1 =
                            offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth
                        val y1 =
                            getYAxisValue(next.calm + next.focussed + next.stressed)
                        val nextYFocussed = getYAxisValue(next.calm + next.focussed)

                        LOGS.d("Y_VALUES Stressed -> $y1")

                        stressedPath.cubicTo(
                            x1 + (x - x1) / phase,
                            stressedY,
                            x - (x - x1) / phase,
                            y1,
                            x1,
                            y1
                        )
                        stressedFillPath.addPath(stressedPath)
                        //draw fill first
                        stressedFillPath.lineTo(x1, nextYFocussed)
                        stressedFillPath.lineTo(x, focussedY)
                        chartLineFillPaint!!.setShader(linearGradientStressed)
                        canvas.drawPath(stressedFillPath, chartLineFillPaint!!)
                        canvas.drawPath(stressedPath, lineStressedPaint!!)
                    }
                    if (current.focussed >= 0 && next.focussed >= 0) {
                        val x1 =
                            offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth
                        val nextYCalm = getYAxisValue(next.calm)
                        val y1 = getYAxisValue(next.calm + next.focussed)
                        LOGS.d("Y_VALUES Focussed -> $y1")

                        focussedPath.cubicTo(
                            x1 + (x - x1) / phase,
                            focussedY,
                            x - (x - x1) / phase,
                            y1,
                            x1,
                            y1
                        )
                        focussedFillPath.addPath(focussedPath)

                        focussedFillPath.lineTo(x1, nextYCalm)
                        focussedFillPath.lineTo(x, calmY)
                        chartLineFillPaint!!.setShader(linearGradientFocussed)
                        canvas.drawPath(focussedFillPath, chartLineFillPaint!!)
                        canvas.drawPath(focussedPath, lineFocussedPaint!!)
                    }

                    if (current.calm >= 0 && next.calm >= 0) {
                        val x1 =
                            offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth
                        val y1 = getYAxisValue(next.calm)
                        LOGS.d("Y_VALUES Calm -> $y1")
                        calmPath.cubicTo(
                            x1 + (x - x1) / phase,
                            calmY,
                            x - (x - x1) / phase,
                            y1,
                            x1,
                            y1
                        )
                        calmFillPath.addPath(calmPath)
                        //draw fill first
                        calmFillPath.lineTo(x1, mHeight - bottomWith)
                        calmFillPath.lineTo(x, mHeight - bottomWith)
                        chartLineFillPaint!!.setShader(linearGradientCalm)
                        canvas.drawPath(calmFillPath, chartLineFillPaint!!)
                        canvas.drawPath(calmPath, lineCalmPaint!!)
                    }
                } else {
                    list.getOrNull(i - 1)?.let { prev ->
                        val isPrevAllZero =
                            prev.calm == 0 && prev.focussed == 0 && prev.stressed == 0
                        if (!isCurrentAllZero && isPrevAllZero) {
                            showDataBar(canvas, current, x)
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
                if (list[i].formattedDate != null) {
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
            canvas.drawLine(x, topWith, x, mHeight - bottomWith, paintVerticalBar)
            if (showLastCircle) {
                if (i == 1 && current.calm > 0) {
                    val width = (glowDotBitmap!!.getWidth() / 2).toFloat()
                    val height = (glowDotBitmap!!.getHeight() / 2).toFloat()
                    canvas.drawBitmap(glowDotBitmap!!, x - width, calmY - height, scaleNodePaint)

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

        showSelectedBar(canvas, list[position], x)

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
            if (list[position].formattedDate != null) {
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

        return

        //show combined top views
        val imageSize = dip2px(16f)
        for (i in stressDNDataModel!!.sections!!.indices) {
            val section = stressDNDataModel!!.sections!![i]
            val calculatedEnd = if (section.end < 95) {
                section.end + 1
            } else {
                section.end
            }


            rectF?.left = section.start * unitHLenth
            rectF?.top = topWith
            rectF?.right = rectF!!.left + (calculatedEnd - section.start) * unitHLenth
            rectF?.bottom = mHeight - bottomWith

            chartLineFillPaint?.setShader(resMap!![i]!!.first)
            chartLineFillPaint?.let { canvas.drawRect(rectF!!, it) }

            rectF?.left = section.start * unitHLenth
            rectF?.top = topWith - dip2px(1f)
            rectF?.right = rectF!!.left + (calculatedEnd - section.start) * unitHLenth
            rectF?.bottom = topWith + dip2px(1f)
            gridPaint?.color = section.color
            gridPaint?.let { canvas.drawRect(rectF!!, it) }




            if (section.type.equals("combined", true)) {

                val text = "${section.count}"
                topCombinedPaint.getTextBounds(text, 0, text.length, xTextBounds)
                canvas.drawText(
                    text,
                    (rectF!!.left + rectF!!.right) / 2 - xTextBounds!!.width() / 2f,
                    rectF!!.top - xTextBounds!!.height(),
                    topCombinedPaint
                )

            } else {
                rectF!!.left = (rectF!!.right + rectF!!.left) / 2 - imageSize / 2f
                rectF!!.top = topWith - imageSize - dip2px(10f)
                rectF!!.right = rectF!!.left + imageSize
                rectF!!.bottom = rectF!!.top + imageSize

                val imageUrl = resMap!![i]?.third
                val bitmap = bitmapMap[i]
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, rectF!!, workoutPaint)
                } else {
                    if (imageUrl.isNullOrEmpty()) {
                        if (resMap!![i]!!.second != null) {
                            canvas.drawBitmap(resMap!![i]!!.second!!, null, rectF!!, null)
                        }
                    } else {
                        Glide.with(context)
                            .asBitmap()
                            .load(imageUrl)
                            .into(object : CustomTarget<Bitmap?>(imageSize, imageSize) {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap?>?
                                ) {
                                    bitmapMap[i] = resource
                                    postInvalidate()
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    }
                }
            }
        }
    }

    private fun showDataBar(canvas: Canvas, current: ChartModelStress, currentX: Float) {
        val widthHalf = dip2px(0.5f)

        if (current.calm > 0) {
            val calmRect = RectF().apply {
                left = currentX - widthHalf
                right = currentX + widthHalf
                bottom = mHeight - bottomWith
                top = getYAxisValue(current.calm)
            }
            canvas.drawRoundRect(calmRect, 10f, 10f, barCalmPaint)
        }

        if (current.focussed > 0) {
            val calmRect = RectF().apply {
                left = currentX - widthHalf
                right = currentX + widthHalf
                bottom = getYAxisValue(current.calm) - dip2px(2f)
                top = getYAxisValue(current.calm + current.focussed)
            }
            canvas.drawRoundRect(calmRect, 10f, 10f, barFocussedPaint)
        }

        if (current.stressed > 0) {
            val calmRect = RectF().apply {
                left = currentX - widthHalf
                right = currentX + widthHalf
                bottom = getYAxisValue(current.calm + current.focussed) - dip2px(2f)
                top = getYAxisValue(current.calm + current.focussed + current.stressed)
            }
            canvas.drawRoundRect(calmRect, 10f, 10f, barStressedPaint)
        }
    }

    private fun showSelectedBar(canvas: Canvas, current: ChartModelStress, currentX: Float) {
        if ((moveOffSet == 0f || offSet + moveOffSet == (list.size - 1) * unitHLenth)) {

            if (current.calm > 0) {
                val calmRect = RectF().apply {
                    left = currentX - dip2px(2f)
                    right = currentX + dip2px(2f)
                    bottom = mHeight - bottomWith
                    top = getYAxisValue(current.calm)
                }
                canvas.drawRoundRect(calmRect, 10f, 10f, barCalmPaint)
            }

            if (current.focussed > 0) {
                val calmRect = RectF().apply {
                    left = currentX - dip2px(2f)
                    right = currentX + dip2px(2f)
                    bottom = getYAxisValue(current.calm) - dip2px(2f)
                    top = getYAxisValue(current.calm + current.focussed)
                }
                canvas.drawRoundRect(calmRect, 10f, 10f, barFocussedPaint)
            }

            if (current.stressed > 0) {
                val calmRect = RectF().apply {
                    left = currentX - dip2px(2f)
                    right = currentX + dip2px(2f)
                    bottom = getYAxisValue(current.calm + current.focussed) - dip2px(2f)
                    top = getYAxisValue(current.calm + current.focussed + current.stressed)
                }
                canvas.drawRoundRect(calmRect, 10f, 10f, barStressedPaint)
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


interface ScrollListenerStress {
    fun onPositionSelected(position: Int, chartModel: ChartModelStress?)
    fun onScrolling(position: Int, chartModel: ChartModelStress?)
}


