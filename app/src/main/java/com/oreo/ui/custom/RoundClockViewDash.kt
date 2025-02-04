package com.oreo.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.noisefit.luna.R
import com.noisefit.timepickerslider.utils.dpToPx
import com.noisefit_commans.utils.LOGS
import java.time.LocalTime
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt


class RoundClockViewDash @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _backgroundImageMain: Drawable? = null
    private var _imageDial: Drawable? = null
    private var _middlePoint = PointF(0f, 0f)

    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null
    private var debtMinutes = 0L

    private lateinit var defaultArcPaintStroke: Paint
    private lateinit var arcPaintStroke: Paint
    private lateinit var debtPaintStroke: Paint

    init {
        initPaint()
        initAttributes(attrs)
        updateMiddlePoint()
    }

    private fun initPaint() {

        arcPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C5A8ED")
            style = Paint.Style.STROKE
            strokeWidth = 16f
        }
        defaultArcPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1FFFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        debtPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A477FF")
            style = Paint.Style.STROKE
            strokeWidth = 16f
        }

    }

    private fun initAttributes(attributeSet: AttributeSet?) {

        val attr: TypedArray =
            context.obtainStyledAttributes(
                attributeSet,
                R.styleable.PlannerClock, 0, 0
            )

        try {
            _backgroundImageMain =
                attr.getDrawable(R.styleable.PlannerClock_background_image_main)?.mutate()
            _imageDial =
                attr.getDrawable(R.styleable.PlannerClock_image_dial)?.mutate()
        } finally {
            attr.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(
            canvas,
            _backgroundImageMain,
            _imageDial,
            width.toFloat() / 2,
            _middlePoint.x,
            _middlePoint.y
        )
        drawDurationArc(canvas)

    }

    private fun drawDurationArc(canvas: Canvas) {
        val padding = defaultArcPaintStroke.strokeWidth / 2
        val rectF = RectF(padding, padding, width.toFloat() - padding, height.toFloat() - padding)

        canvas.drawArc(rectF, 0f, 360f, false, defaultArcPaintStroke)


        if (startTime != null && endTime != null) {

            val startAngle = calculateAngle(startTime!!.hour, startTime!!.minute) - 90f
            val end = (calculateAngle(endTime!!.hour, endTime!!.minute) - 90f)

            val degree = if (startAngle < 0) {
                abs(startAngle) + end
            } else {
                if (end < startAngle) {
                    360f - startAngle + end
                } else {
                    startAngle + end
                }
            }

            val arcPadding =
                defaultArcPaintStroke.strokeWidth - arcPaintStroke.strokeWidth + arcPaintStroke.strokeWidth / 4
            val arcRect =
                RectF(
                    arcPadding,
                    arcPadding,
                    width.toFloat() - arcPadding,
                    height.toFloat() - arcPadding
                )


            val debtStart = endTime!!.minusMinutes(debtMinutes)

            val debtStartAngle = (calculateAngle(debtStart.hour, debtStart.minute) - 90f)
            val debtDegree = end - debtStartAngle

            canvas.drawArc(arcRect, startAngle, degree - debtDegree, false, arcPaintStroke)
            canvas.drawArc(arcRect, debtStartAngle, debtDegree, false, debtPaintStroke)
        }
    }

    private fun calculateAngle(hour: Int, minute: Int): Float {
        val hourAngle = ((hour % 12) * 30).toFloat()
        val minuteAngle = (minute / 60f) * 30
        return hourAngle + minuteAngle
    }

    private fun drawBackground(
        canvas: Canvas,
        backgroundImageMain: Drawable?,
        imageDial: Drawable?,
        widthExternal: Float,
        x: Float,
        y: Float
    ) {
        if (backgroundImageMain != null) {
            val margin = dpToPx(8f).roundToInt()

            backgroundImageMain.setBounds(
                (x - widthExternal).toInt() + margin,
                (y - widthExternal).toInt() + margin,
                (x + widthExternal).toInt() - margin,
                (y + widthExternal).toInt() - margin
            )
            backgroundImageMain.draw(canvas)
        }
        if (imageDial != null) {
            val margin = dpToPx(8f).roundToInt()

            imageDial.setBounds(
                (x - widthExternal).toInt() + margin,
                (y - widthExternal).toInt() + margin,
                (x + widthExternal).toInt() - margin,
                (y + widthExternal).toInt() - margin
            )
            imageDial.draw(canvas)
        }
    }

    private fun updateMiddlePoint() {
        _middlePoint.set(width / 2f, height / 2f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(w, h, oldWidth, oldHeight)

        updateMiddlePoint()
    }


    /**
     * @param start - start time
     */
    fun setData(start: LocalTime, end: LocalTime, debtMinutes: Long) {
        this.startTime = start
        this.endTime = end
        this.debtMinutes = debtMinutes
        invalidate()
    }

}