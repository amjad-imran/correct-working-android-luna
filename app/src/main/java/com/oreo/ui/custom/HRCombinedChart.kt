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
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.heartrate.OnHRClickAction
import com.oreo.ui.sleep2.internal.DEFAULT_LONG_PRESS_TIMEOUT
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import kotlin.math.roundToInt


class HRCombinedChart : View {
    private var bgColor = 0
    private var bgLeftColor = 0
    private var bgRightColor = 0
    private var bgTopColor = 0
    private var bgBottomColor = 0
    private var xTextColor = 0
    private var restLineColor = 0
    private var highColor = 0
    private var mediumColor = 0
    private var lowColor = 0
    private var chartLineWidth = 10f
    private var gridColor = 0
    var max = 0
    private var xMin = 0
    private var leftWith = 0f
    private var rightWith = 0f
    private var bottomWith = 0f
    private var topWith = 0f
    private var xTextSize = 0f
    private var yTextSize = 0f
    private var combineTextSize = 0f
    lateinit var bgPaint: Paint
    lateinit var bgLeftPaint: Paint
    lateinit var bgRightPaint: Paint
    lateinit var bgTopPaint: Paint
    lateinit var bgBottomPaint: Paint
    lateinit var xTextPaint: Paint
    lateinit var paintCalm: Paint
    lateinit var paintFocussed: Paint
    lateinit var paintStressed: Paint
    lateinit var gridPaint: Paint
    lateinit var chartLinePaint: Paint
    lateinit var chartLineFillPaint: Paint
    private val path = Path()
    private val fillPath = Path()
    private var unitHLenth = 0f
    private var mWith = 0
    private var mHeight = 0
    private var xTextBounds: Rect? = null
    private var combineModel: HRCombineModel? = null
    private val list = ArrayList<Item>()
    private val highlightIndexs: MutableList<Int> = ArrayList()
    private var isHighlighted = false
    private var highlightColor = 0
    private var showXAxis = true
    lateinit var rectF: RectF
    lateinit var workoutPaint: Paint
    private var linearGradient: LinearGradient? = null
    private var chartLineGradient: LinearGradient? = null
    private var chartLineGradientInteracting: LinearGradient? = null
    private var linearGradientShadow: LinearGradient? = null
    private var resMap = HashMap<Int, Triple<LinearGradient, Bitmap?, String?>>()
    private var bitmapMap = HashMap<Int, Bitmap>()
    private val shadowWidth = dip2px(100f)
    private var interactiveMode = false
    private var isInteracting = false
    private var touchX = 0f
    lateinit var overlayLinePaint: Paint
    lateinit var topCombinedPaint: Paint
    lateinit var calmDot: Bitmap
    private var listener: OnHRClickAction? = null
    private var lastSentValuePos: Int? = null
    private val effect =
        DashPathEffect(floatArrayOf(dip2px(1f).toFloat(), dip2px(5f).toFloat()), 0f)
    private var isNoDataState = true

    private val toolTipList = ArrayList<Triple<Float, String, Int>>()

    //    private val toolTipList = ArrayList<Triple<Float, String, Item>>()
    lateinit var bgLine: Paint
    var yAxisCount: Int = 3
    lateinit var edgeTextBackPaint: Paint
    lateinit var mTextPaintEdge: Paint
    private var vibrationUtils: VibrationUtils? = null
    lateinit var activeBarPaint: Paint
    lateinit var inActiveBarPaintI: Paint

