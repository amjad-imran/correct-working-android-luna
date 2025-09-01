package com.oreo.ui.custom

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit.timepickerslider.utils.dpToPx
import com.noisefit_commans.ui.dpToPixel
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.*

class CircularScheduleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val fontGilroy =
        ResourcesCompat.getFont(context, com.noisefit_commans.R.font.gilroy_medium)

    private val textPaintTick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99FFFFFF")
        typeface = fontGilroy
        textSize = 16f.dpToPixel()
        textAlign = Paint.Align.CENTER
    }
    private val textPaintWindowMessage = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#99FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
        typeface = fontGilroy
        textSize = 10f.dpToPixel()
    }
    private val textPaintWindowTimer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
        textSize = 24f.dpToPixel()
        typeface = fontGilroy
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f.dpToPixel()
        color = Color.parseColor("#1AFFFFFF")
        style = Paint.Style.STROKE
    }
    private val tickPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f.dpToPixel()
        color = Color.parseColor("#4DFFFFFF")
        style = Paint.Style.STROKE
    }

    var events = ArrayList<ClockEvent>()
        private set
    var energyArray = ArrayList<Float>()
        private set
    var isLocked = false
        private set
    private var sleepStart: LocalTime? = null
    private var sleepEnd: LocalTime? = null

    private var pointerBitmapRaw: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_scheduler_time_pointer)
    private var pointerBitmap: Bitmap = pointerBitmapRaw

    private var sleepWakeBitmapRaw: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_circadian_wake_up)
    private var sleepWakeBitmap: Bitmap = sleepWakeBitmapRaw

    private var sleepBedBitmapRaw: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_circadian_bed_time)
    private var sleepBedBitmap: Bitmap = sleepBedBitmapRaw

    private var staticBitmap: Bitmap? = null
    private var staticCanvas: Canvas? = null
    private var staticDirty = true

    private var cx = 0f
    private var cy = 0f
    private var radiusOuter = 0f
    private var energyRingOuter = 0f
    private var energyTickStart = 0f
    private var energyTickEnd = 0f

    private var precomputedEnergyColors: IntArray = IntArray(0)

    private var eventPairs: List<Pair<LocalTime, LocalTime>> = emptyList()

    private val tickRunnable = object : Runnable {
        override fun run() {
            invalidate()
            val now = System.currentTimeMillis()
            val delay = 1000L - (now % 1000L)
            postInvalidateOnAnimation()
            postDelayed(this, delay)
        }
    }

    init {
        post(tickRunnable)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tickRunnable)
        super.onDetachedFromWindow()
    }

    fun setDataSet(
        events: List<ClockEvent>,
        energyArray: List<Float>,
        sleepStart: String? = null,
        sleepEnd: String? = null,
        isLocked: Boolean
    ) {
        this.events.clear()
        this.events.addAll(events)

        this.energyArray.clear()
        this.energyArray.addAll(energyArray)

        this.isLocked = isLocked

        this.sleepStart = sleepStart?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalTime()
        }
        this.sleepEnd = sleepEnd?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalTime()
        }

        eventPairs = buildEventPairs(this.events)
        precomputedEnergyColors = buildEnergyColors(this.energyArray)

        staticDirty = true
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
        radiusOuter = min(cx, cy) - 12f.dpToPixel()
        energyRingOuter = min(cx, cy) - 76f.dpToPixel()
        energyTickStart = (min(cx, cy) - 74f.dpToPixel())
        energyTickEnd = energyTickStart - 40f.dpToPixel()

        pointerBitmap = pointerBitmapRaw
        sleepWakeBitmap = Bitmap.createScaledBitmap(
            sleepWakeBitmapRaw, 15f.dpToPixel().toInt(), 15f.dpToPixel().toInt(), true
        )
        sleepBedBitmap = Bitmap.createScaledBitmap(
            sleepBedBitmapRaw, 12f.dpToPixel().toInt(), 12f.dpToPixel().toInt(), true
        )

        staticBitmap?.recycle()
        staticBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        staticCanvas = Canvas(staticBitmap!!)
        staticDirty = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (staticDirty) {
            rebuildStaticLayer()
            staticDirty = false
        }

        staticBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        if (!isLocked) {
            showTimer(canvas)
        }
        drawCurrentTimeMarker(canvas)
    }

    private fun rebuildStaticLayer() {
        val sc = staticCanvas ?: return
        sc.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        drawClock(sc)

        if (!isLocked) {
            // drawCircularEnergyCurveWithFade(sc, precomputedEnergyColors) // replaced with a single circle border based on energy
            drawEnergyRingCircle(sc)
            drawEvents(sc)
        }
    }

    private fun showTimer(canvas: Canvas) {
        val timerSec = getCurrentWindowTimerSeconds()
        if (timerSec <= 0L) return

        canvas.drawText(
            "Window closes In",
            width / 2f,
            height / 2f - 12f.dpToPixel(),
            textPaintWindowMessage
        )

        val hours = timerSec / 3600
        val minutes = (timerSec % 3600) / 60
        val seconds = timerSec % 60
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        canvas.drawText(
            timeString,
            width / 2f,
            height / 2f + 14f.dpToPixel(),
            textPaintWindowTimer
        )
    }

    private fun getCurrentWindowTimerSeconds(): Long {
        if (eventPairs.isEmpty()) return 0L
        val currentPair = findCurrentPair(eventPairs) ?: return 0L
        val endTime = currentPair.second
        val now = LocalTime.now()
        val diff = Duration.between(now, endTime).seconds
        return if (diff > 0) diff else 0L
    }

    private fun findCurrentPair(timePairs: List<Pair<LocalTime, LocalTime>>): Pair<LocalTime, LocalTime>? {
        val now = LocalTime.now()
        for ((start, end) in timePairs) {
            if (start <= end) {
                if (now >= start && now <= end) return start to end
            } else {
                if (now >= start || now <= end) return start to end
            }
        }
        return null
    }

    private fun drawCurrentTimeMarker(canvas: Canvas) {
        val radius = min(cx, cy) - 90f.dpToPixel()

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val currentHourFloat = hour + (minute / 60f)

        val angle = ((currentHourFloat / 24f) * 360f) - 90f
        val pointerRadius = radius + (pointerBitmap.height / 2f)
        val rad = Math.toRadians(angle.toDouble())
        val px = (cx + pointerRadius * cos(rad)).toFloat()
        val py = (cy + pointerRadius * sin(rad)).toFloat()

        canvas.save()
        canvas.translate(px, py)
        val imageOffset = 60f
        canvas.rotate(angle - imageOffset)
        canvas.drawBitmap(
            pointerBitmap,
            -pointerBitmap.width / 2f,
            -pointerBitmap.height / 2f,
            null
        )
        canvas.restore()
    }

    private fun drawClock(canvas: Canvas) {
        val tickStart = radiusOuter - min(cx, cy) * 0.07f
        val tickEnd = radiusOuter
        val textRadius = tickStart + (tickEnd - tickStart) / 2f

        for (i in 0 until 24) {
            val angle = Math.toRadians((i * 15.0) - 90.0)
            val x1 = (cx + tickStart * cos(angle)).toFloat()
            val y1 = (cy + tickStart * sin(angle)).toFloat()
            val x2 = (cx + tickEnd * cos(angle)).toFloat()
            val y2 = (cy + tickEnd * sin(angle)).toFloat()

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
                val textX = (cx + textRadius * cos(angle)).toFloat()
                val textY = (cy + textRadius * sin(angle) + textPaintTick.textSize / 3f)
                canvas.drawText(hour, textX, textY.toFloat(), textPaintTick)
            }
        }
    }

    private fun drawCircularEnergyCurveWithFade(canvas: Canvas, colors: IntArray) {
        canvas.drawCircle(cx, cy, energyRingOuter, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#0AFFFFFF".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f.dpToPixel()
        })

        if (colors.isEmpty()) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val circumference = (2 * Math.PI * energyTickStart).toFloat()
        paint.strokeWidth = circumference / colors.size
        val multiplier = 360f / colors.size
        val transparent = "#00000000".toColorInt()

        for (i in colors.indices) {
            val angleDegree = (i * multiplier) - 90f
            val angle = Math.toRadians(angleDegree.toDouble())
            val x1 = (cx + energyTickStart * cos(angle)).toFloat()
            val y1 = (cy + energyTickStart * sin(angle)).toFloat()
            val x2 = (cx + energyTickEnd * cos(angle)).toFloat()
            val y2 = (cy + energyTickEnd * sin(angle)).toFloat()

            val shader = LinearGradient(
                x1, y1, x2, y2,
                intArrayOf(colors[i], colors[i], transparent, transparent),
                floatArrayOf(0f, 0f, 0.50f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        val imageRadius = energyTickStart - 14f.dpToPixel()
        sleepStart?.let {
            val hour = it.hour + it.minute / 60f
            val a = Math.toRadians(hourToAngle(hour).toDouble())
            val x = (cx + imageRadius * cos(a)).toFloat()
            val y = (cy + imageRadius * sin(a)).toFloat()
            canvas.drawBitmap(sleepBedBitmap, x - sleepBedBitmap.width / 2f, y - sleepBedBitmap.height / 2f, null)
        }
        sleepEnd?.let {
            val hour = it.hour + it.minute / 60f
            val a = Math.toRadians(hourToAngle(hour).toDouble())
            val x = (cx + imageRadius * cos(a)).toFloat()
            val y = (cy + imageRadius * sin(a)).toFloat()
            canvas.drawBitmap(sleepWakeBitmap, x - sleepWakeBitmap.width / 2f, y - sleepWakeBitmap.height / 2f, null)
        }

        val startAngle = hourToAngle(13f)
        drawCircularTextCCW(
            canvas,
            energyRingOuter - 20f.dpToPixel(),
            PointF(cx, cy),
            startAngle,
            context.getString(R.string.text_energy),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL_AND_STROKE
                typeface = fontGilroy
                textSize = dpToPx(10f)
                color = "#B2B2B2".toColorInt()
            }
        )
    }

    private fun drawEnergyRingCircle(canvas: Canvas) {
        val circleRadius = energyRingOuter
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f.dpToPixel()
        }

        if (precomputedEnergyColors.isNotEmpty()) {
            val colors = precomputedEnergyColors
            val sweep = SweepGradient(cx, cy, colors, null)
            val matrix = Matrix()
            matrix.preRotate(-90f, cx, cy)
            sweep.setLocalMatrix(matrix)
            ringPaint.shader = sweep
        } else {
            ringPaint.color = "#19FFFFFF".toColorInt()
        }

        canvas.drawCircle(cx, cy, circleRadius, ringPaint)

        val imageRadius = energyTickStart - 14f.dpToPixel()
        sleepStart?.let {
            val hour = it.hour + it.minute / 60f
            val a = Math.toRadians(hourToAngle(hour).toDouble())
            val x = (cx + imageRadius * cos(a)).toFloat()
            val y = (cy + imageRadius * sin(a)).toFloat()
            canvas.drawBitmap(sleepBedBitmap, x - sleepBedBitmap.width / 2f, y - sleepBedBitmap.height / 2f, null)
        }
        sleepEnd?.let {
            val hour = it.hour + it.minute / 60f
            val a = Math.toRadians(hourToAngle(hour).toDouble())
            val x = (cx + imageRadius * cos(a)).toFloat()
            val y = (cy + imageRadius * sin(a)).toFloat()
            canvas.drawBitmap(sleepWakeBitmap, x - sleepWakeBitmap.width / 2f, y - sleepWakeBitmap.height / 2f, null)
        }

        val startAngle = hourToAngle(13f)
        drawCircularTextCCW(
            canvas,
            energyRingOuter - 20f.dpToPixel(),
            PointF(cx, cy),
            startAngle,
            context.getString(R.string.text_energy),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL_AND_STROKE
                typeface = fontGilroy
                textSize = dpToPx(10f)
                color = "#B2B2B2".toColorInt()
            }
        )
    }

    private fun drawEvents(canvas: Canvas) {
        val baseCenterRadiusLine = min(cx, cy) - 67f.dpToPixel()
        val baseCenterRadiusArc = min(cx, cy) - 50f.dpToPixel()
        val arcStrokeWidth = 24f.dpToPixel()

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f.dpToPixel()
            strokeCap = Paint.Cap.ROUND
        }
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = arcStrokeWidth
            strokeCap = Paint.Cap.BUTT
        }

        for (event in events) {
            when (event.eventType) {
                ClockEventType.LINE -> {
                    val rect = RectF(cx - baseCenterRadiusLine, cy - baseCenterRadiusLine, cx + baseCenterRadiusLine, cy + baseCenterRadiusLine)
                    linePaint.color = event.color
                    val startAngle = hourToAngle(event.startHour)
                    val endAngle = hourToAngle(event.endHour)
                    canvas.drawArc(rect, startAngle, endAngle - startAngle, false, linePaint)
                }
                ClockEventType.ARCH -> {
                    val rect = RectF(cx - baseCenterRadiusArc, cy - baseCenterRadiusArc, cx + baseCenterRadiusArc, cy + baseCenterRadiusArc)
                    val gradient = LinearGradient(
                        cx, cy - baseCenterRadiusArc, cx, cy + baseCenterRadiusArc,
                        event.color, event.endColor, Shader.TileMode.CLAMP
                    )
                    arcPaint.shader = gradient

                    val startAngle = hourToAngle(event.startHour) + 1f
                    val sweepAngle = if (event.endHour < event.startHour) {
                        ((((24f - event.startHour) / 24f) * 360f) + (((event.endHour) / 24f) * 360f)) - 1f
                    } else {
                        (((event.endHour - event.startHour) / 24f) * 360f) - 1f
                    }
                    canvas.drawArc(rect, startAngle, sweepAngle, false, arcPaint)

                    val diff = event.endHour - event.startHour
                    if (diff < 3 && diff > 0 && event.image != null) {
                        val icon = BitmapFactory.decodeResource(resources, event.image)
                        val archBitmap = Bitmap.createScaledBitmap(icon, 15f.dpToPixel().toInt(), 15f.dpToPixel().toInt(), true)
                        val hour = event.startHour + (event.endHour - event.startHour) / 2f
                        val a = Math.toRadians(hourToAngle(hour).toDouble())
                        val x = (cx + baseCenterRadiusArc * cos(a)).toFloat()
                        val y = (cy + baseCenterRadiusArc * sin(a)).toFloat()
                        canvas.drawBitmap(archBitmap, x - archBitmap.width / 2f, y - archBitmap.height / 2f, null)
                    } else {
                        drawCircularText(
                            canvas,
                            baseCenterRadiusArc - 3f.dpToPixel(),
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
                    arcPaint.shader = null
                }
                ClockEventType.GRAPH -> Unit
            }
        }
    }

    private fun buildEventPairs(list: List<ClockEvent>): List<Pair<LocalTime, LocalTime>> {
        val out = ArrayList<Pair<LocalTime, LocalTime>>(list.size)
        list.forEach {
            if (it.eventType == ClockEventType.ARCH || it.eventType == ClockEventType.LINE) {
                out.add(fromFloatHour(it.startHour) to fromFloatHour(it.endHour))
            }
        }
        return out
    }

    private fun buildEnergyColors(values: List<Float>): IntArray {
        if (values.isEmpty()) return IntArray(0)
        val colors = ArrayList<Int>()
        val transparent = "#19FFFFFF".toColorInt()//"#00000000".toColorInt()
        val step = 30
        val evaluator = ArgbEvaluator()
        for (i in 0 until values.size - 1 step step) {
            val startColor = if (i == 0) transparent else getColorByValue(values[i])
            val endColor = values.getOrNull(i + step)?.let { getColorByValue(it) } ?: transparent
            val midBarCount = 20
            for (j in 0 until midBarCount) {
                val f = j.toFloat() / (midBarCount - 1)
                val c = evaluator.evaluate(f, startColor, endColor) as Int
                colors.add(c)
            }
        }
        return colors.toIntArray()
    }

    private fun getColorByValue(value: Float): Int {
        if (value == 0.0f) return "#19FFFFFF".toColorInt()//"#00000000".toColorInt()
        return if (value >= 0.5f) "#806AAA5A".toColorInt() else "#80A66767".toColorInt()
    }

    private fun drawCircularText(
        canvas: Canvas, radius: Float, middlePoint: PointF, textAngle: Float, text: String, textPaint: Paint
    ) {
        val circlePath = Path().apply {
            addCircle(middlePoint.x, middlePoint.y, radius, Path.Direction.CW)
        }
        canvas.save()
        canvas.rotate((textAngle + 2f), middlePoint.x, middlePoint.y)
        canvas.drawTextOnPath(text, circlePath, 0f, 0f, textPaint)
        canvas.restore()
    }

    private fun drawCircularTextCCW(
        canvas: Canvas, radius: Float, middlePoint: PointF, textAngle: Float, text: String, textPaint: Paint
    ) {
        val circlePath = Path().apply {
            addCircle(middlePoint.x, middlePoint.y, radius, Path.Direction.CCW)
        }
        canvas.save()
        canvas.rotate((textAngle + 2f), middlePoint.x, middlePoint.y)
        canvas.drawTextOnPath(text, circlePath, 0f, 0f, textPaint)
        canvas.restore()
    }

    fun fromFloatHour(value: Float): LocalTime {
        val hour = floor(value).toInt()
        val minute = ((value - hour) * 60).toInt()
        return LocalTime.of(hour, minute)
    }

    private fun hourToAngle(hour: Float): Float = (hour / 24f) * 360f - 90f
}

data class ClockEvent(
    val startHour: Float,
    val endHour: Float,
    val eventType: ClockEventType,
    val color: Int,
    val endColor: Int,
    val textColor: Int,
    val label: String,
    val image: Int? = null
)

enum class ClockEventType { LINE, ARCH, GRAPH }
