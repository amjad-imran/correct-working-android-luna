package com.oreo.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.noisefit.luna.R
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.DayTimeDataModel
import com.oreo.data.model.DayTimeXYDataModel
import com.oreo.data.model.Item
import com.oreo.data.model.Section
import com.oreo.ui.activity.dpToPx
import com.oreo.ui.sleep2.internal.DEFAULT_LONG_PRESS_TIMEOUT

class ODayTimeInteractiveGraph : View {
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
    private var bottomWith = 0f
    private var topWith = 0f
    private var xTextSize = 0f
    private var yTextSize = 0f
    lateinit var bgTopPaint: Paint
    lateinit var bgBottomPaint: Paint
    lateinit var xTextPaint: Paint
    lateinit var gridPaint: Paint
    private var unitHLenth = 0f
    private var mWith = 0
    private var mHeight = 0
    private var xTextBounds: Rect? = null
    private val list = ArrayList<Item>()
    private var showXAxis = true
    private var rectF: RectF? = null
    private var interactiveMode = false
    private var isInteracting = false
    private var touchX: Float? = null
    private var listener: OnDayTimeClickAction? = null
    private var barStartEndXPosList: ArrayList<DayTimeXYDataModel>? = null

    /*
        * bar types
        * -No data
        * -0-inactive
        * -1-low
        * -2-medium
        * -3-high
        * */


    lateinit var noDataBarPaint: Paint
    lateinit var inActiveBarPaint: Paint
    lateinit var lowBarPaint: Paint
    lateinit var mediumPaint: Paint
    lateinit var highPaint: Paint


    lateinit var noDataBarPaintI: Paint
    lateinit var inActiveBarPaintI: Paint
    lateinit var lowBarPaintI: Paint
    lateinit var mediumPaintI: Paint
    lateinit var highPaintI: Paint


    private lateinit var overlayLinePaint: Paint
    private lateinit var overlayLineOnTopPaint: Paint
    private var resMap: MutableMap<Int, Triple<LinearGradient, Bitmap?, String?>>? = null
    private var lastSentValuePos: Int? = null
    private lateinit var chartLineFillPaint: Paint
    lateinit var topCombinedPaint: Paint
    private var combineTextSize = 0f
    private var bitmapMap = HashMap<Int, Bitmap>()
    private var workoutPaint: Paint? = null
    private var dayTimeDataModel: DayTimeDataModel? = null
    private var mSleepSection: List<Section>? = null
    private var mNapSection: List<Section>? = null

    private var vibrationUtils: VibrationUtils? = null
    lateinit var avgBackBitmap: Bitmap


    constructor(context: Context?) : super(context) {
        resMap = HashMap()
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
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
    }

    fun setClickListener(listener: OnDayTimeClickAction?) {
        this.listener = listener
    }

    private fun initPaint() {
        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

        workoutPaint = Paint()
        workoutPaint?.setColorFilter(PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN))

        overlayLinePaint = Paint()
        overlayLinePaint.color = Color.parseColor("#858e99")

        overlayLineOnTopPaint = Paint()

        xTextPaint = Paint()
        xTextPaint.typeface = fontGilroy
        xTextPaint.textSize = xTextSize
        xTextPaint.isAntiAlias = true

        bgTopPaint = Paint()
        bgTopPaint.color = bgTopColor

        bgBottomPaint = Paint()
        bgBottomPaint.color = bgBottomColor

        gridPaint = Paint()
        gridPaint.color = gridColor

        chartLineFillPaint = Paint()
        chartLineFillPaint.style = Paint.Style.FILL
        chartLineFillPaint.isAntiAlias = true

        xTextBounds = Rect()
        rectF = RectF()

        noDataBarPaint = Paint().apply {
            color = Color.parseColor("#44515f")
        }
        noDataBarPaintI = Paint().apply {
            color = Color.parseColor("#8044515f")
        }

        inActiveBarPaint = Paint().apply {
            color =
                Color.parseColor("#8a9fb3")
        }

        inActiveBarPaintI = Paint().apply {
            color =
                Color.parseColor("#4b5e72")
        }


        lowBarPaint = Paint().apply {
            color =
                Color.parseColor("#387c8e")
        }
        lowBarPaintI = Paint().apply {
            color =
                Color.parseColor("#224d60")
        }

        mediumPaint = Paint().apply {
            color =
                Color.parseColor("#6bc5eb")
        }
        mediumPaintI = Paint().apply {
            color =
                Color.parseColor("#417794")
        }

