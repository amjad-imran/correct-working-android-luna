package com.oreo.ui.custom


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS

class PeriodCycleBar : View {

    private var radius = dp2px(10f).toFloat()
    private var marginVertical = dp2px(10f).toFloat()
    private var width = 0


    private var cycleLength: Int = 0
    private var periodLength: Int = 0
    private var ovStart: Int = 0
    private var ovEnd: Int = 0
    private var ovDay: Int = 0
    private val bgBarPaint = Paint()
    private val periodPaint = Paint()
    private val ovulationPaint = Paint()
    lateinit var rectF: RectF
    private var glowDotBitmap: Bitmap? = null


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

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }


    private fun initPaint() {

        bgBarPaint.apply {
            color = Color.parseColor("#0fffffff")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        periodPaint.apply {
            color = Color.parseColor("#ff84d5")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        ovulationPaint.apply {
            color = Color.parseColor("#1ec9ff")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val res = resources
        val bitmap =
            BitmapFactory.decodeResource(res, com.noisefit_commans.R.drawable.ic_glow_graph)
        glowDotBitmap = Bitmap.createScaledBitmap(bitmap, dip2px(28f), dip2px(28f), true)

        rectF = RectF()
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PercentageBar)
        ta.recycle()
        initPaint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initPaint()
        drawBarBack(canvas)
        drawPeriodBar(canvas)
        drawOvulationBar(canvas)
    }

    private fun drawOvulationBar(canvas: Canvas) {
        val width = width.toFloat()
        val ovulationStart = (width / cycleLength) * ovStart
        val ovulationEnd = (width / cycleLength) * ovEnd
        val ovulationDatePos = (width / cycleLength) * ovDay

        val margin = dip2px(1f).toFloat()
        rectF.apply {
            left = ovulationStart
            right = ovulationEnd
            top = marginVertical + margin
            bottom = height.toFloat() - marginVertical - margin
        }

        val corners = floatArrayOf(
            0f, 0f,
            0f, 0f,
            0f, 0f,
            0f, 0f
        )

        val path = Path()
        path.addRoundRect(rectF, corners, Path.Direction.CW)
        canvas.drawPath(path, ovulationPaint)


        val bitWidth = (glowDotBitmap!!.width / 2).toFloat()
        val bitHeight = (glowDotBitmap!!.height / 2).toFloat()

        canvas.drawBitmap(
            glowDotBitmap!!,
            ovulationDatePos - bitWidth,
            (height / 2) - bitHeight,
            null
        )


    }

    private fun drawPeriodBar(canvas: Canvas) {
        val width = width.toFloat()
        val periodWidth = (width / cycleLength) * periodLength
        val margin = dip2px(1f).toFloat()

        rectF.apply {
            left = margin
            right = periodWidth
            top = marginVertical + margin
            bottom = height.toFloat() - marginVertical - margin
        }

        val corners = floatArrayOf(
            radius, radius,
            0f, 0f,
            0f, 0f,
            radius, radius
        )

        val path = Path()
        path.addRoundRect(rectF, corners, Path.Direction.CW)
        canvas.drawPath(path, periodPaint)
    }

    private fun drawBarBack(canvas: Canvas) {
        rectF.apply {
            left = 0f
            right = width.toFloat()
            top = marginVertical
            bottom = height.toFloat() - marginVertical
        }
        canvas.drawRoundRect(rectF, radius, radius, bgBarPaint)

    }


    private fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun setData(
        cycleLength: Int,
        periodLength: Int,
        ovStart: Int,
        ovEnd: Int,
        ovDay: Int
    ) {
        this.cycleLength = cycleLength
        this.periodLength = periodLength
        this.ovStart = ovStart
        this.ovEnd = ovEnd
        this.ovDay = ovDay

        postInvalidate()
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
