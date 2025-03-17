package com.oreo.ui.custom


import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.LOGS
import java.time.ZoneId
import kotlin.math.min

class WorkoutHeartRateZoneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var zoneId: Int? = 0
        set(value) {
            field = value
            invalidate()
        }

    private fun getColorByZoneId(zoneId: Int): Int {
        return when (zoneId) {
            0 -> Color.parseColor("#5E70F0")
            1 -> Color.parseColor("#76D1F8")
            2 -> Color.parseColor("#90D665")
            3 -> Color.parseColor("#EC9F74")
            4 -> Color.parseColor("#E56BD0")
            else -> {
                Color.parseColor("#0DFFFFFF")
            }
        }
    }

    // Paint for arcs
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val startAngleDegrees = 180f
    private val totalArcDegrees = 180f

    private val gapDegrees = 6f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //setMeasuredDimension(widthMeasureSpec,widthMeasureSpec/2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bottomPadding = 16f.dpToPixel()
        val cx = width / 2f
        val cy = height - bottomPadding

        val radius = min(width, height) - (arcPaint.strokeWidth / 2f) - bottomPadding

        val zoneCount = 5
        val arcDegreesPerZone = (totalArcDegrees - gapDegrees * (zoneCount - 1)) / zoneCount

        var arcStart = startAngleDegrees

        LOGS.d("sdfkljsldkf ${zoneId}")

        for (i in 0 until zoneCount) {

            arcPaint.color = if (zoneId == null) {
                Color.parseColor("#80000000")
            } else if (i == zoneId) {
                getColorByZoneId(zoneId!!)
            } else if (i < zoneId!!) {
                Color.parseColor("#73FFFFFF")
            } else {
                Color.parseColor("#0DFFFFFF")
            }

            canvas.drawArc(
                cx - radius, cy - radius,
                cx + radius, cy + radius,
                arcStart,
                arcDegreesPerZone,
                false,
                arcPaint
            )
            arcStart += arcDegreesPerZone + gapDegrees
        }

    }
}