        highPaint = Paint().apply {
            color =
                Color.parseColor("#ffffff")
        }
        highPaintI = Paint().apply {
            color =
                Color.parseColor("#858e99")
        }

        topCombinedPaint = Paint().apply {
            color = Color.WHITE
            textSize = combineTextSize
            typeface = fontGilroy
        }

        val dimen = dip2px(30f)
        avgBackBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.image_blur_avg
            ), dimen, dimen, true
        )


    }

    fun updateData(dayData: DayTimeDataModel?) {
        dayTimeDataModel = dayData
        mSleepSection = dayTimeDataModel?.sections?.filter { it.type.equals("sleep", true) }
        mNapSection = dayTimeDataModel?.sections?.filter { it.type.equals("nap", true) }
        bitmapMap = HashMap()
        list.clear()
        dayData?.items.let {
            if (it != null) {
                list.addAll(it)
            }
        }
        /*if (dayTimeDataModel?.sections.isNullOrEmpty()) {
            topWith = 0f
        } else {
            topWith = dpToPx(40, context)
        }*/

        postInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWith = w
        mHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        generateResMap()
        drawTop(canvas)
        drawBottom(canvas)
        drawLeft(canvas)
        drawBarContent(canvas)
        drawOverlay(canvas)
        drawRight(canvas)
    }

    private fun drawBackBlur(canvas: Canvas, rectF: RectF) {
        /*canvas.drawRect(
            rectF, gridPaint
        )*/
        canvas.drawBitmap(
            avgBackBitmap,
            null,
            rectF,
            null
        )
    }

    private fun drawRight(canvas: Canvas) {

        if (isInteracting) return

        xTextPaint.color = Color.parseColor("#ffffff")


        val qHeight = (mHeight - bottomWith - topWith) / 4

        val highText = context.getString(R.string.text_high)
        val medText = context.getString(R.string.text_med)
        val lowText = context.getString(R.string.text_low)
        val inactiveText = context.getString(R.string.text_inactive)
        val textPaddingLeft = dip2px(10f).toFloat()


        xTextPaint.getTextBounds(highText, 0, highText.length, xTextBounds)
        val highPosY = topWith + qHeight / 2 + xTextBounds!!.height() / 2

        val textPadding = dpToPx(6, context)

        drawBackBlur(
            canvas, RectF(
                mWith - xTextBounds!!.width() - textPaddingLeft - textPadding,
                highPosY - dip2px(5f) - xTextBounds!!.height() - textPadding,
                mWith.toFloat(),
                highPosY + textPadding
            )
        )

        canvas.drawText(
            highText,
            mWith - xTextBounds!!.width() - textPaddingLeft,
            highPosY - dip2px(5f),
            xTextPaint
        )

        xTextPaint.getTextBounds(medText, 0, medText.length, xTextBounds)

        drawBackBlur(
            canvas, RectF(
                mWith - xTextBounds!!.width() - textPaddingLeft - textPadding,
                highPosY + qHeight - dip2px(5f) - xTextBounds!!.height() - textPadding,
                mWith.toFloat(),
                highPosY + qHeight + textPadding
            )
        )
        canvas.drawText(
            medText,
            mWith - xTextBounds!!.width() - textPaddingLeft,
            highPosY + qHeight - dip2px(5f),
            xTextPaint
        )

        xTextPaint.getTextBounds(lowText, 0, lowText.length, xTextBounds)

        drawBackBlur(
            canvas, RectF(
                mWith - xTextBounds!!.width() - textPaddingLeft - textPadding,
                highPosY + qHeight * 2 - dip2px(5f) - xTextBounds!!.height() - textPadding,
                mWith.toFloat(),
                highPosY + qHeight * 2 + textPadding
            )
        )
        canvas.drawText(
            lowText,
            mWith - xTextBounds!!.width() - textPaddingLeft,
            highPosY + qHeight * 2 - dip2px(5f),
            xTextPaint
        )

        xTextPaint.getTextBounds(inactiveText, 0, inactiveText.length, xTextBounds)

        drawBackBlur(
            canvas, RectF(
                mWith - xTextBounds!!.width() - textPaddingLeft - textPadding,
                highPosY + qHeight * 3 - dip2px(5f) - xTextBounds!!.height() - textPadding,
                mWith.toFloat(),
                highPosY + qHeight * 3 + textPadding
            )
        )
        canvas.drawText(
            inactiveText,
            mWith - xTextBounds!!.width() - textPaddingLeft,
            highPosY + qHeight * 3 - dip2px(5f),
            xTextPaint
        )
    }

    private fun generateResMap() {
        if (dayTimeDataModel == null) return
        var section: Section
        for (i in dayTimeDataModel!!.sections!!.indices) {
            section = dayTimeDataModel!!.sections!![i]
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
        }
    }

    private fun drawTop(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWith.toFloat(), topWith, bgTopPaint)
    }

    private fun drawLeftRect(canvas: Canvas) {
        val leftBgPaint = Paint()
        leftBgPaint.color = Color.parseColor("#33ffffff")
        val rectF = RectF(
            dip2px(6f).toFloat(),
            mHeight - bottomWith + dip2px(9f),
            dip2px(42f).toFloat(),
            mHeight.toFloat()
        )
        canvas.drawRoundRect(rectF, dip2px(5f).toFloat(), dip2px(5f).toFloat(), leftBgPaint)


    }

    private fun drawRightRect(canvas: Canvas) {
        val leftBgPaint = Paint()
        leftBgPaint.color = Color.parseColor("#33ffffff")
        val rectF = RectF(
            mWith - dip2px(42f).toFloat(),
            mHeight - bottomWith + dip2px(9f),
            mWith.toFloat() - dip2px(6f),
            mHeight.toFloat()
        )
        canvas.drawRoundRect(rectF, dip2px(5f).toFloat(), dip2px(3f).toFloat(), leftBgPaint)


    }

    private fun drawBottom(canvas: Canvas) {
        canvas.drawRect(
            0f, mHeight - bottomWith, mWith.toFloat(), mHeight.toFloat() - dip2px(9f), bgBottomPaint
        )
        if (showXAxis) {

            drawLeftRect(canvas)
            drawRightRect(canvas)
            val halfWidth = (mWith) / 2
            val leftHalf = halfWidth / 2

            var xText = "12 am"
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            xTextPaint.color = Color.parseColor("#ffffff")
            canvas.drawText(
                xText,
                (mWith - xTextBounds!!.width()).toFloat() - dip2px(9f),
                mHeight - bottomWith / 3 + dip2px(3f),
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
            xTextPaint.color = Color.parseColor("#ffffff")
            xTextPaint.getTextBounds(xText, 0, xText.length, xTextBounds)
            canvas.drawText(
                xText, dip2px(8f).toFloat(),
                mHeight - bottomWith / 3 + dip2px(3f), xTextPaint
            )
        }
    }

    private fun drawLeft(canvas: Canvas) {
        gridPaint.color = Color.parseColor("#3dffffff")
        val qHeight = (mHeight - bottomWith - topWith) / 4

        canvas.drawLine(0f, topWith, mWith.toFloat(), topWith, gridPaint!!)


        gridPaint?.color = Color.parseColor("#1effffff")

        canvas.drawLine(
            0f, mHeight - bottomWith, mWith.toFloat(), mHeight - bottomWith, gridPaint
        )


        canvas.drawLine(
            0f, topWith + qHeight * 1, mWith.toFloat(), topWith + qHeight * 1, gridPaint
        )
        canvas.drawLine(
            0f,
            topWith + qHeight * 2,
            mWith.toFloat(),
            topWith + qHeight * 2,
            gridPaint
        )
        canvas.drawLine(
            0f,
            topWith + qHeight * 3,
            mWith.toFloat(),
            topWith + qHeight * 3,
            gridPaint
        )
    }

    private fun isInSleepSection(index: Int): Boolean {
        if (mSleepSection.isNullOrEmpty()) return false

        var isInSleepSection = false
        mSleepSection?.forEach {
            if (index in it.start..it.end) {
                isInSleepSection = true
                return@forEach
            }
        }
        return isInSleepSection
    }

    private fun isInNapSection(index: Int): Boolean {
        if (mNapSection.isNullOrEmpty()) return false

        var isInNapSection = false
        mNapSection?.forEach {
            if (index in it.start..it.end) {
                isInNapSection = true
                return@forEach
            }
        }
        return isInNapSection
    }

    private fun drawBarContent(canvas: Canvas) {
        if (list.size == 0) return
        unitHLenth = (mWith.toFloat()) / (list.size)
        var x = 0f
        var barPaints: Paint?
        barStartEndXPosList = ArrayList()
        var barType = 0
        var barHeight: Int = 0

        list.forEachIndexed { index, it ->
            val end = x + unitHLenth / 2
            when (it.value) {
                0 -> {
                    barHeight = dip2px(18f)
                    barPaints = if (isInteracting) inActiveBarPaintI else inActiveBarPaint
                    barType = 0
                }

                1 -> {
                    barHeight = dip2px(55f)
                    barPaints = if (isInteracting) lowBarPaintI else lowBarPaint
                    barType = 1
                }

                2 -> {
                    barHeight = dip2px(92f)
                    barPaints = if (isInteracting) mediumPaintI else mediumPaint
                    barType = 2
                }

                3 -> {
                    barHeight = dip2px(130f)
                    barPaints = if (isInteracting) highPaintI else highPaint
                    barType = 3
                }

                else -> {
                    barHeight = dip2px(8f)
                    barPaints = if (isInteracting) noDataBarPaintI else noDataBarPaint
                    barType = 4
                }
            }

            if (!isInSleepSection(index) && !isInNapSection(index)) {
                val rectF = RectF(
                    x,
                    mHeight.toFloat() - barHeight - bottomWith,
                    end,
                    mHeight.toFloat() - bottomWith
                )
                val corners = floatArrayOf(
                    80f, 80f,   // Top left radius in px
                    80f, 80f,   // Top right radius in px
                    0f, 0f,     // Bottom right radius in px
                    0f, 0f      // Bottom left radius in px
                )
                barPaints?.let {
                    canvas.drawRoundRect(rectF, dip2px(20f).toFloat(), dip2px(20f).toFloat(), it)
                    val path = Path()
                    path.addRoundRect(rectF, corners, Path.Direction.CW)
                    canvas.drawPath(path, barPaints!!)
                }

                barStartEndXPosList?.add(DayTimeXYDataModel(x, end, barType, barHeight))
            } else {
                barStartEndXPosList?.add(DayTimeXYDataModel(x, end, 5, barHeight))
            }

            x = end + unitHLenth / 2
        }


        val imageSize = dip2px(16f)
        for (i in dayTimeDataModel!!.sections!!.indices) {
            val section = dayTimeDataModel!!.sections!![i]
            val calculatedEnd = if (section.end < 95) {
                section.end + 1
            } else {
                section.end
            }


            rectF?.left = section.start * unitHLenth
            rectF?.top = topWith
            rectF?.right = rectF!!.left + (calculatedEnd - section.start) * unitHLenth
            rectF?.bottom = mHeight - bottomWith

            chartLineFillPaint.setShader(resMap!![i]!!.first)
            canvas.drawRect(rectF!!, chartLineFillPaint)

            rectF?.left = section.start * unitHLenth
            rectF?.top = topWith - dip2px(1f)
            rectF?.right = rectF!!.left + (calculatedEnd - section.start) * unitHLenth
            rectF?.bottom = topWith + dip2px(1f)
            gridPaint.color = section.color
            canvas.drawRect(rectF!!, gridPaint)




            if (section.type.equals("combined", true)) {
                val text = "${section.count}"
                topCombinedPaint.getTextBounds(text, 0, text.length, xTextBounds)

                // Center horizontally within section, then clamp to view bounds to avoid cropping
                val textWidth = xTextBounds!!.width().toFloat()
                val centerX = (rectF!!.left + rectF!!.right) / 2f
                var textX = centerX - textWidth / 2f

                if (textX < 0f) {
                    textX = 0f
                } else if (textX + textWidth > mWith) {
                    textX = mWith - textWidth
                }

                canvas.drawText(
                    text,
                    textX,
                    rectF!!.top - xTextBounds!!.height(),
                    topCombinedPaint
                )
            } else {
                // Compute centered image rect, then clamp horizontally to avoid cropping
                val centerX = (rectF!!.right + rectF!!.left) / 2f
                var left = centerX - imageSize / 2f
                val top = topWith - imageSize - dip2px(10f)

                if (left < 0f) {
                    left = 0f
                } else if (left + imageSize > mWith) {
                    left = (mWith - imageSize).toFloat()
                }

                rectF!!.left = left
                rectF!!.top = top
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


    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun enableInteractiveMode(mode: Boolean) {
        interactiveMode = mode
    }

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
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isInteracting) {
                        if (event.y < dip2px(50f)) {
                            listener?.onTopClicked()
                        }
                    }

                    resetState()
                    return true
                }
            }
        } else {
            return super.onTouchEvent(event)
        }
        return false
    }

    private fun resetState() {
        handler.removeCallbacks(mLongPressed)
        isInteracting = false
        listener?.isInteractionOnGoing(false)
        touchX = 0.0f
        invalidate()
    }


    fun resetIfInteracting() {
        handler.removeCallbacks(mLongPressed)
        if (isInteracting) {
            isInteracting = false
            listener?.isInteractionOnGoing(false)
            touchX = 0.0f
            invalidate()
        }
    }

    private fun drawOverlay(canvas: Canvas) {
        if (!isInteracting) return
        if (touchX != null) {
            if (touchX!! > 0 && touchX!! < mWith) {
                val rectF = RectF()
                rectF.left = touchX!! - dip2px(0.5f)
                rectF.right = touchX!! + dip2px(0.5f)
                rectF.top = topWith
                rectF.bottom = mHeight - bottomWith
                //val value: Pair<Int, Int> = getClickedValue(touchX!!)
                //LOGS.d("CLICKED_VALUE value " + value + " Touch " + touchX!!.toInt())
                canvas.drawRect(rectF, overlayLinePaint)
                for (it in 0 until (barStartEndXPosList?.size ?: 0)) {
                    var barColor: Int
                    var barHeight: Int
                    when (barStartEndXPosList!![it].barType) {
                        0 -> {
                            barColor =
                                ContextCompat.getColor(
                                    context,
                                    R.color.daytime_inactive_selected_color
                                )
                            barHeight = dip2px(18f)
                        }

                        1 -> {
                            barColor =
                                ContextCompat.getColor(context, R.color.daytime_low_selected_color)
                            barHeight = dip2px(55f)
                        }

                        2 -> {
                            barColor =
                                ContextCompat.getColor(
                                    context,
                                    R.color.daytime_medium_selected_color
                                )
                            barHeight = dip2px(92f)
                        }

                        3 -> {
                            barColor =
                                ContextCompat.getColor(context, R.color.white)
                            barHeight = dip2px(130f)
                        }

                        5 -> {
                            barColor = ContextCompat.getColor(
                                context,
                                R.color.daytime_zero_data_selected_color
                            )
                            barHeight = 0
                        }

                        else -> {
                            barColor = ContextCompat.getColor(
                                context,
                                R.color.daytime_zero_data_selected_color
                            )
                            barHeight = dip2px(8f)
                        }
                    }
                    if (touchX!! in barStartEndXPosList!![it].startX..barStartEndXPosList!![it].endX) {
                        val rectOnTopF = RectF()
                        rectOnTopF.left = barStartEndXPosList!![it].startX - dip2px(1f)
                        rectOnTopF.right = barStartEndXPosList!![it].endX + dip2px(1f)
                        rectOnTopF.top =
                            mHeight.toFloat() - barHeight - bottomWith
                        rectOnTopF.bottom = mHeight.toFloat() - bottomWith
                        overlayLineOnTopPaint.color = barColor
                        canvas.drawRoundRect(
                            rectOnTopF,
                            dip2px(2f).toFloat(),
                            dip2px(2f).toFloat(),
                            overlayLineOnTopPaint
                        )


                        if (listener != null) {
                            //val position = value.first as Int
                            //val selectedValue = value.second as Int
                            val selectedBar = barStartEndXPosList!![it].barType
                            if (lastSentValuePos == null && selectedBar != 5) {
                                listener?.onValueSelected(selectedBar, 95 - it)
                                lastSentValuePos = it
                                performHapticFeedbackCustom(selectedBar)
                            } else {
                                if (lastSentValuePos != it && selectedBar != 5) {
                                    listener?.onValueSelected(selectedBar, 95 - it)
                                    lastSentValuePos = it
                                    performHapticFeedbackCustom(selectedBar)
                                }
                            }
                        }

                        break
                    }
                }


            }
        }
    }

    private fun getClickedValue(touchX: Float): Pair<Int, Int> {
        var sectionLast = 0f
        var position = -1
        for (i in list.size - 1 downTo 1) {
            val sectionEnd = sectionLast + unitHLenth
            if (touchX <= sectionEnd) {
                position = i
                break
            }
            sectionLast = sectionEnd
        }
        LOGS.d("POSITION_VALUE $position")
        return if (position == -1) {
            Pair(0, 0)
        } else {
            Pair(position, list[95 - position].value)
        }
    }

    fun setVibrationUtil(vibrationUtils: VibrationUtils) {
        this.vibrationUtils = vibrationUtils
    }

    private fun performHapticFeedbackCustom(value: Int) {
        //if (value == 0 || value == 1 || value == 2 || value == 3) {
        vibrationUtils?.vibrate(HAPTIC_VIBRATION)

        /*this.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )*/
        //}
    }


    private val handler = Handler(Looper.getMainLooper())
    private var mLongPressed = Runnable {
        isInteracting = true
        invalidate()
        listener?.isInteractionOnGoing(true)

        vibrationUtils?.vibrate(HAPTIC_VIBRATION)

        val parent = parent
        parent.requestDisallowInterceptTouchEvent(true)


        /*rootView.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )*/
    }
}
