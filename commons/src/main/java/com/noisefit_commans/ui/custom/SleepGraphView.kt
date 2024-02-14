package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit_commans.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.models.SleepData

class SleepGraphView(var mContext: Context) : View(
    mContext
) {
    private var sleepGraphInteractionListener: SleepGraphInteractionListener? = null
    private var isDisable = false
    private var mPaint: Paint
    private var mPaint2: Paint
    private var outerPaint: Paint
    private lateinit var remPaint: Paint
    private lateinit var deepPaint: Paint
    private lateinit var lightPaint: Paint
    private lateinit var awakePaint: Paint
    private var mTextPaint: Paint

    //    private var toolTipPaint: Paint = Paint()
//    private var toolTipTextPaint: Paint
    private var countCardData: CountCardData? = null
    private var sleepArray: ArrayList<SleepData.SleepDataBreakup>? = ArrayList()
    private var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()
//    private var toolEntry: ToolTipEntry? = null

    var endPadding = 0.0f

    fun setInteraction(sleepGraphInteractionListener: SleepGraphInteractionListener) {
        this.sleepGraphInteractionListener = sleepGraphInteractionListener
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val textXPos = (width.toFloat() - endPadding + pxFromDp(mContext, 4f))
        tooltipEntryArray = ArrayList()
        canvas.drawLine(
            0f,
            pxFromDp(mContext, 10f),
            width.toFloat() - endPadding,
            pxFromDp(mContext, 10f),
            mPaint
        )
        if (!isDisable) {
            canvas.drawText("Awake", textXPos, pxFromDp(mContext, 26f), mTextPaint)
            canvas.drawText("REM", textXPos, pxFromDp(mContext, 50f), mTextPaint)
            canvas.drawText("Light", textXPos, pxFromDp(mContext, 74f), mTextPaint)
            canvas.drawText("Deep", textXPos, pxFromDp(mContext, 98f), mTextPaint)
        }


        canvas.drawLine(
            0f,
            pxFromDp(mContext, 34f),
            width.toFloat() - endPadding,
            pxFromDp(mContext, 34f),
            mPaint
        )
        canvas.drawLine(
            0f,
            pxFromDp(mContext, 58f),
            width.toFloat() - endPadding,
            pxFromDp(mContext, 58f),
            mPaint
        )

        canvas.drawLine(
            0f,
            pxFromDp(mContext, 82f),
            width.toFloat() - endPadding,
            pxFromDp(mContext, 82f),
            mPaint
        )
        canvas.drawLine(
            0f,
            pxFromDp(mContext, 106f),
            width.toFloat() - endPadding,
            pxFromDp(mContext, 106f),
            mPaint
        )
        if (sleepArray != null && sleepArray!!.size > 0) {

            countCardData?.leftValue?.let { startTime ->
                canvas.drawText(startTime, 0f, pxFromDp(mContext, 128f), mTextPaint)
            }

            countCardData?.rightValue?.let { endTime ->
                canvas.drawText(
                    endTime,
                    (width - pxFromDp(mContext, 45f) - endPadding),
                    pxFromDp(mContext, 128f),
                    mTextPaint
                )
            }


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
                    if (rowData.sleepType == "deep") {
                        paint = deepPaint
                        top = pxFromDp(mContext, 90f)
                        bottom = pxFromDp(mContext, 98f)
                    }
                    if (rowData.sleepType == "light") {
                        paint = lightPaint
                        top = pxFromDp(mContext, 66f)
                        bottom = pxFromDp(mContext, 74f)
                    }
                    if (rowData.sleepType == "rem") {
                        paint = remPaint
                        top = pxFromDp(mContext, 42f)
                        bottom = pxFromDp(mContext, 52f)
                    }
                    if (rowData.sleepType == "awake") {
                        paint = awakePaint
                        top = pxFromDp(mContext, 19f)
                        bottom = pxFromDp(mContext, 26f)
                    }
                    if (paint != null) {
                        val rectF = RectF(start, top, end, bottom)
                        canvas.drawRoundRect(
                            rectF,
                            pxFromDp(mContext, 4f),
                            pxFromDp(mContext, 4f),
                            paint
                        )


                        val testPaint = Paint()

                        val x0 =0f
                        val y0 = 0f
                        val x1 = 500f
                        val y1 = 500f

                        val linearGradient = LinearGradient(
                            x0, y0, x1, y1, intArrayOf(android.R.color.holo_red_dark, android.R.color.holo_green_light),
                            null, Shader.TileMode.MIRROR
                        )

                        testPaint.shader = linearGradient

                        canvas.drawLine(x0, y0, x1, y1, testPaint)


                        try {
                            val nextElement = sleepArray!![i + 1]
                            when (nextElement.sleepType) {
                                "deep" -> {
                                    when (rowData.sleepType) {

                                        "light" -> {
                                            val rectLight = RectF(
                                                end,
                                                top,
                                                end + pxFromDp(mContext, 1f),
                                                top + pxFromDp(mContext, 30f)
                                            )
                                            canvas.drawRoundRect(
                                                rectLight,
                                                pxFromDp(mContext, 1f),
                                                pxFromDp(mContext, 1f),
                                                paint
                                            )
                                        }

                                        "rem" -> {

                                        }

                                        "awake" -> {

                                        }

                                    }
                                }

                                "light" -> {

                                }

                                "rem" -> {

                                }

                                "awake" -> {

                                }

                            }
                        } catch (exp: Exception) {

                        }


                    }

                    tooltipEntryArray!!.add(
                        ToolTipEntry(
                            x1 = start,
                            x2 = end,
                            y1 = top,
                            y2 = bottom,
                            duration = rowData.duration,
                            type = rowData.sleepType,
                            startTime = sleepArray!![i].startTime,
                            endTime = sleepArray!![i].endTime
                        )
                    )
                    start = end
                }
            }
//            if (toolEntry != null) {
//                var x1 = (toolEntry!!.x1 + (toolEntry!!.x2 - toolEntry!!.x1) / 2) - pxFromDp(
//                    mContext,
//                    25f
//                )
//                if (x1 < 0) {
//                    x1 = 0f
//                }
//                if (x1 > width.toFloat()) {
//                    x1 = width.toFloat() - pxFromDp(mContext, 50f)
//                }
//                val x2 = x1 + pxFromDp(mContext, 50f)
//                var y1 = toolEntry!!.y1
//                var y2 = y1 - pxFromDp(mContext, 30f)
//
//                var value = toolEntry!!.value.toString() + "m"
//                if (toolEntry!!.value >= 60) {
//                    //value = ApplicationUtils.getFormattedSleepDuration(toolEntry!!.value)
//                }
//
//
//                if (toolEntry!!.type.equals("awake", true)) {
//                    y1 += pxFromDp(mContext, 20f)
//                    y2 += pxFromDp(mContext, 20f)
//                }
//                val rectF = RectF(x1, y1, x2, y2)
//
//                canvas.drawRoundRect(
//                    rectF,
//                    pxFromDp(mContext, 1.5f),
//                    pxFromDp(mContext, 1.5f),
//                    toolTipPaint
//                )
//                canvas.drawText(
//                    value,
//                    rectF.centerX(),
//                    rectF.centerY() - pxFromDp(mContext, 3f),
//                    toolTipTextPaint
//                )
//                canvas.drawText(
//                    toolEntry!!.type.uppercase(Locale.ENGLISH),
//                    rectF.centerX(),
//                    rectF.centerY() + pxFromDp(mContext, 10f),
//                    toolTipTextPaint
//                )
//            }
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
        this.sleepArray = sleepArray
    }

    fun setData(countCardData: CountCardData?) {
        this.countCardData = countCardData
    }

    companion object {
        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }
    }


    init {
//        toolTipPaint.color = ContextCompat.getColor(mContext, R.color.white)
//        toolTipPaint.style = Paint.Style.FILL
//        toolTipTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
//        toolTipTextPaint.color = ContextCompat.getColor(mContext, R.color.blood_oxygen_color)
//        toolTipTextPaint.textSize = pxFromDp(mContext, 10f)
//        toolTipTextPaint.textAlign = Paint.Align.CENTER
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint.strokeWidth = pxFromDp(mContext, 2f)
        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint2.strokeWidth = pxFromDp(mContext, 2f)
        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.text_color)
        mTextPaint.textSize = pxFromDp(mContext, 12f)
        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT

    }

    fun toggleStatus(isDisable: Boolean) {
        this.isDisable = isDisable
        setPaint()
        invalidate()
    }

    private fun setPaint() {
        val shaderDeep = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.deep_start),
                ContextCompat.getColor(mContext, R.color.deep_start),
                Shader.TileMode.CLAMP
            )
        }


        deepPaint = Paint()
        deepPaint.shader = shaderDeep

        val shaderLight = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.light_start),
                ContextCompat.getColor(mContext, R.color.light_start),
                Shader.TileMode.CLAMP
            )
        }


        lightPaint = Paint()
        lightPaint.shader = shaderLight
        val shaderAwake = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.awake_start),
                ContextCompat.getColor(mContext, R.color.awake_start),
                Shader.TileMode.CLAMP
            )
        }


        awakePaint = Paint()
        awakePaint.shader = shaderAwake


        val shaderRem = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.rem_start),
                ContextCompat.getColor(mContext, R.color.rem_start),
                Shader.TileMode.CLAMP
            )
        }


        remPaint = Paint()
        remPaint.shader = shaderRem
    }

    fun init(isDisable: Boolean) {
        this.isDisable = isDisable
        setPaint()
        endPadding = pxFromDp(mContext, 48f)
    }
}

data class ToolTipEntry(
    var x1: Float = 0f,
    var x2: Float = 0f,
    var y1: Float = 0f,
    var y2: Float = 0f,
    var duration: Int = 0,
    var type: String = "",
    val startTime:String?=null,
    val endTime:String?=null,
    val range: String = "",
)

interface SleepGraphInteractionListener {
    fun onSleepGraphSelected(toolTipEntry: ToolTipEntry?)
}