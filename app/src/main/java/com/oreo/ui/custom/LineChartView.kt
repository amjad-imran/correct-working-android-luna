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
import com.oreo.data.model.LowestIntervalValue
import com.oreo.data.model.SleepChartModel
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.Collections
import kotlin.math.roundToInt


class LineChartView : View {
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
    private var chartLinePaint: Paint? = null
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
    private var chartType: LineChartType = LineChartType.HEART_RATE
    private var mHasDummyData = true
    private val list: MutableList<ChartModel?> = ArrayList()
    private var showXAxis = true
    lateinit var calmDot: Bitmap
    lateinit var avgBackBitmap: Bitmap

    //    private int xMax;
    //    private int xMin;
    private var avgValue = 0
    private var lastMinValueIndex = 0
    private var lastMaxValueIndex = 0
    private var maxValue = 0
    private var minValue = 0
    private var linearGradient: LinearGradient? = null
    private var linearGradientI: LinearGradient? = null
    lateinit var paintCalm: Paint
    lateinit var lowTopPaint: Paint
    lateinit var avgTextPaint: Paint

    lateinit var bgLine: Paint
    lateinit var dotBitmap: Bitmap
    lateinit var dotBitmap2: Bitmap

    lateinit var mTextPaint: Paint
    lateinit var mTextPaintEdge: Paint
    lateinit var edgeTextBackPaint: Paint
    private val effect =
        DashPathEffect(floatArrayOf(dip2px(1f).toFloat(), dip2px(2f).toFloat()), 0f)

