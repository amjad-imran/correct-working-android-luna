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
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LOGS.w
import com.oreo.data.model.PeriodTempChartModel
import com.oreo.ui.custom.Section
import com.oreo.ui.custom.TempPeriodCombineModel
import com.oreo.ui.femalehealth.cycletracker.CyclePhase
import kotlin.math.abs
import kotlin.math.min

class PeriodTempLineChart : View {
    private lateinit var workoutPaint: Paint
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
    lateinit var bgLineDotted: Paint

    var max: Float = 0.0f
    private var xMin = 0.0f

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

    lateinit var xTextPaint: Paint
    lateinit var xTextPaint2: Paint
    private var xLinePaint: Paint? = null
    lateinit var gridPaint: Paint
    private var centerLinePaint: Paint? = null
    private var centerLineColor = 0
    private var fillColorStart = 0
    private var fillColorEnd = 0
    private var centerLineWidth = 0f
    private var innerCirclePaint: Paint? = null
    private var outCirclePaint: Paint? = null
    lateinit var chartLinePaint: Paint
    private var scaleNodePaint: Paint? = null
    private var glowDotBitmapAbnormal: Bitmap? = null
    private var glowDotBitmapNormal: Bitmap? = null
    private var onChartScrollChangedListener: ScrollListenerPeriodTemp? = null
    private val path = Path()
    private var unitHLenth = 0f
    private var indicatorUnitLength = 0f

    private var sectionsList: MutableList<Section> = ArrayList<Section>()


    private var showAvgValueText = false
    private var startFromRight = false
    private var alwaysShowCircle = true
    private var mWith = 0
    private var mHeight = 0

    private var offSet = 0f
    private var indicatorOffSet = 0f

    private var xTextBounds: Rect? = null

    private val list: MutableList<PeriodTempChartModel> = ArrayList()
    private var prefixCount = 0
    private var mCurrentPos = -1
    private var suffixCount = 0
    private var canScroll = true
    private var showXAxis = true
    private var titleWidth = 0f
    lateinit var bgLine: Paint
    lateinit var bgLineVertical: Paint

    private val fillPath = Path()
    lateinit var chartLineFillPaint: Paint
    private var lGFollecular: LinearGradient? = null
    private var lGLuteal: LinearGradient? = null
    private var resMap = HashMap<Int, Triple<LinearGradient, Bitmap?, String?>>()
    private var bitmapMap = HashMap<Int, Bitmap>()


    //    private int maxValue;
    //    private int minValue;
    private var avgValue = 0

    lateinit var normalBarPaint: Paint


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
        rectF = RectF()

