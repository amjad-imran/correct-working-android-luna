package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.noisefit_commans.ui.dpToPixel
import java.time.Duration
import java.time.LocalTime
import com.noisefit.luna.R
import com.oreo.data.model.CaffeineWindowData
import java.time.format.DateTimeFormatter


class CaffeineGraphView : View {


    private var highlightState: HighlightState = HighlightState.START
    private var graphStart: LocalTime? = null
    private var graphEnd: LocalTime? = null

    private var caffeineStart: LocalTime? = null
    private var caffeineEnd: LocalTime? = null

    private var caffeineDataList = ArrayList<Int>()


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

    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        if (graphStart == null || graphEnd == null || caffeineStart == null || caffeineEnd == null) {
            return
        }

        drawBottomBar(canvas)
    }

    fun updateData(data: CaffeineWindowData) {

        graphStart = LocalTime.parse(data.wakeUpTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        graphEnd = LocalTime.parse(data.bedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        caffeineStart =
            LocalTime.parse(data.caffeineStartTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        caffeineEnd = LocalTime.parse(data.caffeineEndTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        caffeineDataList.clear()
        caffeineDataList.addAll(data.caffeineValues)

        invalidate()

    }


    fun drawBottomBar(canvas: Canvas) {

        val barCenterYPos = height - 12f.dpToPixel()

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

        val segment3Start = segment2End
        val segment3End = segment2End + endBarWidth
        val isEndHighlighted = highlightState == HighlightState.END


        if (isEndHighlighted) {
            drawNoCaffeineZoneLabel(
                canvas,
                segment3Start + endBarWidth / 2,
                barCenterYPos - 8f.dpToPixel()
            )
        }
        canvas.drawRoundRect(
            segment3Start,
            barCenterYPos - (if (isEndHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            segment3End,
            barCenterYPos + (if (isEndHighlighted) barHeightEnabled else barHeightDisabled) / 2,
            radius,
            radius,
            if (highlightState == HighlightState.END) redPaintEnabled else redPaintDisabled
        )


        drawBars(
            startBarWidth,
            startBarWidth + caffeineBarWidth,
            barCenterYPos - 8f.dpToPixel(),
            canvas
        )
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
        val barTopY = 24f.dpToPixel()

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

            if(highlightedIndex==index){
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
            drawTooltip(canvas, left, highlightedBarTop, "Upto 30 mg")
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

        val now = LocalTime.now() //LocalTime.of(9, 0)

        val startDuration = Duration.between(graphStart, caffeineStart).toMinutes().toFloat()
        val caffeineDuration = Duration.between(caffeineStart, caffeineEnd).toMinutes().toFloat()
        val endDuration = Duration.between(caffeineEnd, graphEnd).toMinutes().toFloat()

        val totalDuration = startDuration + caffeineDuration + endDuration

        var startWeight = startDuration / totalDuration
        var caffeineWeight = caffeineDuration / totalDuration
        var endWeight = endDuration / totalDuration

        when {
            now in graphStart..caffeineStart -> {
                startWeight = 0.5f
                val remaining = 1f - startWeight
                val sum = caffeineDuration + endDuration
                caffeineWeight = (caffeineDuration / sum) * remaining
                endWeight = (endDuration / sum) * remaining
                highlightState = HighlightState.START
            }

            now in caffeineStart..caffeineEnd -> {
                caffeineWeight = 0.5f
                val remaining = 1f - caffeineWeight
                val sum = startDuration + endDuration
                startWeight = (startDuration / sum) * remaining
                endWeight = (endDuration / sum) * remaining
                highlightState = HighlightState.CAFFEINE
            }

            now in caffeineEnd..graphEnd -> {
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