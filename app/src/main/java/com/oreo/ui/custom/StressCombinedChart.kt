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
import android.util.Pair
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS.d
import com.oreo.ui.stress.OnStressClickAction


class StressCombinedChart : View {
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
    private var combineModel: StressCombineModel? = null
    private val list = ArrayList<Item>()
    private val highlightIndexs: MutableList<Int> = ArrayList()
    private var isHighlighted = false
    private var highlightColor = 0
    private var showXAxis = true
    private var interval = 0
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
    private var touchX: Float? = null
    lateinit var overlayLinePaint: Paint
    lateinit var stressDot: Bitmap
    lateinit var calmDot: Bitmap
    lateinit var focusedDot: Bitmap
    private var listener: OnStressClickAction? = null
    private var lastSentValuePos: Int? = null
    private val effect =
        DashPathEffect(floatArrayOf(dip2px(1f).toFloat(), dip2px(5f).toFloat()), 0f)

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
                R.drawable.ic_stress_calm_dot
            ), dimen, dimen, true
        )
        focusedDot = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                res,
                R.drawable.ic_stress_focussed_dot
            ), dimen, dimen, true
        )
        stressDot = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                res,
                R.drawable.ic_stress_stressed_dot
            ), dimen, dimen, true
        )
    }

    fun setClickListener(listener: OnStressClickAction?) {
        this.listener = listener
    }

    private fun initPaint() {
        workoutPaint = Paint()
        workoutPaint.setColorFilter(PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN))

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)
        bgPaint = Paint()
        bgPaint.color = bgColor

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
    }

    fun updateData(datas: StressCombineModel?) {
        combineModel = datas
        list.clear()
        datas?.items?.let { list.addAll(it) }
        list.reverse()
        interval = (list.size / 4f).toInt()
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
            intArrayOf(highColor, mediumColor, lowColor),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        chartLineGradientInteracting = LinearGradient(
            0f, topWith, 0f, mHeight - bottomWith, intArrayOf(
                Color.parseColor("#80ff922d"),
                Color.parseColor("#80ffe762"),
                Color.parseColor("#8012cba9")
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
            Color.parseColor("#C0000000"),
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
        drawDesc(canvas)
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
            0f,
            mHeight - bottomWith,
            mWith.toFloat(),
            mHeight.toFloat(),
            bgBottomPaint
        )
        if (showXAxis) {
            var xText = "23:59"
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint.color = Color.parseColor("#a3ffffff")
            canvas.drawText(
                xText,
                mWith - rightWith - xTextBounds!!.width() - dip2px(5f),
                mHeight - bottomWith / 3,
                xTextPaint
            )
            xText = "00:00"
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            canvas.drawText(xText, leftWith + dip2px(5f), mHeight - bottomWith / 3, xTextPaint)
        }
    }

    private fun drawLeft(canvas: Canvas) {
        gridPaint.color = gridColor
        canvas.drawLine(leftWith, topWith, mWith - rightWith, topWith, gridPaint!!)
        canvas.drawLine(
            leftWith,
            mHeight - bottomWith,
            mWith - rightWith,
            mHeight - bottomWith,
            gridPaint
        )
        if (combineModel == null) return
        if (combineModel!!.high > 0) {
            val high =
                mHeight - bottomWith - combineModel!!.high * 1f / max * (mHeight - bottomWith - topWith)
            canvas.drawLine(leftWith, high, mWith - rightWith, high, gridPaint)
        }
        if (combineModel!!.medium > 0) {
            val medium =
                mHeight - bottomWith - combineModel!!.medium * 1f / max * (mHeight - bottomWith - topWith)
            canvas.drawLine(leftWith, medium, mWith - rightWith, medium, gridPaint)
        }
    }

    private fun drawDesc(canvas: Canvas) {
        if (isInteracting) return
        rectF.left = mWith - rightWith - shadowWidth
        rectF.top = topWith
        rectF.right = mWith - rightWith
        rectF.bottom = mHeight - bottomWith

        chartLineFillPaint.setShader(linearGradientShadow)
        canvas.drawRect(rectF, chartLineFillPaint)
        var high = 0f
        if (combineModel == null) return
        if (combineModel!!.high > 0) {
            high =
                mHeight - bottomWith - combineModel!!.high * 1f / max * (mHeight - bottomWith - topWith)
            val highText = "Stressed"
            paintStressed.getTextBounds(highText, 0, highText.length, xTextBounds)
            canvas.drawText(
                highText,
                mWith - rightWith - xTextBounds!!.width() - dip2px(5f),
                (high + topWith) / 2 + xTextBounds!!.height() / 2f,
                paintStressed
            )
        }
        if (combineModel!!.medium > 0) {
            val medium =
                mHeight - bottomWith - combineModel!!.medium * 1f / max * (mHeight - bottomWith - topWith)
            val mediumText = "Focussed"
            paintFocussed.getTextBounds(mediumText, 0, mediumText.length, xTextBounds)
            canvas.drawText(
                mediumText,
                mWith - rightWith - xTextBounds!!.width() - dip2px(5f),
                (medium + high) / 2 + xTextBounds!!.height() / 2f,
                paintFocussed
            )
            val lowText = "Calm"
            paintCalm.getTextBounds(lowText, 0, lowText.length, xTextBounds)
            canvas.drawText(
                lowText,
                mWith - rightWith - xTextBounds!!.width() - dip2px(5f),
                (medium + mHeight - bottomWith) / 2 + xTextBounds!!.height() / 2f,
                paintCalm
            )
        }
    }

    private fun drawContent(canvas: Canvas) {
        if (list.size == 0) {
            return
        }
        unitHLenth = (mWith - leftWith - rightWith) / (list.size - 1)
        val imageSize = dip2px(16f)
        for (i in combineModel!!.sections!!.indices) {
            val (start, end, color) = combineModel!!.sections!![i]
            val calculatedEnd = if (end < 95) {
                end + 1
            } else {
                end
            }


            rectF.left = start * unitHLenth + leftWith
            rectF.top = topWith
            rectF.right = rectF.left + (calculatedEnd - start) * unitHLenth
            rectF.bottom = mHeight - bottomWith
            chartLineFillPaint.setShader(resMap!![i]!!.first)
            canvas.drawRect(rectF, chartLineFillPaint)
            rectF.left = start * unitHLenth + leftWith
            rectF.top = topWith
            rectF.right = rectF.left + (calculatedEnd - start) * unitHLenth
            rectF.bottom = topWith + dip2px(2f)
            gridPaint.color = color
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
        var current: Item?
        var next: Item?
        for (i in list.indices) {
            current = list[i]
            val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
            val y =
                mHeight - bottomWith - current!!.value * (mHeight - topWith - bottomWith) / (max - xMin)
            path.reset()
            path.moveTo(x, y)
            if (i < list.size - 1) {
                next = list[i + 1]
                if (current.value > 0) {
                    if (next!!.value > 0) {
                        val x1 = mWith - leftWith - rightWith + leftWith - (i + 1) * unitHLenth
                        val y1 =
                            mHeight - bottomWith - next.value * (mHeight - topWith - bottomWith) / (max - xMin)
                        path.cubicTo(x1 + (x - x1) / 4, y, x - (x - x1) / 4, y1, x1, y1)
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
                    if (Math.abs(i - lastIndex) < 120 / (1440 / list.size)) {
                        val x = mWith - leftWith - rightWith + leftWith - i * unitHLenth
                        val y =
                            mHeight - bottomWith - current.value * (mHeight - topWith - bottomWith) / (max - xMin)
                        val x1 = mWith - leftWith - rightWith + leftWith - lastIndex * unitHLenth
                        val y1 =
                            mHeight - bottomWith - list[lastIndex]!!.value * (mHeight - topWith - bottomWith) / (max - xMin)
                        chartLinePaint.setShader(null)
                        chartLinePaint.color = if (isHighlighted || isInteracting) {
                            Color.GRAY
                        } else {
                            Color.WHITE
                        }
                        chartLinePaint.setPathEffect(effect)
                        canvas.drawLine(x, y, x1, y1, chartLinePaint)
                        chartLinePaint.setPathEffect(null)
                    }
                    lastIndex = -1
                }
            }
        }
    }

    private fun drawDot(canvas: Canvas, value: Int) {

        val dotBitmap = when (value) {
            in 1..34 -> calmDot
            in 35..69 -> focusedDot
            in 70..100 -> stressDot
            else -> calmDot
        }

        val width = dotBitmap.width.toFloat() / 2
        val height = dotBitmap.height.toFloat() / 2

        canvas.drawBitmap(
            dotBitmap,
            touchX!! - width,
            getDotHeight(value) - height,
            paintStressed
        )


    }

    private fun drawOverlay(canvas: Canvas) {
        if (!isInteracting) return
        if (touchX != null) {
            if (touchX!! > 0 && touchX!! < mWith) {
                val rectF = RectF()
                rectF.left = touchX!! - 2
                rectF.right = touchX!! + 2
                rectF.top = topWith
                rectF.bottom = mHeight - bottomWith

                val value: Pair<Int, Int> = getClickedValue(touchX!!)
                canvas.drawRect(rectF, overlayLinePaint)
                if (value.second != 0) {

                    drawDot(canvas, value.second)

                }
                if (listener != null) {
                    val position = value.first as Int
                    val selectedValue = value.second as Int
                    if (lastSentValuePos == null) {
                        listener?.onValueSelected(selectedValue, position)
                        lastSentValuePos = position
                        performHapticFeedbackCustom(selectedValue)
                    } else {
                        if (lastSentValuePos != position) {
                            listener?.onValueSelected(selectedValue, position)
                            lastSentValuePos = position
                            performHapticFeedbackCustom(selectedValue)
                        }
                    }
                }
            }
        }
    }

    fun performHapticFeedbackCustom(value: Int) {
        if (value != 0) {
            this.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP
            )
        }
    }

    private fun getClickedValue(touchX: Float): Pair<Int, Int> {
        var sectionLast = 0f
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
        }
    }

    private fun getDotHeight(value: Int): Float {
        return mHeight - bottomWith - value * (mHeight - topWith - bottomWith) / (max - xMin)
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
                        ViewConfiguration.getLongPressTimeout().toLong()
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
                    if(!isInteracting){
                        if(event.y<dip2px(50f)){
                            listener?.onTopClicked()
                        }
                    }


                    handler.removeCallbacks(mLongPressed)
                    isInteracting = false
                    listener?.isInteractionOnGoing(false)
                    invalidate()
                    touchX = 0.0f
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
        listener?.isInteractionOnGoing(true)
        rootView.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )
    }
}