        ta.recycle()
        initPaint()
    }

    private fun initPaint() {

        workoutPaint = Paint()
        workoutPaint.setColorFilter(PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN))


        chartLineFillPaint = Paint()
        chartLineFillPaint.style = Paint.Style.FILL
        chartLineFillPaint.isAntiAlias = true

        bgLine = Paint().apply {
            this.color = Color.parseColor("#19ffffff")
        }
        bgLineDotted = Paint().apply {
            this.color = Color.parseColor("#b3ffffff")
            this.style = Paint.Style.STROKE
            this.setAlpha(80)
            this.strokeWidth = dip2px(2f).toFloat()
            this.setPathEffect(DashPathEffect(floatArrayOf(2f, 2f), 0f))
        }
        bgLineVertical = Paint().apply {
            this.color = Color.parseColor("#b3ffffff")
        }

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
        xTextPaint.textSize = xTextSize
        xTextPaint.setTypeface(fontGilroy)
        xTextPaint.isAntiAlias = true

        xTextPaint2 = Paint()
        xTextPaint2.textSize = xTextSize2
        xTextPaint2.setTypeface(fontGilroy)
        xTextPaint2.isAntiAlias = true

        xLinePaint = Paint()
        xLinePaint!!.color = xLineColor

        gridPaint = Paint()
        gridPaint.color = gridColor

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
        chartLinePaint.strokeWidth = chartLineWidth
        chartLinePaint.color = chartLineColor
        chartLinePaint.isAntiAlias = true
        chartLinePaint.style = Paint.Style.STROKE


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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        generateResMap()
        drawBg(canvas)
        drawBottom(canvas)
        drawLeft(canvas)
        drawContent(canvas)
        drawRight(canvas)
    }

    private fun generateResMap() {
        if (sectionsList.isEmpty()) return
        var section: Section
        for (i in sectionsList.indices) {
            section = sectionsList[i]
            resMap[i] = Triple(
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
        }
    }


    fun updateData(
        datas: ArrayList<PeriodTempChartModel>,
        prefixList: ArrayList<PeriodTempChartModel>,
        suffixList: ArrayList<PeriodTempChartModel>,
        sectionList: ArrayList<Section>,
        currentPos: Int,
        maxValue: Float
    ) {
        this.sectionsList.clear()
        this.sectionsList.addAll(sectionList)

        list.clear()
        list.addAll(prefixList)
        list.addAll(datas)
        list.addAll(suffixList)
        prefixCount = prefixList.size
        suffixCount = suffixList.size

        val calculatedMax = getMaxValue(maxValue)
        xMin = -1f * calculatedMax
        max = calculatedMax

        //calculate max and min

        if (currentPos != -1) {
            mCurrentPos = currentPos
            moveToPosition(currentPos)
        }

        postInvalidate()
    }

    private fun getMaxValue(value: Float): Float {
        return when (value) {
            in 0.0f..2.5f -> {
                2.5f
            }

            in 2.6f..5.0f -> {
                5f
            }

            in 5.1f..10.0f -> {
                10f
            }

            in 10.1f..20.0f -> {
                20f
            }

            in 20.1f..50.0f -> {
                50f
            }

            else -> {
                80f
            }
        }
    }

    fun setOnChartScrollChangedListener(listener: ScrollListenerPeriodTemp?) {
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

        lGFollecular = LinearGradient(
            0f,
            0f,
            0f,
            mHeight - bottomWith,
            Color.parseColor("#99ff9252"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )

        lGLuteal = LinearGradient(
            0f,
            0f,
            0f,
            mHeight - bottomWith,
            Color.parseColor("#99a38cff"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }


    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint!!)
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
    }


    private fun drawLeft(canvas: Canvas) {
        getPointsBetween(max).forEach {
            val axisY =
                mHeight - bottomWith - (getCalculatedMax(it) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                    max
                ) - 0)
            drawHorizontalTextWithLine(
                canvas, if (it > 0) {
                    "+$it"
                } else {
                    "$it"
                }, axisY
            )
        }

        val axisYZero =
            mHeight - bottomWith - (getCalculatedMax(0f) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                max
            ) - 0)

        canvas.drawLine(
            leftWith,
            axisYZero,
            mWith - rightWith,
            axisYZero,
            bgLineDotted
        )
    }

    private fun drawHorizontalTextWithLine(
        canvas: Canvas,
        text: String,
        bottomHeight: Float,
        isTop: Boolean = false,
        isBottom: Boolean = false
    ) {

        canvas.drawLine(
            leftWith,
            bottomHeight,
            mWith - rightWith,
            bottomHeight,
            bgLine
        )
        xTextPaint.color = Color.parseColor("#a3ffffff")
        xTextPaint.getTextBounds(text, 0, text.length, xTextBounds)
        val yPos: Float = if (isTop) {
            bottomHeight + xTextBounds!!.height() / 2f + dip2px(4f)
        } else if (isBottom) {
            bottomHeight + xTextBounds!!.height() / 2f - dip2px(4f)
        } else
            bottomHeight + xTextBounds!!.height() / 2f

        val textStart = mWith.toFloat() - xTextBounds!!.width() - dip2px(2f)
        canvas.drawText(
            text,
            textStart,
            yPos,
            xTextPaint
        )
    }

    lateinit var rectF: RectF

    private fun drawContent(canvas: Canvas) {
        if (list.isNullOrEmpty()) {
            return
        }

        var divisor = 0.5f
        var firstPosition = 0
        val tempOffset = offSet + moveOffSet
        if (startFromRight) {
            divisor = (hCount - 1) * 1f / hCount
        }

        val imageSize = dip2px(16f)
        for (i in sectionsList.indices) {
            val section = sectionsList[i]
            val calculatedEnd = /*if (section.end < 13) {
                section.end + 1
            } else {*/
                section.end
            /*}*/

            val sectionStartX =
                offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (15 + section.start) * unitHLenth

            val sectionEndX =
                offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (15 + section.end) * unitHLenth

            rectF.left = sectionStartX/*section.start * unitHLenth + leftWit*/
            rectF.top = topWith
            rectF.right = sectionEndX/*rectF.left + (calculatedEnd - section.start) * unitHLenth*/
            rectF.bottom = mHeight - bottomWith
            chartLineFillPaint.setShader(resMap!![i]!!.first)
            canvas.drawRect(rectF, chartLineFillPaint)

            rectF.left = sectionStartX/*section.start * unitHLenth + leftWith*/
            rectF.top = topWith - dip2px(1f)
            rectF.right = sectionEndX/*rectF.left + (calculatedEnd - section.start) * unitHLenth*/
            rectF.bottom = topWith + dip2px(1f)
            gridPaint.color = section.color
            canvas.drawRect(rectF, gridPaint)



            rectF.left = (rectF.right + rectF.left) / 2 - imageSize / 2f
            rectF.top = topWith - imageSize - dip2px(10f)
            rectF.right = rectF.left + imageSize
            rectF.bottom = rectF.top + imageSize

            val imageUrl = resMap[i]?.third
            val bitmap = bitmapMap[i]
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, null, rectF, workoutPaint)
            } else {
                if (imageUrl.isNullOrEmpty()) {
                    if (resMap[i]!!.second != null) {
                        canvas.drawBitmap(resMap[i]!!.second!!, null, rectF, null)
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



        if (tempOffset > (mWith - leftWith - rightWith) * divisor + unitHLenth) {
            firstPosition =
                ((tempOffset - (mWith - leftWith - rightWith) * divisor) / unitHLenth).toInt()
        }
        val lastPosition = min((firstPosition + hCount + 2).toDouble(), list.size.toDouble())
            .toInt()
        var current: PeriodTempChartModel
        var next: PeriodTempChartModel

        val yLineZero =
            mHeight - bottomWith - (getCalculatedMax(
                0f
            ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)


        for (i in firstPosition until lastPosition) {
            current = list[i]
            val x =
                offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - i * unitHLenth

            canvas.drawLine(
                x,
                topWith,
                x,
                mHeight - bottomWith,
                bgLine
            )

            val y =
                mHeight - bottomWith - (getCalculatedMax(
                    current.value ?: 0f
                ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)
            path.reset()
            path.moveTo(x, y)
            if (i < list.size - 1) {
                next = list[i + 1]
                if (current.value != null && next.value != null) {
                    val x1 =
                        offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth
                    val y1 =
                        mHeight - bottomWith - (getCalculatedMax(
                            next.value ?: 0f
                        ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)
                    //path.lineTo(x1, y1)
                    path.cubicTo(x1 + (x - x1) / 1.5f, y, x - (x - x1) / 1.5f, y1, x1, y1)

                    if (current.phase == CyclePhase.FOLLECULAR && next.phase == CyclePhase.FOLLECULAR) {
                        chartLinePaint.color = Color.parseColor("#ff9252")
                        chartLineFillPaint.setShader(lGFollecular)
                    } else {
                        chartLinePaint.color = Color.parseColor("#a38cff")
                        chartLineFillPaint.setShader(lGLuteal)
                    }

                    fillPath.addPath(path)
                    fillPath.lineTo(x1, yLineZero)
                    fillPath.lineTo(x, yLineZero)
                    canvas.drawPath(fillPath, chartLineFillPaint)
                    fillPath.reset()


                    canvas.drawPath(path, chartLinePaint)
                }
            }

            if (showXAxis) {
                val dayText = list[i].day
                xTextPaint2.color = xTextColor and -0x7f000001

                xTextPaint2.getTextBounds(dayText, 0, dayText!!.length, xTextBounds)

                canvas.drawText(
                    dayText,
                    x - xTextBounds!!.width() / 2f,
                    (mHeight - bottomWith / 2) + xTextBounds!!.height() / 2,
                    xTextPaint2
                )
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
            mHeight - bottomWith - (getCalculatedMax(
                list[position].value ?: 0f
            ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0f)

        /**
         * Current selected point
         */
        if (list[position].value != null && (moveOffSet == 0f || (offSet + moveOffSet) == (list.size - 1) * unitHLenth)) {
            y = y1

            val width = (glowDotBitmapAbnormal!!.width / 2).toFloat()
            val height = (glowDotBitmapAbnormal!!.height / 2).toFloat()
            canvas.drawBitmap(glowDotBitmapNormal!!, x - width, y - height, scaleNodePaint)

        }
        if (moveOffSet == 0f && showXAxis) {
            val dayText = list[position].day

            canvas.drawLine(x, topWith, x, mHeight - bottomWith, bgLineVertical)


            xTextPaint2.color = xTextColor
            xTextPaint2.getTextBounds(dayText, 0, dayText!!.length, xTextBounds)

            canvas.drawText(
                dayText,
                x - xTextBounds!!.width() / 2f,
                (mHeight - bottomWith / 2) + xTextBounds!!.height() / 2,
                xTextPaint2
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
        val currentValue = list[scrollPosition]

        if (currentValue.date.isNullOrEmpty()) return

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

    private fun getCalculatedMax(value: Float): Float {
        return value + (abs(xMin))
    }

    private fun getPointsBetween(maxValue: Float): List<Float> {
        val points = ArrayList<Float>()

        when (maxValue) {
            in 0.0f..2.5f -> {
                points.add(+2.5f)
                points.add(+1.25f)
                points.add(0.0f)
                points.add(-1.25f)
                points.add(-2.5f)
            }

            in 2.6f..5.0f -> {
                points.add(+5f)
                points.add(+2.5f)
                points.add(0.0f)
                points.add(-2.5f)
                points.add(-5f)
            }

            in 5.1f..10.0f -> {
                points.add(+10f)
                points.add(+5f)
                points.add(0.0f)
                points.add(-5f)
                points.add(-10f)
            }

            in 10.1f..20.0f -> {
                points.add(+20f)
                points.add(+10f)
                points.add(0.0f)
                points.add(-10f)
                points.add(-20f)
            }

            in 20.1f..50.0f -> {
                points.add(+50f)
                points.add(+25f)
                points.add(0.0f)
                points.add(-25f)
                points.add(-50f)
            }

            else -> {
                points.add(+80f)
                points.add(+40f)
                points.add(0.0f)
                points.add(-40f)
                points.add(-80f)
            }
        }

        return points
    }
}

