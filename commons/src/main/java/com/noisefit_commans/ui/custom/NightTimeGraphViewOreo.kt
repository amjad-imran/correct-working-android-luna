package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit_commans.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.utils.LOGS

class NightTimeGraphViewOreo(var mContext: Context) : View(
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
    private var mTextPaintEdge: Paint

    private var mPaintHighLine: Paint
    private var mPaintMedLine: Paint
    private var mPaintLowLine: Paint
    private var mPaintLow: Paint
    private var mPaintMed: Paint
    private var mPaintHigh: Paint


    //    private var toolTipPaint: Paint = Paint()
//    private var toolTipTextPaint: Paint
    private var countCardData: CountCardData? = null
    private var sleepArray: ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>? = ArrayList()
    private var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()
//    private var toolEntry: ToolTipEntry? = null

    var endPadding = 0.0f
    var startPadding = 0.0f

    fun setInteraction(sleepGraphInteractionListener: SleepGraphInteractionListener) {
        this.sleepGraphInteractionListener = sleepGraphInteractionListener
    }

    var previousRect: RectF? = null

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        previousRect = null

        val sectionHeight = height.toFloat() / 5

        val textXPos = (width.toFloat() - endPadding + pxFromDp(mContext, 14f))
        tooltipEntryArray = ArrayList()

        if (!isDisable) {
            canvas.drawText("High", textXPos, sectionHeight * 1, mTextPaint)
            canvas.drawText("Med", textXPos, sectionHeight * 2, mTextPaint)
            canvas.drawText("Low", textXPos, sectionHeight * 3, mTextPaint)
        }


        drawLines(sectionHeight, canvas)


        if (sleepArray != null && sleepArray!!.size > 0) {

            countCardData?.leftValue?.let { startTime ->
                canvas.drawText(startTime, 0f, sectionHeight * 4, mTextPaintEdge)
            }

            countCardData?.rightValue?.let { endTime ->
                canvas.drawText(
                    endTime,
                    (width - pxFromDp(mContext, 45f) - endPadding),
                    sectionHeight * 4,
                    mTextPaintEdge
                )
            }


            var totalDuration = 0
            for (i in sleepArray!!.indices) {
                totalDuration += sleepArray!![i].duration
            }
            if (totalDuration != 0) {
                val eachMinutesWidth = (width.toFloat() - endPadding) / totalDuration
                LOGS.d("NIGHT_GRAPH $eachMinutesWidth")

                var start = startPadding
                var end: Float
                val radius = pxFromDp(mContext, 3f)

                for (i in sleepArray!!.indices) {
                    val rowData = sleepArray!![i]

                    end = start + eachMinutesWidth * rowData.duration


                    val mid = start + ((end - start) / 2)

                    LOGS.d("NIGHT_GRAPH start:$start mid:$mid end:$end")

                    when (SleepMovementType.getValueFromString(rowData.movementType)) {
                        SleepMovementType.LOW -> {
                            canvas.drawCircle(
                                mid,
                                sectionHeight * 3,
                                radius,
                                mPaintLow
                            )
                        }

                        SleepMovementType.MEDIUM -> {
                            canvas.drawCircle(
                                mid,
                                sectionHeight * 2,
                                radius,
                                mPaintMed
                            )
                        }

                        SleepMovementType.INTENSE -> {
                            canvas.drawCircle(
                                mid,
                                sectionHeight * 1,
                                radius,
                                mPaintHigh
                            )
                        }

                        SleepMovementType.NO_MOVEMENT -> {}
                    }

                    start = end
                }
            }
        }
    }

    private fun drawLines(sectionHeight: Float, canvas: Canvas) {
        canvas.drawLine(
            startPadding,
            sectionHeight * 1,
            width.toFloat() - endPadding,
            sectionHeight * 1,
            mPaintHighLine
        )
        canvas.drawLine(
            startPadding,
            sectionHeight * 2,
            width.toFloat() - endPadding,
            sectionHeight * 2,
            mPaintMedLine
        )
        canvas.drawLine(
            startPadding,
            sectionHeight * 3,
            width.toFloat() - endPadding,
            sectionHeight * 3,
            mPaintLowLine
        )
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

    fun setData(sleepArray: List<OreoSleepData.OreoSleepMovementDataBreakup>?) {
        this.sleepArray?.clear()
        this.sleepArray = sleepArray?.filterNot {
            it.duration == 0
        } as ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>
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


        mPaintHighLine = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#80ffffff")
            strokeWidth = pxFromDp(mContext, 1f)
        }
        mPaintMedLine = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#80ca99ff")
            strokeWidth = pxFromDp(mContext, 1f)
        }
        mPaintLowLine = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#805e0cda")
            strokeWidth = pxFromDp(mContext, 1f)
        }

        mPaintLow = Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#5e0cda")
        }
        mPaintMed = Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#b470ff")
        }
        mPaintHigh = Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#ffffff")
        }



        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = ContextCompat.getColor(mContext, R.color.white_12)
        mPaint.strokeWidth = pxFromDp(mContext, 1f)
        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint2.strokeWidth = pxFromDp(mContext, 2f)
        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.white_64)
        mTextPaint.textSize = pxFromDp(mContext, 10f)
        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT

        mTextPaintEdge = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintEdge.color = ContextCompat.getColor(mContext, R.color.white)
        mTextPaintEdge.textSize = pxFromDp(mContext, 10f)

    }

    fun toggleStatus(isDisable: Boolean) {
        previousRect = null
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
                ContextCompat.getColor(mContext, R.color.deep_start_oreo),
                ContextCompat.getColor(mContext, R.color.deep_start_oreo),
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
                ContextCompat.getColor(mContext, R.color.light_start_oreo),
                ContextCompat.getColor(mContext, R.color.light_start_oreo),
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
                ContextCompat.getColor(mContext, R.color.awake_start_oreo),
                ContextCompat.getColor(mContext, R.color.awake_start_oreo),
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
                ContextCompat.getColor(mContext, R.color.rem_start_oreo),
                ContextCompat.getColor(mContext, R.color.rem_start_oreo),
                Shader.TileMode.CLAMP
            )
        }


        remPaint = Paint()
        remPaint.shader = shaderRem
    }

    fun init(isDisable: Boolean) {
        previousRect = null
        this.isDisable = isDisable
        setPaint()
        endPadding = pxFromDp(mContext, 48f)
        startPadding = pxFromDp(mContext,3f)
    }
}