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
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import kotlin.math.abs
import kotlin.math.roundToInt


class TempPeriodCombinedChart : View {
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
    var max = 0.0f
    private var xMin = 0.0f
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
    private var combineModel: TempPeriodCombineModel? = null
    private val list = ArrayList<ItemTemp>()
    lateinit var rectF: RectF
    lateinit var workoutPaint: Paint
    private var linearGradient: LinearGradient? = null
    private var resMap = HashMap<Int, Triple<LinearGradient, Bitmap?, String?>>()
    private var bitmapMap = HashMap<Int, Bitmap>()
    lateinit var overlayLinePaint: Paint
    lateinit var topCombinedPaint: Paint
    lateinit var calmDot: Bitmap
    private var listener: OnHRClickAction? = null
    private val effect =
        DashPathEffect(floatArrayOf(dip2px(1f).toFloat(), dip2px(5f).toFloat()), 0f)

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

    fun updateData(data: TempPeriodCombineModel?) {
        combineModel = data
        list.clear()
        data?.items?.let { list.addAll(it) }
        //list.reverse()
        this.yAxisCount = yAxisCount
        xMin = -2.5f
        max = 2.5f
        postInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWith = w
        mHeight = h
        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            mHeight - bottomWith,
            Color.parseColor("#99ff9252"),
            Color.TRANSPARENT,
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
    }

    private fun getCalculatedMax(value: Float): Float {
        return value + (abs(xMin))
    }

    private fun drawLeft(canvas: Canvas) {
        gridPaint.color = gridColor

        val maxPos =
            mHeight - bottomWith - (getCalculatedMax(max) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                max
            ) - 0)

        drawHorizontalTextWithLine(
            canvas,
            "+2.5",
            maxPos,
            true,
            false
        )

        val min =
            mHeight - bottomWith - (0 - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                max
            ) - 0)

        drawHorizontalTextWithLine(canvas, "-2.5", min, false, true)

        val xAxis2 =
            mHeight - bottomWith - (getCalculatedMax(1.25f) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                max
            ) - 0)

        drawHorizontalTextWithLine(canvas, "1.25", xAxis2)

        val xAxis3 =
            mHeight - bottomWith - (getCalculatedMax(0f) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                max
            ) - 0)

        drawHorizontalTextWithLine(canvas, "0", xAxis3)
        val xAxis4 =
            mHeight - bottomWith - (getCalculatedMax(-1.25f) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
                max
            ) - 0)

        drawHorizontalTextWithLine(canvas, "-1.25", xAxis4)
    }

    /* private fun calculateYAxisValue(yAxisCount: Int): ArrayList<Int> {
         var minHrValue = xMin
         var maxHrValue = max
         return getPointsBetween(maxHrValue, yAxisCount)
     }*/

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

        if (list.size == 0) {
            return
        }
        unitHLenth = (mWith - leftWith - rightWith) / (list.size - 1)

        val imageSize = dip2px(16f)
        for (i in combineModel!!.sections!!.indices) {
            val section = combineModel!!.sections!![i]
            val calculatedEnd = if (section.end < 13) {
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

        val yLineZero =
            mHeight - bottomWith - (getCalculatedMax(
                0f
            ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)

        chartLinePaint.color =
            ContextCompat.getColor(context, R.color.color_temp_line)
        var current: ItemTemp?
        var next: ItemTemp?
        for (i in list.indices) {
            current = list[i]
            val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
            val y =
                mHeight - bottomWith - (getCalculatedMax(
                    current!!.value ?: 0.0f
                ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)

            path.reset()
            path.moveTo(x, y)

            if (i < list.size - 1) {

                val x1 = mWith - leftWith - rightWith + leftWith - (i + 1) * unitHLenth

                canvas.drawLine(
                    x1,
                    topWith,
                    x1,
                    mHeight - bottomWith,
                    bgLine
                )


                next = list[i + 1]
                if (current.value != null) {
                    if (next.value != null) {
                        //val x1 = mWith - leftWith - rightWith + leftWith - (i + 1) * unitHLenth
                        val y1 =
                            mHeight - bottomWith - (getCalculatedMax(next.value!!) - 0) *
                                    (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)

                        path.cubicTo(x1 + (x - x1) / 1.5f, y, x - (x - x1) / 1.5f, y1, x1, y1)

                        fillPath.addPath(path)
                        fillPath.lineTo(x1, yLineZero)
                        fillPath.lineTo(x, yLineZero)

                        chartLineFillPaint.setShader(linearGradient)
                        canvas.drawPath(fillPath, chartLineFillPaint)
                        fillPath.reset()
                        canvas.drawPath(path, chartLinePaint)
                    }
                }
            }

            if (i == 1) {
                if (current.value != null) {
                    drawDot(
                        canvas,
                        getCalculatedMax(current.value ?: 0.0f),
                        x
                    )
                }
                val text = DateFormats.formatDate(
                    current.date,
                    DateFormats.dateFormat3,
                    DateFormats.dateFormat7
                )
                val textWidth = xTextPaint.measureText(text)
                xTextPaint.color = Color.parseColor("#ffffff")
                canvas.drawText(
                    text,
                    (mWith - textWidth - rightWith),
                    mHeight - bottomWith / 3,
                    xTextPaint
                )
            } else if (i == list.size - 1) {
                val text = DateFormats.formatDate(
                    current.date,
                    DateFormats.dateFormat3,
                    DateFormats.dateFormat7
                )
                xTextPaint.color = Color.parseColor("#7affffff")
                canvas.drawText(
                    text,
                    leftWith,
                    mHeight - bottomWith / 3,
                    xTextPaint
                )
            } else if ((i == list.size / 2)) {
                val text = DateFormats.formatDate(
                    current.date,
                    DateFormats.dateFormat3,
                    DateFormats.dateFormat7
                )
                xTextPaint.color = Color.parseColor("#7affffff")
                val textWidth = xTextPaint.measureText(text)
                canvas.drawText(
                    text,
                    x - textWidth / 2,
                    mHeight - bottomWith / 3,
                    xTextPaint
                )
            }

        }
        /* var start = 0
         for (i in list.indices) {
             if (list[i]!!.value ?: 0.0f > 0) {
                 start = i
                 break
             }
         }
         var end = 0
         for (i in list.indices.reversed()) {
             if (list[i]!!.value ?: 0.0f > 0) {
                 end = i
                 break
             }
         }

         var lastIndex = -1
         for (i in start..end) {
             current = list[i]
             if (current.value == 0.0f) {
                 if (i > 0 && lastIndex == -1) {
                     lastIndex = i - 1
                 }
             } else {
                 if (lastIndex != -1) {
                     //show dot line on 3 hour interval
                     if (Math.abs(i - lastIndex) <= 6) {//TODO check logic
                         val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
                         val y =
                             mHeight - bottomWith - (getCalculatedMax(
                                 current!!.value
                                     ?: 0.0f
                             ) - xMin) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)

                         val x1 = mWith - leftWith - rightWith + leftWith - lastIndex * unitHLenth
                         val y1 =
                             mHeight - bottomWith - (getCalculatedMax(
                                 list[lastIndex]!!.value
                                     ?: 0.0f
                             ) - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(max) - 0)
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
         }*/
    }

    fun setClickListener(listener: OnHRClickAction?) {
        this.listener = listener
    }

    private fun drawDot(canvas: Canvas, value: Float, calculatedTouchX: Float) {

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


    private fun getDotHeight(value: Float): Float {
        return mHeight - bottomWith - (value - 0) * (mHeight - topWith - bottomWith) / (getCalculatedMax(
            max
        ) - 0)
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