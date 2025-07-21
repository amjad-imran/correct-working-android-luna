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
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel


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

    val firstLabelBgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = "#2C241F".toColorInt()
        strokeWidth = 2f
        isAntiAlias = true
    }
    val secondLabelBgPaint = Paint().apply {
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

    private val firstPaint = Paint().apply {
        color = Color.WHITE
    }

    private val secondPaint = Paint().apply {
        color = Color.YELLOW
    }

    private val barPaint = Paint().apply {
        isAntiAlias = true
    }

    fun updateBars(barData: List<CircadianGraphModel>, colors: List<Int>) {
        this.barData = barData
        this.barColors = colors
        invalidate()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barWidth = width.toFloat() / totalBars
        val centerY = height / 2f
        val barHeight = 100f
        val avgBarHeight = 120f

        var firstCircadianMidPointModel: CircadianMidPointModel? = null
        var secondCircadianMidPointModel: CircadianMidPointModel? = null
        var secondMidPointLeftStart = -1f
        var firstMidPointLeftStart = -1f
        barData.forEachIndexed { index, value ->
            barPaint.color = barColors[index]
            val left = index * barWidth
            val top = centerY - barHeight
            val bottom = centerY + barHeight

            canvas.drawRoundRect(left, top, left + barWidth * 0.6f, bottom, 4f, 4f, barPaint)

            if (value.firstMidPoint != null) {
                firstCircadianMidPointModel = value.firstMidPoint
                firstMidPointLeftStart = left
                val top = centerY - avgBarHeight
                val bottom = centerY + avgBarHeight
                value.firstMidPoint.color?.let {
                    secondPaint.color = it
                }
                canvas.drawRoundRect(
                    left,
                    top,
                    left + barWidth * 0.6f,
                    bottom,
                    4f,
                    4f,
                    firstPaint
                )
            }

            if (value.secondMidPoint != null) {
                secondCircadianMidPointModel = value.secondMidPoint
                secondMidPointLeftStart = left
                val top = centerY - avgBarHeight
                val bottom = centerY + avgBarHeight
                value.secondMidPoint.color?.let {
                    secondPaint.color = it
                }
                canvas.drawRoundRect(left, top, left + barWidth * 0.6f, bottom, 4f, 4f, secondPaint)
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

        drawTwoAboveText(
            canvas,
            firstMidPointLeftStart,
            secondMidPointLeftStart,
            firstCircadianMidPointModel,
            secondCircadianMidPointModel
        )

    }

    private fun drawTwoAboveText(
        canvas: Canvas,
        firstMidPointLeftStart: Float,
        secondMidPointLeftStart: Float,
        firstCircadianMidPointModel: CircadianMidPointModel?,
        secondCircadianMidPointModel: CircadianMidPointModel?
    ) {
        val cornerRadius = 100f
        val label1 = firstCircadianMidPointModel?.title ?: ""
        val label2 = secondCircadianMidPointModel?.title ?: ""
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

        var label1Left = firstMidPointLeftStart - label1BoxWidth / 2f
        var label2Left = secondMidPointLeftStart - label2BoxWidth / 2f

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
        firstCircadianMidPointModel?.bgColor?.let {
            firstLabelBgPaint.color = it
        }

        firstCircadianMidPointModel?.color?.let {
            textPaint.color = it
        }
        canvas.drawRoundRect(label1Rect, cornerRadius, cornerRadius, firstLabelBgPaint)
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


        secondCircadianMidPointModel?.bgColor?.let {
            secondLabelBgPaint.color = it
        }
        secondCircadianMidPointModel?.color?.let {
            textPaint.color = it
        }
        canvas.drawRoundRect(label2Rect, cornerRadius, cornerRadius, secondLabelBgPaint)
        val text2X = label2Rect.centerX()
        val text2Y = label2Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(label2, text2X, text2Y, textPaint)
    }


}
