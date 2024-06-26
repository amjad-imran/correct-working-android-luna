package com.oreo.ui.custom.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import kotlin.math.min


class CircularProgressBar constructor(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var progress = 0
    private var max = 100
    private val startAngle = -225f
    private var rectF: RectF? = null
    lateinit var backgroundPaint: Paint
    lateinit var progressPaint: Paint


    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        var strokeWidth = 10f
        attrs?.let {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar)
            strokeWidth = ta.getDimension(R.styleable.CircularProgressBar_archWidth, 10f)
            ta.recycle()
        }

        rectF = RectF()

        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.setColor(Color.parseColor("#19ffffff"))
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = strokeWidth
        backgroundPaint.strokeCap = Paint.Cap.ROUND

        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        //progressPaint.setColor(-0x9dff12)
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = strokeWidth
        progressPaint.strokeCap = Paint.Cap.ROUND


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val colors = intArrayOf(
            Color.parseColor("#725a93"),
            Color.parseColor("#f0e4ff")
        )
        val positions = floatArrayOf(0.0f, 1.0f)
        val sweepGradient = SweepGradient(width / 2F, height / 2F, colors, positions)
        sweepGradient.apply {
            val rotate = 90f
            val gradientMatrix = Matrix()
            gradientMatrix.preRotate(rotate, width / 2F, height / 2F)
            setLocalMatrix(gradientMatrix)
        }
        progressPaint.setShader(sweepGradient)

    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val radius = (min(width.toDouble(), height.toDouble()) / 2).toFloat()

        rectF!![width / 2 - radius + 30, height / 2 - radius + 30, width / 2 + radius - 30] =
            height / 2 + radius - 30

        canvas.drawArc(rectF!!, startAngle, 270f, false, backgroundPaint)

        val sweepAngle = ((270 * progress) / max).toFloat()
        canvas.drawArc(rectF!!, startAngle, sweepAngle, false, progressPaint)

    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}