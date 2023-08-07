package com.noisefit_commans.ui.custom


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.getColor


class SleepProgressbarView(var mContext: Context) : View(
    mContext
) {
    private var sleepGraphInteractionListener: SleepGraphInteractionListener? = null

    private var mPaint: Paint = Paint()

    private lateinit var deepPaint: Paint

    private var sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList()
    private var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()


    var endPadding = 0.0f

    fun setInteraction(sleepGraphInteractionListener: SleepGraphInteractionListener) {
        this.sleepGraphInteractionListener = sleepGraphInteractionListener
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawLine(
            0f,
            SleepGraphView.pxFromDp(mContext, 4f),
            width.toFloat() - endPadding,
            SleepGraphView.pxFromDp(mContext, 4f),
            mPaint
        )

        tooltipEntryArray = ArrayList()

        if (!sleepArray.isNullOrEmpty()) {

            var totalDuration = 0
            for (i in sleepArray!!.indices) {
                totalDuration += sleepArray!![i].duration
            }
            if (totalDuration != 0) {
                val eachMinutesWidth = (width.toFloat() - endPadding) / totalDuration

                var start = 0f
                var end: Float
                var top = 0f
                var bottom = 0f
                for (i in sleepArray!!.indices) {
                    var paint: Paint? = null
                    val rowData = sleepArray!![i]
                    end = start + eachMinutesWidth * rowData.duration
                    if (rowData.sleepType.lowercase() == "deep") {
                        paint = deepPaint
                        top = pxFromDp(mContext, 0f)
                        bottom = pxFromDp(mContext, 8f)
                    }

                    if (paint != null) {
                        val rectF = RectF(start, top, end, bottom)
                        canvas.drawRoundRect(
                            rectF, pxFromDp(mContext, 4f), pxFromDp(mContext, 4f), paint
                        )

                    }
                    val range = sleepArray!![i].startTime + " - " + sleepArray!![i].endTime

                    tooltipEntryArray!!.add(
                        ToolTipEntry(
                            start, end, top, bottom, rowData.duration, rowData.sleepType, range
                        )
                    )
                    start = end
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val x = event!!.x
        val y = event.y
        if (event.action == MotionEvent.ACTION_UP) {
            if (tooltipEntryArray != null && tooltipEntryArray?.size!! > 0) {
                val entry =
                    tooltipEntryArray!!.filter { it1 -> x > it1.x1 && x < it1.x2 && y > it1.y1 && y < it1.y2 }
                if (entry.isNotEmpty()) {
                    sleepGraphInteractionListener?.onSleepGraphSelected(entry[0])
                } else {
                    sleepGraphInteractionListener?.onSleepGraphSelected(null)
                }
            }
        }
        return true
    }

    fun setData(sleepArray: ArrayList<SleepData.SleepDataBreakup>?) {
        val array = sleepArray?.filter {
            it.duration >= 60
        }
        this.sleepArray.clear()
        this.sleepArray.addAll(array ?: ArrayList())
    }


    companion object {
        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }
    }


    init {
        setPaint()
        endPadding = pxFromDp(mContext, 0f)
        //mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.color = com.noisefit_commans.R.color.sleep_pg_bg.getColor()
        mPaint.strokeWidth = pxFromDp(mContext, 4f)
        mPaint.strokeCap = Paint.Cap.ROUND

    }


    private fun setPaint() {
        val shaderDeep = LinearGradient(
            0f,
            0f,
            0f,
            pxFromDp(mContext, 10f),
            ContextCompat.getColor(mContext, com.noisefit_commans.R.color.sleep_chart),
            ContextCompat.getColor(mContext, com.noisefit_commans.R.color.sleep_chart_end),
            Shader.TileMode.CLAMP
        )

        deepPaint = Paint()
        deepPaint.shader = shaderDeep


    }


}