    constructor(context: Context?) : super(context) {
        resMap = HashMap()
        initPaint()
        initBitmap()
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
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CombineLineChart)
        bgColor = ta.getColor(R.styleable.CombineLineChart_bgColor, 0x00000000)
        bgLeftColor = ta.getColor(R.styleable.CombineLineChart_bgLeftColor, 0x00000000)
        bgRightColor = ta.getColor(R.styleable.CombineLineChart_bgRightColor, 0x00000000)
        bgTopColor = ta.getColor(R.styleable.CombineLineChart_bgTopColor, 0x00000000)
        bgBottomColor = ta.getColor(R.styleable.CombineLineChart_bgBottomColor, 0x00000000)
        xTextColor = ta.getColor(R.styleable.CombineLineChart_xTextColor, -0x1000000)
        gridColor = ta.getColor(R.styleable.CombineLineChart_gridColor, -0xff01)
        max = ta.getInt(R.styleable.CombineLineChart_xMax, 10)
        xMin = ta.getInt(R.styleable.CombineLineChart_xMin, 0)
        xTextSize = ta.getDimension(R.styleable.CombineLineChart_xTextSize, 8f)
        yTextSize = ta.getDimension(R.styleable.CombineLineChart_yTextSize, 12f)
        combineTextSize = ta.getDimension(R.styleable.CombineLineChart_combineTextSize, 12f)
        leftWith = ta.getDimension(R.styleable.CombineLineChart_leftWith, 16f)
        rightWith = ta.getDimension(R.styleable.CombineLineChart_rightWith, 8f)
        bottomWith = ta.getDimension(R.styleable.CombineLineChart_bottomWith, 16f)
        topWith = ta.getDimension(R.styleable.CombineLineChart_topWith, 8f)
        restLineColor = ta.getColor(R.styleable.CombineLineChart_restLineColor, -0x1000000)
        highColor = ta.getColor(R.styleable.CombineLineChart_highColor, -0x1000000)
        mediumColor = ta.getColor(R.styleable.CombineLineChart_mediumColor, -0x1000000)
        lowColor = ta.getColor(R.styleable.CombineLineChart_lowColor, -0x1000000)
        chartLineWidth = ta.getDimension(R.styleable.CombineLineChart_chartLineWidth, 10f)
        showXAxis = ta.getBoolean(R.styleable.CombineLineChart_showXAxis, true)
        ta.recycle()
        resMap = HashMap()
        initPaint()
        initBitmap()
    }

    private fun initBitmap() {
        val res = resources
        val dimen = dip2px(30f)
        calmDot = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                res,
                R.drawable.ic_hr_graph_dots
            ), dimen, dimen, true
        )

    }


    private fun initPaint() {

        edgeTextBackPaint = Paint()
        edgeTextBackPaint.color = Color.parseColor("#394653")
        mTextPaintEdge = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintEdge.color = ContextCompat.getColor(context, com.noisefit_commans.R.color.white)
        mTextPaintEdge.textSize = dip2px(12f).toFloat()

        workoutPaint = Paint()
        workoutPaint.setColorFilter(PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN))

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)
        bgPaint = Paint()
        bgPaint.color = bgColor

        topCombinedPaint = Paint().apply {
            color = Color.WHITE
            textSize = combineTextSize
            typeface = fontGilroy
        }
        bgLine = Paint().apply {
            this.color = Color.parseColor("#19ffffff")
        }

        overlayLinePaint = Paint()
        overlayLinePaint.color = Color.parseColor("#939aa3")

        bgLeftPaint = Paint()
        bgLeftPaint.color = bgLeftColor

        bgRightPaint = Paint()
        bgRightPaint.color = bgRightColor

        bgTopPaint = Paint()
        bgTopPaint.color = bgTopColor

        bgBottomPaint = Paint()
        bgBottomPaint.color = bgBottomColor

        xTextPaint = Paint()
        xTextPaint.textSize = xTextSize
        xTextPaint.isAntiAlias = true

        paintCalm = Paint()
        paintCalm.textSize = yTextSize
        paintCalm.setTypeface(fontGilroy)
        paintCalm.color = Color.parseColor("#3fe8b5")
        paintFocussed = Paint()
        paintFocussed.textSize = yTextSize
        paintFocussed.setTypeface(fontGilroy)
        paintFocussed.color = Color.parseColor("#ffed91")
        paintStressed = Paint()
        paintStressed.textSize = yTextSize
        paintStressed.setTypeface(fontGilroy)
        paintStressed.color = Color.parseColor("#ffad60")
        gridPaint = Paint()
        gridPaint.color = gridColor
        chartLinePaint = Paint()
        chartLinePaint.strokeWidth = chartLineWidth
        //        chartLinePaint.setColor(restLineColor);
        chartLinePaint.isAntiAlias = true
        chartLinePaint.style = Paint.Style.STROKE
        chartLinePaint.strokeCap = Paint.Cap.ROUND
        chartLineFillPaint = Paint()
        //        chartLineFillPaint.setColor(Color.GRAY);
        chartLineFillPaint.style = Paint.Style.FILL
        chartLineFillPaint.isAntiAlias = true
        xTextBounds = Rect()
        rectF = RectF()

        activeBarPaint = Paint().apply {
            color =
                Color.parseColor("#59ff3371")
        }

        inActiveBarPaintI = Paint().apply {
            color =
                Color.parseColor("#26ff3371")
        }
    }

    fun updateData(datas: HRCombineModel?, yAxisCount: Int, minYAxis: Int, maxYAxis: Int) {
        isNoDataState = true
        combineModel = datas
        list.clear()
        datas?.items?.let { list.addAll(it) }
        list.reverse()
        this.yAxisCount = yAxisCount
        xMin = 40
        max = maxYAxis


        if (minYAxis in 1..39) {
            xMin = 0
            max = 120
        }

        if (maxYAxis < 120) {
            max = 120
        } else if (maxYAxis < 160) {
            max = 160
        } else if (maxYAxis < 200) {
            max = 200
        }


        list.forEach {
            if (it.value > 0) {
                isNoDataState = false
                return@forEach
            }
        }

        postInvalidate()
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
        isHighlighted = true
        postInvalidate()
    }

    fun removeHighlights() {
        highlightIndexs.clear()
        isHighlighted = false
        postInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWith = w
        mHeight = h
        chartLineGradient = LinearGradient(
            0f,
            topWith,
            0f,
            mHeight - bottomWith,
            intArrayOf(
                Color.parseColor("#ff3371"),
                Color.parseColor("#ff3371"),
                Color.parseColor("#ff3371")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        chartLineGradientInteracting = LinearGradient(
            0f, topWith, 0f, mHeight - bottomWith, intArrayOf(
                Color.parseColor("#ff3371"),
                Color.parseColor("#ff3371"),
                Color.parseColor("#ff3371")
            ), floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            mHeight - bottomWith,
            highlightColor,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        linearGradientShadow = LinearGradient(
            mWith - rightWith - shadowWidth,
            mHeight / 2f,
            mWith - rightWith,
            mHeight / 2f,
            Color.TRANSPARENT,
            Color.parseColor("#26ff3371"),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        generateResMap()
        drawBg(canvas)
        drawTop(canvas)
        drawBottom(canvas)
        drawRight(canvas)
        drawLeft(canvas)
        drawContent(canvas)
//        drawDesc(canvas)
        drawOverlay(canvas)
    }

    private fun generateResMap() {
        if (combineModel == null) return
        var section: Section
        for (i in combineModel!!.sections!!.indices) {
            section = combineModel!!.sections!![i]
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

    private fun drawBg(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), mHeight.toFloat(), bgPaint)
    }

    private fun drawTop(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), topWith, bgTopPaint!!)
    }

    private fun drawRight(canvas: Canvas) {
        canvas.drawRect(mWith - rightWith, 0f, mWith.toFloat(), mHeight.toFloat(), bgRightPaint!!)
    }

    private fun drawBottom(canvas: Canvas) {
        canvas.drawRect(
            0f, mHeight - bottomWith, mWith.toFloat(), mHeight.toFloat() - dip2px(9f), bgBottomPaint
        )
        if (showXAxis) {
            val halfWidth = (mWith - leftWith - rightWith) / 2
            val leftHalf = halfWidth / 2
            //end point
            var xText = "12 am"
            val textWidth = mTextPaintEdge.measureText(xText)
            xTextPaint.color = Color.parseColor("#a3ffffff")
            canvas.drawText(
                xText,
                (mWith - textWidth - rightWith),
                mHeight - bottomWith / 3,
                xTextPaint
            )

            xText = "6 am"
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint.color = Color.parseColor("#a3ffffff")
            canvas.drawText(
                xText,
                (leftHalf - xTextBounds?.width()!! / 2).toFloat(),
                mHeight - bottomWith / 3,
                xTextPaint
            )
            xText = "12 pm"
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint.color = Color.parseColor("#a3ffffff")
            canvas.drawText(
                xText,
                (halfWidth - xTextBounds?.width()!! / 2).toFloat(),
                mHeight - bottomWith / 3,
                xTextPaint
            )
            xText = "6 pm"
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint.color = Color.parseColor("#a3ffffff")
            canvas.drawText(
                xText,
                ((halfWidth + leftHalf - xTextBounds?.width()!! / 2).toFloat()),
                mHeight - bottomWith / 3,
                xTextPaint
            )
            xText = "12 am"
            xTextPaint.color = Color.parseColor("#a3ffffff")
            canvas.drawText(
                xText,
                leftWith,
                mHeight - bottomWith / 3,
                xTextPaint
            )
        }
    }

    private fun drawLeft(canvas: Canvas) {
        gridPaint.color = gridColor
        if (yAxisCount > 3) {
            val yaxisData = calculateYAxisValue(yAxisCount)
            val sectionH = ((max - xMin).toFloat() / 4).roundToInt()
            val max =
                mHeight - bottomWith - (max - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(
                canvas,
                yaxisData[4].toString(),
                max,
                true,
                false
            )

            val min =
                mHeight - bottomWith - (xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(canvas, yaxisData[0].toString(), min, false, true)

            val xAxis2 =
                mHeight - bottomWith - (sectionH + xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(canvas, yaxisData[1].toString(), xAxis2)

            val xAxis3 =
                mHeight - bottomWith - ((sectionH * 2) + xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            if (isNoDataState) {
                val text = "No data"
                val textStart = (mWith - leftWith) / 2 - xTextPaint.measureText(text) / 2
                xTextPaint.getTextBounds(text, 0, text.length, xTextBounds)
                canvas.drawText(
                    text,
                    textStart,
                    xAxis3 + xTextBounds!!.height() / 2,
                    xTextPaint
                )
            }

            drawHorizontalTextWithLine(canvas, yaxisData[2].toString(), xAxis3)
            val xAxis4 =
                mHeight - bottomWith - ((sectionH * 3) + xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(canvas, yaxisData[3].toString(), xAxis4)
        } else {
            val yaxisData = calculateYAxisValue(yAxisCount)
            val max =
                mHeight - bottomWith - (max - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(
                canvas,
                yaxisData[2].toString(),
                max,
                true,
                false
            )

            val min =
                mHeight - bottomWith - (xMin - xMin) * (mHeight - topWith - bottomWith) / (this.max - xMin)

            drawHorizontalTextWithLine(canvas, yaxisData[0].toString(), min, false, true)

            val xAxis2 = (max + min) / 2

            if (isNoDataState) {
                val text = "No data"
                val textStart = (mWith - leftWith) / 2 - xTextPaint.measureText(text) / 2
                xTextPaint.getTextBounds(text, 0, text.length, xTextBounds)
                canvas.drawText(
                    text,
                    textStart,
                    xAxis2 + xTextBounds!!.height() / 2,
                    xTextPaint
                )
            }

            drawHorizontalTextWithLine(canvas, yaxisData[1].toString(), xAxis2)
        }

    }

    private fun calculateYAxisValue(yAxisCount: Int): ArrayList<Int> {
        var minHrValue = xMin
        var maxHrValue = max
        return getPointsBetween(maxHrValue, yAxisCount)
    }

    private fun getPointsBetween(end: Int, numPoints: Int): ArrayList<Int> {
        val points = ArrayList<Int>()
        if (end == 120) {
            if (numPoints == 3) {
                points.add(40)
                points.add(80)
                points.add(120)
            } else {
                points.add(40)
                points.add(60)
                points.add(80)
                points.add(100)
                points.add(120)
            }
        } else if (end == 160) {
            if (numPoints == 3) {
                points.add(40)
                points.add(100)
                points.add(160)
            } else {
                points.add(40)
                points.add(70)
                points.add(100)
                points.add(130)
                points.add(160)
            }
        } else {
            if (numPoints == 3) {
                points.add(40)
                points.add(120)
                points.add(200)
            } else {
                points.add(40)
                points.add(80)
                points.add(120)
                points.add(160)
                points.add(200)
            }
        }
        return points
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


    private fun drawContent(canvas: Canvas) {
        toolTipList.clear()

        if (list.size == 0) {
            return
        }
        unitHLenth = (mWith - leftWith - rightWith) / (list.size - 1)

        val imageSize = dip2px(16f)
        for (i in combineModel!!.sections!!.indices) {
            val section = combineModel!!.sections!![i]
            val calculatedEnd = if (section.end < 47) {
                section.end + 1
            } else {
                section.end
            }
            rectF.left = section.start * unitHLenth + leftWith
            rectF.top = topWith
            rectF.right = rectF.left + (calculatedEnd - section.start) * unitHLenth
            rectF.bottom = mHeight - bottomWith
            chartLineFillPaint.setShader(resMap!![i]!!.first)
            canvas.drawRect(rectF, chartLineFillPaint)

            rectF.left = section.start * unitHLenth + leftWith
            rectF.top = topWith - dip2px(1f)
            rectF.right = rectF.left + (calculatedEnd - section.start) * unitHLenth
            rectF.bottom = topWith + dip2px(1f)
            gridPaint.color = section.color
            canvas.drawRect(rectF, gridPaint)




            if (section.type.equals("combined", true)) {

                val text = "${section.count}"
                topCombinedPaint.getTextBounds(text, 0, text.length, xTextBounds)
                canvas.drawText(
                    text,
                    (rectF.left + rectF.right) / 2 - xTextBounds!!.width() / 2f,
                    rectF.top - xTextBounds!!.height(),
                    topCombinedPaint
                )

            } else {
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
        }
        var current: Item?
        var next: Item?
        for (i in list.indices) {
            current = list[i]
            val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
            val y =
                mHeight - bottomWith - (current!!.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

            path.reset()
            path.moveTo(x, y)

            //for bar draw
            val barPaints: Paint = if (isInteracting)
                inActiveBarPaintI
            else
                activeBarPaint
            val corners = floatArrayOf(
                80f, 80f,   // Top left radius in px
                80f, 80f,   // Top right radius in px
                80f, 80f,     // Bottom right radius in px
                80f, 80f      // Bottom left radius in px
            )
            val yTop =
                mHeight - bottomWith - (current!!.minValue - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
            val yBottom =
                mHeight - bottomWith - (current!!.maxValue - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

            if (current.value > 0) {
                val rectBar = RectF(
                    x - 5,
                    yTop,
                    x + 5,
                    yBottom
                )
                barPaints.let {
                    canvas.drawRoundRect(rectBar, dip2px(20f).toFloat(), dip2px(20f).toFloat(), it)
                    val path = Path()
                    path.addRoundRect(rectBar, corners, Path.Direction.CW)
                    canvas.drawPath(path, barPaints)
                }
            }
            //end bar draw

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
                            chartLineFillPaint.setShader(linearGradient)
                            canvas.drawPath(fillPath, chartLineFillPaint)
                            chartLinePaint.color = highlightColor
                            fillPath.reset()
                        } else {
                            if (!isHighlighted) {
                                if (isInteracting) {
                                    chartLinePaint.setShader(chartLineGradientInteracting)
                                } else {
                                    chartLinePaint.setShader(chartLineGradient)
                                }
                            } else {
                                chartLinePaint.setShader(null)
                                chartLinePaint.color = restLineColor
                            }
                        }
                        //draw chart line second, need to cover fill color
                        canvas.drawPath(path, chartLinePaint)
                    } else {
                        if (highlightIndexs.contains(list.size - 1 - i)) {
                            chartLinePaint.color = highlightColor
                        } else {
                            if (!isHighlighted /*highlightIndexs.isEmpty()*/) {
                                if (isInteracting) {
                                    chartLinePaint.setShader(chartLineGradientInteracting)
                                } else {
                                    chartLinePaint.setShader(chartLineGradient)
                                }
                                //                                chartLinePaint.setColor(chartLineColor);
                            } else {
                                chartLinePaint.setShader(null)
                                chartLinePaint.color = restLineColor
                            }
                        }
                        canvas.drawPoint(x, y, chartLinePaint)
                    }
                }
            }

            val startTime = DateFormats.getMidnightDateTime()
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            val startDateTime = LocalDateTime.parse(startTime, formatter)
            val formatterDisplayStart = DateTimeFormat.forPattern("h:mm a")
            val startTimeRep =
                startDateTime.plusMinutes((list.size - i - 1) * 30).toString(formatterDisplayStart)
                    .lowercase()

            var updatedTime = startDateTime.plusMinutes((list.size - i) * 30)
            val formatterDisplay = DateTimeFormat.forPattern("h:mm a")
            val time = "$startTimeRep - ${updatedTime.toString(formatterDisplay).lowercase()}"
            toolTipList.add(Triple(x, time ?: "", current.value))

            /*if (showXAxis && i % interval == 0 && i > 0 && i < 4 * interval) {
                String xText = String.valueOf(list.get(i).getIndex());
                xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
                xTextPaint.setColor(xTextColor & 0x80ffffff);
                canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);
            }*/
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
                    //show dot line on 3 hour interval
                    if (Math.abs(i - lastIndex) <= 6) {//TODO check logic
                        val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
                        val y =
                            mHeight - bottomWith - (current!!.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)

                        val x1 = mWith - leftWith - rightWith + leftWith - lastIndex * unitHLenth
                        val y1 =
                            mHeight - bottomWith - (list[lastIndex]!!.value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
                        chartLinePaint.setShader(null)
                        chartLinePaint.color =
                            ContextCompat.getColor(context, R.color.color_hr_cubic_line)
                        chartLinePaint.setPathEffect(effect)
                        canvas.drawLine(x, y, x1, y1, chartLinePaint)
                        chartLinePaint.setPathEffect(null)
                    }
                    lastIndex = -1
                }
            }
        }
    }

    fun setClickListener(listener: OnHRClickAction?) {
        this.listener = listener
    }

    private fun drawDot(canvas: Canvas, value: Int, calculatedTouchX: Float) {

        val dotBitmap = calmDot
        val width = dotBitmap.width.toFloat() / 2
        val height = dotBitmap.height.toFloat() / 2

        canvas.drawBitmap(
            dotBitmap,
            calculatedTouchX - width,
            getDotHeight(value) - height,
            paintStressed
        )


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


        val rectF = RectF()
        rectF.left = calculatedTouchX - 2
        rectF.right = calculatedTouchX + 2
        rectF.top = topWith
        rectF.bottom = mHeight - bottomWith

        val value: Triple<Int, Item?, String> = getClickedValue(calculatedTouchX)
        canvas.drawRect(rectF, overlayLinePaint)
        if (value.second != null) {

            drawDot(canvas, value.second?.value ?: 0, calculatedTouchX)

        }
        if (listener != null) {
            val position = value.first
            val item = value.second
//            val selectedValue = item?.value ?: 0
            if (lastSentValuePos == null) {
                listener?.onValueSelected(
                    item,
                    position,
                    value.third
                )
                lastSentValuePos = position
                performHapticFeedbackCustom(item?.value ?: 0)
            } else {
                if (lastSentValuePos != position) {
                    listener?.onValueSelected(
                        item, position, value.third
                    )
                    lastSentValuePos = position
                    performHapticFeedbackCustom(item?.value ?: 0)
                }
            }
        }
    }

    fun performHapticFeedbackCustom(value: Int) {
        if (value != 0) {
            vibrationUtils?.vibrate(HAPTIC_VIBRATION)
        }
    }

    private fun getClickedValue(touchX: Float): Triple<Int, Item?, String> {
        val index = findNumber(toolTipList, touchX)
        return if (index.first < 0) {
            lastSentValuePos = null
            Triple(0, null, "")
        } else {
            Triple(index.first, list[index.first], index.second)
        }

        /*  var sectionLast = 0f
          var position = -1
          for (i in list.size - 1 downTo 1) {
              val sectionEnd = sectionLast + unitHLenth
              if (touchX < sectionEnd) {
                  position = i
                  break
              }
              sectionLast = sectionEnd
          }
          return if (position == -1) {
              Pair(0, 0)
          } else {
              Pair(position, list[position].value)
          }*/
    }

    fun findNumber(
        toolTipList: List<Triple<Float, String, Int>>,
        touchX: Float
    ): kotlin.Pair<Int, String> {
        val range = unitHLenth / 2//dip2px(3f)
        for (i in 0 until toolTipList.size) {

            if (touchX in (toolTipList.get(i).first - range)..(toolTipList.get(i).first + range)) {
                return kotlin.Pair(i, toolTipList.get(i).second)
            }
        }

        return kotlin.Pair(-1, "")
    }


    private fun getDotHeight(value: Int): Float {
        return mHeight - bottomWith - (value - xMin) * (mHeight - topWith - bottomWith) / (max - xMin)
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun sp2px(spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    fun enableInteractiveMode(mode: Boolean) {
        interactiveMode = mode
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interactiveMode) {
            val parent = parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.x
                    handler.postDelayed(
                        mLongPressed,
                        DEFAULT_LONG_PRESS_TIMEOUT
                    )
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isInteracting) {
                        touchX = event.x
                        invalidate()
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isInteracting) {
                        if (event.y < dip2px(50f)) {
                            listener?.onTopClicked()
                        }
                    }

                    handler.removeCallbacks(mLongPressed)
                    isInteracting = false
                    listener?.isInteractionOnGoing(false)
                    touchX = 0.0f
                    invalidate()
                    return true
                }
            }
        } else {
            return super.onTouchEvent(event)
        }
        return false
    }


    private val handler = Handler(Looper.getMainLooper())
    private var mLongPressed = Runnable {
        if (isHighlighted) return@Runnable
        isInteracting = true
        invalidate()

        vibrationUtils?.vibrate(HAPTIC_VIBRATION)
        listener?.isInteractionOnGoing(true)
        /*rootView.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )*/
    }

    fun setVibrationUtil(vibrationUtils: VibrationUtils) {
        this.vibrationUtils = vibrationUtils
    }
}