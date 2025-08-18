package com.oreo.ui.custom

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit_commans.ui.dpToPixel
import java.util.Calendar
import kotlin.math.min

class CircularScheduleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pointerBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_scheduler_time_pointer)

    var events: List<ClockEvent> = emptyList()
        set(value) {
            field = value; invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClock(canvas)
        drawEvents(canvas)

        drawCurrentTimeMarker(canvas)
    }

    private fun drawCurrentTimeMarker(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 90f.dpToPixel()

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentHourFloat = hour + (minute / 60f)

        val angle = ((hour / 24f) * 360f) - 90f

        val pointerRadius = radius + (pointerBitmap.height / 2)

        val rad = Math.toRadians(angle.toDouble())
        val px = (cx + pointerRadius * Math.cos(rad)).toFloat()
        val py = (cy + pointerRadius * Math.sin(rad)).toFloat()

        canvas.save()
        canvas.translate(px, py)
        val imageOffset = 60f
        canvas.rotate(angle - imageOffset) // +90 so tip faces outwards
        canvas.drawBitmap(
            pointerBitmap,
            -pointerBitmap.width / 2f, // Center it
            -pointerBitmap.height / 2f,
            null
        )
        canvas.restore()

    }

    private fun drawClock(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 12f.dpToPixel()

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 2f.dpToPixel()
            color = Color.parseColor("#1AFFFFFF")
        }
        val tickPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 2f.dpToPixel()
            color = Color.parseColor("#4DFFFFFF")
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBCADA")
            textSize = min(cx, cy) * 0.13f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val tickStart = radius - min(cx, cy) * 0.07f
        val tickEnd = radius
        val textRadius = tickStart + (tickEnd - tickStart) / 2

        for (i in 0 until 24) {
            val angle = Math.toRadians((i * 15.0) - 90.0)
            val x1 = (cx + tickStart * Math.cos(angle)).toFloat()
            val y1 = (cy + tickStart * Math.sin(angle)).toFloat()
            val x2 = (cx + tickEnd * Math.cos(angle)).toFloat()
            val y2 = (cy + tickEnd * Math.sin(angle)).toFloat()

            if (i % 6 != 0) {
                canvas.drawLine(x1, y1, x2, y2, if (i % 2 == 0) tickPaint2 else tickPaint)
            } else {
                val hour = when (i) {
                    0 -> "00"
                    6 -> "06"
                    12 -> "12"
                    18 -> "18"
                    else -> i.toString().padStart(2, '0')
                }
                val textX = (cx + textRadius * Math.cos(angle)).toFloat()
                val textY = (cy + textRadius * Math.sin(angle) + textPaint.textSize / 3)
                canvas.drawText(hour, textX, textY.toFloat(), textPaint)
            }
        }
    }

    private fun drawEvents(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f

        val radius = min(cx, cy) - 76f.dpToPixel()
        canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#66000000".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f.dpToPixel()
        })


        for (event in events) {

            when(event.eventType){
                ClockEventType.LINE -> {
                    val radius = min(cx, cy) - 67f.dpToPixel()
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 4f.dpToPixel()
                        strokeCap = Paint.Cap.ROUND
                    }
                    val rect = RectF(
                        cx - radius,
                        cy - radius,
                        cx + radius,
                        cy + radius
                    )
                    paint.color = event.color
                    val startAngle = hourToAngle(event.startHour)
                    val endAngle = hourToAngle(event.endHour)
                    canvas.drawArc(rect, startAngle, endAngle - startAngle, false, paint)
                }
                ClockEventType.ARCH -> {
                    val radius = min(cx, cy) - 50f.dpToPixel()
                    val arcStrokeWidth = 24f.dpToPixel()
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = arcStrokeWidth
                        strokeCap = Paint.Cap.BUTT
                    }
                    val rect = RectF(
                        cx - radius,
                        cy - radius,
                        cx + radius,
                        cy + radius
                    )
                    val linearGradient = LinearGradient(
                        cx, cy - radius,
                        cx, cy + radius,
                        event.color , event.endColor,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = linearGradient


                    val startAngle = (event.startHour / 24f) * 360f - 90f
                    val sweepAngle = ((event.endHour - event.startHour) / 24f) * 360f
                    canvas.drawArc(rect, startAngle, sweepAngle, false, paint)


                    //val startAngle = hourToAngle(event.startHour)
                    val endAngle = hourToAngle(event.endHour)
                    val midAngle = (startAngle + endAngle) / 2
                }
                ClockEventType.GRAPH -> {


                }
            }

            /*drawEventLabel(
                canvas,
                cx, cy,
                midAngle,
                radius + 40f, // push the label a bit outside the arc
                event.label,
                event.color
            )*/
        }
    }


    private fun hourToAngle(hour: Float): Float {
        // 0 hour = -90deg (top), increases clockwise
        return (hour / 24f) * 360f - 90f
    }

    private fun drawEventLabel(
        canvas: Canvas,
        cx: Float, // center x
        cy: Float, // center y
        angle: Float, // angle in degrees where to place label
        radius: Float, // distance from center
        label: String,
        color: Int
    ) {
        val rectWidth = 120f
        val rectHeight = 36f
        val cornerRadius = 18f

        // Calculate position on the circle's edge
        val rad = Math.toRadians(angle.toDouble())
        val x = (cx + radius * Math.cos(rad)).toFloat()
        val y = (cy + radius * Math.sin(rad)).toFloat()

        // Center the rectangle at (x, y)
        val left = x - rectWidth / 2
        val top = y - rectHeight / 2
        val right = x + rectWidth / 2
        val bottom = y + rectHeight / 2

        // Draw rounded rectangle
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }
        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, rectPaint)

        // Draw text centered in rectangle
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f * resources.displayMetrics.density // for scale
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setColor("#ffffff".toColorInt())
        }

        // Center text vertically
        val textY = y - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(label, x, textY, textPaint)
    }

}

data class ClockEvent(
    val startHour: Float,
    val endHour: Float,
    val eventType: ClockEventType,
    val color: Int,
    val endColor: Int,
    val label: String
)

enum class ClockEventType {
    LINE, ARCH, GRAPH
}

