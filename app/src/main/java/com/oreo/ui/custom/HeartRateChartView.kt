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
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.noisefit.luna.R
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.SleepChartModel
import com.oreo.ui.sleep2.internal.DEFAULT_LONG_PRESS_TIMEOUT
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.Collections
import kotlin.math.roundToInt


class HeartRateChartView : View {
    private var overlayLinePaint: Paint? = null
    private val toolTipList = ArrayList<Triple<Float, String, Int>>()
    private var showLowCircle = false
    private var showHighCircle = false
    private var yTextColor = 0
    private var bgColor = 0
    private var bgLeftColor = 0
    private var bgRightColor = 0
    private var bgTopColor = 0
    private var bgBottomColor = 0
    private var xTextColor = 0
    private var chartLineColor = 0
    private var chartLineColorI = 0
    private var chartLineWidth = 10f
    private var scaleNodeColor = 0
    private var outCirclePaint: Paint? = null
    private var gridColor = 0
    private var graphOriginalWidth = 0f
    var max = 0
    private var xMin = 0
    private var leftWith = 0f
    private var rightWith = 0f
    private var bottomWith = 0f
    private var topWith = 0f
    private var xTextSize = 0f
    private var noDataSize = 0f
    private var scaleNodeRadius = 0f
    private var bgPaint: Paint? = null
    private var bgLeftPaint: Paint? = null
    private var bgRightPaint: Paint? = null
    private var bgTopPaint: Paint? = null
    private var bgBottomPaint: Paint? = null
    private var xTextPaint: Paint? = null
    private var noDataPaint: Paint? = null
    private var gridPaint: Paint? = null
    private var centerLinePaint: Paint? = null
    private var centerLineColor = 0
    private var fillColorStart = 0
    private var fillColorEnd = 0
    private var centerLineWidth = 0f
    lateinit var chartLinePaint: Paint
    lateinit var chartLineFillPaint: Paint
    private var avgBackPaint: Paint? = null
    private var scaleNodePaint: Paint? = null
    private val onChartScrollChangedListener: ScrollListener? = null
    private val path = Path()
    private val fillPath = Path()
    private var unitHLenth = 0f
    private var mWith = 0
    private var mHeight = 0
    private var xTextBounds: Rect? = null
    private var sleepModel: SleepChartModel? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var mHasDummyData = true
    private val list: MutableList<ChartModel?> = ArrayList()
    private var showXAxis = true
    private lateinit var calmDot: Bitmap
    private lateinit var avgBackBitmap: Bitmap

    //    private int xMax;
    //    private int xMin;
    private var avgValue = 0
    private var lastMinValueIndex = 0
    private var lastMaxValueIndex = 0
    private var maxValue = 0
    private var minValue = 0
    private var linearGradient: LinearGradient? = null
    private var linearGradientI: LinearGradient? = null
    private var linearGradientH: LinearGradient? = null
    private lateinit var paintCalm: Paint
    private lateinit var lowTopPaint: Paint
    private lateinit var avgTextPaint: Paint

    private lateinit var bgLine: Paint
    private lateinit var dotBitmap: Bitmap
    private lateinit var dotBitmap2: Bitmap
    lateinit var rightBackBitmap: Bitmap

    private lateinit var mTextPaint: Paint
    private lateinit var mTextPaintEdge: Paint
    private lateinit var edgeTextBackPaint: Paint
    private val effect =
        DashPathEffect(floatArrayOf(dip2px(1f).toFloat(), dip2px(2f).toFloat()), 0f)

    private var chartLineGradient: LinearGradient? = null
    private var chartLineGradientInteracting: LinearGradient? = null

