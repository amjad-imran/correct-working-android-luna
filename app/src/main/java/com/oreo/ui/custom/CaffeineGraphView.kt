package com.oreo.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.noisefit.luna.R
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CaffeineWindowData
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class CaffeineGraphView : View {


    private var highlightState: HighlightState = HighlightState.START
    private var graphStart: LocalTime? = null
    private var graphEnd: LocalTime? = null

    private var caffeineStart: LocalTime? = null
    private var caffeineEnd: LocalTime? = null

    private var caffeineDataList = ArrayList<Int>()

    lateinit var sunRiseBitmap: Bitmap
    lateinit var moonRiseBitmap: Bitmap

    lateinit var textPaint: Paint


    private val barPaintDefault = Paint().apply {
        color = Color.parseColor("#26FFFFFF")
        style = Paint.Style.FILL
    }

    private val barPaintHighlighted = Paint().apply {
        color = Color.parseColor("#99FFFFFF")
        style = Paint.Style.FILL
    }

    private val redPaintDisabled = Paint().apply {
        color = Color.parseColor("#66FF6389")
        style = Paint.Style.FILL
    }
    private val redPaintEnabled = Paint().apply {
        color = Color.parseColor("#FF6389")
        style = Paint.Style.FILL
    }
    private val greenPaintDisabled = Paint().apply {
        color = Color.parseColor("#549CFFC9")
        style = Paint.Style.FILL
    }
    private val greenPaintEnabled = Paint().apply {
        color = Color.parseColor("#2DE983")
        style = Paint.Style.FILL
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val bitmap =
            BitmapFactory.decodeResource(resources, R.drawable.ic_sun_rise)
        sunRiseBitmap = Bitmap.createScaledBitmap(
            bitmap, dip2px(12f),
            dip2px(12f), true
        )

        val bitmapMoon =
            BitmapFactory.decodeResource(resources, R.drawable.ic_moon_rise)
        moonRiseBitmap = Bitmap.createScaledBitmap(
            bitmapMoon, dip2px(12f),
            dip2px(12f), true
        )

        val fontGilroy =
            ResourcesCompat.getFont(this.context, com.noisefit_commans.R.font.gilroy_medium)

        textPaint = Paint().apply {
            this.color = Color.parseColor("#CDA390")
            this.typeface = fontGilroy
            this.textSize = dip2px(10f).toFloat()
        }
    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        if (graphStart == null || graphEnd == null || caffeineStart == null || caffeineEnd == null) {
            return
        }

        drawContent(canvas)
    }

    fun updateData(data: CaffeineWindowData) {

        LOGS.d("caffeine_graph $data")
        graphStart = LocalTime.parse(data.wakeUpTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        graphEnd = LocalTime.parse(data.bedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        caffeineStart =
            LocalTime.parse(data.caffeineStartTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        caffeineEnd = LocalTime.parse(data.caffeineEndTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        caffeineDataList.clear()
        caffeineDataList.addAll(data.caffeineValues)

        invalidate()

    }


    fun drawContent(canvas: Canvas) {

        val barCenterYPos = height - 24f.dpToPixel()

        val (startBarWidth, caffeineBarWidth, endBarWidth) = calculateBarWidths(
            graphStart!!,
            graphEnd!!,
            caffeineStart!!,
            caffeineEnd!!,
            width.toFloat()
        )

        val barHeightDisabled = 4f.dpToPixel()
        val barHeightEnabled = 6f.dpToPixel()
        val radius = 2f.dpToPixel()

        val segment1Start = 0f
        val segment1End = startBarWidth
        val isStartHighlighted = highlightState == HighlightState.START

        if (isStartHighlighted) {
            drawNoCaffeineZoneLabel(
                canvas,
                segment1Start + startBarWidth / 2,
                barCenterYPos - 8f.dpToPixel()
            )
        }


        canvas.drawRoundRect(
            segment1Start,
            barCenterYPos - (if (isStartHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            segment1End,
            barCenterYPos + (if (isStartHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            radius,
            radius,
            if (isStartHighlighted) redPaintEnabled else redPaintDisabled
        )

        val segment2Start = segment1End
        val segment2End = segment1End + caffeineBarWidth
        val isCaffeineHighlighted = highlightState == HighlightState.CAFFEINE
        canvas.drawRoundRect(
            segment2Start,
            barCenterYPos - (if (isCaffeineHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            segment2End,
            barCenterYPos + (if (isCaffeineHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            radius,
            radius,
            if (isCaffeineHighlighted) greenPaintEnabled else greenPaintDisabled
        )

        val caffeineTextStart =
            caffeineStart!!.format(DateTimeFormatter.ofPattern("hh:mma", Locale.US)).lowercase()
        val textHeight = textPaint.descent() - textPaint.ascent()
        canvas.drawText(
            caffeineTextStart,
            segment2Start,
            barCenterYPos + textHeight + 8f.dpToPixel(),
            textPaint
        )


        val segment3Start = segment2End
        val segment3End = segment2End + endBarWidth
        val isEndHighlighted = highlightState == HighlightState.END


        if (isEndHighlighted) {
            drawNoCaffeineZoneLabel(
                canvas,
                segment3Start + endBarWidth / 2,
                barCenterYPos - 8f.dpToPixel()
            )
        }/* else {*/

        if(isStartHighlighted.not()){
            val caffeineTextEnd =
                caffeineEnd!!.format(DateTimeFormatter.ofPattern("hh:mma", Locale.US)).lowercase()
            val textWidth = textPaint.measureText(caffeineTextEnd, 0, caffeineTextEnd.length)
            canvas.drawText(
                caffeineTextEnd, segment3Start - textWidth / 2,
                barCenterYPos + textHeight + 8f.dpToPixel(), textPaint
            )
        }

        /*}*/
        canvas.drawRoundRect(
            segment3Start,
            barCenterYPos - (if (isEndHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            segment3End,
            barCenterYPos + (if (isEndHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            radius,
            radius,
            if (highlightState == HighlightState.END) redPaintEnabled else redPaintDisabled
        )


        val endText =
            graphEnd!!.format(DateTimeFormatter.ofPattern("hh:mma", Locale.US)).lowercase()
        val textWidthEnd = textPaint.measureText(endText, 0, endText.length)
        canvas.drawText(
            endText, segment3End - textWidthEnd,
            barCenterYPos + textHeight + 8f.dpToPixel(), textPaint
        )

        canvas.drawBitmap(
            moonRiseBitmap,
            segment3End - textWidthEnd - 14f.dpToPixel(),
            barCenterYPos + 8f.dpToPixel(),
            barPaintDefault
        )

        drawBars(
            startBarWidth,
            startBarWidth + caffeineBarWidth,
            barCenterYPos - 8f.dpToPixel(),
            canvas
        )

        canvas.drawBitmap(sunRiseBitmap, 0f, barCenterYPos + 8f.dpToPixel(), barPaintDefault)
    }

    private fun drawXAxis(canvas: Canvas, yAxis: Float) {

        val width = (sunRiseBitmap.getWidth() / 2).toFloat()
        val height = (sunRiseBitmap.getHeight() / 2).toFloat()


    }

    fun drawNoCaffeineZoneLabel(canvas: Canvas, centerX: Float, bottom: Float) {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.label_no_caffeine_zone, null)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        canvas.save()
        val drawX = centerX - view.measuredWidth / 2f
        val drawY = bottom - view.measuredHeight
        canvas.translate(drawX, drawY)

        view.draw(canvas)
        canvas.restore()
    }

    fun drawTooltip(canvas: Canvas, centerX: Float, bottom: Float, text: String) {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.tooltip_caffene_amount, null)

        view.findViewById<TextView>(R.id.tvAmount).text = text

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        canvas.save()
        val drawX = centerX// - view.measuredWidth / 2f
        val drawY = bottom - view.measuredHeight
        canvas.translate(drawX, drawY)

        view.draw(canvas)
        canvas.restore()
    }


    fun drawBars(startX: Float, endX: Float, barBottomY: Float, canvas: Canvas) {
        val maxVal = caffeineDataList.maxOrNull() ?: return
        val availableWidth = endX - startX
        val barCount = caffeineDataList.size

        val spacing = 2f.dpToPixel()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (availableWidth - totalSpacing) / barCount
        val barTopY = 26f.dpToPixel()

        val cornerRadius = 3f.dpToPixel()

        val currentTime = LocalTime.now()

        caffeineStart

        var selectedBarStart = 0
        val highlightedIndex = getTimeIndex(
            caffeineStart!!,
            caffeineEnd!!,
            currentTime,
            caffeineDataList.size
        )
        var highlightedBarTop = barTopY
        caffeineDataList.forEachIndexed { index, value ->
            val heightRatio = value / maxVal.toFloat()
            val barHeight = heightRatio * (barBottomY - barTopY)

            val left = startX + index * (barWidth + spacing)
            val top = barBottomY - barHeight
            val right = left + barWidth
            val bottom = barBottomY

            if (highlightedIndex == index) {
                highlightedBarTop = top
            }

            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(
                rect,
                cornerRadius,
                cornerRadius,
                if (highlightedIndex == index) barPaintHighlighted else barPaintDefault
            )
        }


        if (highlightedIndex != -1) {
            val left = startX + highlightedIndex * (barWidth + spacing)
            drawTooltip(
                canvas,
                left,
                highlightedBarTop,
                context.getString(R.string.text_upto_value_mg, caffeineDataList[highlightedIndex])
            )
        }
    }

    fun getTimeIndex(
        startTime: LocalTime,
        endTime: LocalTime,
        currentTime: LocalTime,
        parts: Int
    ): Int {
        if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) return -1

        val totalDuration = Duration.between(startTime, endTime).toMinutes()
        val interval = totalDuration / parts

        val minutesSinceStart = Duration.between(startTime, currentTime).toMinutes()

        return (minutesSinceStart / interval).toInt().coerceAtMost(parts - 1)
    }


    fun calculateBarWidths(
        graphStart: LocalTime,
        graphEnd: LocalTime,
        caffeineStart: LocalTime,
        caffeineEnd: LocalTime,
        totalWidth: Float
    ): Triple<Float, Float, Float> {

        val startDuration = Duration.between(graphStart, caffeineStart).toMinutes().toFloat()
        val caffeineDuration = Duration.between(caffeineStart, caffeineEnd).toMinutes().toFloat()


        val endDuration = if (caffeineEnd <= graphEnd) {//same day case
            Duration.between(caffeineEnd, graphEnd).toMinutes().toFloat()
        } else {//Next day case
            (Duration.between(caffeineEnd, LocalTime.of(23, 59)).toMinutes()
                .toFloat() + Duration.between(LocalTime.of(0, 0), graphEnd).toMinutes().toFloat())
        }

        val totalDuration = startDuration + caffeineDuration + endDuration

        var startWeight = startDuration / totalDuration
        var caffeineWeight = caffeineDuration / totalDuration
        var endWeight = endDuration / totalDuration


        val todayDate = LocalDate.now()
        val nowDateTime = LocalDateTime.now()

        val graphStartDate = LocalDateTime.of(todayDate, graphStart)
        val caffeineStartDate = LocalDateTime.of(todayDate, caffeineStart)
        val caffeineEndDate = LocalDateTime.of(todayDate, caffeineEnd)
        val graphEndDate = if (caffeineEnd <= graphEnd) {//same day case
            LocalDateTime.of(todayDate, graphEnd)
        } else {//Next day case
            LocalDateTime.of(todayDate.plusDays(1L), graphEnd)
        }

        val startOffset = graphStartDate.minusHours(3)

        when {
            nowDateTime in startOffset..caffeineStartDate -> {
                startWeight = 0.5f
                val remaining = 1f - startWeight
                val sum = caffeineDuration + endDuration
                caffeineWeight = (caffeineDuration / sum) * remaining
                endWeight = (endDuration / sum) * remaining
                highlightState = HighlightState.START
            }

            nowDateTime in caffeineStartDate..caffeineEndDate -> {
                caffeineWeight = 0.5f
                val remaining = 1f - caffeineWeight
                val sum = startDuration + endDuration
                startWeight = (startDuration / sum) * remaining
                endWeight = (endDuration / sum) * remaining
                highlightState = HighlightState.CAFFEINE
            }

            (nowDateTime in caffeineEndDate..graphEndDate) -> {
                endWeight = 0.5f
                val remaining = 1f - endWeight
                val sum = startDuration + caffeineDuration
                startWeight = (startDuration / sum) * remaining
                caffeineWeight = (caffeineDuration / sum) * remaining
                highlightState = HighlightState.END
            }

            else -> {
                endWeight = 0.5f
                val remaining = 1f - endWeight
                val sum = startDuration + caffeineDuration
                startWeight = (startDuration / sum) * remaining
                caffeineWeight = (caffeineDuration / sum) * remaining
                highlightState = HighlightState.END
            }
        }

        val startBarWidth = startWeight * totalWidth
        val caffeineBarWidth = caffeineWeight * totalWidth
        val endBarWidth = endWeight * totalWidth

        return Triple(startBarWidth, caffeineBarWidth, endBarWidth)
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = getContext().getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }


}

enum class HighlightState {
    START,
    CAFFEINE,
    END
}

data class CaffeineData(
    val value: Int,
    val duration: Int,//minutes
    var startTime: Long
)