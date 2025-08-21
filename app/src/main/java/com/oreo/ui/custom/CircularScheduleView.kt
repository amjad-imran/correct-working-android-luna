package com.oreo.ui.custom

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit.timepickerslider.utils.dpToPx
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class CircularScheduleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pointerBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_scheduler_time_pointer)

    val fontGilroy =
        ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99FFFFFF")
        typeface = fontGilroy
        textSize = 16f.dpToPixel()
        textAlign = Paint.Align.CENTER
    }

    private val textPaintWindowMessage = Paint().apply {
        color = "#99FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
        typeface = fontGilroy
        textSize = 10f.dpToPixel()
        isAntiAlias = true
    }
    private val textPaintWindowTimer = Paint().apply {
        color = "#FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
        textSize = 24f.dpToPixel()
        typeface = fontGilroy
        isAntiAlias = true
    }

    var events = ArrayList<ClockEvent>()
    var energyArray = ArrayList<Float>()
    var isLocked = false
    private var sleepStart: LocalTime? = null
    private var sleepEnd: LocalTime? = null

    private var sleepWakeBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.ic_circadian_wake_up),
        15f.dpToPixel().toInt(),
        15f.dpToPixel().toInt(),
        true
    )

    private var sleepBedBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.ic_circadian_bed_time),
        12f.dpToPixel().toInt(),
        12f.dpToPixel().toInt(),
        true
    )

    init {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                invalidate()
                delay(1000)
            }
        }
    }

    fun setDataSet(
        events: List<ClockEvent>,
        energyArray: List<Float>,
        sleepStart: String? = null,
        sleepEnd: String? = null,
        isLocked: Boolean
    ) {
        this.events.addAll(events)
        this.energyArray.addAll(energyArray)
        this.isLocked = isLocked


        sleepStart?.let {
            this.sleepStart =
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toLocalTime()
        }
        sleepEnd?.let {
            this.sleepEnd =
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toLocalTime()
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClock(canvas)
        if (isLocked.not()) {
            drawEvents(canvas)
            drawCircularEnergyCurveWithFade(
                canvas, energyArray
            )
            showTimer(canvas)
            drawCurrentTimeMarker(canvas)
        }
    }

    private fun showTimer(canvas: Canvas) {
        val timer = getCurrentWindowTimer() * 1000L
        if (timer == 0L) return

        canvas.drawText(
            "Window closes In",
            width / 2f,
            height / 2f - 12f.dpToPixel(),
            textPaintWindowMessage
        )

        val seconds = (timer / 1000) % 60
        val minutes = (timer / (1000 * 60)) % 60
        val hours = (timer / (1000 * 60 * 60))

        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        canvas.drawText(timeString, width / 2f, height / 2f + 14f.dpToPixel(), textPaintWindowTimer)
    }

    private fun getCurrentWindowTimer(): Long {

        val eventsPair = ArrayList<Pair<LocalTime, LocalTime>>()
        events.forEach {
            if (it.eventType.equals(ClockEventType.ARCH)) {
                eventsPair.add(
                    Pair(
                        fromFloatHour(it.startHour),
                        fromFloatHour(it.endHour),
                    )
                )
            } else if (it.eventType.equals(ClockEventType.LINE)) {
                eventsPair.add(
                    Pair(
                        fromFloatHour(it.startHour),
                        fromFloatHour(it.endHour),
                    )
                )
            }
        }

        val currentPair = findCurrentPair(eventsPair)

        if (currentPair == null) return 0L

        val endTime = currentPair.second

        val currentTime = LocalTime.now()
        val diff = Duration.between(currentTime, endTime)

        return diff.toSeconds()
    }


    private fun findCurrentPair(timePairs: List<Pair<LocalTime, LocalTime>>): Pair<LocalTime, LocalTime>? {
        val now = LocalTime.now()

        for ((startTime, endTime) in timePairs) {

            // Handle ranges that don't cross midnight
            if (startTime <= endTime) {
                if (now >= startTime && now <= endTime) {
                    return Pair(startTime, endTime)
                }
            } else {
                // Handle ranges that cross midnight (e.g. 23:00–02:00)
                if (now >= startTime || now <= endTime) {
                    return Pair(startTime, endTime)
                }
            }
        }
        return null
    }


    private fun getColorByValue(value: Float): Int {
        if (value == 0.0f) {
            return "#00000000".toColorInt()
        }
        return if (value >= 0.5f) {
            "#806AAA5A".toColorInt()
        } else {
            "#80A66767".toColorInt()
        }
    }

   /* fun getEnergyColor(value: Float): Int {
        val clamped = value.coerceIn(0f, 1f)

        return if (clamped <= 0.5f) {
            // RED section
            val normalized = clamped / 0.5f // 0..1
            // Create a "triangle" alpha curve: 0.1 → 1 → 0.1
            val alpha = (1f - abs(normalized - 0.5f) * 2) * 0.9f + 0.1f
            Color.argb((alpha * 255).toInt(), 166, 103, 103)

        } else {
            // GREEN section
            val normalized = (clamped - 0.5f) / 0.5f // 0..1
            val alpha = 0.1f + normalized * 0.9f // 0.1 → 1.0
            Color.argb((alpha * 255).toInt(), 106, 170, 90)
        }
    }*/
   fun getEnergyColor(value: Float): Int {
       val clamped = value.coerceIn(0f, 1f)

       return if (clamped <= 0.5f) {
           // RED section
           val normalized = clamped / 0.5f // 0..1
           // Triangle: 0.3 → 0.5 → 0.3
           val alpha = (1f - abs(normalized - 0.5f) * 2) * 0.2f + 0.3f
           Color.argb((alpha * 255).toInt(), 166, 103, 103)

       } else {
           // GREEN section
           val normalized = (clamped - 0.5f) / 0.5f // 0..1
           // Keep flat at 0.3 until 0.6f, then increase linearly to 0.5
           val alpha = if (clamped <= 0.6f) {
               0.3f
           } else {
               0.3f + ((normalized - 0.2f) / 0.8f) * 0.2f
           }
           Color.argb((alpha * 255).toInt(), 106, 170, 90)
       }
   }

    /**
     * 24 * 60 - 1440 values
     */
    private fun drawCircularEnergyCurveWithFade(canvas: Canvas, values: List<Float>) {
        val cx = width / 2f
        val cy = height / 2f
        val radiusCircle = min(cx, cy) - 76f.dpToPixel()
        canvas.drawCircle(cx, cy, radiusCircle, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#0AFFFFFF".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f.dpToPixel()
        })

        val colors = ArrayList<Int>()
        val transparentColor = "#00000000".toColorInt()

        /*values.forEachIndexed { index, value ->
            val startColor =
                if (index == 0) {
                    transparentColor
                } else {
                    getColorByValue(value)
                }
            val endColor = try {
                getColorByValue(values[index + 1])
            } catch (exp: Exception) {
                transparentColor
            }

            val evaluator = ArgbEvaluator()
            val barColors = mutableListOf<Int>()

            val midBarCount = 30
            for (i in 0 until midBarCount) {
                val fraction = i.toFloat() / (midBarCount - 1)
                val color = evaluator.evaluate(fraction, startColor, endColor) as Int
                barColors.add(color)
            }
            colors.addAll(barColors)
        }*/

        values.forEachIndexed { index, value ->
            val startColor =
                if (index == 0) {
                    transparentColor
                } else {
                    getEnergyColor(value)
                    //getColorByValue(value)
                }

            /*val endColor = try {
                getColorByValue(values[index + 1])
            } catch (exp: Exception) {
                transparentColor
            }*/

            val evaluator = ArgbEvaluator()
            /*val barColors = mutableListOf<Int>()

            val midBarCount = 30
            for (i in 0 until midBarCount) {
                val fraction = i.toFloat() / (midBarCount - 1)
                val color = evaluator.evaluate(fraction, startColor, endColor) as Int
                barColors.add(color)
            }*/
            colors.add(startColor)
        }


        val radius = min(cx, cy) - 74f.dpToPixel()

        val tickStart = radius
        val tickEnd = radius - 40f.dpToPixel()

        val paint = Paint()
        paint.style = Paint.Style.FILL


        val circumference = 2 * Math.PI * radius
        paint.strokeWidth = (circumference / colors.size).toFloat()

        val multiplier = 360f / colors.size

        for (i in 0 until colors.size) {

            val angleDegree = (i * multiplier.toDouble()) - 90f
            val angle = Math.toRadians(angleDegree)
            val x1 = (cx + tickStart * Math.cos(angle)).toFloat()
            val y1 = (cy + tickStart * Math.sin(angle)).toFloat()
            val x2 = (cx + tickEnd * Math.cos(angle)).toFloat()
            val y2 = (cy + tickEnd * Math.sin(angle)).toFloat()


            val shader = LinearGradient(
                x1, y1, x2, y2,
                intArrayOf(colors[i], colors[i], transparentColor, transparentColor),
                floatArrayOf(0f, 0f, 0.50f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = shader

            canvas.drawLine(x1, y1, x2, y2, paint)
            paint.shader = null
        }


        val imageRadius = radius - 14f.dpToPixel()
        sleepStart?.let {
            val hour = it.hour + it.minute / 60f
            val startAngle = hourToAngle(hour).toDouble()
            val angleRad = Math.toRadians(startAngle.toDouble())
            val x1 = (cx + imageRadius * Math.cos(angleRad)).toFloat()
            val y1 = (cy + imageRadius * Math.sin(angleRad)).toFloat()
            canvas.drawBitmap(
                sleepBedBitmap,
                x1 - sleepBedBitmap.width / 2,
                y1 - sleepBedBitmap.width / 2,
                null
            )
        }
        sleepEnd?.let {
            val hour = it.hour + it.minute / 60f
            val startAngle = hourToAngle(hour).toDouble()
            val angleRad = Math.toRadians(startAngle.toDouble())
            val x1 = (cx + imageRadius * Math.cos(angleRad)).toFloat()
            val y1 = (cy + imageRadius * Math.sin(angleRad)).toFloat()
            canvas.drawBitmap(
                sleepWakeBitmap,
                x1 - sleepWakeBitmap.width / 2,
                y1 - sleepWakeBitmap.width / 2,
                null
            )
        }

        val startAngle =
            hourToAngle(13f)

        drawCircularTextCCW(
            canvas,
            radiusCircle - 20f.dpToPixel(),
            PointF(cx, cy),
            startAngle,
            "Energy",
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL_AND_STROKE
                typeface = fontGilroy
                textSize = dpToPx(10f)
                color = "#B2B2B2".toColorInt()
            }
        )

    }

    private fun drawCurrentTimeMarker(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 90f.dpToPixel()

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentHourFloat = hour + (minute / 60f)

        val angle = ((currentHourFloat / 24f) * 360f) - 90f

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


        for (event in events) {

            when (event.eventType) {
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
                        event.color, event.endColor,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = linearGradient


                    val startAngle =
                        hourToAngle(event.startHour) + 1//(event.startHour / 24f) * 360f - 90f

                    val sweepAngle = if (event.endHour < event.startHour) {//day change case
                        ((((24f - event.startHour) / 24f) * 360f) + (((event.endHour) / 24f) * 360f)) - 1
                    } else {
                        (((event.endHour - event.startHour) / 24f) * 360f) - 1
                    }


                    //val sweepAngle = (((event.endHour - event.startHour) / 24f) * 360f) - 1
                    canvas.drawArc(rect, startAngle, sweepAngle, false, paint)


                    drawCircularText(
                        canvas,
                        radius - 3f.dpToPixel(),
                        PointF(cx, cy),
                        startAngle,
                        event.label,
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL_AND_STROKE
                            typeface = fontGilroy
                            textSize = dpToPx(10f)
                            color = event.textColor
                        }
                    )


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

        /* val startAngle =
             hourToAngle(13f)

         drawCircularTextCCW(
             canvas,
             radius - 20f.dpToPixel(),
             PointF(cx, cy),
             startAngle,
             "Energy",
             Paint(Paint.ANTI_ALIAS_FLAG).apply {
                 style = Paint.Style.FILL_AND_STROKE
                 typeface = fontGilroy
                 textSize = dpToPx(10f)
                 color = "#B2B2B2".toColorInt()
             }
         )*/
    }


    private fun drawCircularText(
        canvas: Canvas,
        radius: Float,
        middlePoint: PointF,
        textAngle: Float,
        text: String,
        textPaint: Paint
    ) {
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val circlePath = Path()
        circlePath.addCircle(middlePoint.x, middlePoint.y, radius, Path.Direction.CW)

        canvas.save()
        canvas.rotate((textAngle + 2), middlePoint.x, middlePoint.y)
        canvas.drawTextOnPath(text, circlePath, 0f, 0f, textPaint)
        canvas.restore()
    }

    fun fromFloatHour(value: Float): LocalTime {
        val hour = floor(value).toInt()
        val minute = ((value - hour) * 60).toInt()
        return LocalTime.of(hour, minute)
    }

    private fun drawCircularTextCCW(
        canvas: Canvas,
        radius: Float,
        middlePoint: PointF,
        textAngle: Float,
        text: String,
        textPaint: Paint
    ) {
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val circlePath = Path()
        circlePath.addCircle(middlePoint.x, middlePoint.y, radius, Path.Direction.CCW)

        canvas.save()
        canvas.rotate((textAngle + 2), middlePoint.x, middlePoint.y)
        canvas.drawTextOnPath(text, circlePath, 0f, 0f, textPaint)
        canvas.restore()
    }


    private fun hourToAngle(hour: Float): Float {
        // 0 hour = -90deg (top), increases clockwise
        return (hour / 24f) * 360f - 90f
    }

}

data class ClockEvent(
    val startHour: Float,
    val endHour: Float,
    val eventType: ClockEventType,
    val color: Int,
    val endColor: Int,
    val textColor: Int,
    val label: String
)

enum class ClockEventType {
    LINE, ARCH, GRAPH
}

