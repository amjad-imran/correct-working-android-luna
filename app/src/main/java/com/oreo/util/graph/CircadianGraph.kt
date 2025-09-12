package com.oreo.util.graph

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit_commans.ui.dpToPixel
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel


class CircadianGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var totalHours = 6
    private val perHourInterval = 6 ///60/10 = 6 (10 minutes right now)
    // Track total bars directly so we can render arbitrary minute spans (10-min units)
    private var totalBarsCount: Int = totalHours * perHourInterval

    var drawOnSameIndex = false

    private val barHeightHalf = 26f.dpToPixel() / 2
    private val avgBarHeightHalf = 34f.dpToPixel() / 2

    private var barData: List<CircadianGraphModel> = ArrayList()
    private var barColors: List<Int> = List(totalBars()) { Color.DKGRAY }


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
        val fontGilroy = ResourcesCompat.getFont(context, com.noisefit_commans.R.font.gilroy_medium)
        typeface = fontGilroy
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val iconBitmapStart = BitmapFactory.decodeResource(resources, R.drawable.ic_sunset_grey)
    private val iconBitmapEnd = BitmapFactory.decodeResource(resources, R.drawable.ic_sunrise_grey)

    fun totalBars(): Int {
        return totalBarsCount
    }

    fun updateTotalHours(hour: Int) {
        totalHours = hour
        totalBarsCount = (totalHours * perHourInterval)
    }

    // New: explicitly set number of bars (10‑minute units)
    fun updateTotalBars(bars: Int) {
        totalBarsCount = bars.coerceAtLeast(1)

    }

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
        val barWidth = width.toFloat() / totalBars()
        val centerY = height / 2f


        var firstCircadianMidPointModel: CircadianMidPointModel? = null
        var secondCircadianMidPointModel: CircadianMidPointModel? = null
        var secondMidPointLeftStart = -1f
        var firstMidPointLeftStart = -1f
        barData.forEachIndexed { index, value ->
            barPaint.color = barColors[index]
            val left = index * barWidth
            val top = centerY - barHeightHalf
            val bottom = centerY + barHeightHalf

            canvas.drawRoundRect(left, top, left + barWidth * 0.6f, bottom, 4f, 4f, barPaint)

            if (drawOnSameIndex) {
                if (value.bothMidPoint != null) {
                    firstCircadianMidPointModel = value.bothMidPoint?.first
                    firstMidPointLeftStart = left
                    val top = centerY - avgBarHeightHalf
                    val bottom = centerY + avgBarHeightHalf
                    value.bothMidPoint?.first?.color?.let {
                        firstPaint.color = it
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

                    secondCircadianMidPointModel = value.bothMidPoint?.second
                    secondMidPointLeftStart = left
                    val top2 = centerY - avgBarHeightHalf
                    val bottom2 = centerY + avgBarHeightHalf
                    value.bothMidPoint?.second?.color?.let {
                        secondPaint.color = it
                    }
                    canvas.drawRoundRect(
                        left,
                        top2 + 10f,
                        left + barWidth * 0.6f,
                        bottom2 - 10f,
                        4f,
                        4f,
                        secondPaint
                    )
                }

            } else {
                if (value.firstMidPoint != null) {
                    firstCircadianMidPointModel = value.firstMidPoint
                    firstMidPointLeftStart = left
                    val top = centerY - avgBarHeightHalf
                    val bottom = centerY + avgBarHeightHalf
                    value.firstMidPoint?.color?.let {
                        firstPaint.color = it
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
                } else if (value.secondMidPoint != null) {
                    secondCircadianMidPointModel = value.secondMidPoint
                    secondMidPointLeftStart = left
                    val top = centerY - avgBarHeightHalf
                    val bottom = centerY + avgBarHeightHalf
                    value.secondMidPoint?.color?.let {
                        secondPaint.color = it
                    }
                    canvas.drawRoundRect(
                        left,
                        top,
                        left + barWidth * 0.6f,
                        bottom,
                        4f,
                        4f,
                        secondPaint
                    )


                }
            }


            if (index == 0 && !value.xAxis.isNullOrEmpty()) {
                textPaint.color = "#555A5E".toColorInt()
                val textStartX = iconBitmapStart.width.toFloat() + 60f
                /*canvas.drawBitmap(iconBitmapStart, left, bottom + 40, null)*/
               /* canvas.drawText(
                    value.xAxis,
                    textStartX,
                    bottom + 35 + iconBitmapStart.height.toFloat(),
                    textPaint
                )*/


            } else if (index == barData.size - 1 && !value.xAxis.isNullOrEmpty()) {
                textPaint.color = "#555A5E".toColorInt()
                val textStartX = left - iconBitmapEnd.width.toFloat() - 40f
               /* canvas.drawBitmap(
                    iconBitmapEnd,
                    left - iconBitmapEnd.width / 2,
                    bottom + 40,
                    null
                )*/
               /* canvas.drawText(
                    value.xAxis,
                    textStartX,
                    bottom + 35 + iconBitmapStart.height.toFloat(),
                    textPaint
                )*/

            } else if (!value.xAxis.isNullOrEmpty()) {
                textPaint.color = "#555A5E".toColorInt()
                val label1Width = textPaint.measureText(value.xAxis)
                canvas.drawText(
                    value.xAxis,
                    left,
                    bottom + 35 + iconBitmapStart.height.toFloat(),
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

        val labelTop = 0f

        val centerY = height / 2f
        val spacing = 4f.dpToPixel()


        firstCircadianMidPointModel?.let {
            val textBottom = centerY - avgBarHeightHalf - spacing
            val label1Rect = RectF(
                label1Left,
                textBottom - labelBoxHeight,
                label1Left + label1BoxWidth,
                textBottom
            )

            firstCircadianMidPointModel.bgColor?.let {
                firstLabelBgPaint.color = it
            }

            firstCircadianMidPointModel.color?.let {
                textPaint.color = it
            }
            canvas.drawRoundRect(label1Rect, cornerRadius, cornerRadius, firstLabelBgPaint)
            textPaint.textAlign = Paint.Align.CENTER
            val text1X = label1Rect.centerX()
            val text1Y = label1Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(label1, text1X, text1Y, textPaint)
        }

        secondCircadianMidPointModel?.let {
            val text2Bottom = centerY - avgBarHeightHalf - spacing

            val label2Rect = RectF(
                label2Left,
                text2Bottom - labelBoxHeight,
                label2Left + label2BoxWidth,
                text2Bottom
            )


            secondCircadianMidPointModel.bgColor?.let {
                secondLabelBgPaint.color = it
            }
            secondCircadianMidPointModel.color?.let {
                textPaint.color = it
            }
            canvas.drawRoundRect(label2Rect, cornerRadius, cornerRadius, secondLabelBgPaint)
            val text2X = label2Rect.centerX()
            val text2Y = label2Rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(label2, text2X, text2Y, textPaint)
        }


    }


}
