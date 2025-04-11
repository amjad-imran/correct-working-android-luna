package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.noisefit_commans.ui.dpToPixel
import java.time.Duration
import java.time.LocalTime
import com.noisefit.luna.R


class CaffeineGraphView : View {



    private var highlightState: HighlightState = HighlightState.START
    private val graphStart = LocalTime.of(7, 0)
    private val graphEnd = LocalTime.of(23, 0)

    private val caffeineStart = LocalTime.of(9, 0)
    private val caffeineEnd = LocalTime.of(18, 0)


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



        drawBottomBar(canvas)
    }



    fun drawBottomBar(canvas: Canvas) {


        val barCenterYPos = height - 12f.dpToPixel()

        /*val startBarWidth  = width/3f
        val caffeineBarWidth  = width/3f
        val endBarWidth  = width/3f*/

        val (startBarWidth, caffeineBarWidth, endBarWidth) = calculateBarWidths(
            graphStart,
            graphEnd,
            caffeineStart,
            caffeineEnd,
            width.toFloat()
        )

        val barHeightDisabled = 4f.dpToPixel()
        val barHeightEnabled = 6f.dpToPixel()
        val radius = 2f.dpToPixel()


        val segment1Start = 0f
        val segment1End = startBarWidth
        val isStartHighlighted = highlightState == HighlightState.START

        drawNoCaffeineZoneLabel(canvas, segment1Start + startBarWidth / 2, barCenterYPos-8f.dpToPixel())

        canvas.drawRoundRect(
            segment1Start,
            barCenterYPos - (if(isStartHighlighted) barHeightEnabled else  barHeightDisabled) / 2,
            segment1End,
            barCenterYPos + (if(isStartHighlighted) barHeightEnabled else  barHeightDisabled) / 2,
            radius,
            radius,
            if(isStartHighlighted) redPaintEnabled else redPaintDisabled
        )

        val segment2Start = segment1End
        val segment2End = segment1End + caffeineBarWidth
        val isCaffeineHighlighted = highlightState == HighlightState.CAFFEINE
        canvas.drawRoundRect(
            segment2Start,
            barCenterYPos - (if(isCaffeineHighlighted) barHeightEnabled else  barHeightDisabled) / 2,
            segment2End,
            barCenterYPos + (if(isCaffeineHighlighted) barHeightEnabled else  barHeightDisabled) / 2,
            radius,
            radius,
            if(isCaffeineHighlighted) greenPaintEnabled else greenPaintDisabled
        )

        val segment3Start = segment2End
        val segment3End = segment2End + endBarWidth
        val isEndHighlighted = highlightState == HighlightState.END
        canvas.drawRoundRect(
            segment3Start,
            barCenterYPos - (if(isEndHighlighted) barHeightEnabled else  barHeightDisabled) / 2,
            segment3End,
            barCenterYPos + (if(isEndHighlighted) barHeightEnabled else  barHeightDisabled) / 2,
            radius,
            radius,
            if(highlightState==HighlightState.END) redPaintEnabled else redPaintDisabled
        )


        //draw Bars


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



    fun drawBars(startX: Float, endX: Float, barBottomY: Float, canvas: Canvas) {
        val caffeineDataList = listOf(50, 40, 30, 20, 15, 10, 10)

        val maxVal = caffeineDataList.maxOrNull() ?: return
        val availableWidth = endX - startX
        val barCount = caffeineDataList.size

        val spacing = 2f.dpToPixel()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (availableWidth - totalSpacing) / barCount
        val barTopY = 24f.dpToPixel()

        val cornerRadius = 3f.dpToPixel()

        caffeineDataList.forEachIndexed { index, value ->
            val heightRatio = value / maxVal.toFloat()
            val barHeight = heightRatio * (barBottomY - barTopY)

            val left = startX + index * (barWidth + spacing)
            val top = barBottomY - barHeight
            val right = left + barWidth
            val bottom = barBottomY

            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaintDefault)
        }

    }


    fun updateData() {


    }

    fun calculateBarWidths(
        graphStart: LocalTime,
        graphEnd: LocalTime,
        caffeineStart: LocalTime,
        caffeineEnd: LocalTime,
        totalWidth: Float
    ): Triple<Float, Float, Float> {

        val now = LocalTime.of(9, 0)//LocalTime.now()

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