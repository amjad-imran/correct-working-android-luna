package com.oreo.util.graph

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CircadianGraphModel


class CircadianGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val totalHours = 6
    private val perHourInterval = 6
    val totalBars = totalHours * perHourInterval
    private var barData: List<CircadianGraphModel> = ArrayList()
    private var barColors: List<Int> = List(totalBars) { Color.DKGRAY }

    var avgBeforeIndex: Int = 6 // Example: 12:00 AM
    var avgNowIndex: Int = 9    // Example: 01:30 AM

    val firstLabelPaint = Paint().apply {
        style = Paint.Style.FILL
        color = "#2C241F".toColorInt()
        strokeWidth = 2f
        isAntiAlias = true
    }
    val secondLabelPaint = Paint().apply {
        style = Paint.Style.FILL
        color = "#3D3F43".toColorInt()
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = "#555A5E".toColorInt()
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val iconBitmapStart = BitmapFactory.decodeResource(resources, R.drawable.ic_sunset_grey)
    private val iconBitmapEnd = BitmapFactory.decodeResource(resources, R.drawable.ic_sunrise_grey)

    private val avgBeforePaint = Paint().apply {
        color = Color.WHITE
    }

    private val avgNowPaint = Paint().apply {
        color = Color.YELLOW
    }

    private val barPaint = Paint().apply {
        isAntiAlias = true
    }

    fun updateBars(barData: List<CircadianGraphModel>, colors: List<Int>) {
        this.barData = barData
        barColors = colors
        invalidate()        // --- Draw Label 2 ---

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barWidth = width.toFloat() / totalBars
        val centerY = height / 2f
        val barHeight = 100f
        val avgBarHeight = 120f

        var nowAvgLeftStart = -1f
        var beforeAvgLeftStart = -1f
        barData.forEachIndexed { index, value ->
            barPaint.color = barColors[index]
            val left = index * barWidth
            val top = centerY - barHeight
            val bottom = centerY + barHeight

            canvas.drawRoundRect(left, top, left + barWidth * 0.6f, bottom, 8f, 8f, barPaint)

            if (value.secondMidPoint) {
                nowAvgLeftStart = left
                val top = centerY - avgBarHeight
                val bottom = centerY + avgBarHeight
                canvas.drawRoundRect(left, top, left + barWidth * 0.6f, bottom, 8f, 8f, avgNowPaint)
            }

            if (value.firstMidPoint) {
                beforeAvgLeftStart = left
                val top = centerY - avgBarHeight
                val bottom = centerY + avgBarHeight
                canvas.drawRoundRect(
                    left,
                    top,
                    left + barWidth * 0.6f,
                    bottom,
                    8f,
                    8f,
                    avgBeforePaint
                )
            }

            if (index == 0 && !value.xAxis.isNullOrEmpty()) {
                val textStartX = iconBitmapStart.width.toFloat() + 100f
                canvas.drawBitmap(iconBitmapStart, left, bottom + 40, null)
                canvas.drawText(
                    value.xAxis,
                    textStartX,
                    bottom + 30 + iconBitmapStart.height.toFloat(),
                    textPaint
                )


            } else if (index == barData.size - 1 && !value.xAxis.isNullOrEmpty()) {
                val textStartX = left - iconBitmapEnd.width.toFloat() - 80f
                canvas.drawBitmap(
                    iconBitmapEnd,
                    left - iconBitmapEnd.width / 2,
                    bottom + 40,
                    null
                )
                canvas.drawText(
                    value.xAxis,
                    textStartX,
                    bottom + 30 + iconBitmapStart.height.toFloat(),
                    textPaint
                )

            }


        }

        drawTwoAboveText(canvas, beforeAvgLeftStart, nowAvgLeftStart)

    }

    private fun drawTwoAboveText(
        canvas: Canvas,
        beforeAvgLeftStart: Float,
        nowAvgLeftStart: Float
    ) {
        val cornerRadius = 100f
        val label1 = "Avg Before"
        val label2 = "Avg Now"
        val minSpacing = 24f
        val paddingH = 16f
        val paddingV = 8f
        val screenWidth = width.toFloat()

        val label1Width = textPaint.measureText(label1)
        val label2Width = textPaint.measureText(label2)
        val textHeight = textPaint.fontMetrics.run { bottom - top }
        val labelBoxHeight = textHeight + paddingV * 4
        val fontMetrics = textPaint.fontMetrics

        val label1BoxWidth = label1Width + paddingH * 4
        val label2BoxWidth = label2Width + paddingH * 4

        var label1Left = beforeAvgLeftStart - label1BoxWidth / 2f
        var label2Left = nowAvgLeftStart - label2BoxWidth / 2f

        label1Left = label1Left.coerceIn(0f, screenWidth - label1BoxWidth)
        label2Left = label2Left.coerceIn(0f, screenWidth - label2BoxWidth)

        val label1Right = label1Left + label1BoxWidth
        val label2Right = label2Left + label2BoxWidth

        if (label1Right + minSpacing > label2Left) {
            label2Left = label1Right + minSpacing
            if (label2Left + label2BoxWidth > screenWidth) {
                label2Left = screenWidth - label2BoxWidth
                label1Left = label2Left - minSpacing - label1BoxWidth
                label1Left = label1Left.coerceAtLeast(0f)
            }
        }

        val labelTop = 40f

        val label1Rect = RectF(
            label1Left,
            labelTop,
            label1Left + label1BoxWidth,
            labelTop + labelBoxHeight
        )
        canvas.drawRoundRect(label1Rect, cornerRadius, cornerRadius, firstLabelPaint)
        textPaint.textAlign = Paint.Align.CENTER
        val text1X = label1Rect.centerX()
        val text1Y = label1Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(label1, text1X, text1Y, textPaint)

        val label2Rect = RectF(
            label2Left,
            labelTop,
            label2Left + label2BoxWidth,
            labelTop + labelBoxHeight
        )
        canvas.drawRoundRect(label2Rect, cornerRadius, cornerRadius, secondLabelPaint)
        val text2X = label2Rect.centerX()
        val text2Y = label2Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(label2, text2X, text2Y, textPaint)
    }


    private fun drawFirstAboveText(canvas: Canvas, firstBoxLeft: Float) {
        val cornerRadius = 100f
        val label2 = "Avg Now"
        val label1 = "Avg Before"
        val minSpacing = 24f

        val label1Width = textPaint.measureText(label1)
        val label2Width = textPaint.measureText(label2)
        val textHeight = textPaint.fontMetrics.run { bottom - top }
        val paddingH = 16f
        val paddingV = 8f

        val label1BoxWidth = label1Width + paddingH * 4
        val label2BoxWidth = label2Width + paddingH * 4
        val labelBoxHeight = textHeight + paddingV * 4

        val totalWidthNeeded = label1BoxWidth + minSpacing + label2BoxWidth
        val screenWidth = width.toFloat()

        val label1BoxCenter = label1BoxWidth / 2

        var label1Left = firstBoxLeft - label1BoxCenter

        LOGS.d("label1Left $label1Left  --> firstBoxLeft $firstBoxLeft ===> label1BoxWidth $label1BoxWidth")

        if (label1Left < 0) {
            label1Left = firstBoxLeft
        } else if (firstBoxLeft + totalWidthNeeded > screenWidth) {
            label1Left = screenWidth - totalWidthNeeded
        }

        val label2Left = label1Left + label1BoxWidth + minSpacing
        val labelTop = 40f
        val fontMetrics = textPaint.fontMetrics

        val label1Rect =
            RectF(label1Left, labelTop, label1Left + label1BoxWidth, labelTop + labelBoxHeight)
        canvas.drawRoundRect(label1Rect, cornerRadius, cornerRadius, firstLabelPaint)
        textPaint.textAlign = Paint.Align.CENTER
        val textX = label1Rect.centerX()
        val textY = label1Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(label1, textX, textY, textPaint)


//        val label2Rect =
//            RectF(label2Left, labelTop, label2Left + label2BoxWidth, labelTop + labelBoxHeight)
//        canvas.drawRoundRect(label2Rect, cornerRadius, cornerRadius, secondLabelPaint)
//        val text2X = label2Rect.centerX()
//        val text2Y = label2Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
//        canvas.drawText(label2, text2X, text2Y, textPaint)
    }

    private fun drawSecondAboveText(canvas: Canvas, firstBoxLeft: Float) {
        val cornerRadius = 100f
        val label2 = "Avg Now"
        val label1 = "Avg Before"
        val minSpacing = 24f

        val label1Width = textPaint.measureText(label1)
        val label2Width = textPaint.measureText(label2)
        val textHeight = textPaint.fontMetrics.run { bottom - top }
        val paddingH = 16f
        val paddingV = 8f

        val label1BoxWidth = label1Width + paddingH * 4
        val label2BoxWidth = label2Width + paddingH * 4
        val labelBoxHeight = textHeight + paddingV * 4

        val totalWidthNeeded = label1BoxWidth + minSpacing + label2BoxWidth
        val screenWidth = width.toFloat()

        val label1BoxCenter = label1BoxWidth / 2

        var label1Left = firstBoxLeft - label1BoxCenter

        LOGS.d("label1Left $label1Left  --> firstBoxLeft $firstBoxLeft ===> label1BoxWidth $label1BoxWidth")

        if (label1Left < 0) {
            label1Left = firstBoxLeft
        } else if (firstBoxLeft + totalWidthNeeded > screenWidth) {
            label1Left = screenWidth - totalWidthNeeded
        }

        val label2Left = label1Left + label1BoxWidth + minSpacing
        val labelTop = 40f
        val fontMetrics = textPaint.fontMetrics

//        val label1Rect =
//            RectF(label1Left, labelTop, label1Left + label1BoxWidth, labelTop + labelBoxHeight)
//        canvas.drawRoundRect(label1Rect, cornerRadius, cornerRadius, firstLabelPaint)
//        textPaint.textAlign = Paint.Align.CENTER
//        val textX = label1Rect.centerX()
//        val textY = label1Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
//        canvas.drawText(label1, textX, textY, textPaint)


        val label2Rect =
            RectF(label2Left, labelTop, label2Left + label2BoxWidth, labelTop + labelBoxHeight)
        canvas.drawRoundRect(label2Rect, cornerRadius, cornerRadius, secondLabelPaint)
        val text2X = label2Rect.centerX()
        val text2Y = label2Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(label2, text2X, text2Y, textPaint)
    }
}
