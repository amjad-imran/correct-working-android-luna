package com.noisefit_commans.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.Path.Direction
import android.util.AttributeSet
import android.view.View
import com.noisefit_commans.R
import java.util.*


class SemiCircleArcProgressBar : View {
    private var padding = 25
    private var progressPlaceHolderColor = 0
    private var progressBarStartColor = 0
    private var progressBarEndColor = 0
    private var progressPlaceHolderWidth = 0
    private var progressBarWidth = 0
    private var percent = 0
    private var top = 0
    private var left = 0
    private var right = 0
    private var bottom = 0

    private var context: Context? = null

    //Constructors
    constructor(context: Context?) : super(context) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setAttrs(context, attrs)
        this.context = context
    }

    override fun onDraw(canvas: Canvas) {
        padding =
            if (progressBarWidth > progressPlaceHolderWidth) progressBarWidth + 5 else progressPlaceHolderWidth + 5
        top = padding
        left = padding
        right = measuredWidth
        bottom = measuredHeight * 2
        val progressAmount = percent * 1.8.toFloat()
        canvas.drawArc(
            progressBarRectF,
            180f,
            180f,
            false,
            getPaint(progressPlaceHolderColor, progressPlaceHolderWidth)
        ) //arg2: For the starting point, the starting point is 0 degrees from the positive direction of the x coordinate system. How many angles are arg3 selected to rotate clockwise?
        canvas.drawArc(
            progressBarRectF,
            180f,
            180f,
            false,
            getPaint(progressPlaceHolderColor, progressBarWidth)
        )
        canvas.drawArc(
            progressBarRectF,
            180f,
            progressAmount,
            false,
            getPaint(progressBarStartColor, progressBarEndColor, progressBarWidth)
        ) //arg2: For the starting point, the starting point is 0 degrees from the positive direction of the x coordinate system. How many angles are arg3 selected to rotate clockwise?

        drawProgressArc(canvas)

    }

    private fun drawProgressArc(canvas: Canvas) {
        val pathShape = Path()


        val advance = 20.0f
        val phase = 20.0f
        val style = PathDashPathEffect.Style.ROTATE

        val rectF = RectF(-5f, -10f, 5f, 30f)
        pathShape.addRoundRect(rectF, 5f, 5f, Direction.CW)
        val progressPaint = Paint()
        progressPaint.isAntiAlias = true
        progressPaint.strokeWidth = 40f
        progressPaint.color = progressBarStartColor
        progressPaint.style = Paint.Style.STROKE
        progressPaint.pathEffect = PathDashPathEffect(pathShape, advance, phase, style);

        /*DashPathEffect(
            floatArrayOf(10f, 10f),
            10f
        )*/

        val arcPadding = 50f
        val archRectF = progressBarRectF
        archRectF.left = archRectF.left + arcPadding
        archRectF.right = archRectF.right - arcPadding
        archRectF.top = archRectF.top + arcPadding
        archRectF.bottom = archRectF.bottom - arcPadding

        canvas.drawArc(archRectF, 180f, 180f, false, progressPaint)

    }

    //Private Methods
    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SemiCircleArcProgressBar, 0, 0)
        try {
            progressPlaceHolderColor = typedArray.getColor(
                R.styleable.SemiCircleArcProgressBar_progressPlaceHolderColor,
                Color.GRAY
            )
            progressBarStartColor = typedArray.getColor(
                R.styleable.SemiCircleArcProgressBar_progressBarStartColor,
                Color.WHITE
            )
            progressBarEndColor = typedArray.getColor(
                R.styleable.SemiCircleArcProgressBar_progressBarEndColor,
                Color.WHITE
            )
            progressPlaceHolderWidth =
                typedArray.getInt(R.styleable.SemiCircleArcProgressBar_progressPlaceHolderWidth, 25)
            progressBarWidth =
                typedArray.getInt(R.styleable.SemiCircleArcProgressBar_progressBarWidth, 10)
            percent = typedArray.getInt(R.styleable.SemiCircleArcProgressBar_percent, 76)
        } finally {
            typedArray.recycle()
        }
    }

    private fun getPaint(startColor: Int, endColor: Int, strokeWidth: Int): Paint {
        val shaderDeep = LinearGradient(
            0f,
            0f,
            0f,
            SleepProgressbarView.pxFromDp(context!!, 10f),
            endColor,
            startColor,
            Shader.TileMode.CLAMP
        )

        val paint = Paint()
        paint.shader = shaderDeep
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth.toFloat()
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }

    private fun getPaint(color: Int, strokeWidth: Int): Paint {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth.toFloat()
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }

    private val progressBarRectF: RectF
        private get() = RectF(
            left.toFloat(),
            top.toFloat(),
            (right - padding).toFloat(),
            (bottom - padding * 2).toFloat()
        )

    //Setters
    fun setProgressPlaceHolderColor(color: Int) {
        progressPlaceHolderColor = color
        postInvalidate()
    }

    fun setProgressBarColor(color: Int) {
        progressBarStartColor = color
        postInvalidate()
    }

    fun setProgressPlaceHolderWidth(width: Int) {
        progressPlaceHolderWidth = width
        postInvalidate()
    }

    fun setProgressBarWidth(width: Int) {
        progressBarWidth = width
        postInvalidate()
    }

    fun setPercent(percent: Int) {
        this.percent = percent
        postInvalidate()
    }

    //Custom Setter
    fun setPercentWithAnimation(percent: Int) {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            var step = 0
            override fun run() {
                if (step <= percent) setPercent(step++)
            }
        }, 0, 12)
    }
}