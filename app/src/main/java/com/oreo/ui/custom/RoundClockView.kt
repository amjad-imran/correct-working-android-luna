package com.oreo.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
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
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class RoundClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _backgroundImageMain: Drawable? = null
    private var _imageDial: Drawable? = null
    private var _hourNeedle: Drawable? = null
    private var _middlePoint = PointF(0f, 0f)

    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null
    private var debtMinutes = 0L

    private lateinit var arcPaint: Paint
    private lateinit var arcPaintStroke: Paint
    private lateinit var debtPaintStroke: Paint
    private lateinit var arcBounds: RectF

    init {
        initPaint()
        initAttributes(attrs)
        updateMiddlePoint()
    }

    private fun initPaint() {
        arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#29C5A8ED")
            style = Paint.Style.FILL
        }
        arcPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C5A8ED")
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        debtPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A477FF")
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }

        arcBounds = RectF()
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
            _hourNeedle =
                attr.getDrawable(R.styleable.PlannerClock_hour_needle_image)?.mutate()
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


        drawHourNeedle(
            canvas,
            width.toFloat() / 2,
            _middlePoint
        )

        /*  val width = width.toFloat()
          val height = height.toFloat()
          val radius = (Math.min(width, height) / 2)
          val centerX = _middlePoint.x
          val centerY = _middlePoint.y


          //canvas.drawCircle(centerX, centerY, radius, clockPaint)

          // Get the current time
          val calendar = Calendar.getInstance()
          val hours = calendar.get(Calendar.HOUR)
          val minutes = calendar.get(Calendar.MINUTE)

          // Calculate the angle of the hour hand
          val hourAngle = (hours + minutes / 60f) * 30 // 360° / 12 = 30° per hour

          // Draw the hour hand
          val hourHandLength = radius * 0.8f
          val hourHandX =
              centerX + hourHandLength * cos(Math.toRadians((hourAngle - 90).toDouble())).toFloat()
          val hourHandY =
              centerY + hourHandLength * sin(Math.toRadians((hourAngle - 90).toDouble())).toFloat()


          //canvas.drawPath(needlePath, needlePaint)

          canvas.drawLine(centerX, centerY, hourHandX, hourHandY, handPaint)*/
    }

    private fun drawText(canvas: Canvas, path: Path, halfWidth: Float, middlePoint: PointF) {

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            textSize = dpToPx(12f)
            color = Color.parseColor("#C5A8ED")
        }

        val text = "7h 32m"
        val textBounds = Rect()

        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val circlePath = Path()
        circlePath.addCircle(middlePoint.x, middlePoint.y, halfWidth, Path.Direction.CCW)
        canvas.drawTextOnPath(text, circlePath, 0f, 0f, textPaint)

    }

    private fun drawHourNeedle(canvas: Canvas, halfWidth: Float, middlePoint: PointF) {

        if (_hourNeedle != null) {
            val margin = dpToPx(24f).roundToInt()

            _hourNeedle!!.setBounds(
                (middlePoint.x - halfWidth).toInt() + margin,
                (middlePoint.y - halfWidth).toInt() + margin,
                (middlePoint.x + halfWidth).toInt() - margin,
                (middlePoint.y + halfWidth).toInt() - margin
            )

            val calendar = Calendar.getInstance()
            val hours = calendar.get(Calendar.HOUR)
            val minutes = calendar.get(Calendar.MINUTE)

            val hourAngle = (hours + minutes / 60f) * 30

            canvas.save()
            canvas.rotate(hourAngle, middlePoint.x, middlePoint.y)
            _hourNeedle!!.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawDurationArc(canvas: Canvas) {

        if (startTime != null && endTime != null) {

            val startAngle = calculateAngle(startTime!!.hour, startTime!!.minute)
            val end = (calculateAngle(endTime!!.hour, endTime!!.minute))

            val degree = if (startAngle < 0) {
                abs(startAngle) + end
            } else {
                if (end < startAngle) {
                    360f - startAngle + end
                } else {
                    startAngle + end
                }
            }

            val padding = dpToPx(20f)
            val sleepArcRectF =
                RectF(padding, padding, width.toFloat() - padding, height.toFloat() - padding)

            canvas.drawArc(sleepArcRectF, startAngle, degree, true, arcPaint)
            val path = Path()
            path.addArc(sleepArcRectF, startAngle, degree)

            drawText(
                canvas,
                path,
                width.toFloat() / 2,
                _middlePoint
            )


            val debtStart = endTime!!.minusMinutes(debtMinutes)

            val debtStartAngle = (calculateAngle(debtStart.hour, debtStart.minute))
            val debtDegree = end - debtStartAngle

            canvas.drawArc(sleepArcRectF, startAngle+1, degree - debtDegree, false, arcPaintStroke)

            canvas.drawArc(sleepArcRectF, debtStartAngle, debtDegree, false, debtPaintStroke)

            drawAngleLines(canvas, startAngle)

            val wakeAngle = calculateAngle(endTime!!.hour, endTime!!.minute)

            drawAngleLines(canvas, wakeAngle)


            if (debtMinutes > 0) {
                drawAngleLines(canvas, debtStartAngle)
            }


        }
    }

    private fun drawAngleLines(canvas: Canvas, angle: Float) {
        val hourHandLength = width / 2 - dpToPx(16f)
        val hourHandX =
            _middlePoint.x + hourHandLength * cos(Math.toRadians((angle).toDouble())).toFloat()
        val hourHandY =
            _middlePoint.y + hourHandLength * sin(Math.toRadians((angle).toDouble())).toFloat()

        val hourHandStartX =
            _middlePoint.x + (hourHandLength - dpToPx(8f)) * cos(Math.toRadians((angle).toDouble())).toFloat()
        val hourHandStartY =
            _middlePoint.y + (hourHandLength - dpToPx(8f)) * sin(Math.toRadians((angle).toDouble())).toFloat()


        val paint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(2f)
        }

        canvas.drawLine(
            hourHandStartX,
            hourHandStartY,
            hourHandX,
            hourHandY,
            paint
        )


    }

    private fun calculateAngle(hour: Int, minute: Int): Float {
        val hourAngle = ((hour % 12) * 30).toFloat()
        val minuteAngle = (minute / 60f) * 30
        return (hourAngle + minuteAngle) - 90f
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
            val margin = dpToPx(19f).roundToInt()

            backgroundImageMain.setBounds(
                (x - widthExternal).toInt() + margin,
                (y - widthExternal).toInt() + margin,
                (x + widthExternal).toInt() - margin,
                (y + widthExternal).toInt() - margin
            )
            backgroundImageMain.draw(canvas)
        }
        if (imageDial != null) {
            val margin = dpToPx(28f).roundToInt()

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
        val padding = 20
        arcBounds!![padding.toFloat(), padding.toFloat(), (w - padding).toFloat()] =
            (h - padding).toFloat()
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