    private var chartLineGradient: LinearGradient? = null
    private var chartLineGradientInteracting: LinearGradient? = null


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
                res,
                R.drawable.ic_pink_hot_dot
            ), dimen, dimen, true
        )

        avgBackBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                res,
                R.drawable.image_blur_avg
            ), dimen, dimen, true
        )
    }

    private fun initPaint() {
        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

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
            this.color = Color.parseColor("#9cbdff")
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
        chartLineColor: Int,
        chartLineColorI: Int,
        fillColorStart: Int,
        fillColorEnd: Int
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

    fun updateDataWithMax(
        datas: SleepChartModel?,
        maxOffset: Int,
        showHighCircle: Boolean,
        showLowCircle: Boolean,
        dummy: GraphDummyModel,
        averageValue: Int?,
        startTime: String?,
        endTime: String?,
        chartType: LineChartType
    ): Int {
        this.chartType = chartType

        when (chartType) {
            LineChartType.HEART_RATE -> {
                dotBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_dot)
                dotBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_lowest_dot)
            }

            LineChartType.HRV -> {
                dotBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_hrv_dot)
                dotBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_hr_lowest_dot)
            }
        }


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

        LOGS.d("lowestHr fasdfsfdfsd $minValue")
        val minValueWithIndex = getMinValueWithIndex(minValue)

        minValueWithIndex?.let {
            minValue = minValueWithIndex.second
            lastMinValueIndex = minValueWithIndex.first
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

    private fun getMinValueWithIndex(minValue: Int): Pair<Int, Int>? {
        if (list.isEmpty()) {
            return null
        }

        if (list.size <= 3) {
            return null
        }

        var lowestPointAvg = 0.0f
        var lowestPointMinValue = 0
        var lowestPointIndex = 0

        list.forEachIndexed { index, chartModel ->
            val value = chartModel?.value!!
            if (value != 0 && value == minValue) {
                if (index == 0) {
                    val nextValue = list[index + 1]!!.value
                    val avg = (value + nextValue).toFloat() / 2

                    if (lowestPointAvg == 0.0f) {
                        lowestPointAvg = avg
                        lowestPointIndex = index
                        lowestPointMinValue = value
                    } else if (avg <= lowestPointAvg) {
                        lowestPointAvg = avg
                        lowestPointIndex = index
                        lowestPointMinValue = value
                    }
                } else if (index == list.size - 1) {
                    val previousValue = list[index - 1]!!.value
                    val avg = (value + previousValue).toFloat() / 2

                    if (lowestPointAvg == 0.0f) {
                        lowestPointAvg = avg
                        lowestPointIndex = index
                        lowestPointMinValue = value
                    } else if (avg <= lowestPointAvg) {
                        lowestPointAvg = avg
                        lowestPointIndex = index
                        lowestPointMinValue = value
                    }
                } else {
                    val previousValue = list[index - 1]!!.value
                    val nextValue = list[index + 1]!!.value

                    val avg = (previousValue + value + nextValue).toFloat() / 3

                    if (lowestPointAvg == 0.0f) {
                        lowestPointAvg = avg
                        lowestPointIndex = index
                        lowestPointMinValue = value
                    } else if (avg <= lowestPointAvg) {
                        lowestPointAvg = avg
                        lowestPointIndex = index
                        lowestPointMinValue = value
                    }
                }
            }

        }
        return Pair(lowestPointIndex, lowestPointMinValue)
    }


    /*  private fun getMinValueWithIndex(minValue: Int): Pair<Int, Int> {
          val lowestPointHr = ArrayList<LowestIntervalValue>()

          if (list.isEmpty()) {
              return Pair(0, 0)
          }

          for (index in list.indices) {
              val current = list[index]?.value
              if (current == minValue && current !=0) {
                  val lowestValueData = list.getOrNull(index - 1)
                  val highestValueData = list.getOrNull(index + 1)
                  var lowestValue = -1
                  if (lowestValueData != null) {
                      lowestValue = lowestValueData.value
                  }
                  var highestValue = -1
                  if (highestValueData != null) {
                      highestValue = highestValueData.value
                  }

                  lowestPointHr.add(LowestIntervalValue(lowestValue, highestValue, current, index))
              }
          }
          var avgMinIndex = 0
          var avgValue = 0

          var avgMinValue = Int.MAX_VALUE
          if (lowestPointHr.size == 1) {
              avgMinIndex = lowestPointHr[0].index
              return Pair(lowestPointHr[0].index, lowestPointHr[0].value)
          } else {
              lowestPointHr.forEach {
                  var count = 0
                  var sum = 0
                  if (it.lowestValue != -1) {
                      sum += it.lowestValue
                      count += 1
                  }
                  if (it.highestValue != -1) {
                      sum += it.highestValue
                      count += 1
                  }
                  sum += it.value
                  count += 1
                  val avg = (sum / count)
                  //println("lowestHr ---------------->lowest:- ${it.lowestValue}, highest:- ${it.highestValue}, current:- ${it.value}, avg:- $avg")
                  if (avg <= avgMinValue) {
                      avgMinValue = avg
                      avgMinIndex = it.index
                      avgValue = it.value
                  }

              }
              //println("index $avgMinIndex")
              return Pair(avgMinIndex, avgValue)

          }
      }*/


    private var touchX = 0f
    private var isInteracting = false
    private var interactiveMode = false


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interactiveMode) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.x
                    handler.postDelayed(
                        mLongPressed, ViewConfiguration.getLongPressTimeout().toLong()
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
                    if (!isInteracting) {
                        if (event.y < dip2px(50f)) {
                            listener?.onTopClicked()
                        }
                    }
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

    fun setClickListener(listener: OnLinearChartClickAction?) {
        this.listener = listener
    }

    fun setInteractiveMode(interactiveMode: Boolean) {
        this.interactiveMode = interactiveMode
    }

    private var listener: OnLinearChartClickAction? = null

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

        /*rootView.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )*/
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

        val calculatedTouchX = if (touchX < leftWith) {
            leftWith
        } else if (touchX > (mWith - rightWith)) {
            (mWith - rightWith)
        } else {
            touchX
        }


        /* if (touchX > leftWith && touchX < (mWith - rightWith)) {*/
        val rectF = RectF()
        rectF.left = calculatedTouchX
        rectF.right = calculatedTouchX
        rectF.top = topWith
        rectF.bottom = mHeight - bottomWith
        val value = getClickedValue(calculatedTouchX)
        overlayLinePaint!!.color = Color.WHITE
        canvas.drawRect(rectF, overlayLinePaint!!)


        val y =
            mHeight - bottomWith - (value.second - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

        when (chartType) {
            LineChartType.HEART_RATE -> {
                if (value.second != 0) {

                    val bitmap =
                        if (lastMinValueIndex == value.first || (lastMinValueIndex - 1) == value.first
                            || (lastMinValueIndex + 1) == value.first
                        ) {
                            dotBitmap2
                        } else {
                            dotBitmap
                        }

                    canvas.drawBitmap(
                        bitmap,
                        calculatedTouchX - dotBitmap.width / 2,
                        y - dotBitmap.height / 2,
                        null
                    )
                }
            }

            LineChartType.HRV -> {
                if (value.second != 0) {
                    canvas.drawBitmap(
                        dotBitmap,
                        calculatedTouchX - dotBitmap.width / 2,
                        y - dotBitmap.height / 2,
                        null
                    )
                }

            }
        }

        if (listener != null) {
            val position = value.first
            val selectedValue = value.second
            //d("CLICKED_VALUE value value Touch $position $selectedValue")
            if (lastSentValuePos == null) {
                listener?.onValueSelected(selectedValue, true, value.third)
                lastSentValuePos = position
                performHapticFeedbackCustom(selectedValue)
            } else {
                if (lastSentValuePos != position) {
                    listener?.onValueSelected(selectedValue, true, value.third)
                    lastSentValuePos = position
                    performHapticFeedbackCustom(selectedValue)
                }
            }
        }
        /*}*/
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

    private fun drawLeft(canvas: Canvas) {
        canvas.drawRect(0f, 0f, leftWith, mHeight.toFloat(), bgLeftPaint!!)
        val maxStr = max.toString()
        val minStr = xMin.toString()
        val avgStr = avgValue.toString()
        val max =
            mHeight - bottomWith - (max - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
        canvas.drawLine(leftWith, max, mWith - rightWith, max, gridPaint!!)
        xTextPaint!!.color = Color.parseColor("#7affffff")
        xTextPaint!!.getTextBounds(maxStr, 0, maxStr.length, xTextBounds)
        canvas.drawText(
            maxStr,
            mWith - rightWith + dip2px(10f),
            max + xTextBounds!!.height() / 2f,
            xTextPaint!!
        )
        val min =
            mHeight - bottomWith - (xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)
        canvas.drawLine(leftWith, min, mWith - rightWith, min, gridPaint!!)
        xTextPaint!!.getTextBounds(maxStr, 0, maxStr.length, xTextBounds)
        canvas.drawText(
            minStr,
            mWith - rightWith + dip2px(10f),
            min + xTextBounds!!.height() / 2f,
            xTextPaint!!
        )
        val avg =
            mHeight - bottomWith - (avgValue - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)
        canvas.drawLine(leftWith, avg, mWith - rightWith, avg, centerLinePaint!!)
        xTextPaint!!.getTextBounds(avgStr, 0, avgStr.length, xTextBounds)
        xTextPaint!!.color = Color.WHITE

//        float width = xTextPaint.measureText(avgStr)
//        float padding = dip2px(2)
//        canvas.drawRect(leftWith + dip2px(5) - padding, avg - dip2px(18),
//                leftWith + dip2px(5) + width + padding, avg - dip2px(4),
//                avgBackPaint)
//        canvas.drawText(avgStr, leftWith + dip2px(5), avg - xTextBounds.height(), xTextPaint)
    }


    fun initLineGradient() {

        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            mHeight.toFloat(),
            if (chartType == LineChartType.HEART_RATE) {
                Color.parseColor("#ff7f96")
            } else {
                Color.parseColor("#FF71D2")
            },//intArrayOf(Color.parseColor("#99ff718b"), Color.parseColor("#00ff5f7c")),
            Color.TRANSPARENT/*floatArrayOf(0.3f, 0.6f)*/,
            Shader.TileMode.CLAMP
        )
        linearGradientI = LinearGradient(
            0f,
            0f,
            0f,
            mHeight.toFloat(),
            if (chartType == LineChartType.HEART_RATE) {
                Color.parseColor("#80ff7f96")
            } else {
                Color.parseColor("#80FF71D2")
            },//intArrayOf(Color.parseColor("#99ff718b"), Color.parseColor("#00ff5f7c")),
            Color.TRANSPARENT/*floatArrayOf(0.3f, 0.6f)*/,
            Shader.TileMode.CLAMP
        )


        var arrayDef = intArrayOf()
        var arrayDefI = intArrayOf()

        when (chartType) {
            LineChartType.HEART_RATE -> {

                arrayDef = intArrayOf(
                    Color.parseColor("#ff7f96"),
                    Color.parseColor("#fc3559"),
                    Color.parseColor("#fc3559")
                )
                arrayDefI = intArrayOf(
                    Color.parseColor("#844B60"),
                    Color.parseColor("#833947"),
                    Color.parseColor("#832930")
                )
            }

            LineChartType.HRV -> {
                arrayDef = intArrayOf(
                    Color.parseColor("#FF7FD6"),
                    Color.parseColor("#FD5ACA"),
                    Color.parseColor("#FC35BD")
                )
                arrayDefI = intArrayOf(
                    Color.parseColor("#844A7F"),
                    Color.parseColor("#833878"),
                    Color.parseColor("#822571")
                )
            }
        }

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

    private fun drawContent(canvas: Canvas) {
        if (list.size == 0) {
            return
        }
        initLineGradient()
        unitHLenth = (mWith - leftWith - rightWith) / (list.size - 1)

        //hack for touch and hold position
        graphOriginalWidth = (mWith - leftWith)

        val firstPosition = 0
        val lastPosition = list.size
        var leftTextEndPos = 0f
        var endTextStartPos = 0f
        var current: ChartModel?
        var next: ChartModel?


        toolTipList.clear()
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
                if (current.value > 0 && next!!.value > 0) {
                    val x1 = mWith - leftWith - rightWith + leftWith - (i + 1) * unitHLenth
                    val y1 =
                        mHeight - bottomWith - (next.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)


                    path.cubicTo(x1 + (x - x1) / 1.5f, y, x - (x - x1) / 1.5f, y1, x1, y1)
                    fillPath.addPath(path)
                    //draw fill first
                    fillPath.lineTo(x1, mHeight - bottomWith)
                    fillPath.lineTo(x, mHeight - bottomWith)
                    chartLineFillPaint!!.setShader(if (isInteracting) linearGradientI else linearGradient)
                    canvas.drawPath(fillPath, chartLineFillPaint!!)
                    //draw chart line second, need to cover fill color
                    chartLinePaint?.color = if (isInteracting) chartLineColorI else chartLineColor


                    if (isInteracting) {
                        chartLinePaint?.setShader(chartLineGradientInteracting)
                    } else {
                        chartLinePaint?.setShader(chartLineGradient)
                    }

                    canvas.drawPath(path, chartLinePaint!!)
                }
            }


            if (startTime != null) {
                val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

                val startDateTime = LocalDateTime.parse(startTime, formatter)
                var updatedTime = startDateTime.plusMinutes((list.size - i - 1) * 5)
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



            if (!mHasDummyData) {
                if (showHighCircle && i == lastMaxValueIndex && !isInteracting && maxValue != 0) {
                    canvas.drawBitmap(
                        dotBitmap2,
                        x - dotBitmap.width / 2,
                        y - dotBitmap.height / 2,
                        null
                    )
                }

                if (showLowCircle && i == lastMinValueIndex && !isInteracting && minValue != 0) {
                    canvas.drawBitmap(
                        dotBitmap2,
                        x - dotBitmap.width / 2,
                        y - dotBitmap.height / 2,
                        null
                    )
                }

                if (!showLowCircle && !showHighCircle && i == lastMinValueIndex) {

                    chartLineFillPaint.setShader(
                        LinearGradient(
                            0f,
                            0f,
                            0f,
                            mHeight - bottomWith,
                            Color.parseColor("#66ffffff"),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP
                        )
                    )

                    val offset = dip2px(unitHLenth) / 2
                    val rectF = RectF().apply {
                        this.left = x - offset
                        this.right = x + offset
                        this.top = topWith
                        this.bottom = mHeight - bottomWith
                    }

                    canvas.drawRect(rectF, chartLineFillPaint)

                    rectF.apply {
                        this.left = x - offset
                        this.right = x + offset
                        this.top = topWith - dip2px(1f)
                        this.bottom = topWith + dip2px(1f)
                    }

                    canvas.drawRect(rectF, lowTopPaint)


                    rectF.apply {
                        this.left = x - offset
                        this.right = x + offset
                        this.top = topWith - dip2px(1f)
                        this.bottom = topWith + dip2px(1f)
                    }

                    xTextPaint!!.color = Color.WHITE

                    val textWidth = xTextPaint!!.measureText("Lowest HR")

                    var lowestHrPos = x - textWidth / 2

                    if (lowestHrPos < leftWith) {
                        lowestHrPos = leftWith
                    }

                    canvas.drawText(
                        "Lowest HR",
                        lowestHrPos,
                        topWith - dip2px(8f),
                        xTextPaint!!
                    )
                }

            }
        }

        var start = 0
        for (i in list.indices) {
            if (list[i]!!.value > 0) {
                start = i
                break
            }
        }
        var end = 0
        for (i in list.indices.reversed()) {
            if (list[i]!!.value > 0) {
                end = i
                break
            }
        }
        var lastIndex = -1
        for (i in start..end) {
            current = list[i]
            if (current!!.value == 0) {
                if (i > 0 && lastIndex == -1) {
                    lastIndex = i - 1
                }
            } else {
                if (lastIndex != -1) {
                    if (Math.abs(i - lastIndex) < 4) {
                        val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
                        val y =
                            mHeight - bottomWith - (current!!.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

                        path.reset()
                        fillPath.reset()
                        path.moveTo(x, y)

                        val x1 = mWith - leftWith - rightWith + leftWith - lastIndex * unitHLenth
                        val y1 =
                            mHeight - bottomWith - (list[lastIndex]!!.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

                        chartLinePaint?.setShader(null)
                        chartLinePaint?.color = if (isInteracting) {
                            chartLineColorI
                        } else {
                            chartLineColor
                        }
                        chartLinePaint?.setPathEffect(effect)
                        canvas.drawLine(x, y, x1, y1, chartLinePaint!!)
                        chartLinePaint?.setPathEffect(null)

                        path.cubicTo(x1 + (x - x1) / 1.5f, y, x - (x - x1) / 1.5f, y1, x1, y1)

                        fillPath.addPath(path)
                        //draw fill first
                        fillPath.lineTo(x1, mHeight - bottomWith)
                        fillPath.lineTo(x, mHeight - bottomWith)
                        chartLineFillPaint!!.setShader(if (isInteracting) linearGradientI else linearGradient)
                        canvas.drawPath(fillPath, chartLineFillPaint!!)


                    }
                    lastIndex = -1
                }
            }
        }


        val eachSecondsWidth = (width.toFloat() - rightWith - leftWith) / (list.size * 5 * 60)
        if (showXAxis) {

            drawXAxisTime(
                canvas,
                mHeight - bottomWith / 4,
                eachSecondsWidth,
                startTime,
                endTime
            )

            /*
                            if (list[i] != null && list[i]!!.index != null && !list[i]!!.index!!.isEmpty()) {
                                val xText = list[i]!!.index
                                xTextPaint!!.getTextBounds(xText, 0, xText!!.length, xTextBounds)
                                if (endTextStartPos == 0f) {
                                    val text = list[0]!!.index
                                    xTextPaint!!.color = Color.parseColor("#ffffff")
                                    endTextStartPos = if (text != null) {
                                        mWith - leftWith - xTextPaint!!.measureText(text)
                                    } else {
                                        mWith - leftWith - xTextPaint!!.measureText("00:00 am")
                                    }
                                }
                                if (leftTextEndPos == 0f) {
                                    val lastText = list[list.size - 1]!!.index
                                    xTextPaint!!.color = Color.parseColor("#ffffff")
                                    leftTextEndPos = leftWith + xTextPaint!!.measureText(lastText)
                                }
                                if (i == 0) {
                                    xTextPaint!!.color = Color.parseColor("#ffffff")
                                    canvas.drawText(
                                        xText!!, x - xTextBounds!!.width(), mHeight - bottomWith / 4,
                                        xTextPaint!!
                                    )
                                    //leftTextEndPos = xTextPaint.measureText(xText);
                                } else if (i == list.size - 1) {
                                    xTextPaint!!.color = Color.parseColor("#ffffff")
                                    canvas.drawText(xText!!, x, mHeight - bottomWith / 4, xTextPaint!!)
                                } else {
                                    if (leftTextEndPos < x - xTextBounds!!.width() / 2f - dip2px(6f)
                                        && x + xTextBounds!!.width() < endTextStartPos
                                    ) {
                                        xTextPaint!!.color = xTextColor and -0x7f000001
                                        canvas.drawText(
                                            xText!!, x - xTextBounds!!.width() / 2f, mHeight - bottomWith / 4,
                                            xTextPaint!!
                                        )
                                    }
                                }
                            }*/
        }

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
            val endTime = ""
            //            if(sleepModel!=null && sleepModel.getEndTime() != null){
//                endTime = sleepModel.getEndTime();
//            }
//            String xText = endTime;
//            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
//            xTextPaint.setColor(Color.parseColor("#ffffff"));
//            canvas.drawText(xText, mWith - rightWith - xTextBounds.width() - dip2px(5), mHeight - bottomWith / 4, xTextPaint);
//
//            String startTime = "";
//            if(sleepModel!=null && sleepModel.getStartTime() != null){
//                startTime = sleepModel.getStartTime();
//            }
//
//            xText = startTime;
//            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
//            canvas.drawText(xText, leftWith + dip2px(5), mHeight - bottomWith / 4, xTextPaint);
        }
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
        val yPos: Float = if (isTop) {
            bottomHeight + xTextBounds!!.height() / 2f + dip2px(4f)
        } else if (isBottom) {
            bottomHeight + xTextBounds!!.height() / 2f - dip2px(4f)
        } else
            bottomHeight + xTextBounds!!.height() / 2f


        val textStart = mWith.toFloat() - xTextBounds!!.width()

        canvas.drawText(
            text,
            textStart,
            yPos,
            xTextPaint!!
        )
    }

    private fun drawHorizontalLines(canvas: Canvas) {
        canvas.drawRect(0f, 0f, leftWith, mHeight.toFloat(), bgLeftPaint!!)
        val maxStr = max.toString()
        val minStr = xMin.toString()

        val sectionH = ((max - xMin).toFloat() / 3).roundToInt()


        val avgStr = avgValue.toString()
        val max =
            mHeight - bottomWith - (max - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

        drawHorizontalTextWithLine(canvas, maxStr, max, true, false)

        val min =
            mHeight - bottomWith - (xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

        drawHorizontalTextWithLine(canvas, minStr, min, false, true)

        if (!mHasDummyData) {
            val xAxis2 =
                mHeight - bottomWith - (sectionH + xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(canvas, (xMin + sectionH).toString(), xAxis2)

            val xAxis3 =
                mHeight - bottomWith - (this.max - sectionH - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)
            drawHorizontalTextWithLine(canvas, (xMin + sectionH * 2).toString(), xAxis3)


            if (avgValue > 0) {
                val avg =
                    mHeight - bottomWith - (avgValue - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)
                avgTextPaint!!.getTextBounds(avgStr, 0, avgStr.length, xTextBounds)
                //xTextPaint!!.color = Color.parseColor("#9cbdff")
                avgBackPaint!!.color = Color.parseColor("#b3172941")

                if (!isInteracting) {
                    canvas.drawLine(leftWith, avg, mWith.toFloat(), avg, centerLinePaint!!)
                }

                val padding = dip2px(8f)
                canvas.drawBitmap(
                    avgBackBitmap,
                    null,
                    RectF(
                        leftWith + dip2px(5f) - padding,
                        avg - dip2px(3f) - xTextBounds!!.height() - padding,
                        leftWith + dip2px(5f) + xTextBounds!!.width() + padding,
                        avg - dip2px(6f) + padding
                    ),
                    null
                )



                canvas.drawText(
                    avgStr,
                    leftWith + dip2px(5f),
                    avg - dip2px(6f),
                    avgTextPaint
                )
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
        toolTipList: List<Triple<Float, String, Int>>,
        touchX: Float
    ): Pair<Int, String> {

//        LOGS.d("Dassdadsaasdasdas ${Gson().toJson(toolTipList)}")

//        var low = 0
//        var high = toolTipList.size - 1
//        var mid: Int
//        while (low <= high) {
//            mid = low + ((high - low) / 2)
//            when {
//                eleToSearch > input[mid] -> low =
//                    mid + 1    // element is greater than middle element of array, so it will be in right half of array
//                eleToSearch == input[mid] -> return mid // found the element
//                eleToSearch < input[mid] -> high =
//                    mid - 1   //element is less than middle element of array, so it will be in left half of the array.
//            }
//        }

        // Iterate and find the element
        // Iterate and find the element
        val range = unitHLenth / 2//dip2px(3f)
        for (i in 0 until toolTipList.size) {

            // If K lies in the current range
            if (touchX in (toolTipList.get(i).first - range)..(toolTipList.get(i).first + range)) {
                return Pair(i, toolTipList.get(i).second)
            }
        }

//        var low = 0
//        var high = toolTipList.size - 1
//        LOGS.d("Fsajfajfddsf size $low ---> $high")
//        // Binary search
//        while (low <= high) {
//
//            // Find the mid element
//            val mid = low + ((high - low) / 2)
//
//            LOGS.d("Fsajfajfddsf mid value $mid")
//            // If element is found
//            if (touchX >= toolTipList[mid].second && touchX <= toolTipList[mid].first) {
//                LOGS.d("Fsajfajfddsf 1 $mid")
//                return mid
//            } else if (touchX < toolTipList[mid].first) {
//
//                high = mid - 1
//                LOGS.d("Fsajfajfddsf high $high")
//            } else {
//                low = mid + 1
//                LOGS.d("Fsajfajfddsf low $low")
//            }
//        }

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
                rectF,
                dip2px(4f).toFloat(),
                dip2px(4f).toFloat(),
                edgeTextBackPaint
            )

            canvas.drawText(
                startText,
                leftWith + edgeTextPadding.toFloat(),
                yPos + dip2px(2f),
                mTextPaintEdge
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
                rectF,
                dip2px(4f).toFloat(),
                dip2px(4f).toFloat(),
                edgeTextBackPaint
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
            startTimeStr,
            DateFormats.dateTimeFormat5,
            DateFormats.timeFormat12_2
        ).lowercase()

        var rectF = RectF(
            leftWith,
            yPos - dip2px(10f),
            leftWith + mTextPaintEdge.measureText(startText) + edgeTextPadding * 2,
            height.toFloat()
        )
        canvas.drawRoundRect(
            rectF,
            dip2px(4f).toFloat(),
            dip2px(4f).toFloat(),
            edgeTextBackPaint
        )

        canvas.drawText(
            startText,
            leftWith + edgeTextPadding.toFloat(),
            yPos + dip2px(2f),
            mTextPaintEdge
        )


        val text = DateFormats.formatDate(
            endTimeStr,
            DateFormats.dateTimeFormat5,
            DateFormats.timeFormat12_2
        ).lowercase()
        val textWidth = mTextPaintEdge.measureText(text)


        rectF = RectF(
            (width - textWidth - rightWith) - edgeTextPadding * 2,
            yPos - dip2px(10f),
            width - rightWith,
            height.toFloat()
        )
        canvas.drawRoundRect(
            rectF,
            dip2px(4f).toFloat(),
            dip2px(4f).toFloat(),
            edgeTextBackPaint
        )

        canvas.drawText(
            text,
            (width - textWidth - rightWith) - edgeTextPadding,
            yPos + dip2px(2f),
            mTextPaintEdge
        )
    }
}

enum class LineChartType {
    HEART_RATE, HRV
}

interface OnLinearChartClickAction {
    fun onValueSelected(value: Int, isInteracting: Boolean, time: String? = null)
    fun onTopClicked()
}