    private val highlightIndexs: MutableList<Int> = ArrayList()
    private var highlightColor = 0
    private var highlightPaintDot: Paint? = null
    private var isHighlighted = false
    private lateinit var restLineColor: Paint


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


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawTop(canvas)
        drawBottom(canvas)
        drawRight(canvas)
        drawContent(canvas)
        drawHorizontalLines(canvas)
        drawOverlay(canvas)
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SleepLineChart)
        dotBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_dot)
        dotBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_lowest_dot)


        bgColor = ta.getColor(R.styleable.SleepLineChart_bgColor, -0x1)
        bgLeftColor = ta.getColor(R.styleable.SleepLineChart_bgLeftColor, -0x1)
        bgRightColor = ta.getColor(R.styleable.SleepLineChart_bgRightColor, 0xfffffff)
        bgTopColor = ta.getColor(R.styleable.SleepLineChart_bgTopColor, -0x1)
        bgBottomColor = ta.getColor(R.styleable.SleepLineChart_bgBottomColor, -0x1)
        xTextColor = ta.getColor(R.styleable.SleepLineChart_xTextColor, -0x1000000)
        gridColor = ta.getColor(R.styleable.SleepLineChart_gridColor, -0xff01)
        max = ta.getInt(R.styleable.SleepLineChart_xMax, 10)
        xMin = ta.getInt(R.styleable.SleepLineChart_xMin, 0)
        xTextSize = ta.getDimension(R.styleable.SleepLineChart_xTextSize, 8f)
        noDataSize = ta.getDimension(R.styleable.SleepLineChart_noDataSize, 12f)
        leftWith = ta.getDimension(R.styleable.SleepLineChart_leftWith, 16f)
        rightWith = ta.getDimension(R.styleable.SleepLineChart_rightWith, 8f)
        bottomWith = ta.getDimension(R.styleable.SleepLineChart_bottomWith, 16f)
        topWith = ta.getDimension(R.styleable.SleepLineChart_topWith, 8f)
        chartLineColor = ta.getColor(R.styleable.SleepLineChart_chartLineColor, -0x1000000)
        chartLineWidth = ta.getDimension(R.styleable.SleepLineChart_chartLineWidth, 10f)
        scaleNodeColor = ta.getColor(R.styleable.SleepLineChart_scaleNodeColor, -0x1000000)
        scaleNodeRadius = ta.getDimension(R.styleable.SleepLineChart_scaleNodeRadius, 3f)
        centerLineWidth = ta.getDimension(R.styleable.SleepLineChart_centerLineWidth, 2f)
        centerLineColor = ta.getColor(R.styleable.SleepLineChart_centerLineColor, -0x1000000)
        fillColorStart = ta.getColor(R.styleable.SleepLineChart_fillColorStart, -0x7f000001)
        fillColorEnd = ta.getColor(R.styleable.SleepLineChart_fillColorEnd, 0x00000000)
        showXAxis = ta.getBoolean(R.styleable.SleepLineChart_showXAxis, true)
        yTextColor = ta.getColor(R.styleable.LineChart_yTextColor, -0x1000000)
        ta.recycle()
        initPaint()
        initBitmap()
    }

    private fun initBitmap() {
        val res = resources
        val dimen = dip2px(30f)
        calmDot = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                res, R.drawable.ic_pink_hot_dot
            ), dimen, dimen, true
        )

        avgBackBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                res, R.drawable.image_blur_avg
            ), dimen, dimen, true
        )
        rightBackBitmap =
            BitmapFactory.decodeResource(
                res,
                R.drawable.back_stress_left
            )
    }

    private fun initPaint() {
        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

        restLineColor = Paint().apply {
            color = Color.parseColor("#ff0000")
        }

        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(context, com.noisefit_commans.R.color.white_64)
        mTextPaint.textSize = dip2px(12f).toFloat()
        mTextPaint.setTypeface(fontGilroy)


        mTextPaintEdge = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintEdge.color = ContextCompat.getColor(context, com.noisefit_commans.R.color.white)
        mTextPaintEdge.setTypeface(fontGilroy)
        mTextPaintEdge.textSize = dip2px(12f).toFloat()

        edgeTextBackPaint = Paint()
        edgeTextBackPaint.color = Color.parseColor("#394653")


        bgPaint = Paint()
        bgPaint!!.color = bgColor
        bgLeftPaint = Paint()
        bgLeftPaint!!.color = bgLeftColor
        bgRightPaint = Paint()
        bgRightPaint!!.color = bgRightColor
        bgTopPaint = Paint()
        bgTopPaint!!.color = bgTopColor
        bgBottomPaint = Paint()
        bgBottomPaint!!.color = bgBottomColor

        xTextPaint = Paint()
        xTextPaint!!.textSize = xTextSize
        xTextPaint!!.setTypeface(fontGilroy)
        xTextPaint!!.isAntiAlias = true
        noDataPaint = Paint()
        noDataPaint!!.textSize = noDataSize
        noDataPaint!!.color = resources.getColor(R.color.white)
        noDataPaint!!.setTypeface(fontGilroy)
        noDataPaint!!.isAntiAlias = true
        gridPaint = Paint()
        gridPaint!!.color = gridColor
        centerLinePaint = Paint()
        centerLinePaint!!.color = centerLineColor
        centerLinePaint!!.strokeWidth = centerLineWidth
        centerLinePaint!!.style = Paint.Style.STROKE
        centerLinePaint!!.setPathEffect(DashPathEffect(floatArrayOf(2f, 6f), 0f))

        chartLinePaint = Paint()
        chartLinePaint!!.strokeWidth = chartLineWidth
        chartLinePaint!!.color = chartLineColor
        chartLinePaint!!.isAntiAlias = true
        chartLinePaint!!.style = Paint.Style.STROKE



        chartLineFillPaint = Paint()
        //        chartLineFillPaint.setColor(Color.GRAY);
        chartLineFillPaint!!.style = Paint.Style.FILL
        chartLineFillPaint!!.isAntiAlias = true
        avgBackPaint = Paint()
        avgBackPaint!!.style = Paint.Style.FILL
        avgBackPaint!!.color = Color.parseColor("#07121e")
        avgBackPaint!!.isAntiAlias = true
        scaleNodePaint = Paint()
        scaleNodePaint!!.color = scaleNodeColor
        scaleNodePaint!!.isAntiAlias = true
        paintCalm = Paint()
        paintCalm.textSize = xTextSize
        paintCalm.setTypeface(fontGilroy)
        paintCalm.color = Color.parseColor("#3fe8b5")

        lowTopPaint = Paint().apply {
            this.color = Color.parseColor("#ffffff")
        }

        avgTextPaint = Paint().apply {
            this.color = Color.parseColor("#ffffff")
            this.typeface = fontGilroy
            this.textSize = dip2px(14f).toFloat()
        }

        bgLine = Paint().apply {
            this.color = Color.parseColor("#19ffffff")
        }



        overlayLinePaint = Paint()
        overlayLinePaint!!.style = Paint.Style.STROKE
        overlayLinePaint!!.isAntiAlias = true
        overlayLinePaint!!.strokeWidth = 1f
        overlayLinePaint!!.color = Color.parseColor("#bad4f2")
//        overlayLinePaint!!.setPathEffect(DashPathEffect(floatArrayOf(20f, 10f), 2f))
        xTextBounds = Rect()
    }

    fun updateGraphColor(
        chartLineColor: Int, chartLineColorI: Int, fillColorStart: Int, fillColorEnd: Int
    ) {
        this.chartLineColor = chartLineColor
        this.chartLineColorI = chartLineColorI
        this.fillColorEnd = fillColorEnd
        this.fillColorStart = fillColorStart
        chartLinePaint = Paint()
        chartLinePaint!!.strokeWidth = chartLineWidth
        chartLinePaint!!.color = chartLineColor
        chartLinePaint!!.isAntiAlias = true
        chartLinePaint!!.style = Paint.Style.STROKE
        outCirclePaint = Paint()
        outCirclePaint!!.color = chartLineColor
        outCirclePaint!!.isAntiAlias = true
    }


    fun updateHighlight(indexList: List<Int>, color: Int) {
        highlightIndexs.clear()
        highlightIndexs.addAll(indexList)
        highlightColor = color
        if (mHeight > 0) {
            linearGradient = LinearGradient(
                0f,
                0f,
                0f,
                mHeight - bottomWith,
                highlightColor,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        highlightPaintDot = Paint().apply {
            setColorFilter(PorterDuffColorFilter(highlightColor, PorterDuff.Mode.SRC_IN))
        }
        isHighlighted = true
        postInvalidate()
    }

    fun removeHighlights() {
        highlightIndexs.clear()
        isHighlighted = false
        postInvalidate()
    }

    fun updateDataWithMax(
        datas: SleepChartModel?,
        maxOffset: Int,
        showHighCircle: Boolean,
        showLowCircle: Boolean,
        dummy: GraphDummyModel,
        averageValue: Int?,
        startTime: String?,
        endTime: String?
    ): Int {

        dotBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_dot)
        dotBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_lowest_dot)



        this.startTime = startTime
        this.endTime = endTime

        sleepModel = datas
        mHasDummyData = dummy.hasDummyData
        list.clear()
        list.addAll(sleepModel!!.list)
        this.showLowCircle = showLowCircle
        this.showHighCircle = showHighCircle
        maxValue = 0
        minValue = 0
        Collections.reverse(list)
        var item: ChartModel?
        var sum = 0
        var count = 0
        for (i in list.indices) {
            item = list[i]
            if (item!!.value == 0) {
                continue
            }
            sum += item.value
            count += 1
            if (maxValue == 0 && minValue == 0) {
                lastMinValueIndex = i
                lastMaxValueIndex = i
                maxValue = item.value
                minValue = item.value
            }
            if (item.value > maxValue) {
                lastMaxValueIndex = i
                maxValue = item.value
            }
            if (item.value > 0 && item.value < minValue) {
                lastMinValueIndex = i
                minValue = item.value
            }
        }

        if (averageValue == null) {
            if (count > 0) {
                avgValue = sum / count
            }
        } else {
            avgValue = averageValue
        }


//        xMax += maxOffset;
        if (mHasDummyData) {
            max = dummy.max
            xMin = dummy.min
        } else {
            max = maxValue + maxOffset
            xMin = minValue - maxOffset
        }
        if (xMin < 0) {
            xMin = 0
        }
        postInvalidate()

        return list.size - lastMinValueIndex
    }

    private var touchX = 0f
    private var isInteracting = false
    private var interactiveMode = false


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interactiveMode) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.x
                    handler.postDelayed(
                        mLongPressed, DEFAULT_LONG_PRESS_TIMEOUT
                    )
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isInteracting) {
                        touchX = event.x
                        invalidate()
                    }
                    return super.onTouchEvent(event)
                }

                MotionEvent.ACTION_UP -> {
                    resetState()
                    return super.onTouchEvent(event)
                }
            }
        } else {
            return super.onTouchEvent(event)
        }
        return false
    }

    private fun resetState() {
        lastSentValuePos = null
        handler.removeCallbacks(mLongPressed)
        isInteracting = false
        listener?.onValueSelected(0, false)
        touchX = 0.0f
        invalidate()
    }


    fun resetIfInteracting() {
        handler.removeCallbacks(mLongPressed)
        if (isInteracting) {
            lastSentValuePos = null
            isInteracting = false
            listener?.onValueSelected(0, false)
            touchX = 0.0f
            invalidate()
        }
    }

    fun setClickListener(listener: OnHeartRateChartClickAction?) {
        this.listener = listener
    }

    fun setInteractiveMode(interactiveMode: Boolean) {
        this.interactiveMode = interactiveMode
    }

    private var listener: OnHeartRateChartClickAction? = null

    private var vibrationUtils: VibrationUtils? = null


    private fun performHapticFeedbackCustom(value: Int) {
        if (value != 0) {
            vibrationUtils?.vibrate(HAPTIC_VIBRATION)


            /*this.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS
            )*/
        }
    }

    fun setVibrationUtil(vibrationUtils: VibrationUtils) {
        this.vibrationUtils = vibrationUtils
    }

    private var lastSentValuePos: Int? = null
    private val handler = Handler()
    private var mLongPressed = Runnable {
        isInteracting = true
        invalidate()

        vibrationUtils?.vibrate(HAPTIC_VIBRATION)
        val parent = parent
        parent.requestDisallowInterceptTouchEvent(true)
    }

    private fun getClickedValue(touchX: Float): Triple<Int, Int, String> {
        val index = findNumber(toolTipList, touchX)
        return if (index.first < 0) {
            lastSentValuePos = null
            Triple(0, 0, "")
        } else {
            Triple(index.first, list[index.first]!!.value, index.second)
        }

    }

    private fun drawOverlay(canvas: Canvas) {
        if (!isInteracting) return


        var calculatedTouchX = if (touchX < leftWith) {
            leftWith
        } else if (touchX > (mWith - rightWith)) {
            (mWith - rightWith)
        } else {
            touchX
        }


        /* if (touchX > leftWith && touchX < (mWith - rightWith)) {*/

        val value = getClickedValue(calculatedTouchX)
        var selectedPos = value.first
        var time = value.third
        var hrValue = value.second


        var showOverlay = true
        if (isHighlighted) {
            showOverlay = highlightIndexs.contains(list.size - 1 - value.first)
            if (showOverlay.not()) {
                val newTouchValue = getNextValue(highlightIndexs, list.size - 1 - value.first)
                if (newTouchValue != null) {
                    calculatedTouchX = newTouchValue.first
                    selectedPos = newTouchValue.second.first
                    time = newTouchValue.second.third
                    hrValue = newTouchValue.second.second
                    showOverlay = true
                }
            }

        }

        if (!showOverlay) {
            return
        }

        val rectF = RectF()
        rectF.left = calculatedTouchX
        rectF.right = calculatedTouchX
        rectF.top = topWith
        rectF.bottom = mHeight - bottomWith
        overlayLinePaint!!.color = Color.WHITE
        canvas.drawRect(rectF, overlayLinePaint!!)


        val y =
            mHeight - bottomWith - (hrValue - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

        if (hrValue != 0) {

            val bitmap = if (lastMinValueIndex == value.first) {
                dotBitmap2
            } else {
                dotBitmap
            }

            val paint = if (isHighlighted) {
                highlightPaintDot
            } else {
                null
            }

            canvas.drawBitmap(
                bitmap, calculatedTouchX - dotBitmap.width / 2, y - dotBitmap.height / 2, paint
            )

        }


        if (listener != null) {
            val position = selectedPos
            val selectedValue = hrValue
            //d("CLICKED_VALUE value value Touch $position $selectedValue")
            if (lastSentValuePos == null) {
                listener?.onValueSelected(selectedValue, true, time)
                lastSentValuePos = position
                performHapticFeedbackCustom(selectedValue)
            } else {
                if (lastSentValuePos != position) {
                    listener?.onValueSelected(selectedValue, true, time)
                    lastSentValuePos = position
                    performHapticFeedbackCustom(selectedValue)
                }
            }
        }/*}*/
    }

    /**
     * Pair(new touch position,Triple(newpos,value,time))
     */
    private fun getNextValue(
        highlightIndex: MutableList<Int>,
        value: Int
    ): Pair<Float, Triple<Int, Int, String>>? {
        var selectedPos: Int? = null

        for (i in 0 until highlightIndex.size) {

            if (highlightIndex[i] >= value) {
                selectedPos = highlightIndex[i]
                break
            }
        }

        if (selectedPos == null) return null
        val range = unitHLenth / 2//dip2px(3f)
        val selected = toolTipList.get(list.size - 1 - selectedPos!!)
        return Pair(
            selected.first - range,
            Triple(list.size - 1 - selectedPos!!, selected.third, selected.second)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        mWith = w
        mHeight = h
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint!!)
    }

    private fun drawTop(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), topWith, bgTopPaint!!)
    }

    private fun drawRight(canvas: Canvas) {
        canvas.drawRect(mWith - rightWith, 0f, mWith.toFloat(), mHeight.toFloat(), bgRightPaint!!)
    }


    fun initLineGradient() {

        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            mHeight.toFloat(),
            Color.parseColor("#ff7f96"),//intArrayOf(Color.parseColor("#99ff718b"), Color.parseColor("#00ff5f7c")),
            Color.TRANSPARENT/*floatArrayOf(0.3f, 0.6f)*/,
            Shader.TileMode.CLAMP
        )
        linearGradientI = LinearGradient(
            0f,
            0f,
            0f,
            mHeight.toFloat(),
            Color.parseColor("#80ff7f96"),//intArrayOf(Color.parseColor("#99ff718b"), Color.parseColor("#00ff5f7c")),
            Color.TRANSPARENT/*floatArrayOf(0.3f, 0.6f)*/,
            Shader.TileMode.CLAMP
        )

        linearGradientH = LinearGradient(
            0f,
            0f,
            0f,
            mHeight.toFloat(),
            highlightColor,//intArrayOf(Color.parseColor("#99ff718b"), Color.parseColor("#00ff5f7c")),
            Color.TRANSPARENT/*floatArrayOf(0.3f, 0.6f)*/,
            Shader.TileMode.CLAMP
        )


        var arrayDef = intArrayOf()
        var arrayDefI = intArrayOf()


        arrayDef = intArrayOf(
            Color.parseColor("#ff7f96"), Color.parseColor("#fc3559"), Color.parseColor("#fc3559")
        )
        arrayDefI = intArrayOf(
            Color.parseColor("#844B60"), Color.parseColor("#833947"), Color.parseColor("#832930")
        )


        chartLineGradient = LinearGradient(
            0f,
            topWith,
            0f,
            mHeight - bottomWith,
            arrayDef,
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        chartLineGradientInteracting = LinearGradient(
            0f,
            topWith,
            0f,
            mHeight - bottomWith,
            arrayDefI,
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun calculateX(index: Int): Float {
        return mWith - leftWith - rightWith + leftWith - index * unitHLenth
    }

    private fun calculateY(value: Int): Float {
        return mHeight - bottomWith - (value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
    }

    private fun drawCubicLine(canvas: Canvas, index: Int, x: Float, y: Float, value: Int) {
        val nextIndex = index + 1
        val next = list[nextIndex]!!

        if (value > 0 && next.value > 0) {
            val x1 = calculateX(nextIndex)
            val y1 = calculateY(next.value)

            path.cubicTo(x1 + (x - x1) / 1.5f, y, x - (x - x1) / 1.5f, y1, x1, y1)
            if (highlightIndexs.contains(list.size - 1 - index)) {
                drawHighlightedFill(canvas, x, x1, y, y1)
            } else {
                drawRegularFill(canvas, x, x1, y, y1)
            }
            canvas.drawPath(path, chartLinePaint)
        } else {
            if (highlightIndexs.contains(list.size - 1 - index)) {
                chartLinePaint.setShader(linearGradientH)
                chartLinePaint.color = highlightColor
            } else {
                if (!isHighlighted) {
                    if (isInteracting) {
                        chartLinePaint.setShader(chartLineGradientInteracting)
                    } else {
                        chartLinePaint.setShader(chartLineGradient)
                    }
                } else {
                    chartLinePaint.setShader(null)
                    chartLinePaint.color = Color.parseColor("#596f80")
                }
            }
            canvas.drawPoint(x, y, chartLinePaint)
        }
    }

    private fun drawHighlightedFill(canvas: Canvas, x: Float, x1: Float, y: Float, y1: Float) {
        fillPath.addPath(path)
        fillPath.lineTo(x1, mHeight - bottomWith)
        fillPath.lineTo(x, mHeight - bottomWith)
        chartLineFillPaint.setShader(linearGradientH)
        canvas.drawPath(fillPath, chartLineFillPaint)
        chartLinePaint.setShader(linearGradientH)
        chartLinePaint.color = highlightColor
        fillPath.reset()
    }

    private fun drawRegularFill(canvas: Canvas, x: Float, x1: Float, y: Float, y1: Float) {
        fillPath.addPath(path)
        fillPath.lineTo(x1, mHeight - bottomWith)
        fillPath.lineTo(x, mHeight - bottomWith)

        if (!isHighlighted) {
            if (isInteracting) {
                chartLinePaint.setShader(chartLineGradientInteracting)
                chartLineFillPaint.setShader(linearGradientI)
                chartLinePaint.color = chartLineColorI
            } else {
                chartLinePaint.setShader(chartLineGradient)
                chartLineFillPaint.setShader(linearGradient)
                chartLinePaint.color = chartLineColor
            }
            canvas.drawPath(fillPath, chartLineFillPaint)
        } else {
            chartLineFillPaint.setShader(null)
            chartLinePaint.setShader(null)
            chartLinePaint.color = Color.parseColor("#596f80")
        }
    }

    private fun addToolTip(index: Int, x: Float, value: Int) {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val startDateTime = LocalDateTime.parse(startTime, formatter)
        val updatedTime = startDateTime.plusSeconds((list.size - index - 1) * 30)
        val formatterDisplay = DateTimeFormat.forPattern("h:mm a")
        val time = updatedTime.toString(formatterDisplay).lowercase()
        toolTipList.add(Triple(x, time ?: "", value))
    }

    private fun drawCircleIfNecessary(canvas: Canvas, index: Int, value: Int) {
        if (value > 0) {
            val next = list.getOrNull(index + 1)
            val prev = list.getOrNull(index - 1)

            if (index == 0 && next?.value == 0) {
                canvas.drawCircle(x, y, 1f, outCirclePaint!!)
            } else if (index == list.size - 1 && prev?.value == 0) {
                canvas.drawCircle(x, y, 1f, outCirclePaint!!)
            } else if (prev?.value == 0 && next?.value == 0) {
                canvas.drawCircle(x, y, 1f, outCirclePaint!!)
            }
        }
    }

    private fun drawContent(canvas: Canvas) {
        if (list.size == 0) {
            return
        }
        initLineGradient()
        unitHLenth = (mWith - leftWith - rightWith) / (list.size - 1)

        //hack for touch and hold position
        graphOriginalWidth = (mWith - leftWith)

        /* val firstPosition = 0
         val lastPosition = list.size
         var current: ChartModel?
         var next: ChartModel?*/

        toolTipList.clear()

        for (i in list.indices) {
            val current = list[i]!!
            val x = calculateX(i)
            val y = calculateY(current.value)

            path.reset()
            fillPath.reset()
            path.moveTo(x, y)

            if (i < list.size - 1) {
                drawCubicLine(canvas, i, x, y, current.value)
            }

            if (startTime != null) {
                addToolTip(i, x, current.value)
            }

            if (current.value > 0) {
                drawCircleIfNecessary(canvas, i, current.value)
            }
        }
        /*



                for (i in list.indices) {
                    current = list[i]
                    val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
                    val y =
                        mHeight - bottomWith - (current!!.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

                    path.reset()
                    fillPath.reset()
                    path.moveTo(x, y)
                    if (i < list.size - 1) {
                        next = list[i + 1]
                        if (current.value > 0) {
                            if (next!!.value > 0) {
                                val x1 = mWith - leftWith - rightWith + leftWith - (i + 1) * unitHLenth
                                val y1 =
                                    mHeight - bottomWith - (next.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)


                                path.cubicTo(x1 + (x - x1) / 1.5f, y, x - (x - x1) / 1.5f, y1, x1, y1)
                                if (highlightIndexs.contains(list.size - 1 - i)) {
                                    fillPath.addPath(path)
                                    //draw fill first
                                    fillPath.lineTo(x1, mHeight - bottomWith)
                                    fillPath.lineTo(x, mHeight - bottomWith)
                                    chartLineFillPaint.setShader(linearGradientH)
                                    canvas.drawPath(fillPath, chartLineFillPaint)
                                    chartLinePaint.setShader(linearGradientH)
                                    chartLinePaint.color = highlightColor
                                    fillPath.reset()
                                } else {
                                    fillPath.addPath(path)
                                    fillPath.lineTo(x1, mHeight - bottomWith)
                                    fillPath.lineTo(x, mHeight - bottomWith)

                                    if (!isHighlighted) {
                                        if (isInteracting) {
                                            chartLinePaint.setShader(chartLineGradientInteracting)
                                            chartLineFillPaint.setShader(linearGradientI)
                                            chartLinePaint.color = chartLineColorI
                                        } else {
                                            chartLinePaint.setShader(chartLineGradient)
                                            chartLineFillPaint.setShader(linearGradient)
                                            chartLinePaint.color = chartLineColor

                                        }
                                        canvas.drawPath(fillPath, chartLineFillPaint)
                                    } else {
                                        chartLineFillPaint.setShader(null)
                                        chartLinePaint.setShader(null)
                                        chartLinePaint.color = Color.parseColor("#596f80")
                                    }


                                }
                                //draw chart line second, need to cover fill color
                                canvas.drawPath(path, chartLinePaint)
                            } else {
                                if (highlightIndexs.contains(list.size - 1 - i)) {
                                    chartLinePaint.setShader(linearGradientH)
                                    chartLinePaint.color = highlightColor
                                } else {
                                    if (!isHighlighted *//*highlightIndexs.isEmpty()*//*) {
                                if (isInteracting) {
                                    chartLinePaint.setShader(chartLineGradientInteracting)
                                } else {
                                    chartLinePaint.setShader(chartLineGradient)
                                }
                                //                                chartLinePaint.setColor(chartLineColor);
                            } else {
                                chartLinePaint.setShader(null)
                                chartLinePaint.color = Color.parseColor("#596f80")
                            }
                        }
                        canvas.drawPoint(x, y, chartLinePaint)
                    }
                }
            }


            if (startTime != null) {
                val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

                val startDateTime = LocalDateTime.parse(startTime, formatter)
                var updatedTime = startDateTime.plusSeconds((list.size - i - 1) * 30)
                val formatterDisplay = DateTimeFormat.forPattern("h:mm a")

                val time = updatedTime.toString(formatterDisplay).lowercase()

                toolTipList.add(Triple(x, time ?: "", current.value))

            }

            if (current.value > 0) {
                if (i == firstPosition) {
                    next = list[i + 1]
                    if (next!!.value == 0) {
                        canvas.drawCircle(x, y, 1f, outCirclePaint!!)
                    }
                } else if (i == lastPosition - 1) {
                    val pre = list[i - 1]
                    if (pre!!.value == 0) {
                        canvas.drawCircle(x, y, 1f, outCirclePaint!!)
                    }
                } else {
                    val pre = list[i - 1]
                    next = list[i + 1]
                    if (pre!!.value == 0 && next!!.value == 0) {
                        canvas.drawCircle(x, y, 1f, outCirclePaint!!)
                    }
                }
            }
        }*/

        //4 - > 120
        val eachSecondsWidth = (width.toFloat() - rightWith - leftWith) / (list.size * 30)
        if (showXAxis) {

            drawXAxisTime(
                canvas, mHeight - bottomWith / 4, eachSecondsWidth, startTime, endTime
            )
        }

    }


    private fun drawBottom(canvas: Canvas) {
        canvas.drawRect(
            0f, mHeight - bottomWith, mWith.toFloat(), mHeight.toFloat(), bgBottomPaint!!
        )
    }

    private fun drawHorizontalTextWithLine(
        canvas: Canvas,
        text: String,
        bottomHeight: Float,
        isTop: Boolean = false,
        isBottom: Boolean = false
    ) {

        canvas.drawLine(leftWith, bottomHeight, mWith - rightWith, bottomHeight, bgLine)
        xTextPaint!!.color = Color.parseColor("#a3ffffff")
        xTextPaint!!.getTextBounds(text, 0, text.length, xTextBounds)
        val yPos: Float = /*if (isTop) {*/
            bottomHeight + xTextBounds!!.height() + dip2px(4f)
        /* } else if (isBottom) {
             bottomHeight + xTextBounds!!.height() / 2f - dip2px(4f)
         } else bottomHeight + xTextBounds!!.height() / 2f*/


        val textStart = mWith.toFloat() - leftWith - xTextBounds!!.width()

        canvas.drawText(
            text, textStart, yPos, xTextPaint!!
        )
    }

    private fun drawHorizontalLines(canvas: Canvas) {
        canvas.drawRect(0f, 0f, leftWith, mHeight.toFloat(), bgLeftPaint!!)
        val maxStr = max.toString()
        val minStr = xMin.toString()

        /* val rectF = RectF().apply {
             left = mWith - dip2px(70f).toFloat()
             top = topWith
             right = mWith.toFloat()// - rightWith
             bottom = mHeight - bottomWith
         }
         canvas.drawBitmap(rightBackBitmap, null, rectF, null)*/

        val sectionH = ((max - xMin).toFloat() / 3).roundToInt()


        val avgStr = avgValue.toString()
        val max =
            mHeight - bottomWith - (max - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

        drawHorizontalTextWithLine(canvas, maxStr, max, true, false)

        val min =
            mHeight - bottomWith - (xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

        drawHorizontalTextWithLine(canvas, "", min, false, true)

        if (!mHasDummyData) {
            val xAxis2 =
                mHeight - bottomWith - (sectionH + xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(canvas, (xMin + sectionH).toString(), xAxis2)

            val xAxis3 =
                mHeight - bottomWith - (this.max - sectionH - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)
            drawHorizontalTextWithLine(canvas, (xMin + sectionH * 2).toString(), xAxis3)


            if (avgValue > 0) {


                if (!isInteracting && !isHighlighted) {
                    val avg =
                        mHeight - bottomWith - (avgValue - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)
                    avgTextPaint!!.getTextBounds(avgStr, 0, avgStr.length, xTextBounds)
                    //xTextPaint!!.color = Color.parseColor("#9cbdff")
                    avgBackPaint!!.color = Color.parseColor("#b3172941")
                    canvas.drawLine(
                        leftWith,
                        avg,
                        mWith.toFloat() - leftWith,
                        avg,
                        centerLinePaint!!
                    )
                    val padding = dip2px(8f)
                    canvas.drawBitmap(
                        avgBackBitmap, null, RectF(
                            leftWith + dip2px(5f) - padding,
                            avg - dip2px(3f) - xTextBounds!!.height() - padding,
                            leftWith + dip2px(5f) + xTextBounds!!.width() + padding,
                            avg - dip2px(6f) + padding
                        ), null
                    )



                    canvas.drawText(
                        avgStr, leftWith + dip2px(5f), avg - dip2px(6f), avgTextPaint
                    )
                }
            }

        } else {
            val noDataText = "No data available"
            val textWidth = noDataPaint!!.measureText(noDataText)
            val textX = (mWith - leftWith) / 2 - textWidth / 2
            val textY = (mHeight / 2 + dip2px(4f)).toFloat()
            canvas.drawText(noDataText, textX, textY, noDataPaint!!)
            canvas.drawLine(
                leftWith,
                (mHeight / 2).toFloat(),
                textX - dip2px(8f),
                (mHeight / 2).toFloat(),
                gridPaint!!
            )
            canvas.drawLine(
                textX + textWidth + dip2px(8f),
                (mHeight / 2).toFloat(),
                mWith - rightWith,
                (mHeight / 2).toFloat(),
                gridPaint!!
            )
        }


    }


    private fun drawHorizontalLine(canvas: Canvas, x: Float) {
        overlayLinePaint!!.color = Color.parseColor("#14ffffff")
        val rectF = RectF()
        rectF.left = x
        rectF.right = x
        rectF.top = topWith
        rectF.bottom = mHeight - bottomWith
        canvas.drawRect(rectF, overlayLinePaint!!)
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


    fun findNumber(
        toolTipList: List<Triple<Float, String, Int>>, touchX: Float
    ): Pair<Int, String> {
        val range = unitHLenth / 2//dip2px(3f)
        for (i in 0 until toolTipList.size) {

            // If K lies in the current range
            if (touchX in (toolTipList.get(i).first - range)..(toolTipList.get(i).first + range)) {
                return Pair(i, toolTipList.get(i).second)
            }
        }

        // Not found
        return Pair(-1, "")
    }

    private fun drawXAxisTime(
        canvas: Canvas,
        yPos: Float,
        eachSecondsWidth: Float,
        startTimeStr: String?,
        endTimeStr: String?
    ) {

        if (startTimeStr == null || endTimeStr == null) {

            val edgeTextPadding = dip2px(4f)

            val startText = "12 am"

            var rectF = RectF(
                leftWith,
                yPos - dip2px(10f),
                leftWith + mTextPaintEdge.measureText(startText) + edgeTextPadding * 2,
                height.toFloat()
            )
            canvas.drawRoundRect(
                rectF, dip2px(4f).toFloat(), dip2px(4f).toFloat(), edgeTextBackPaint
            )

            canvas.drawText(
                startText, leftWith + edgeTextPadding.toFloat(), yPos + dip2px(2f), mTextPaintEdge
            )


            val text = "12 am"
            val textWidth = mTextPaintEdge.measureText(text)


            rectF = RectF(
                (width - textWidth - rightWith) - edgeTextPadding * 2,
                yPos - dip2px(10f),
                width - rightWith,
                height.toFloat()
            )
            canvas.drawRoundRect(
                rectF, dip2px(4f).toFloat(), dip2px(4f).toFloat(), edgeTextBackPaint
            )

            canvas.drawText(
                text,
                (width - textWidth - rightWith) - edgeTextPadding,
                yPos + dip2px(2f),
                mTextPaintEdge
            )



            return
        }

        tryCatch {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

            val startDateTime = LocalDateTime.parse(startTimeStr, formatter)
            val endDateTime = LocalDateTime.parse(endTimeStr, formatter)

            var currentDateTime = startDateTime

            while (currentDateTime < endDateTime) {
                var nextEvenHour =
                    currentDateTime.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
                if (nextEvenHour.hourOfDay % 2 != 0) {
                    nextEvenHour = nextEvenHour.plusHours(1)
                }

                currentDateTime = nextEvenHour
                if (nextEvenHour > endDateTime) {
                    break
                }

                val formatterDisplay = DateTimeFormat.forPattern("h a")
                val duration = Duration(startDateTime.toDateTime(), currentDateTime.toDateTime())
                val secondsDifference = duration.toStandardSeconds().seconds
                val startX = secondsDifference * eachSecondsWidth
                val textWidth =
                    mTextPaint.measureText(currentDateTime.toString(formatterDisplay).lowercase())

                val maxWidth = width - leftWith - rightWith
                if (startX + textWidth < maxWidth && startX > leftWith) {
                    mTextPaint.color = ContextCompat.getColor(context, R.color.white_64)
                    canvas.drawText(
                        currentDateTime.toString(formatterDisplay).lowercase(),
                        leftWith + startX - textWidth / 2,
                        yPos,
                        mTextPaint
                    )
                }
            }
        }

        val edgeTextPadding = dip2px(4f)

        val startText = DateFormats.formatDate(
            startTimeStr, DateFormats.dateTimeFormat5(), DateFormats.timeFormat12_2()
        ).lowercase()

        var rectF = RectF(
            leftWith,
            yPos - dip2px(10f),
            leftWith + mTextPaintEdge.measureText(startText) + edgeTextPadding * 2,
            height.toFloat()
        )
        canvas.drawRoundRect(
            rectF, dip2px(4f).toFloat(), dip2px(4f).toFloat(), edgeTextBackPaint
        )

        canvas.drawText(
            startText, leftWith + edgeTextPadding.toFloat(), yPos + dip2px(2f), mTextPaintEdge
        )


        val text = DateFormats.formatDate(
            endTimeStr, DateFormats.dateTimeFormat5(), DateFormats.timeFormat12_2()
        ).lowercase()
        val textWidth = mTextPaintEdge.measureText(text)


        rectF = RectF(
            (width - textWidth - rightWith) - edgeTextPadding * 2,
            yPos - dip2px(10f),
            width - rightWith,
            height.toFloat()
        )
        canvas.drawRoundRect(
            rectF, dip2px(4f).toFloat(), dip2px(4f).toFloat(), edgeTextBackPaint
        )

        canvas.drawText(
            text,
            (width - textWidth - rightWith) - edgeTextPadding,
            yPos + dip2px(2f),
            mTextPaintEdge
        )
    }
}


interface OnHeartRateChartClickAction {
    fun onValueSelected(value: Int, isInteracting: Boolean, time: String? = null)
}